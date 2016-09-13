#
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/LogHelper.py,v 1.1 2009/05/22 22:16:42 sanderst Exp $
#
import KBC.fotech

from Util import kbclog

def setLevel( theLog, verboseMode ):
    if verboseMode:
        theLog.setLevel( kbclog.DEBUG )
    else:
        theLog.setLevel( kbclog.INFO )

errorSet = False

def getLogger( name ):
    log = kbclog.getLogger( name )
    
    log.setLevel( kbclog.INFO )
    
    global errorSet
    
    if not errorSet:
        kbclog.logErrorsToStderrOnly()
        errorSet = True
    
    return log
