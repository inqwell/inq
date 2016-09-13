#
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/dbHelper.py,v 1.1 2009/05/22 22:16:31 sanderst Exp $
#
import os

import KBC.fotech

from Util import db

from FotechUtils import LogHelper
from FotechUtils import dbUtils
from FotechUtils.FotechRoot import FOTechRoot

from FotechUtils.pwHelper import get_password_file

log = LogHelper.getLogger( __name__ )

"""
    The main 'database.xml' file used by Front Office
"""

def get_database_xml():
    return FOTechRoot.make_relative_path( "etc", "database.xml" )

"""
    The system names defined in the database.xml files for Front Office
"""
FDESystem = 'fdenterprise'

def get_fde_connection( level, access = 'read' ):  
    return get_any_connection( get_database_xml(), get_password_file(), level, access, FDESystem )

def get_any_connection( db_file, pw_file, level, access, system ):
    """
        Util method to get a connection
    """
    log.info("FOTech DBHelper.getConnection: %s, %s" % (db_file, pw_file))
    return dbUtils.getConnection( db_file, system, level, access, pwdfile = pw_file )
