#!/usr/local/bin/python
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/dbConfig.py,v 1.1 2009/05/22 22:16:27 sanderst Exp $

import os
import os.path
import socket
from StringIO import StringIO
        
from xml.dom.minidom import parseString

import KBC.fotech
from Util.getpw import GetPW
from Util import kbclog
from FotechUtils.dbError import dbError
from FotechUtils import execcontext

log = kbclog.getLogger("dbConfig")

#
# TODO - this is duplicated from db.py. Duplication is necessary as we cannot import db.py when
# running on windows. When the connection info and connection code is split in db.py then
# this duplication should be removed.
#

class configurationProvider:
    '''A database connection configuration provider. This class is capable of scanning a configuration
    file for connection information and providing methods for getting the appropriate connection
    info for a given server, user, site etc.

    This differs from db.py in that it uses databases.xml for specification of database connection info

    Note that the cfgFile passwed to the constructor can be either a file name or a file/file-like object.'''

    def __init__(self, cfgFile = None, pwdfile = None):
        # cfgFile should either be a string file name or a file/file-like object, or None in which case getDefaultCfgFileName() is used
        if cfgFile is None:
            cfgFile = open(self.getDefaultCfgFileName())
        elif type(cfgFile) == type("string"):
            cfgFile = open(cfgFile)
        try:
            self.connections = self.parseCfgFile(cfgFile)
        finally:
            cfgFile.close()
        self.__pwfile = pwdfile

    def getDefaultCfgFileName(self):
        parentPath = os.path.abspath(__file__)
        while parentPath != os.sep:
            parentPath, currentDir = os.path.split(parentPath)
            if currentDir == "lib":
                return os.path.join(parentPath, "etc" + os.sep + "database.xml")

    def extractValFromDict(self, d, mandatory, key):
        try:
            return d[key]
        except KeyError:
            if mandatory:
                raise dbError("connection %s does not contain %s property" % (d.get("system", "unknown"), key))

    def parseConnectionElement(self, e):
        ''' Parses an xml element specifying a connection. Returns three elements:
        * system name
        * tuple containing (site, access, level)
        * tuple containing (db type, server, schema, user, password, host, port)

        XML must be of the format:

        <!-- London prod -->
        <connection provider="sybase">
            <access   value="write"/>
            <site     value="London"/>
            <system   value="insight-credit"/>
            <level    value="prod"/>
            <host     value="db1.nyc.kbcfp.com"/>
            <port     value="1400"/>
            <server   value="GELBASE"/>
            <database value="production"/>
            <user     value="data_user"/>
        </connection>
        '''
        if e.localName != "connection" or e.hasAttribute("provider") is False:
            raise dbError("Cannot parse XML element named '%s'" % e.localName)

        nodeDict = {}
        nodeDict["provider"] = e.getAttribute("provider")

        for child in e.childNodes:
            if child.nodeType == child.ELEMENT_NODE and child.hasAttribute("value"):
                nodeDict[child.nodeName] = child.getAttribute("value")

        return ( self.extractValFromDict(nodeDict, True, "system"),
                 (
                   self.extractValFromDict(nodeDict, True,  "site"),
                   self.extractValFromDict(nodeDict, True,  "access"),
                   self.extractValFromDict(nodeDict, True,  "level"),
                 ),
                 (
                   self.extractValFromDict(nodeDict, True,  "provider"),
                   self.extractValFromDict(nodeDict, True,  "server"),
                   self.extractValFromDict(nodeDict, False, "database"),
                   self.extractValFromDict(nodeDict, True,  "user"),
                   self.extractValFromDict(nodeDict, False, "password"),
                   self.extractValFromDict(nodeDict, False, "passwordFile"),
                   self.extractValFromDict(nodeDict, False, "host"),
                   self.extractValFromDict(nodeDict, False, "port")
                 )
               )

    def parseCfgFile(self, cfgFile):
        '''
        Parse the xml configuration file and turn it into a dictionary keyed by system name, with
        the value being a list of two elements:
         * a tuple containing (site, [levels], [access modes]); and
         * a tuple specifying the connection (db type, server, schema, user, password)
        '''
        if not isinstance(cfgFile, StringIO):
            log.info("Parsing %s" % cfgFile.name)

        connections = {}
        cfg = parseString(cfgFile.read())
        try:
            for e in cfg.getElementsByTagName("connection"):

                (system, (site, access, level), connectionInfo) = self.parseConnectionElement(e)

                skipAdd = False
                systemEntries = connections.setdefault(system, [])

                for entry in systemEntries:
                    (entrySite, entryLevels, entryAccessModes), entryConnectionInfo = entry

                    if entrySite == site and entryConnectionInfo == connectionInfo:
                        # add the access and level specifications into the entry specifiers lists
                        if level not in entryLevels:
                            entryLevels.append(level)
                        if access not in entryAccessModes:
                            entryAccessModes.append(access)

                        # skip adding this entry to the list of connections as it already exists
                        skipAdd = True
                        break

                if skipAdd is False:
                    systemEntries.append(((site, [level], [access]), connectionInfo))
        finally:
            cfg.unlink()

        return connections

    #
    # TODO - this function is duplicated from db.py.
    #
    def getConnectionDetails(self, system, level, access = "read", site = None, user = None):

        if user == None:
            try: user = pwd.getpwuid(os.getuid())[0]
            except KeyError: pass
            except NameError: pass;

        if site == None:
            site = execcontext.getSite()
            if site == "nyc":
                site = execcontext.getSiteAndCompany()

        databaseDescription = "%s:%s:%s:%s:%s" % (system,level,access,site,user)
        log.info("FIND %s" % databaseDescription )

        siteAndLevelFilter = lambda sc: sc and site == sc[0][0].lower() and level in sc[0][1]
        exactAccessFilter = lambda sc: access in sc[0][2]
        writeAccessFallbackFilter = lambda sc: "write" in sc[0][2]
      
        siteAndLevelConnections = filter(siteAndLevelFilter, self.connections.get(system, []))    
        matchingConnections = filter(exactAccessFilter, siteAndLevelConnections )
        
        if len(matchingConnections) == 0 and access == "read":
             matchingConnections = filter(writeAccessFallbackFilter, siteAndLevelConnections )
        
        if len(matchingConnections) > 1:
            raise dbError("More than one connection found for %s" % databaseDescription)
        if len(matchingConnections) < 1:
            raise dbError("Could not find database %s" % databaseDescription)        
        
        (vendor, server, schema, user, password, pwfile, host, port) = matchingConnections[0][1]
        
        #
        # Override password file if specified in config
        #
        if not pwfile and self.__pwfile:
            pwfile = self.__pwfile

        if not password and pwfile:
            password = GetPW( configFile = pwfile ).get( server, user )

        return (vendor, server, user, password, schema, host, port)    


