#!/usr/local/bin/bash
"""
Two options:
1) Build DB-specific data files from meta-data files
2) Build a single file containing all the DB-specific 'insert' statements in the correct dependency
   order from meta-data files and XML table files

NOTE:
- The data files must be named "xxx.dat"; for option (2) the corresponding XML table file must be
  "xxx.sql"
- For option (2), the data must be tab-separated

$Header: /home/inqwell/cvsroot/dev/scripts/python/meta_to_db_data.py,v 1.1 2009/05/22 22:15:44 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/05/01 17:04:46 $
$Change: 165582 $
"""

import xml.etree.ElementTree as ET

from xml_to_db_utils import get_table_info
from xml_to_db_utils import get_table_creation_order
import xml_to_mysql_utils
import xml_to_oracle_utils


# Mapping from DB type to function taking a Xylinq name and returning its DB-compatible name
_name_func_by_db_type = {
    "mysql" : xml_to_mysql_utils.get_db_compatible_name,
    "oracle": xml_to_oracle_utils.get_db_compatible_name,
}


# Mapping from DB type to meta-data converter class
_meta_data_converter_cls_by_db_type = {
    "mysql" : xml_to_mysql_utils.MetaDataConverter,
    "oracle": xml_to_oracle_utils.MetaDataConverter,
}


def meta_data_text_to_db_data_text(meta_data_text, db_type):
    """
    Convert a meta-data text into a DB-specific data text.
    @param IN meta_data_text Meta-data text
    @param IN db_type        DB type (MySQL, Oracle, ...)
    @return A DB-specific data text
    """

    # Get the DB-specific meta-data converter class
    try:
        meta_data_converter = _meta_data_converter_cls_by_db_type[db_type.lower()]()
    except KeyError:
        raise Exception("DB type not supported: '%s'" % db_type)

    # Convert the meta-data in the data text
    db_data_text = meta_data_converter.meta_to_db_text(meta_data_text)

    return db_data_text


def meta_data_to_db_insert_text(info_and_data_list, db_type, db_statement_sep=None):
    """
    Convert a list of meta-data texts (along with table info objects) into a text containing insert
    statement for a given database.
    @param IN info_and_data_list List of TableInfo object and meta-data text pairs
    @param IN db_type            DB type (MySQL, Oracle, ...)
    @param IN db_statement_sep   Separator to use for the insert statements; default: ";"
    @return The insert statements as a string
    """

    if db_statement_sep is None:
        db_statement_sep = ";"

    # Get the DB-specific functions/classes
    try:
        xy_to_db_name_func = _name_func_by_db_type[db_type.lower()]
        meta_data_converter = _meta_data_converter_cls_by_db_type[db_type.lower()]()
    except KeyError:
        raise Exception("DB type not supported: '%s'" % db_type)
    
    # Identify the order of insertion
    info_and_data_by_table_name = dict([(item[0].name, item) for item in info_and_data_list])
    table_info_list = [item[0] for item in info_and_data_list]
    table_order = get_table_creation_order(table_info_list)

    # Process each table in the insertion order
    output_lines = []
    for table_name in table_order:
        table_info, meta_data_text = info_and_data_by_table_name[table_name]

        # Convert the meta-data in the data text
        db_data_text = meta_data_converter.meta_to_db_text(meta_data_text)

        # Get the DB table and column names
        db_table_name = xy_to_db_name_func(table_name)
        db_col_names = [xy_to_db_name_func(col_info.name) for col_info in table_info.columns]
        db_col_list_str = ", ".join(db_col_names)
        nb_col_names = len(db_col_names)

        # Process the data rows
        rows = db_data_text.splitlines()
        for row in rows:
            row = row.strip()
            if not row or row.startswith("//"):
                continue
            values = row.split("\t")
            if len(values) != nb_col_names:
                raise Exception("Incorrect number of values (%d expected):\n%s" % (nb_col_names,
                    values))
            insert_statement = "INSERT INTO %s (%s) VALUES (%s)%s" % (
                db_table_name,
                db_col_list_str,
                ", ".join(values),
                db_statement_sep)
            output_lines.append(insert_statement)

    return "\n".join(output_lines)


