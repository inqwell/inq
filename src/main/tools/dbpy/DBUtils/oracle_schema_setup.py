#!/usr/local/bin/python
"""
Class for Oracle schema setup (tables creation).

$Header: /home/inqwell/cvsroot/dev/scripts/python/DBUtils/oracle_schema_setup.py,v 1.1 2009/05/22 22:16:52 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/14 15:00:46 $
$Change: 164219 $
"""

from schema_setup import SchemaSetupAbstract


class OracleSchemaSetup(SchemaSetupAbstract):
    """
    Class for Oracle schema setup (tables creation).
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

        return """DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_tables WHERE table_name = '%s';
    IF (n > 0) THEN
        EXECUTE IMMEDIATE 'DROP TABLE %s';
    END IF;
END;""" % (table_name.upper(), table_name)

