#!/usr/local/bin/python
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/jmxclient.py,v 1.1 2009/05/22 22:16:39 sanderst Exp $

import KBC.fotech
from Util import kbclog
from Util.date import Date, DateTime
from subprocess import Popen, PIPE
from decimal import Decimal
import cPickle as pickle
import os

INFO = 0
ACTION = 1
ACTION_INFO = 2
UNKNOWN = 3

# these can be set differently when testing
SCRIPT = '%s/bin/jmxclient' % os.getenv('FOTECH_HOME', '/prod/fotech')
DEBUG = False

log = kbclog.getLogger(__name__)

class JavaObject(object):
    def __init__(self, type, value):
        self.type = type
        self.value = value
        
    def __repr__(self):
        return '%s(%s)' % (self.type, str(self.value))

class Bean(object):
    def __init__(self, description, attributes, operations):
        object.__setattr__(self, 'description', description)
        object.__setattr__(self, 'attributes', dict((attribute.name, attribute) for attribute in attributes))
        object.__setattr__(self, 'operations', dict((operation.signature, operation) for operation in operations))

        # create a map of name -> operation, checking for overloads
        operationsByName = {}
        for operation in operations:
            if operation.name in operationsByName:
                operationsByName[operation.name] = OverloadedOperation(operation.name, self)
            else:
                operationsByName[operation.name] = operation

        object.__setattr__(self, 'operationsByName', operationsByName)

        for attribute in attributes:
            attribute.bean = self

        for operation in operations:
            operation.bean = self
            
    def _connect(self, process, name):
        object.__setattr__(self, 'process', process)
        object.__setattr__(self, 'name', name)

    def get(self, name):
        return self.process.get(self.name, name)

    def getAll(self):
        return self.process.getAll(self.name)

    def set(self, attribute, value):
        return self.process.set(self.name, attribute, value)

    def setAll(self, attrvalues):
        return self.process.setAll(self.name, attrvalues)

    def invoke(self, signature, *params):
        return self.process.invoke(self.name, signature, *params)
        
    def __getattr__(self, name):
        if name in self.attributes:
            return self.attributes[name].get()
        elif name in self.operationsByName:
            return self.operationsByName[name]
        else:
            raise AttributeError("'Bean' object has no attribute '%s'" % name)
        
    def __setattr__(self, name, value):
        if name in self.attributes:
            return self.attributes[name].set(value)
        else:
            return object.__setattr__(self, name, value)
        
    def __repr__(self):
        return '%s: %s' % (self.name, self.description)

class Attribute(object):
    def __init__(self, name, type, description, readable, writable):
        self.name = name
        self.type = type
        self.description = description
        self.readable = readable
        self.writable = writable

    def get(self):
        return self.bean.get(self.name)

    def set(self, value):
        return self.bean.set(self.name, value)
        
    def __repr__(self):
        return '%s: %s' % (self.name, self.description)

class Operation(object):
    def __init__(self, name, returnType, parameters, description, impact):
        self.name = name
        self.returnType = returnType
        self.parameters = parameters
        self.description = description
        self.impact = impact
        self.bean = None

        if parameters == None:
            self.signature = name
        else:
            self.signature = '%s(%s)' % (self.name, ','.join(param.type for param in parameters))

    def invoke(self, *params):
        return self.bean.invoke(self.signature, *params)
        
    def __call__(self, *params):
        return self.invoke(*params)

    def __repr__(self):
        return '%s: %s' % (self.signature, self.description)

class OverloadedOperation(Operation):
    """ 
    Simple override to provide convient constructor for overloaded operations.
    Resolution of overridden methods is delegated to the Java jmxclient.
    """
    def __init__(self, name, bean):
        Operation.__init__(self, name, None, None, '%s(*): Overloaded operation' % name, UNKNOWN)
        self.bean = bean

class Parameter(object):
    def __init__(self, name, type, description):
        self.name = name
        self.type = type
        self.description = description
        
    def __repr__(self):
        return '%s: %s' % (self.name, self.description)

class JMXException(StandardError):
    def __init__(self, message):
        Exception.__init__(self, message)

class ParseException(JMXException):
    def __init__(self, message):
        JMXException.__init__(self, message)

class CommandException(JMXException):
    def __init__(self, message):
        JMXException.__init__(self, message)

class MBeanException(JMXException):
    def __init__(self, message, type = None, file = None, line = 0):
        JMXException.__init__(self, '%s: %s, %s:%d' % (type, message, file, line))
        self.type = type
        self.file = file
        self.line = line

