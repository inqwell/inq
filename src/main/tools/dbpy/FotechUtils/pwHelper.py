#
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/pwHelper.py,v 1.1 2009/05/22 22:16:45 sanderst Exp $
#
import os
import KBC.fotech

from Util.getpw import GetPW

from FotechUtils import LogHelper
from FotechUtils.FotechRoot import FOTechRoot

log = LogHelper.getLogger( __name__ )

"""
    The main front office password file
"""
def get_password_file():
    return FOTechRoot.make_relative_path( "etc", "pwd.cfg" )

class PasswordHelper:
    def __init__( self, passwordfile = None ):
        if passwordfile == None:
            passwordfile = get_password_file()
        log.info( "Using password file: %s" %( passwordfile ) )
        self.__getpw = GetPW( configFile = passwordfile )
    
    def get_password( self, server, user ):
        """
            Standard get password for server and user
        """
        return self.__getpw.get( server, user )