def main():
    import glob
    from optparse import OptionParser
    import os

    parser = OptionParser()
    parser.add_option("--mode", dest="mode", help="'data_files' or 'insert_file'")
    parser.add_option("--meta_data_dir", dest="meta_data_dir", help="Input directory for meta-data "
        "files")
    parser.add_option("--xml_dirs", dest="xml_dirs", help="Input directories for XML table files; "
        "'insert_file' mode only")
    parser.add_option("--out_dir", dest="output_dir", help="Output dir for data files; 'data_files'"
        " mode only")
    parser.add_option("--out", dest="output_file", help="Output file for insert statements; "
        "'insert_file' mode only")
    parser.add_option("--db", dest="db_type", help="DB type: MySQL, Oracle, ...")
    parser.add_option("--sep", dest="db_statement_sep", help="Separator for the insert statements; "
        "'insert_file' mode only")
    options, dummy = parser.parse_args()

    mode = options.mode
    if mode is None:
        raise Exception("Missing mandatory argument '--mode'")
    meta_data_dir = options.meta_data_dir
    if meta_data_dir is None:
        raise Exception("Missing mandatory argument '--meta_data_dir'")
    db_type = options.db_type
    if db_type is None:
        raise Exception("Missing mandatory argument '--db'")

    if mode == "data_files":
        output_dir = options.output_dir
        if output_dir is None:
            raise Exception("Missing mandatory argument '--out_dir'")

        meta_data_files = glob.glob(os.path.join(meta_data_dir, "*.dat"))
        for meta_data_file in meta_data_files:
            print "Processing meta-data file %s" % meta_data_file

            # Read the data file
            fh = open(meta_data_file)
            try:
                meta_data_text = fh.read()
            finally:
                fh.close()

            # Convert the meta-data into DB-specific data
            db_data_text = meta_data_text_to_db_data_text(meta_data_text, db_type)

            # Build the DB-specific data file
            db_data_file = os.path.join(output_dir, os.path.basename(meta_data_file))
            fh = open(db_data_file, "w")
            try:
                fh.write(db_data_text)
            finally:
                fh.close()

    elif mode == "insert_file":
        xml_dir_list = options.xml_dirs
        if xml_dir_list is None:
            raise Exception("Missing mandatory argument '--xml_dirs'")
        xml_dir_list = [item.strip() for item in xml_dir_list.split(",")]
        output_file = options.output_file
        if output_file is None:
            raise Exception("Missing mandatory argument '--out'")
        db_statement_sep = options.db_statement_sep
        if db_statement_sep:
            db_statement_sep = db_statement_sep.replace("\\n", "\n")

        info_and_data_list = []
        meta_data_files = glob.glob(os.path.join(meta_data_dir, "*.dat"))
        for meta_data_file in meta_data_files:
            # Read the corresponding XML table file
            for xml_dir in xml_dir_list:
                xml_file = os.path.join(xml_dir, "%s.xml" %
                    os.path.splitext(os.path.basename(meta_data_file))[0])
                if os.path.exists(xml_file):
                    break
            else:
                raise Exception("No XML table file found for meta-data file %s" % meta_data_file)
            table_elt_tree = ET.parse(xml_file)
            table_elt = table_elt_tree.getroot()
            table_info = get_table_info(table_elt)
            # Read the data file
            fh = open(meta_data_file)
            try:
                meta_data_text = fh.read()
            finally:
                fh.close()
            info_and_data_list.append((table_info, meta_data_text))

        output_text = meta_data_to_db_insert_text(info_and_data_list, db_type, db_statement_sep)
        fh = open(output_file, mode="w")
        try:
            fh.write(output_text)
        finally:
            fh.close()

    else:
        raise Exception("Unknown mode: '%s'" % mode)


if __name__ == "__main__":
    main()

