#!/usr/local/bin/bash
"""
Convert an XML database table file into a file containing corresponding Oracle statements.

$Header: /home/inqwell/cvsroot/dev/scripts/python/xml_to_oracle_table_file.py,v 1.1 2009/05/22 22:16:05 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/14 16:57:31 $
$Change: 164242 $
"""

from xml_to_db_table_file import XmlToDbTable
from xml_to_db_utils import get_xy_column_description
from xml_to_db_utils import get_xy_table_description
from xml_to_oracle_utils import get_db_column_name
from xml_to_oracle_utils import get_db_column_type
from xml_to_oracle_utils import get_db_foreign_key_column_target
from xml_to_oracle_utils import get_db_foreign_key_name
from xml_to_oracle_utils import get_db_foreign_key_target_table
from xml_to_oracle_utils import get_db_index_name
from xml_to_oracle_utils import get_db_primary_key_name
from xml_to_oracle_utils import get_db_table_name
from xml_to_oracle_utils import get_db_unique_key_name


class XmlToOracleTable(XmlToDbTable):
    """
    Class to convert an XML database table file into a file containing corresponding Oracle
    statements.
    """

    def _get_column_description_comment(self, column_elt):
        """
        Get a column's description as an end-of-line comment.
        THE XYLINQ COLUMN DESCRIPTION MUST BE SINGLE-LINE.
        @param IN column_elt Column XML element
        @return The description comment as a string
        """

        description = get_xy_column_description(column_elt)
        if not description:
            return ""
        description = description.strip()
        if not description:
            return ""
         # This is inserted into an EXECUTE IMMEDIATE string, so we need to escape single quotes
        description = description.replace("'", "''")
        return " -- %s" % description

    def _get_column_format(self, max_name_size, max_type_size, has_non_nullable_columns):
        """
        Get the formatting for a column of a "create table" statement.
        @param IN max_name_size            The length of the longest column name
        @param IN max_type_size            The length of the longest column type
        @param IN has_non_nullable_columns Whether some columns are non-nullable
        @return A string with substitution placeholders for the column name, type and nullablility
        """

        return """            %%-%ds %%-%ds %%%ds%%s""" % (
            max_name_size,
            max_type_size,
            has_non_nullable_columns and 8 or 4)

    def _get_column_nullability_text(self, is_column_nullable):
        """
        Get the nullability string for a column.
        @param IN is_column_nullable Whether the column is nullable
        @return Nullability string
        """

        return is_column_nullable and "NULL" or "NOT NULL"

    def _get_column_name(self, column_elt):
        """
        Get the database-compatible name of a column.
        @param IN column_elt Column XML element
        @return The name as a string
        """

        return get_db_column_name(column_elt)

    def _get_column_type(self, column_elt):
        """
        Get the database type of a column.
        @param IN column_elt Column XML element
        @return The database type as a string
        """

        return get_db_column_type(column_elt)

    def _get_foreign_key_column_target(self, column_elt):
        """
        Get the database-compatible name of the column targeted by foreign key column.
        @param IN column_elt Foreign key column XML element
        @return The name as a string
        """

        return get_db_foreign_key_column_target(column_elt)

    def _get_foreign_key_format(self, table_name):
        """
        Get the formatting for a "foreign key" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the foreign key name and columns, and
            the referenced table name and columns
        """

        return """    EXECUTE IMMEDIATE '
        ALTER TABLE %s
        ADD CONSTRAINT %%s
        FOREIGN KEY (%%s)
        REFERENCES %%s (%%s)
        INITIALLY DEFERRED
        ';""" % table_name

    def _get_foreign_key_name(self, foreign_key_elt):
        """
        Get the database-compatible name of a foreign key.
        @param IN foreign_key_elt Foreign key XML element
        @return The name as a string
        """

        return get_db_foreign_key_name(foreign_key_elt)

    def _get_foreign_key_target_table(self, foreign_key_elt):
        """
        Get the database-compatible name of the table targeted by foreign key.
        @param IN foreign_key_elt Foreign key XML element
        @return The name as a string
        """

        return get_db_foreign_key_target_table(foreign_key_elt)

    def _get_index_format(self, table_name):
        """
        Get the formatting for a "create index" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the index name and columns
        """

        return """    EXECUTE IMMEDIATE '
        CREATE INDEX %%s ON %s (%%s)
        ';""" % table_name

    def _get_index_name(self, index_elt):
        """
        Get the database-compatible name of an index.
        @param IN index_elt Index XML element
        @return The name as a string
        """

        return get_db_index_name(index_elt)

    def _get_primary_key_format(self, table_name):
        """
        Get the formatting for a "primary key" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the primary key name and columns
        """

        return """    EXECUTE IMMEDIATE '
        ALTER TABLE %s
        ADD CONSTRAINT %%s
        PRIMARY KEY (%%s)
        ';""" % table_name

    def _get_primary_key_name(self, primary_key_elt):
        """
        Get the database-compatible name of a primary key.
        @param IN primary_key_elt Primary key XML element
        @return The name as a string
        """

        return get_db_primary_key_name(primary_key_elt)

    def _get_table_description_file_header(self, table_elt):
        """
        Get a table's description DB file header.
        @param IN table_elt Table XML element
        @return The description header as a string
        """

        description = get_xy_table_description(table_elt)
        description = description.strip()
        if description:
            description_header = "/*\n%s\n */" % ("\n".join([" * %s" % l.strip()
                for l in description.splitlines()]))
        else:
            description_header = ""
        return description_header

    def _get_table_format(self, table_name):
        """
        Get the formatting for a "create table" statement.
        @param IN table_name Table name
        @return A string with a substitution placeholder for the columns
        """

        return """    EXECUTE IMMEDIATE '
        CREATE TABLE %s
        (
%%s
        )
        ';""" % table_name

    def _get_table_name(self, table_elt):
        """
        Get the database-compatible name of a table.
        @param IN table_elt Table XML element
        @return The name as a string
        """

        return get_db_table_name(table_elt)

    def _get_unique_key_format(self, table_name):
        """
        Get the formatting for a "unique key" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the unique key name and columns
        """

        return """    EXECUTE IMMEDIATE '
        ALTER TABLE %s
        ADD CONSTRAINT %%s
        UNIQUE (%%s)
        ';""" % table_name

    def _get_unique_key_name(self, unique_key_elt):
        """
        Get the database-compatible name of a unique key.
        @param IN unique_key_elt Unique key XML element
        @return The name as a string
        """

        return get_db_unique_key_name(unique_key_elt)


def main():
    from optparse import OptionParser
    import string

    parser = OptionParser()
    parser.add_option("--in", dest="input_file", help="Input XML file")
    parser.add_option("--in_dir", dest="input_dir", help="Input directory for XML files")
    parser.add_option("--out", dest="output_file", help="Output Oracle file")
    parser.add_option("--out_dir", dest="output_dir", help="Output directory for Oracle files")
    parser.add_option("--template", dest="template_file", help="Template file for the Oracle files")
    options, dummy = parser.parse_args()

    fh = open(options.template_file)
    try:
        template = string.Template(fh.read())
    finally:
        fh.close()

    converter = XmlToOracleTable()
    if options.input_file is not None:
        converter.table_xml_file_to_db_file(options.input_file, options.output_file, template)
    else:
        import glob
        import os
        input_files = glob.glob(os.path.join(options.input_dir, "*.xml"))
        for input_file in input_files:
            try:
                print "Processing %s" % input_file
                output_file = os.path.join(options.output_dir,
                    "%s.sql" % os.path.splitext(os.path.basename(input_file))[0])
                converter.table_xml_file_to_db_file(input_file, output_file, template)
            except Exception, err:
                print "!!! FAILED: %s !!!" % err


if __name__ == "__main__":
    main()

