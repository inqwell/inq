#!/usr/local/bin/python
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/locality.py,v 1.1 2009/05/22 22:16:41 sanderst Exp $

import os
import socket
from Util import winutils

class LocalityExecption(Exception):
    def __init__(self, msg):
        Exception.__init__(self, msg)       
               
class Locality:
    '''Class that identifies the site and company of the environment where this process is running'''
    def __init__(self):
        if winutils.onWindows():
            [self._site, self._company] = self._getSiteAndCompanyOnWindows()
        else:
            [self._site, self._company] = self._getSiteAndCompanyOnUnix()        
    
    def _getSiteAndCompanyOnUnix(self):
        '''extract the site and company from the fully qualified domain name. Throws an exception if
        the site cannot be extracted from the domain name'''
        domainName = socket.getfqdn(socket.gethostname()) 
        names = domainName.lower().split('.')
        if len(names) < 3:
            raise LocalityExecption("Invalid domain name: %s. Need at least 3 period-seperated components (got %s)" % (domainName, len(names)))
        
        return names[-3:-1]    
                
    def _getSiteAndCompanyOnWindows(self):
        '''gets the site name and company using the systems-defined environment variable'''
        keys = ["KBCSITENAME", "USERDOMAIN"]
        try:
            return [os.environ[x] for x in keys]
        except KeyError, e:
            raise LocalityExecption("Environment variable %s is not defined" % e.args)        

    def site(self):
        return self._site
    
    def company(self):
        return self._company
    
    def siteAndCompany(self):
        return "%s.%s" %( self.site(), self.company())

                          
if __name__ == '__main__':
    print Locality().siteAndCompany()