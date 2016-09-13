#!/usr/local/bin/python
"""
Xylinq schema setup for MySQL(tables creation).

$Header: /home/inqwell/cvsroot/dev/scripts/python/setup_mysql_XySchema.py,v 1.1 2009/05/22 22:15:53 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/22 14:59:21 $
$Change: 164836 $
"""

import glob
from optparse import OptionParser
import os

from DBUtils.mysql_schema_setup import MySqlSchemaSetup


def main(args):
    parser = OptionParser()
    parser.add_option("--dbserver", dest="db_server", help="Database server")
    parser.add_option("--db", dest="db", help="Database")
    parser.add_option("--dbuser", dest="db_user", help="Database user")
    parser.add_option("--dbpassword", dest="db_password", help="Database password")
    parser.add_option("--dirs", dest="dir_list", help="List of directories for the SQL files")
    parser.add_option("--no_fk", dest="do_foreign_keys", action="store_false", default=True,
        help="Ignore foreign key constraints")
    parser.add_option("--test", dest="test_mode", action="store_true", default=False,
        help="Test mode")
    options, dummy = parser.parse_args()
    test_mode = options.test_mode
    db_server = options.db_server
    if db_server is None and not test_mode:
        raise Exception("Missing mandatory 'dbserver' argument")
    db = options.db
    if db is None and not test_mode:
        raise Exception("Missing mandatory 'db' argument")
    db_user = options.db_user
    if db_user is None and not test_mode:
        raise Exception("Missing mandatory 'dbuser' argument")
    db_password = options.db_password
    if db_password is None and not test_mode:
        raise Exception("Missing mandatory 'dbpassword' argument")
    dir_list = options.dir_list
    if dir_list is None:
        raise Exception("Missing mandatory 'dirs' argument")
    dir_list = [dir_path.strip() for dir_path in dir_list.split(",")]
    do_foreign_keys = options.do_foreign_keys

    # Get the table creation SQL texts
    filenames = []
    for dir in dir_list:
        path = os.path.join(dir, "*.sql")
        filenames.extend(glob.glob(path))
    table_text_list = []
    for filename in filenames:
        fh = open(filename)
        try:
            table_text = fh.read()
        finally:
            fh.close()
        table_text_list.append(table_text)

    # Create the tables
    if not test_mode:
        import MySQLdb
        dbh = MySQLdb.connect(host=db_server, db=db, user=db_user, passwd=db_password)
        db_cursor = dbh.cursor()
    else:
        db_cursor = None
    setup = MySqlSchemaSetup()
    setup.create_tables(db_cursor, table_text_list, do_foreign_keys=do_foreign_keys,
        test_mode=test_mode)
    if not test_mode:
        db_cursor.close()
        dbh.commit()
        dbh.close()


if __name__ == "__main__":
    import sys
    sys.exit(main(sys.argv))

