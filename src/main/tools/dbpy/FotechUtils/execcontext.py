#!/usr/local/bin/python
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/execcontext.py,v 1.1 2009/05/22 22:16:35 sanderst Exp $

import os, platform, socket, sys

def onWindows():
    #
    # Microsoft gets return on ts 2008 under python 2.4 :-(
    #
    return platform.system() in ( "Windows", "Microsoft" )

# os specific functions
if onWindows():
    import win32api

    def getUser():
        """ Get the current user's username """
        return win32api.GetUserName()

    def getLogin():
        """ Get the current user's username """
        return getUser()

    def getUserDomain():
        """ Gets the Windows domain of the current user """
        if "USERDOMAIN" in os.environ:
            return os.environ["USERDOMAIN"]
        
        # returns e.g. KBCFP\username
        fqun = win32api.GetUserNameEx(win32api.NameSamCompatible)
        names = fqun.split('\\')
        if len(names) != 2:
            raise ValueError("Invalid user name: %s. Need 2 backslash-separated components (got %d)" % (fqun, len(names)))
        return names[0]
        
    def getSite():
        """ Gets the site of the current host """
        # KBCSITENAME is more reliable on Windows boxes, but not always set
        return os.environ.get("KBCSITENAME", _getSiteAndCompanyFromFqdn()[0]).lower()
        
    def getCompany():
        """ Gets the company of the current user """
        # to lower so that values are comparable under Windows and Unix
        return getUserDomain().lower()
else:
    import pwd

    def getUser():
        """ Gets the username of the user being used to execute this script """
        return pwd.getpwuid(os.getuid())[0]

    def getLogin():
        """ Gets the actual logged in user (not the sudo -u user) """
        # os.getlogin() fails if not run from a terminal
        if os.isatty(sys.stdin.fileno()):
            try:
                return os.getlogin()
            except OSError:
                # OSError if it is a detached SCREEN
                pass

        return getUser()
            
    # no such thing as a user domain under unix

    def getSite():
        """ Gets the site of the current host """
        return _getSiteAndCompanyFromFqdn()[0]
    
    def getCompany():
        """ Gets the company of the current host """
        return _getSiteAndCompanyFromFqdn()[1]
        
# cross platform functions

def _getSiteAndCompanyFromFqdn():
    '''extract the site and company from the fully qualified domain name. Throws an exception if
    the site cannot be extracted from the domain name'''
    domainName = socket.getfqdn(socket.gethostname())
    names = domainName.lower().split('.')
    if len(names) < 3:
        raise ValueError("Invalid domain name: %s. Need at least 3 period-separated components (got %d)" % (domainName, len(names)))
    
    return names[-3:-1]

def getSiteAndCompany():
    return "%s.%s" % (getSite(), getCompany())