import unittest
class __tester(unittest.TestCase):
    
    mockConfigXml = '''<?xml version="1.0" encoding="utf-8" ?>
<connections>
    <!--
        $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/dbConfig.py,v 1.1 2009/05/22 22:16:27 sanderst Exp $
    -->
    <!-- Tests for getting seperate read and write connections -->
    <connection provider="oracle">
        <access   value="write"/>
        <site     value="london"/>
        <system   value="writeOnlySystem"/>
        <level    value="prod"/>
        <server   value="fodev3.london"/>
        <user     value="general_rw_user"/>
    </connection>
     <connection provider="oracle">
        <access   value="read"/>
        <site     value="london"/>
        <system   value="readOnlySystem"/>
        <level    value="prod"/>
        <server   value="fodev3.london"/>
        <user     value="reader"/>
    </connection>
   
     <connection provider="oracle">
        <access   value="read"/>
        <site     value="london"/>
        <system   value="readerAndWriterSystem"/>
        <level    value="prod"/>
        <server   value="fodev3.london"/>
        <user     value="reader"/>
    </connection>
    <connection provider="oracle">
        <access   value="write"/>
        <site     value="london"/>
        <system   value="readerAndWriterSystem"/>
        <level    value="prod"/>
        <server   value="fodev3.london"/>
        <user     value="writer"/>
    </connection>

    <!-- End of test section -->
    
    <!-- Test that we don't get a hk connection for a system with only london access -->
    <connection provider="oracle">
        <access   value="write"/>
        <site     value="london"/>
        <system   value="londonOnlySystem"/>
        <level    value="prod"/>
        <server   value="fodev3.london"/>
        <user     value="general_rw_user"/>
    </connection>    
    
    <!-- End of test section -->
  
</connections>    
    '''
    
    def setUp(self):
        self.provider = configurationProvider()
        self.mockProvider = configurationProvider(cfgFile = StringIO(self.mockConfigXml))

    def testGetDefaultCfg(self):
        cfg = self.provider.getDefaultCfgFileName()
        self.assertTrue(os.path.split(os.path.dirname(cfg))[1] == "etc")
        self.assertTrue(os.path.basename(cfg) == "database.xml")

    def testConnectionList(self):
        self.assertTrue(self.provider.connections.has_key("sophis"))
        self.assertTrue(self.provider.connections["sophis"][0][0][0] == "London")

    def testGetConnectionDetailsForSophis(self):
        (vendor, server, user, password, schema, host, port) = self.provider.getConnectionDetails("sophis", "prod")
        self.assertEqual(server, "sophis1.london")
        self.assertTrue(vendor == "oracle")
        self.assertTrue(user == "risk")
        (vendor, server, user, password, schema, host, port) = self.provider.getConnectionDetails("sophis", "qa", "write")
        self.assertTrue(server == "sopdev.london")
        self.assertTrue(vendor == "oracle")
        self.assertTrue(user == "riskint")

    def testGetConnectionDetailsForImagine(self):
        (vendor, server, user, password, schema, host, port) = self.provider.getConnectionDetails("imagine", "prod", "write")
        self.assertTrue(server == "GEDX_LN")
        self.assertTrue(vendor == "sybase")
        self.assertTrue(user == "derivapp")
        self.assertTrue(port == "1400")
        
    def testConnectionDetailsProductionAllSites(self):
        (vendor, server, user, password, schema, host, port) = self.provider.getConnectionDetails("production", "prod", "read", "london")
        self.assertEqual(server, "GELBASE_LN")
        self.assertTrue(vendor == "sybase")
        self.assertTrue(user == "cbrun")
        self.assertTrue(port == "1400")
        
        (vendor, server, user, password, schema, host, port) = self.provider.getConnectionDetails("production", "prod", "read", "newyork")
        self.assertTrue(server == "GELBASE")
        self.assertTrue(vendor == "sybase")
        self.assertTrue(user == "cbrun")
        self.assertTrue(port == "1400")
        
        (vendor, server, user, password, schema, host, port) = self.provider.getConnectionDetails("production", "prod", "read", "tokyo")
        self.assertTrue(server == "GELBASE_TK")
        self.assertTrue(vendor == "sybase")
        self.assertTrue(user == "cbrun")
        self.assertTrue(port == "1400")
        
        (vendor, server, user, password, schema, host, port) = self.provider.getConnectionDetails("production", "prod", "read", "hongkong")
        self.assertTrue(server == "GELBASE_TK")
        self.assertTrue(vendor == "sybase")
        self.assertTrue(user == "cbrun")
        self.assertTrue(port == "1400")

    def testGetConnectionDetailsForBadDetails(self):
        self.assertRaises(dbError, self.mockProvider.getConnectionDetails, "sldjfnsaf", "prod")
        self.assertRaises(dbError, self.mockProvider.getConnectionDetails, "londonOnlySystem", "badlevel")
        self.assertRaises(dbError, self.mockProvider.getConnectionDetails, "londonOnlySystem", "prod", "badaccess")
        self.assertRaises(dbError, self.mockProvider.getConnectionDetails, "londonOnlySystem", "prod", "read", "badsite")

    def testMockSystemWithOnlyAWriteAccessEntry(self):
        (vendor, server, user, password, schema, host, port) = self.mockProvider.getConnectionDetails("writeOnlySystem", "prod", "read")
        self.assertTrue(user == "general_rw_user")
        (vendor, server, user, password, schema, host, port) = self.mockProvider.getConnectionDetails("writeOnlySystem", "prod", "write")
        self.assertTrue(user == "general_rw_user")

    def testMockSystemWithOnlyAReadAccessEntry(self):
        (vendor, server, user, password, schema, host, port) = self.mockProvider.getConnectionDetails("readOnlySystem", "prod", "read")
        self.assertTrue(user == "reader")
        self.assertRaises(dbError, self.mockProvider.getConnectionDetails, "readOnlySystem", "prod", "write")

    def testMockSystemWithSeperateReadWriteEntries(self):
        (vendor, server, user, password, schema, host, port) = self.mockProvider.getConnectionDetails("readerAndWriterSystem", "prod", "read")
        self.assertTrue(user == "reader")
        (vendor, server, user, password, schema, host, port) = self.mockProvider.getConnectionDetails("readerAndWriterSystem", "prod", "write")
        self.assertTrue(user == "writer")

    def testMockSystemWithLondonOnlyEntries(self):
        self.mockProvider.getConnectionDetails("londonOnlySystem", "prod", "read", "london")
        self.assertRaises(dbError, self.mockProvider.getConnectionDetails, "londonOnlySystem", "prod", "read", "hk")       
    

if __name__ == '__main__':
    unittest.main()