class JavaProcess(object):
    def __init__(self, server, port, type = 'standard', user = None, password = None, classpaths = []):
        cmd = [
            SCRIPT,
            '--server=%s' % server, 
            '--port=%d' % port, 
            '--interface=%s' % type, 
            '--output=pickle'
            ]

        if user:
            cmd.append('--user=%s' % user)
            cmd.append('--pass=%s' % password)

        self.server = server
        self.port = port

        # set classpath (appending to start if necessary)
        if classpaths:
            classpaths.append(os.environ.get('CLASSPATH', None))
            os.environ['CLASSPATH'] = ':'.join(classpath for classpath in classpaths if classpath)

        log.debug('Creating child process: %s' % ' '.join(cmd))
        self.child = Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=PIPE, close_fds = True, shell = False)

        try:
            # if jmxclient running in debug mode, stdout will have a line with the debug port info
            if DEBUG:
                self.child.stdout.readline()

            # check to make sure client has started properly
            result = pickle.load(self.child.stdout)
            if isinstance(result, JMXException):
                raise result

            log.debug('Child process created successfully')

        except JMXException, e:
            raise
        except Exception, e:
            # get stdout and stderr
            out, err = self.close()

            if err:
                message = err
            else:
                message = out

            # get exit code
            code = self.child.poll()
            
            if code == 1:
                raise MBeanException(message)
            elif code == 2:
                raise CommandException(message)
            elif code == 3:
                raise ParseException(message)
            else:
                raise CommandException('%s, exit code = %d' % (message, code))

    # for auto-closing 'with JavaProcess(*args) as p' support
    def __enter__(self):
        return self

    def __exit__(self, type, value, traceback):
        if not self.isClosed():
            self.close()

    def isClosed(self):
        return self.child and self.child.poll() != None

    def close(self):
        # close stdin and wait for termination, returning a tuple of remaining output on stdout and stderr
        log.debug('Closing child process')
        o, e = self.child.communicate()
        log.debug('Child process closed')

        return o, e

    def _execute(self, args):
        if self.isClosed():
            raise IOError('Attempt to execute command on terminated process')
            
        cmd = u' '.join(self._escapeQuote(arg) for arg in args)
        log.debug('Sending command to child process: %s' % cmd)

        self.child.stdin.write('%s\n' % cmd.encode('UTF8'))
        result = pickle.load(self.child.stdout)

        if isinstance(result, JMXException):
            raise result
        else:
            return result

    def _escapeQuote(self, s):
        return u"'%s'" % s.replace("'", "\\\'")

    def _escapeBracket(self, s):
        return s.replace("(", "\(").replace(")", "\)").replace(",", "\,")

    def _toUnicode(self, value):
        if value == None:
            return u'null'
        elif isinstance(value, basestring):
            return u'String(%s)' % self._escapeBracket(unicode(value))
        elif isinstance(value, Decimal):
            return u'java.math.BigDecimal(String(%s))' % self._escapeBracket(unicode(value))
        elif hasattr(value, '__iter__'):
            return u'(%s)' % u','.join(self._toUnicode(elem) for elem in value)
        elif hasattr(value, 'year') and hasattr(value, 'month') and hasattr(value, 'day'):
            if hasattr(value, 'hour') and hasattr(value, 'minute') and hasattr(value, 'second'):
                ms = 1000 * DateTime(value.year, value.month, value.day, value.hour, value.minute, value.second).toUTCSeconds()
                if hasattr(value, 'microseconds'):
                    ms += int(value.microseconds / 1000)
            else:
                ms = 1000 * Date(value.year, value.month, value.day).toUTCSeconds()
            return u'java.util.Date(long(%d))' % int(ms)
        else:
            return self._escapeBracket(unicode(value))

    def getBeanNames(self, pattern = None):
        args = ['beans']
        if pattern:
            args.append(pattern)
        return self._execute(args)
        
    def getBean(self, name):
        bean = self._execute(['features', name])
        bean._connect(self, name)
        return bean

    def getBeans(self, pattern = None):
        return [self.getBean(name) for name in self.getBeanNames(pattern)]
    
    def get(self, bean, attribute):
        return self._execute(['get', bean, attribute])

    def getAll(self, bean):
        return self._execute(['get', bean])

    def set(self, bean, attribute, value):
        return self._execute(['set', bean, u'%s=%s' % (attribute, self._toUnicode(value))])

    def setAll(self, bean, attrvalues):
        args = ['set', bean]
        args.extend(u'%s=%s' % (attr, self._toUnicode(value)) for attr, value in attrvalues)
        return self._execute(args)

    def invoke(self, bean, signature, *params):
        args = ['invoke', bean, signature]
        args.extend(self._toUnicode(param) for param in params)
        return self._execute(args)

    def __repr__(self):
        return '%s:%d' % (self.server, self.port)
        
    def __del__(self):
        if hasattr(self, 'child') and not self.isClosed():
            self.close()
