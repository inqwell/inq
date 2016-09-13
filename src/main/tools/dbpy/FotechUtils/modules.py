#!/usr/local/bin/python
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/modules.py,v 1.1 2009/05/22 22:16:44 sanderst Exp $

import re, os.path, os, sys;
from Util import kbclog

log = kbclog.getLogger(__name__)

class ArbitraryModule:
    """
    After creation an ArbitraryModule instance holds the name, the fullname (i.e.
    fully qualified name) and the filename of a Python module. For example the
    module file "/python/libs/KBC/fotech.py" would, assuming the Python Path was
    "/python/libs/" have the following information:

        name     := fotech
        fullname := KBC.fotech
        filename := /python/libs/KBC/fotech.py

    The load method will attempt to import the module in to the current
    interpreter state. This should only be called once. If the load is successful
    the ArbitraryModule instance module will also hold an reference to the actual
    module instance.

    The has and get methods are used to query whether the module provides a
    particular function and to retrieve a reference to the function callable.

    NOTE - Normally this class is not seen by code outside of this module. All
           functionality should be accessed via the ArbitraryModuleManager.
    """
    def __init__ ( self, name, fullname, filename, extendpackagepaths = False ):
        """
        Creates an ArbitraryModule instance, in an unloaded state. The arguments
        are:
            
            name     - The 'local' name of the module, i.e. the last element of a
                       dotted module name (see example in class documentation).
            fullname - The 'global' name of the module; the full dotted module
                       name that one would use to import it from an external
                       module.
            filename - The full filename of the module file.
            extendpackagepaths - 
                       Whether to automatically extend a package's __path__ so
                       that modules in the same relative path to two or more
                       different python paths can be imported.  Defaults to False,
                       which is the default Python behaviour in this case
        """
        self.name = name;
        self.fullname = fullname;
        self.filename = filename;
        self.extendpackagepaths = extendpackagepaths;
        self.instance = None;
        self.functions = [ ];

    class ModuleNotInPackagePathsImportError(ImportError):
        """
        Raised by load() if an attempt is made to import a module which is not in 
        the search paths of its already loaded package, and self.extendpackagepaths is False
        """
        pass

    def load ( self ):
        """
        Attempts to import the module and get a reference to its instance. This
        can only be called once (if successful).
        """
        if self.instance: raise Exception, 'Cannot import module %s twice!' % ( str ( self ) );

        components = self.fullname.split ( '.' );

        # check to make sure that this module file is in the search path of its package
        if len ( components ) > 1:
            modulepath = os.path.dirname ( self.filename );
            packagename = '.'.join ( components [ 0 : len ( components ) - 1 ] );
            # importing nothing from the package returns the correct package path
            # a normal import would return the path of the top level package
            package = __import__ ( packagename, fromlist = [ '' ] );

            # check module path is in package search paths
            if modulepath not in [os.path.normpath(path) for path in package.__path__]:
                if self.extendpackagepaths:
                    # extend package search paths
                    package.__path__.append(modulepath);
                else:
                    raise self.ModuleNotInPackagePathsImportError, "Cannot import '%s' as another package with the name '%s' has been imported from path(s) %s" % ( self.filename, packagename, ', '.join ( package.__path__ ) )

        self.instance = __import__ ( self.fullname );

        if len ( components ) > 1:
            for component in components [ 1 : ]:
                self.instance = getattr ( self.instance, component );

        return self.instance;

    def has ( self, functionname ):
        """
        Returns true if the function name (string) provided is implemented by the
        module; otherwise false.
        """
        return functionname in self.functions;

    def get ( self, functionname ):
        """
        Returns a reference to the function callable. Takes the function name as a
        string.
        """
        return getattr ( self.instance, functionname );

    def __str__ ( self ):
        funcstring = '';
        if self.functions: funcstring = ','.join ( self.functions );
        return 'ArbitraryModule(%s(%s@%s)(%s))' % ( self.name, self.fullname, self.filename, funcstring );


