#!/usr/local/bin/python
import KBC.fotech
import os, popen2, fcntl, select
from Util import kbclog

log = kbclog.getLogger(__name__)

def makeNonBlocking(fd):
    'helper for getCommandOutput'
    fl = fcntl.fcntl(fd, fcntl.F_GETFL)
    fcntl.fcntl(fd, fcntl.F_SETFL, fl | os.O_NDELAY)

def getCommandOutput(command = None, inChild = None, returnError = False, returnCode = False):
    """
    Runs the given command, returning the standard output.

    If inChild is specified, no command is executed, but the already existing child process is read.
    If returnError is True, then return a tuple of standard output and standard error.
    If returnError and returnCode are both True, then return a tuple of standard output, standard error, and return code.

    see http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/52296 for how to run a Unix cmd and control stderr/stdout.
    This is modified from that source to allow caller to supply the child, and remove closed file descriptors from select()
    Consider also http://www.pixelbeat.org/libs/subProcess.py
    """
    child = inChild or popen2.Popen3(command, 1) # capture stdout and stderr from command
    child.tochild.close()             # don't need to talk to child
    outfile = child.fromchild
    outfd = outfile.fileno()
    errfile = child.childerr
    errfd = errfile.fileno()
    makeNonBlocking(outfd)            # don't deadlock!
    makeNonBlocking(errfd)
    outdata = []
    errdata = []
    fds = [outfd,errfd]
    while len(fds) != 0:
        ready = select.select(fds,[],[]) # wait for input
        if outfd in ready[0]:
            outchunk = outfile.read()
            if outchunk == '': fds.remove(outfd)
            outdata.append( outchunk )
        if errfd in ready[0]:
            errchunk = errfile.read()
            if errchunk == '': fds.remove(errfd)
            errdata.append( errchunk )
    err = child.wait()
    if err != 0:
        # e.g. grep will fail with status=1 if no matches are found, which many dashboard tests do
        # normally, so just log the fact
        #raise RuntimeError, '%s failed w/ exit code %d\n%s' % (command, err, errdata)
        log.debug('%s failed w/ exit code %d\n%s' % (command, err, errdata))

    stdout = "".join(outdata)

    if not returnError:
        return stdout
    else:
        result = [stdout, "".join(errdata)]
        if returnCode: result.append(err)
        return tuple(result)
