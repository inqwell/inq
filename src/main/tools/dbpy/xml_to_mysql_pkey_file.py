#!/usr/local/bin/bash
"""
Convert an XML database table file into a file containing corresponding MySQL statements.

$Header: /home/inqwell/cvsroot/dev/scripts/python/xml_to_mysql_pkey_file.py,v 1.1 2009/05/22 22:16:00 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/14 17:09:25 $
$Change: 164248 $
"""

import codecs
import xml.etree.ElementTree as ET

from xml_to_db_utils import get_xy_column_name
from xml_to_mysql_utils import get_db_column_name
from xml_to_mysql_utils import get_db_table_name


#--------------------------------------------------------------------------------------------------
# Helper class and functions
#--------------------------------------------------------------------------------------------------


class _TableInfo(object):
    """
    Objects encapsulating the table information.
    Used to make it easier to pass datat to sub-functions.
    """

    pass


def _build_select_stmt_section(table_info):
    """
    Build the "select-stmt" section of the pkey file.
    @param IN table_info Table information
    @return The section as a string
    """

    result_lines = []
    text = """    "select-stmt",
        "
        SELECT"""
    result_lines.append(text)
    line_format = """            XY.%%-%ds AS \\"%%s\\\"""" % table_info.all_db_col_name_max_size
    lines = []
    for db_col_name, xy_col_name in zip(table_info.all_db_col_names, table_info.all_xy_col_names):
        lines.append(line_format % (db_col_name, xy_col_name))
    result_lines.append(",\n".join(lines))
    text = """        FROM %s XY
        \"""" % table_info.table_name
    result_lines.append(text)
    return "\n".join(result_lines)


def _build_read_sql_section(table_info):
    """
    Build the "read-sql" section of the pkey file.
    @param IN table_info Table information
    @return The section as a string
    """

    result_lines = []
    text = """    "read-sql",
        "
        {select-stmt}
        WHERE """
    line_format = """%%-%ds = ?""" % table_info.pk_db_col_name_max_size
    lines = []
    for db_col_name in table_info.pk_db_col_names:
        lines.append(line_format % db_col_name)
    result_lines.append("%s%s" % (text, "\n        AND   ".join(lines)))
    text = """        \""""
    result_lines.append(text)
    return "\n".join(result_lines)


def _build_write_sql_section(table_info):
    """
    Build the "write-sql" section of the pkey file.
    @param IN table_info Table information
    @return The section as a string
    """

    result_lines = []
    text = """    "write-sql",
        "
        REPLACE %s
        SET""" % table_info.table_name
    result_lines.append(text)
    line_format = """            %%-%ds = ?""" % table_info.all_db_col_name_max_size
    lines = []
    for db_col_name in table_info.all_db_col_names:
        lines.append(line_format % db_col_name)
    result_lines.append(",\n".join(lines))
    text = """        \""""
    result_lines.append(text)
    return "\n".join(result_lines)


def _build_delete_sql_section(table_info):
    """
    Build the "delete-sql" section of the pkey file.
    @param IN table_info Table information
    @return The section as a string
    """

    result_lines = []
    text = """    "delete-sql",
        "
        DELETE FROM %s
        WHERE """ % table_info.table_name
    line_format = """%%-%ds = ?""" % table_info.pk_db_col_name_max_size
    lines = []
    for db_col_name in table_info.pk_db_col_names:
        lines.append(line_format % db_col_name)
    result_lines.append("%s%s" % (text, "\n        AND   ".join(lines)))
    text = """        \""""
    result_lines.append(text)
    return "\n".join(result_lines)


#--------------------------------------------------------------------------------------------------
# Main functionality
#--------------------------------------------------------------------------------------------------


def table_elt_to_pkey_text(table_elt):
    """
    Translate a table XML element into a pkey text for Xylinq.
    @param IN table_elt Table XML element
    @return Pkey text
    """

    # Get the necessary data
    table_info = _TableInfo()
    # - Table name
    table_info.table_name = get_db_table_name(table_elt)
    # - Columns
    columns_elt = table_elt.find("columns")
    if not columns_elt:
        raise Exception("No columns section for table %s" % table_info.table_name)
    column_elt_list = [c for c in columns_elt.findall("column")]
    if not column_elt_list:
        raise Exception("No columns for table %s" % table_info.table_name)
    table_info.all_xy_col_names = [get_xy_column_name(c) for c in column_elt_list]
    table_info.all_db_col_names = [get_db_column_name(c) for c in column_elt_list]
    table_info.all_db_col_name_max_size = max([len(db_col_name) for db_col_name in
        table_info.all_db_col_names])
    # - Primary key columns
    primary_key_elt = table_elt.find("primary_key")
    if not primary_key_elt:
        raise Exception("No primary key defined for table %s" % table_info.table_name)
    column_elt_list = [c for c in primary_key_elt.findall("column")]
    if not column_elt_list:
        raise Exception("No columns for the primary key of table %s" % table_info.table_name)
    table_info.pk_db_col_names = [get_db_column_name(c) for c in column_elt_list]
    table_info.pk_db_col_name_max_size = max([len(db_col_name) for db_col_name in
        table_info.pk_db_col_names])

    # Build the pkey file text
    result_lines = []
    # - File header
    text = """Xylinq pkey file for %s.""" % table_info.table_name
    file_header = "/*\n%s\n */" % ("\n".join([" * %s" % l.strip() for l in text.splitlines()]))
    result_lines.append(file_header)
    # - Start
    text = """
auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP TO THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,"""
    result_lines.append(text)
    # - select-stmt
    text = _build_select_stmt_section(table_info)
    result_lines.append("%s," % text)
    # - read-sql
    text = _build_read_sql_section(table_info)
    result_lines.append("%s," % text)
    # - write-sql
    text = _build_write_sql_section(table_info)
    result_lines.append("%s," % text)
    # - delete-sql
    text = _build_delete_sql_section(table_info)
    result_lines.append(text)
    # - End
    text = """))"""
    result_lines.append(text)

    result_text = "\n".join(result_lines)
    return result_text


def table_xml_file_to_pkey_file(input_file, output_file):
    """
    Translate a table XML file into a pkey file for Xylinq.
    @param IN input_file  Table XML file
    @param IN output_file Pkey file
    """

    table_elt_tree = ET.parse(input_file)
    table_elt = table_elt_tree.getroot()
    if table_elt.tag != "table":
        raise Exception("The root of the XML tree in not a 'table' element")
    output_text = table_elt_to_pkey_text(table_elt)
    fh = codecs.open(output_file, encoding="utf-8", mode="w")
    try:
        fh.write(output_text)
    finally:
        fh.close()


def main():
    from optparse import OptionParser

    parser = OptionParser()
    parser.add_option("--in", dest="input_file", help="Input XML file")
    parser.add_option("--in_dir", dest="input_dir", help="Input directory for XML files")
    parser.add_option("--out", dest="output_file", help="Output pkey file")
    parser.add_option("--out_dir", dest="output_dir", help="Output directory for pkey files")
    options, dummy = parser.parse_args()

    if options.input_file is not None:
        table_xml_file_to_pkey_file(options.input_file, options.output_file)
    else:
        import glob
        import os
        input_files = glob.glob(os.path.join(options.input_dir, "*.xml"))
        for input_file in input_files:
            try:
                print "Processing %s" % input_file
                output_file = os.path.join(options.output_dir,
                    "%s.pkey.sql" % os.path.splitext(os.path.basename(input_file))[0])
                table_xml_file_to_pkey_file(input_file, output_file)
            except Exception, err:
                print "!!! FAILED: %s !!!" % err


if __name__ == "__main__":
    main()