class ArbitraryModuleManager:
    """
    Provides the public interface to the Arbitrary Module functionality. Can be
    instantiated normally but most often used as a Singleton.

    On creation it finds and loads all the modules in the PYTHONPATH that match
    the arguments provided to the constructor. These arguments are:

        regexstring
      
            A string (uncompiled) regular expression that is used to match the
            module filenames. Note that this is applied only to files that are
            local to a current directory. As such any un-escaped file separator
            characters will result in no modules being found.

            For example "*_wibble.py$" will match any modules in the PYTHONPATH
            that end in "_wibble.py".

       subdirectories
       
            A list of directories that should be appended to the various
            PYTHONPATH elements when searching for modules. These are not
            cumulative and every subdirectory will be applied to every PYTHONPATH
            element.

            For example if PYTHONPATH=/python/lib:/extra and subdirectories is
            [ "", "bin", "etc" ] then the following directories will be searched:

                /python/lib
                /python/lib/bin
                /python/lib/etc
                /extra
                /extra/bin
                /extra/etc

            Note that if you want the python path elements to be searched without
            an appended subdirectory, you need to specify "" or None in the list

            Named subdirectories must be proper Python modules (they are
            subdirectories of the PYTHONPATH after all); as such they must 
            contain a __init__.py (or .pyc) file and will be prepended to the 
            module's full name. For example:

                /extra/module1.py would have a fullname of "module1"
                /extra/bin/module2.py would have a fullname of "bin.module2"

        functions

            A list of function names (strings) from which the modules must provide
            at least on of. This can't include function names that begin with
            double-underscore as no attempt is made to mimic Python's name mangling.

        extendpackagepaths

            Whether to automatically extend a package's __path__ so that modules in 
            the same relative path to two or more different python paths can be 
            imported.  Defaults to False, which is the default Python behaviour.

        recursive

            Whether to search each subdirectory recursively for modules
    """

    def __init__ ( self, regexstring, subdirectories, functions, extendpackagepaths = False, recursive = False ):
        """
        Create an ArbitraryModuleManager instance - see the class level
        documentation for details on the arguments.

        Note - this is usually called via the static initialise method.
        """
        self.modules = self.loadModules ( self.findModules ( regexstring, subdirectories, extendpackagepaths, recursive ), functions );

    def findModules ( self, regexstring, subdirectories, extendpackagepaths, recursive ):
        """ Used by the constructor. """
        regex = re.compile ( regexstring );
        modules = { };
        for subdirectory in subdirectories:
            for path in self.findModulePaths ( subdirectory ):
                pathModules = self.findModuleNames ( regex, subdirectory, path, extendpackagepaths, recursive );
                # don't replace modules already found earlier in the search path
                modules.update ( [ ( key, value ) for key, value in pathModules.iteritems() if key not in modules ] );
        return modules;

    def loadModules ( self, modules, functions ):
        """ Used by the constructor. """
        functions = frozenset ( functions );
        loadedmodules = [ ];
        for module in modules.values ( ):
            try:
                instance = module.load ( );

                requiredfuncs = set ( dir ( instance ) ).intersection ( functions );
                if requiredfuncs:
                    module.functions = requiredfuncs;
                    loadedmodules.append ( module );
            except ArbitraryModule.ModuleNotInPackagePathsImportError, e:
                # ignore, just as a Python import would do in this case
                log.debug(e.message)
            except Exception:
                log.warn('Error importing module %s. Skipping', module.filename, exc_info = True)
        return loadedmodules;

    def findModulePaths ( self, subdirectory ):
        """ Used by the constructor. """
        paths = [ ];
        for syspath in sys.path:
            path = os.path.abspath ( os.path.join ( syspath, subdirectory ) );
            if os.path.isdir ( path ):
                paths.append ( path );
        return paths;

    def findModuleNames ( self, regex, subdirectory, directory, extendpackagepaths, recursive ):
        """ Used by the constructor. """
        if not os.path.isfile ( os.path.join ( directory, '__init__.py' ) ) and not os.path.isfile ( os.path.join ( directory, '__init__.pyc' ) ):
            # this will cause the importing of the parent package to fail, so skip such folders
            log.debug('Skipping directory %s as it does not contain a __init__.py or __init__.pyc file', directory)
            return { }
 
        modules = { }; 

        if subdirectory: prefix = '%s.' % ( subdirectory )
        else: prefix = ''

        for filename in os.listdir ( directory ):
            path = os.path.join ( directory, filename )
            if os.path.isdir ( path ):
                if recursive:
                    modules.update ( self.findModuleNames ( regex, '%s%s' % ( prefix, filename ), path, extendpackagepaths, recursive ) )
            else: 
                match = regex.match ( filename );
                if match:
                    modulename = match.group ( 'module' );
                    fullname = '%s%s' % ( prefix, modulename )

                    modules [ modulename ] = ArbitraryModule ( modulename, fullname, path, extendpackagepaths );
        return modules;

    def getModules ( self, functionname = None ):
        """
        If no functionname (string) is provided this will return a list of all the
        module instances in the manager. If a functionname is provided then this
        will return a list of all the module instances in the manager that provide 
        the function name in question.
        """
        return [ module.instance for module in self.modules if ( not functionname ) or functionname in module.functions ];

    def getFunctions ( self, functionname ):
        """
        Returns a list of the function callables in the manager's module set that
        have the name provided in functionname (string).
        """
        return [ getattr ( module, functionname ) for module in self.getModules ( functionname ) ];

    def runFunctions ( self, functionname, *arguments ):
        """
        Takes a functionname (string) and argument list (as normal arguments). 
        Every instance of that function provided by the manager's modules is run
        using said arguments. Note that the execution order of the functions
        cannot be assured.
        """
        for function in self.getFunctions ( functionname ):
            function ( *arguments );

    def getFilenames ( self ):
        """
        Returns a set of the manager's module filenames (as strings).
        """
        return frozenset ( [ module.filename for module in self.modules ] );
    
    # Singleton accessors
    theInstance = None;

    @staticmethod
    def instance ( ):
        """
        Returns the singleton instance of the ArbitraryModuleManager. This should
        only be called after the instance has been initialised using the static
        initialise method.
        """
        if ArbitraryModuleManager.theInstance is None: raise Exception, 'ArbitraryModuleManager.initialise ( ... ) must be called before instance is used!';
        return ArbitraryModuleManager.theInstance;
    
    @staticmethod
    def initialise ( regexstring, subdirectories, functions, extendpackagepaths = False, recursive = False ):
        """
        Initialises the ArbitraryModuleManager singleton instance (see the 
        ArbitraryModuleManager class documentation for an explanation of the
        arguments) and returns a reference to the new instance. This should
        only be called once, and any further calls to the instance should be
        done via the instance static method.
        """
        if not ArbitraryModuleManager.theInstance is None: raise Exception, 'ArbitraryModuleManager.initialise ( ... ) can only be called once!';
        ArbitraryModuleManager.theInstance = ArbitraryModuleManager ( regexstring, subdirectories, functions, extendpackagepaths, recursive );
        return ArbitraryModuleManager.theInstance;


##############################################################################
# Unit test

if __name__ == '__main__':
    requiredfunctions = [ 'InitialiseNormalTests', 'InitialiseRoamingTests' ];
    ArbitraryModuleManager.initialise ( '(?P<module>initialise_[^\.]+)\.py$', [ 'focam' ], requiredfunctions );
 
    print 'Filenames for modules';
    for filename in ArbitraryModuleManager.instance ( ).getFilenames ( ):
        print '   ', filename;
    print;
    
    for function in requiredfunctions:
        print 'Module instances that support "%s" :' % function;
        for module in ArbitraryModuleManager.instance ( ).getModules ( function ):
            print '   ', module;
        print 'Function instances for "%s"        :' % function;
        for function in ArbitraryModuleManager.instance ( ).getFunctions ( function ):
            print '   ', function;
        print;

