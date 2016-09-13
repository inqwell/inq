#!/usr/local/bin/python
"""
Abstract class for schema setup (tables creation).

NOTE:
The table creation SQL statements should be tagged as shown in "oracle_schema_setup_example.sql".

$Header: /home/inqwell/cvsroot/dev/scripts/python/DBUtils/schema_setup.py,v 1.1 2009/05/22 22:16:54 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/04/14 15:00:46 $
$Change: 164219 $
"""

import re


class SchemaSetupAbstract(object):
    """
    Abstract class for schema setup (tables creation).
    """

    #----------------------------------------------------------------------------------------------
    # Public methods

    def __init__(self):
        """
        Constructor.
        """

        self._table_name_regexp = re.compile(self._get_table_name_regexp(), re.IGNORECASE)
        self._references_regexp = re.compile(self._get_references_regexp(), re.IGNORECASE)

    def create_tables(self, dbh, table_text_list, do_foreign_keys=True, test_mode=False):
        """
        Create the tables corresponding to the provided list of SQL table texts (one per table).
        If the tables already exist in the database, they are dropped before bewing re-created.
        If foreign keys are enabled, the tables are dropped/created in the correct dependency order.
        @param IN dbh             Database connection handle
        @param IN table_text_list List of SQL table texts
        @param IN do_foreign_keys Whether to create foreign key constraint
        @param IN test_mode       Whether to run in test mode, i.e. SQL statement not run
        """

        # Identify the tables the SQL texts correspond to
        text_by_table = {}
        for table_text in table_text_list:
            table_name = self._get_table_name(table_text)
            text_by_table[table_name] = table_text

        # Determine the order of table creation
        table_creation_order = []
        if do_foreign_keys:
            # Determine the dependencies
            dependencies_by_table = {}
            for table_name, table_text in text_by_table.iteritems():
                dependencies = self._get_other_table_references(table_name, table_text)
                # Only keep the dependencies on the input tables
                dependencies.intersection_update(text_by_table)
                dependencies_by_table[table_name] = dependencies
            # Order the tables by level of dependency
            tables_by_dependency_level = []
            prev_tables_with_no_deps = set()
            while dependencies_by_table:
                tables_with_no_deps = set()
                for table_name, dependencies in dependencies_by_table.iteritems():
                    dependencies.difference_update(prev_tables_with_no_deps)
                    if not dependencies:
                        tables_with_no_deps.add(table_name)
                if not tables_with_no_deps:
                    error_msg = "Circular foreign key dependencies:\n%s" % ("\n".join(
                        ["- %s: %s" % (table_name, ", ".join(dependencies))
                        for table_name, dependencies in dependencies_by_table.iteritems()]))
                    raise Exception(error_msg)
                for table_name in tables_with_no_deps:
                    del dependencies_by_table[table_name]
                tables_by_dependency_level.append(tables_with_no_deps)
                prev_tables_with_no_deps = tables_with_no_deps
            # Dependency order, plus alphabetical order for same dependency level
            for same_level_tables in tables_by_dependency_level:
                table_creation_order.extend(sorted(same_level_tables))
        else:
            # Alphabetical order
            table_creation_order = sorted(text_by_table)
            # Remove the foreign key constraints
            for table_name, table_text in text_by_table.iteritems():
                table_text_lines = table_text.splitlines()
                self._remove_foreign_keys_section(table_text_lines)
                new_table_text = "\n".join(table_text_lines)
                text_by_table[table_name] = new_table_text

        # Drop the tables in reverse order of creation
        for table_name in reversed(table_creation_order):
            self._drop_table(dbh, table_name, test_mode)

        # Create the tables
        for table_name in table_creation_order:
            sql = text_by_table[table_name]
            if test_mode:
                print sql
            else:
                try:
                    dbh.execute(sql)
                except:
                    print "The following SQL statement failed:\n%s" % sql
                    raise

    #----------------------------------------------------------------------------------------------
    # Protected methods

    def _get_table_name(self, table_text):
        """
        Get the name of the table the table creation string corresponds to.
        @param IN table_text Table creation string
        @return The table name (lower case)
        """

        match_obj = self._table_name_regexp.search(table_text)
        if match_obj is None:
            raise Exception("No table name found in:\n%s" % table_text)
        # table_name = match_obj.group(1).lower()
        table_name = match_obj.group(1)
        return table_name

    def _get_other_table_references(self, table_name, table_text):
        """
        Get the tables referenced by a table.
        @param IN table_name Table name (lower case)
        @param IN table_text Table creation string
        @return A set of table names (lower case)
        """

        refs = set()
        for match_obj in self._references_regexp.finditer(table_text):
            other_table_name = match_obj.group(1).lower()
            if other_table_name != table_name:
                refs.add(other_table_name)
        return refs

    def _find_section(self, table_text_lines, section_name):
        """
        Find a given section in a table creation string.
        @param IN table_text_lines Table creation string lines
        @param IN section_name     Name of the searched section
        @return If found, a pair of line numbers (start and end of the section); None otherwise
        """

        _section_start_regexp = re.compile("<%s>" % section_name, re.IGNORECASE)
        _section_end_regexp = re.compile("</%s>" % section_name, re.IGNORECASE)

        nb_lines = len(table_text_lines)
        # Section start line
        for line_num in xrange(0, nb_lines):
            line = table_text_lines[line_num]
            if _section_start_regexp.search(line):
                break
        else:
            return None
        line_num_start = line_num
        # Section end line
        for line_num in xrange(line_num_start + 1, nb_lines):
            line = table_text_lines[line_num]
            if _section_end_regexp.search(line):
                break
        line_num_end = line_num

        return line_num_start, line_num_end

    def _remove_foreign_keys_section(self, table_text_lines):
        """
        Remove the foreign keys section from a table creation string.
        @param IN table_text_lines Table creation string lines
        """

        foreign_key_section = self._find_section(table_text_lines, "foreign_keys")
        if foreign_key_section:
            section_start, section_end = foreign_key_section
            del table_text_lines[section_start:section_end + 1]

    def _drop_table(self, dbh, table_name, test_mode):
        """
        Drop a table if it exists.
        @param IN dbh        Database connection handle
        @param IN table_name Table name
        @param IN test_mode  Whether to run in test mode, i.e. SQL statement not run
        """

        sql = self._get_drop_table_sql(table_name)
        if test_mode:
            print sql
        else:
            try:
                dbh.execute(sql)
            except:
                print "The following SQL statement failed:\n%s" % sql
                raise

    #----------------------------------------------------------------------------------------------
    # Protected methods to override

    def _get_table_name_regexp(self):
        """
        Get the regular expression to identify the table name in a table creation string.
        THIS METHOD MUST BE OVERRIDDEN.
        @return Regular expression string
        """

        raise Exception("Abstract method; must be overridden")

    def _get_references_regexp(self):
        """
        Get the regular expression to identify the references to other tables in a table creation
        string.
        THIS METHOD MUST BE OVERRIDDEN.
        @return Regular expression string
        """

        raise Exception("Abstract method; must be overridden")

    def _get_drop_table_sql(self, table_name):
        """
        Get the SQL to drop a table if it exists.
        THIS METHOD MUST BE OVERRIDDEN.
        @param IN table_name Table name
        @return SQL statement as a string
        """

        raise Exception("Abstract method; must be overridden")

