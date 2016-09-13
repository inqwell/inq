#!/usr/local/bin/python
"""
Xylinq schema setup (tables creation).

$Header: /home/inqwell/cvsroot/dev/scripts/python/setup_oracle_XySchema.py,v 1.1 2009/05/22 22:15:54 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/14 17:17:10 $
$Change: 164250 $
"""

import KBC.fotech

import glob
import os

from DBUtils.oracle_schema_setup import OracleSchemaSetup
from Util import commandline
from XyUtil import XyDbHelper


def main(args):
    arg_spec = [
        ("level", "l", "level", True,  True,  False, "Database level to connect to"),
        ("dirs",  "d", "dirs",  True,  True,  False, "List of directories for the SQL files"),
        ("no_fk", "n", "no_fk", False, False, False, "Ignore foreign key constraints"),
        ("test",  "t", "test",  False, False, False, "Test mode"),
    ]
    parser = commandline.Parser(args[0], arg_spec)
    matched_args, dummy = parser.parse(args[1:])
    level = matched_args["level"]
    dir_list = [dir.strip() for dir in matched_args["dirs"].split(",")]
    do_foreign_keys = "no_fk" not in matched_args
    test_mode = "test" in matched_args

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
    dbh = XyDbHelper.get_schema_connection(level)
    setup = OracleSchemaSetup()
    setup.create_tables(dbh, table_text_list, do_foreign_keys=do_foreign_keys, test_mode=test_mode)


if __name__ == "__main__":
    import sys
    sys.exit(main(sys.argv))

