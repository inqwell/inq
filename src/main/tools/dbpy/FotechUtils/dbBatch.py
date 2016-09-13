#
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/dbBatch.py,v 1.1 2009/05/22 22:16:26 sanderst Exp $
#
# Helper methods to support selection/deletion of multiple entities
#
import os

import KBC.fotech

from Util import db
from FotechUtils import LogHelper
from FotechUtils import dbUtils

log = LogHelper.getLogger( __name__ )

def setLevel( level ):
    LogHelper.setLevel( log, level )

def select_in_batches( con, sql, token, batchClause, clauseList, batchsize, withResults = True ):
    """
        Batch up selects and return data. This is intended for cases where you want to execute a query to a big
        list of securities for example. The sql query should contain some form of __TOKEN__ that is replaced
        by this query for each batched execution. A list of ids must be given that the are batched up with
        each query:
        
        sql query  = "SELECT * from HISTOMVTS WHERE __TOKEN__"
        token      = "__TOKEN__"
        batchClase = " sicovam in ( %s )"
        clauseList = [ 1,2,3,4,5 .. 10001, 1002 etc ]
        batchsize  = 3
        
        then the queries are executed:
        
        SELECT * from HISTOMVTS WHERE sicovam in ( 1,2,3 )
        SELECT * from HISTOMVTS WHERE sicovam in ( 4,5,6 )
        SELECT * from HISTOMVTS WHERE sicovam in ( 7,8,9 )
        etc
        
        and all results are returned in one list
    """
    data = []
    
    if clauseList:
        for i in xrange( 0, len( clauseList ), batchsize ):
            clause = batchClause % ( ", ".join( map( str, clauseList[ i : i + batchsize ] ) ) )
            thisSql = sql.replace( token, clause )
            
            log.info( thisSql )
            
            results = con.execute( thisSql )[ 0 ] 
            
            if results and withResults:
                data.extend( results )
    else:
        thisSql = sql.replace(  token, "" )
        log.info( thisSql )
        data = con.execute( thisSql )[ 0 ]
    
    return data

def delete_in_batches( con, sql, token, batchClause, clauseList, batchsize ):
    """
        Do deletes in batches
    """
    select_in_batches( con, sql, token, batchClause, clauseList, batchsize, False )
