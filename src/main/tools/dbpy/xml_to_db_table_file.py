#!/usr/local/bin/bash
"""
Abstract class to convert an XML database table file into a file containing corresponding DB
statements.

$Header: /home/inqwell/cvsroot/dev/scripts/python/xml_to_db_table_file.py,v 1.1 2009/05/22 22:15:55 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/14 16:57:31 $
$Change: 164242 $
"""

import codecs
import xml.etree.ElementTree as ET

from xml_to_db_utils import is_nullable


class XmlToDbTable(object):
    """
    Abstract class to convert an XML database table file into a file containing corresponding DB
    statements.
    """

    #-----------------------------------------------------------------------------------------------
    # Protected methods to override
    #-----------------------------------------------------------------------------------------------

    def _get_column_description_comment(self, column_elt):
        """
        Get a column's description as an end-of-line comment.
        THE XYLINQ COLUMN DESCRIPTION MUST BE SINGLE-LINE.
        @param IN column_elt Column XML element
        @return The description comment as a string
        """

        raise Exception("Must be overridden")

    def _get_column_format(self, max_name_size, max_type_size, has_non_nullable_columns):
        """
        Get the formatting for a column of a "create table" statement.
        @param IN max_name_size            The length of the longest column name
        @param IN max_type_size            The length of the longest column type
        @param IN has_non_nullable_columns Whether some columns are non-nullable
        @return A string with substitution placeholders for the column name, type and nullablility
        """

        raise Exception("Must be overridden")

    def _get_column_name(self, column_elt):
        """
        Get the database-compatible name of a column.
        @param IN column_elt Column XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    def _get_column_nullability_text(self, is_column_nullable):
        """
        Get the nullability string for a column.
        @param IN is_column_nullable Whether the column is nullable
        @return Nullability string
        """

        raise Exception("Must be overridden")

    def _get_column_type(self, column_elt):
        """
        Get the database type of a column.
        @param IN column_elt Column XML element
        @return The database type as a string
        """

        raise Exception("Must be overridden")

    def _get_foreign_key_column_target(self, column_elt):
        """
        Get the database-compatible name of the column targeted by foreign key column.
        @param IN column_elt Foreign key column XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    def _get_foreign_key_format(self, table_name):
        """
        Get the formatting for a "foreign key" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the foreign key name and columns, and the
            referenced table name and columns
        """

        raise Exception("Must be overridden")

    def _get_foreign_key_name(self, foreign_key_elt):
        """
        Get the database-compatible name of a foreign key.
        @param IN foreign_key_elt Foreign key XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    def _get_foreign_key_target_table(self, foreign_key_elt):
        """
        Get the database-compatible name of the table targeted by foreign key.
        @param IN foreign_key_elt Foreign key XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    def _get_index_format(self, table_name):
        """
        Get the formatting for a "create index" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the index name and columns
        """

        raise Exception("Must be overridden")

    def _get_index_name(self, index_elt):
        """
        Get the database-compatible name of an index.
        @param IN index_elt Index XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    def _get_primary_key_format(self, table_name):
        """
        Get the formatting for a "primary key" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the primary key name and columns
        """

        raise Exception("Must be overridden")

    def _get_primary_key_name(self, primary_key_elt):
        """
        Get the database-compatible name of a primary key.
        @param IN primary_key_elt Primary key XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    def _get_table_description_file_header(self, table_elt):
        """
        Get a table's description DB file header.
        @param IN table_elt Table XML element
        @return The description header as a string
        """

        raise Exception("Must be overridden")

    def _get_table_format(self, table_name):
        """
        Get the formatting for a "create table" statement.
        @param IN table_name Table name
        @return A string with a substitution placeholder for the columns
        """

        raise Exception("Must be overridden")

    def _get_table_name(self, table_elt):
        """
        Get the database-compatible name of a table.
        @param IN table_elt Table XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    def _get_unique_key_format(self, table_name):
        """
        Get the formatting for a "unique key" statement.
        @param IN table_name Table name
        @return A string with substitution placeholders for the unique key name and columns
        """

        raise Exception("Must be overridden")

    def _get_unique_key_name(self, unique_key_elt):
        """
        Get the database-compatible name of a unique key.
        @param IN unique_key_elt Unique key XML element
        @return The name as a string
        """

        raise Exception("Must be overridden")

    #-----------------------------------------------------------------------------------------------
    # Public methods
    #-----------------------------------------------------------------------------------------------

    def table_elt_to_db_string(self, table_elt, template):
        """
        Translate a table XML element into a set of DB statements arranged according to a template.
        @param IN table_elt Table XML element
        @param IN template  Template string
        @return DB statements as a string
        """

        substitution_dict = {}

        # Table name
        table_name = self._get_table_name(table_elt)

        # File header
        file_header = self._get_table_description_file_header(table_elt)
        substitution_dict["file_header"] = file_header

        # Table creation
        columns_elt = table_elt.find("columns")
        if columns_elt:
            create_table_format = self._get_table_format(table_name)
            # Identify the maximum width for the column names, types and nullability
            column_elt_list = [c for c in columns_elt.findall("column")]
            if not column_elt_list:
                raise Exception("No columns for the table")
            column_name_size = 0
            column_type_size = 0
            has_non_nullable_columns = False
            for column_elt in column_elt_list:
                col_name = self._get_column_name(column_elt)
                column_name_size = max(column_name_size, len(col_name))
                col_type = self._get_column_type(column_elt)
                column_type_size = max(column_type_size, len(col_type))
                if not is_nullable(column_elt):
                    has_non_nullable_columns = True
            # Build the column definition lines
            column_lines = []
            column_format = self._get_column_format(column_name_size, column_type_size,
                has_non_nullable_columns)
            nb_cols = len(column_elt_list)
            for col_num in xrange(0, nb_cols - 1):
                column_elt = column_elt_list[col_num]
                col_comment = self._get_column_description_comment(column_elt)
                column_line = column_format % (
                    self._get_column_name(column_elt),
                    self._get_column_type(column_elt),
                    self._get_column_nullability_text(is_nullable(column_elt)),
                    ",%s" % col_comment)
                column_lines.append(column_line)
            # - No column definition separator for the last column
            column_elt = column_elt_list[nb_cols - 1]
            col_comment = self._get_column_description_comment(column_elt)
            column_line = column_format % (
                self._get_column_name(column_elt),
                self._get_column_type(column_elt),
                self._get_column_nullability_text(is_nullable(column_elt)),
                col_comment and " %s" % col_comment or "")
            column_lines.append(column_line)
            substitution_dict["create_table"] = create_table_format % ("\n".join(column_lines))
        else:
            substitution_dict["create_table"] = ""

        # Primary key
        primary_key_elt = table_elt.find("primary_key")
        if primary_key_elt:
            primary_key_format = self._get_primary_key_format(table_name)
            primary_key_name = self._get_primary_key_name(primary_key_elt)
            column_elt_list = [c for c in primary_key_elt.findall("column")]
            if not column_elt_list:
                raise Exception("No columns for the primary key")
            column_name_list = [self._get_column_name(c) for c in column_elt_list]
            substitution_dict["primary_key"] = primary_key_format % (
                primary_key_name, ", ".join(column_name_list))
        else:
            substitution_dict["primary_key"] = ""

        # Unique keys
        unique_keys_elt = table_elt.find("unique_keys")
        if unique_keys_elt:
            unique_key_format = self._get_unique_key_format(table_name)
            unique_key_text_list = []
            unique_key_elt_list = [u for u in unique_keys_elt.findall("unique_key")]
            if not unique_key_elt_list:
                raise Exception("No unique key in the unique keys section")
            for unique_key_elt in unique_key_elt_list:
                unique_key_name = self._get_unique_key_name(unique_key_elt)
                column_elt_list = [c for c in unique_key_elt.findall("column")]
                if not column_elt_list:
                    raise Exception("No columns for unique key %s" % unique_key_name)
                column_name_list = [self._get_column_name(c) for c in column_elt_list]
                unique_key_text_list.append(unique_key_format % (
                    unique_key_name, ", ".join(column_name_list)))
            substitution_dict["unique_keys"] = "\n".join(unique_key_text_list)
        else:
            substitution_dict["unique_keys"] = ""

        # Foreign keys
        foreign_keys_elt = table_elt.find("foreign_keys")
        if foreign_keys_elt:
            foreign_key_format = self._get_foreign_key_format(table_name)
            foreign_key_text_list = []
            foreign_key_elt_list = [u for u in foreign_keys_elt.findall("foreign_key")]
            if not foreign_key_elt_list:
                raise Exception("No foreign key in the foreign keys section")
            for foreign_key_elt in foreign_key_elt_list:
                foreign_key_name = self._get_foreign_key_name(foreign_key_elt)
                foreign_key_target_table = self._get_foreign_key_target_table(foreign_key_elt)
                column_elt_list = [c for c in foreign_key_elt.findall("column")]
                if not column_elt_list:
                    raise Exception("No columns for foreign key %s" % foreign_key_name)
                column_name_list = [self._get_column_name(c) for c in column_elt_list]
                target_column_name_list = [self._get_foreign_key_column_target(c)
                    for c in column_elt_list]
                foreign_key_text_list.append(foreign_key_format % (
                    foreign_key_name,
                    ", ".join(column_name_list),
                    foreign_key_target_table,
                    ", ".join(target_column_name_list)))
            substitution_dict["foreign_keys"] = "\n".join(foreign_key_text_list)
        else:
            substitution_dict["foreign_keys"] = ""

        # Indexes
        indexes_elt = table_elt.find("indexes")
        if indexes_elt:
            index_format = self._get_index_format(table_name)
            index_text_list = []
            index_elt_list = [u for u in indexes_elt.findall("index")]
            if not index_elt_list:
                raise Exception("No index in the indexes section")
            for index_elt in index_elt_list:
                index_name = self._get_index_name(index_elt)
                column_elt_list = [c for c in index_elt.findall("column")]
                if not column_elt_list:
                    raise Exception("No columns for index %s" % index_name)
                column_name_list = [self._get_column_name(c) for c in column_elt_list]
                index_text_list.append(index_format % (index_name, ", ".join(column_name_list)))
            substitution_dict["indexes"] = "\n".join(index_text_list)
        else:
            substitution_dict["indexes"] = ""

        return template.substitute(substitution_dict)

    def table_xml_file_to_db_file(self, input_file, output_file, template):
        """
        Translate a table XML file into an DB statements file arranged according to a template.
        @param IN input_file  Table XML file
        @param IN output_file DB statements file
        @param IN template    Template string
        @return DB statements as a string
        """

        table_elt_tree = ET.parse(input_file)
        table_elt = table_elt_tree.getroot()
        if table_elt.tag != "table":
            raise Exception("The root of the XML tree in not a 'table' element")
        output_text = self.table_elt_to_db_string(table_elt, template)
        fh = codecs.open(output_file, encoding="utf-8", mode="w")
        try:
            fh.write(output_text)
        finally:
            fh.close()

