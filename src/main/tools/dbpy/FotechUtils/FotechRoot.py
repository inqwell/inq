"""
$Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/FotechRoot.py,v 1.1 2009/05/22 22:16:37 sanderst Exp $
$Author: sanderst $
$Revision: 1.1 $
$DateTime: 2009/04/03 13:49:55 $
$Change: 163617 $

Module for obtaining the network root path, based on original Sophis implementation

Network root is the path to the "fotech" directory for the current environment.
This is /prod/fotech/ for the production environment, for example.

By default the network root is "guessed" relative to the location of this module.
This is not always appropriate - for example, when testing run scripts this file
may be located outside of the tree where the run script is located.
"""

import KBC.fotech

import os
from FotechUtils import LogHelper

log = LogHelper.getLogger( __name__ )

class SystemRoot:
    def __init__( self, path, name ):
        self._path = path
        self._name = name
        self._network_root = "not set yet"
        self._guess_root()
    
    def get( self ):
        """
            Returns the current network root.
        """
        return self._network_root

    def set( self, *new_path_parts ):
        """
            Sets the network root to the specified path. Multiple parts can be passed in
            - such parts will be joined using os.path.join.
        
            Note: do not modify the network root from within libraries as this defeats the purpose
            of the module! Only the end-user application should use this function.
        """
        
        self._network_root = os.path.abspath( os.path.join( *new_path_parts ) )
        log.info( "%s network root set to: %s" % ( self._name,  self._network_root ) )

    def make_relative_path( self, *rel_path ):
        """
            Combines the relative path passed in with the network root to obtain an
            absolute path. Several parts may be passed in - eg make_relative_path("..", "..", "something")
            - such parts will be combined using os.path.join.
        """
        return os.path.abspath( os.path.join( self._network_root, *rel_path ) )

    def _guess_root( self ):
        """
            Guess the network root
        """
        curpath = os.path.abspath( os.path.dirname( self._path ) )
        
        while True:
            dirlist = [ name for name in os.listdir( curpath ) if os.path.isdir( os.path.join( curpath, name ) ) ]
            if self.is_root( dirlist ):
                self.set( curpath )
                return
            oldpath = curpath
            curpath = os.path.dirname( curpath )
            if oldpath == curpath:
                break
        
        raise Exception( "Could not guess root" )

    def is_root( self, dirlist ):
        raise Exception( "Should override is_root" )

class ClsFotechRoot( SystemRoot ):
    def __init__( self ):
        SystemRoot.__init__( self, os.path.abspath( os.path.dirname( __file__ ) ), "FOTech" )

    def is_root( self, dirlist ):
        return 'lib' in dirlist and 'etc' in dirlist and 'bin' in dirlist and 'EquityDerivs' in dirlist

global FOTechRoot
FOTechRoot = ClsFotechRoot()
