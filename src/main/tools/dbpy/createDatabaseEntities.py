#!/usr/local/bin/python
#
# $Header: /home/inqwell/cvsroot/dev/scripts/python/createDatabaseEntities.py,v 1.1 2009/05/22 22:15:43 sanderst Exp $
#
# For a given user 'x', make sure that x_app and x_reader have w synonyms to db entities x_app is r/w
# while x_reader is r/o
#
import KBC.fotech

from Util import commandline

from FotechUtils import LogHelper

from XyUtil.XyDbHelper import get_connection
from XyUtil.XyDbHelper import get_schema_connection

log = LogHelper.getLogger( __name__ )

ignorable_entities = ( 'CONSUMER GROUP', 'SCHEDULE', 'OPERATOR', 'WINDOW', 'LIBRARY', 'JOB CLASS', 'WINDOW GROUP', 'TYPE', 'EVALUATION CONTEXT', 'INDEX', 'LOB' )

def statement( ofile, text ):
    ofile.write( "%s\n/\n" % ( text ) )

def create_synonym( schema, objname, synowner ):
    return "CREATE OR REPLACE SYNONYM %s.%s FOR %s.%s" % ( synowner, objname, schema, objname )

def create_app_synonym( schema, objname ):
    return create_synonym( schema, objname, schema + "_APP" )

def create_reader_synonym( schema, objname ):
    return create_synonym( schema, objname, schema + "_READER" )

def grant_all( schema, objname, role ):
    return "GRANT SELECT, INSERT, UPDATE, DELETE ON %s.%s TO %s" % ( schema, objname, role )

def grant_select( schema, objname, role ):
    return "GRANT SELECT ON %s.%s TO %s" % ( schema, objname, role )

def grant_execute( schema, objname, role ):
    return "GRANT EXECUTE ON %s.%s TO %s" % ( schema, objname, role )

def valid_table( objname ):
    return objname and not objname.startswith( 'BIN$' )

def process_table( objtype, objname, ofile, schema ):
    """
        Process a table
    """
    if valid_table( objname ):
        log.info( "Processing table : %s" % ( objname ) )
        write_entity_header( ofile, objname )
    
        statement( ofile, create_app_synonym( schema, objname ) )
        statement( ofile, create_reader_synonym( schema, objname ) )
    
        statement( ofile, grant_all ( schema, objname, schema + "_RW" ) )
        statement( ofile, grant_select( schema, objname, schema + "_RO" ) )

def process_view( objtype, objname, ofile, schema ):
    """
        Process a view
    """
    log.info( "Processing view : %s" % ( objname ) )
    write_entity_header( ofile, objname )
    
    statement( ofile, create_app_synonym( schema, objname ) )
    statement( ofile, create_reader_synonym( schema, objname ) )
    
    statement( ofile, grant_select( schema, objname, schema + "_RW" ) )
    statement( ofile, grant_select( schema, objname, schema + "_RO" ) )

def process_sequence( objtype, objname, ofile, schema ):
    """
        Process a sequence
    """
    log.info( "Processing sequence : %s" % ( objname ) )
    write_entity_header( ofile, objname )
    
    statement( ofile, create_app_synonym( schema, objname ) )
    statement( ofile, create_reader_synonym( schema, objname ) )
    
    statement( ofile, grant_select( schema, objname, schema + "_RW" ) )
    statement( ofile, grant_select( schema, objname, schema + "_RO" ) )

def process_procedure( objtype, objname, ofile, schema ):
    """
        Process a procedure
    """
    log.info( "Processing procedure : %s" % ( objname ) )
    write_entity_header( ofile, objname )
    
    statement( ofile, create_app_synonym( schema, objname ) )
    statement( ofile, create_reader_synonym( schema, objname ) )
    
    statement( ofile, grant_execute( schema, objname, schema + "_RW" ) )

def process_unknown( objtype, objname, ofile, schema ):
    print "Ignoring object %s/%s" % ( objtype, objname )

objmap =    {
                'TABLE'     : process_table,
                'VIEW'      : process_view,
                'SEQUENCE'  : process_sequence,
                'PROCEDURE' : process_procedure
            }

def get_all_entities( con, owner ):
    """
        returns a map of object type -> list of objects for the current owner
    """
    sql = "select object_name, object_type from all_objects where status = 'VALID' and owner = '%s'" % ( owner )
    log.info( sql )
    
    objects = con.execute( sql )[ 0 ]
    
    ret = {}
    
    for ( name, type ) in objects:
        if type not in ret:
            ret[ type ] = []
        
        ret[ type ].append( name )
    
    print ret
    
    return ret

def write_entity_header( ofile, objname ):
    ofile.write( "--\n" )
    ofile.write( "--\n" )
    ofile.write( "-- Entity : %s\n" % ( objname  ) )
    ofile.write( "--\n" )
    ofile.write( "--\n\n" )

def process( con, owner, ofile ):
    """
        Get all entities on the schema connection and make synonyms on app and reader
    """
    objs = get_all_entities( con, owner )
    
    ofile.write( "--\n" )
    ofile.write( "-- $Header: /home/inqwell/cvsroot/dev/scripts/python/createDatabaseEntities.py,v 1.1 2009/05/22 22:15:43 sanderst Exp $\n" )
    ofile.write( "--\n" )
    ofile.write( "-- DLL for Synonyms and Permissions for schema : %s\n" % ( owner ) )
    ofile.write( "--\n" )
    ofile.write( "--\n" )
        
    for objtype in objs:
        if objtype not in ignorable_entities:
            ofile.write( "--\n" )
            ofile.write( "--\n" )
            ofile.write( "-- Entity type : %s\n" % ( objtype  ) )
            ofile.write( "--\n" )
            ofile.write( "--\n" )
        
            for objname in objs[ objtype ]:            
                objmap.get( objtype, process_unknown )( objtype, objname, ofile, owner )
    
    ofile.write( "--\n" )
    ofile.write( "-- Roles\n" )
    ofile.write( "--\n" )
    
    statement( ofile, "GRANT %s_RW TO %s_APP" % ( owner, owner ) )
    statement( ofile, "GRANT %s_RO TO %s_READER" % ( owner, owner ) )
    
    ofile.close()

def main( args ):
    argSpec =   [
                    ( 'level',     'l', 'level',      True,  True,  False, "Level to connect to" ),
                    ( 'schema',    's', 'schema',     True,  True,  False, "Schema owner name" ),
                    ( 'output',    'o', 'output',     True,  True,  False, "Output file to write to" ),
                    ( 'verbose',   'v', 'verbose',    False, False, False, "Verbose mode" )
                ]
    
    parser = commandline.Parser(args[0], argSpec)
    
    matchedArgs, unmatchedArgs = parser.parse(args[1:])
    
    level   = matchedArgs[ 'level'  ]
    schema  = matchedArgs[ 'schema' ]
    output  = matchedArgs[ 'output' ]
    
    verbose = 'verbose' in matchedArgs
    
    LogHelper.setLevel( log, verbose )
    
    conschema = get_schema_connection( level )
    
    process( conschema, schema.upper(), open( output, 'w' ) )

if __name__ == "__main__":
    import sys
    sys.exit( main( sys.argv ) )

