#
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/dbUtils.py,v 1.1 2009/05/22 22:16:32 sanderst Exp $
#
import KBC.fotech

from Util import db

from dbConfig import configurationProvider

def getConnection( confile, system, level, access = "read", site = None, user = None, pwdfile = None ):
    """
        Partial replacement for the db.py mess in cbtech/python2.5. You should use /prod/fotech/bin/generateDatabaseXml.py
        to generate an xml file containing your system/level config from the old db.py. Then replace any call to db.getConnection
        with dbUtils.getConnection and you should get back the same object that you would have got in the old strategy.
    """
    config = configurationProvider( confile, pwdfile )
    
    vendor, server, user, password, schema, host, port = config.getConnectionDetails( system, level, access, site, user )
    
    return db._getConnection( vendor.upper(), server, schema, user, password )
