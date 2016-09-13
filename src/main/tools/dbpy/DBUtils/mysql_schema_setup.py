#!/usr/local/bin/python
"""
Class for MySQL schema setup (tables creation).

$Header: /home/inqwell/cvsroot/dev/scripts/python/DBUtils/mysql_schema_setup.py,v 1.1 2009/05/22 22:16:50 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/14 15:00:46 $
$Change: 164219 $
"""

from schema_setup import SchemaSetupAbstract


class MySqlSchemaSetup(SchemaSetupAbstract):
    """
    Class for MySQL schema setup (tables creation).
    """

    #----------------------------------------------------------------------------------------------
    # Overriden protected methods

    def _get_table_name_regexp(self):
        """
        Get the regular expression to identify the table name in a table creation string.
        @return Regular expression string
        """

        return "create\s+table\s+(\w+)"

    def _get_references_regexp(self):
        """
        Get the regular expression to identify the references to other tables in a table creation
        string.
        @return Regular expression string
        """

        return "references\s+(\w+)"

    def _get_drop_table_sql(self, table_name):
        """
        Get the SQL to drop a table if it exists.
        @param IN table_name Table name
        @return SQL statement as a string
        """

        return """DROP TABLE IF EXISTS %s;""" % table_name

