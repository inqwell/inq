#!/usr/local/bin/bash
"""
Utility functions for conversion from an XML table file to a database file.
The functions here are not database-specific.

$Header: /home/inqwell/cvsroot/dev/scripts/python/xml_to_db_utils.py,v 1.1 2009/05/22 22:15:57 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/05/01 17:04:46 $
$Change: 165582 $
"""

import re


#--------------------------------------------------------------------------------------------------
# Helper functions
#--------------------------------------------------------------------------------------------------


def _get_xy_name(elt, elt_type, attr_name="name"):
    """
    Get the Xylinq name of an XML element.
    @param IN elt       XML element
    @param IN elt_type  XML element type
    @param IN attr_name XML attribute name; 'name' by default
    @return The name as a string
    """

    name = elt.get(attr_name)
    if not name:
        raise Exception("Missing %s '%s' attribute" % (elt_type, attr_name))
    return name


#--------------------------------------------------------------------------------------------------
# The following functions should really be local helper functions
# Some are currently used by other modules, and their use should be replaced by the use of TableInfo
# and the related functions
#--------------------------------------------------------------------------------------------------


def get_xy_column_description(column_elt):
    """
    Get a column's description.
    IT MUST BE SINGLE-LINE.
    @param IN column_elt Column XML element
    @return The description as a string
    """

    description = column_elt.text
    if not description:
        return ""
    description = description.strip()
    if "\n" in description:
        raise Exception("Multi-line column descriptions are not allowed")
    return description


def get_xy_column_name(column_elt):
    """
    Get the Xylinq name of a column.
    @param IN column_elt Column XML element
    @return The name as a string
    """

    return _get_xy_name(column_elt, "column")


def get_xy_column_type(column_elt):
    """
    Get the Xylinq type of a column.
    @param IN column_elt Column XML element
    @return The type as a string
    """

    return _get_xy_name(column_elt, "column", attr_name="type")


def get_xy_column_type_precision(column_elt):
    """
    Get the Xylinq type precision of a column.
    @param IN column_elt Column XML element
    @return The type precision as a (potentially empty) list
    """

    precision_list = []
    precision_level = 1
    precision = column_elt.get("type_precision_%d" % precision_level)
    while precision is not None:
        precision_list.append(precision)
        precision_level += 1
        precision = column_elt.get("type_precision_%d" % precision_level)
    return precision_list


def get_xy_foreign_key_column_name(foreign_key_column_elt):
    """
    Get the Xylinq name of a foreign key column.
    @param IN foreign_key_column_elt Foreign key column XML element
    @return The name as a string
    """

    return _get_xy_name(foreign_key_column_elt, "foreign key column")


def get_xy_foreign_key_name(foreign_key_elt):
    """
    Get the Xylinq name of a foreign key.
    @param IN foreign_key_elt Foreign key XML element
    @return The name as a string
    """

    return _get_xy_name(foreign_key_elt, "foreign key")


def get_xy_foreign_key_target_column_name(foreign_key_column_elt):
    """
    Get the Xylinq name of a foreign key target column.
    @param IN foreign_key_column_elt Foreign key column XML element
    @return The name as a string
    """

    return _get_xy_name(foreign_key_column_elt, "foreign key column", attr_name="to")


def get_xy_foreign_key_target_table_name(foreign_key_elt):
    """
    Get the Xylinq name of the table targeted by foreign key.
    @param IN foreign_key_elt Foreign key XML element
    @return The name as a string
    """

    return _get_xy_name(foreign_key_elt, "foreign key", attr_name="to")


def get_xy_index_column_name(index_column_elt):
    """
    Get the Xylinq name of an index column.
    @param IN index_column_elt Index column XML element
    @return The name as a string
    """

    return _get_xy_name(index_column_elt, "index column")


def get_xy_index_name(index_elt):
    """
    Get the Xylinq name of an index.
    @param IN index_elt Index XML element
    @return The name as a string
    """

    return _get_xy_name(index_elt, "index")

def get_xy_primary_key_column_name(primary_key_column_elt):
    """
    Get the Xylinq name of a primary key column.
    @param IN primary_key_column_elt Primary key column XML element
    @return The name as a string
    """

    return _get_xy_name(primary_key_column_elt, "primary key column")


def get_xy_primary_key_name(primary_key_elt):
    """
    Get the Xylinq name of a primary key.
    @param IN primary_key_elt Primary key XML element
    @return The name as a string
    """

    return _get_xy_name(primary_key_elt, "primary key")


def get_xy_table_description(table_elt):
    """
    Get a table's description.
    @param IN table_elt Table XML element
    @return The description as a string
    """

    description = table_elt.text
    return description or ""


def get_xy_table_name(table_elt):
    """
    Get the Xylinq name of a table.
    @param IN table_elt Table XML element
    @return The name as a string
    """

    return _get_xy_name(table_elt, "table")


def get_xy_unique_key_column_name(unique_key_column_elt):
    """
    Get the Xylinq name of a unique key column.
    @param IN unique_key_column_elt Unique key column XML element
    @return The name as a string
    """

    return _get_xy_name(unique_key_column_elt, "unique key column")


def get_xy_unique_key_name(unique_key_elt):
    """
    Get the Xylinq name of a unique key.
    @param IN unique_key_elt Unique key XML element
    @return The name as a string
    """

    return _get_xy_name(unique_key_elt, "unique key")


def is_nullable(column_elt):
    """
    Identify whether a column is nullable.
    @param IN column_elt Column XML element
    @return True if it's nullable, False otherwise
    """

    col_nullable = column_elt.get("nullable")
    if not col_nullable:
        raise Exception("Missing column 'nullable' attribute")
    return col_nullable.lower() == "true"


#--------------------------------------------------------------------------------------------------
# Main functionality
#--------------------------------------------------------------------------------------------------


#------------------
# Table information
#------------------


class ColumnInfo(object):
    """
    Objects encapsulating column information.
    """

    def __init__(self):
        self.name = None
        self.type = None
        self.type_precision = None
        self.nullable = None


class ForeignKeyInfo(object):
    """
    Objects encapsulating foreign key information.
    """

    def __init__(self):
        self.name = None
        self.target_table_name = None
        self.column_names = None
        self.target_column_names = None


class IndexInfo(object):
    """
    Objects encapsulating index information.
    """

    def __init__(self):
        self.name = None
        self.column_names = None


class PrimaryKeyInfo(object):
    """
    Objects encapsulating primary key information.
    """

    def __init__(self):
        self.name = None
        self.column_names = None


class TableInfo(object):
    """
    Objects encapsulating table information.
    """

    def __init__(self):
        self.name = None
        self.columns = None
        self.primary_key = None
        self.unique_keys = None
        self.foreign_keys = None
        self.indexes = None


class UniqueKeyInfo(object):
    """
    Objects encapsulating unique key information.
    """

    def __init__(self):
        self.name = None
        self.column_names = None


def get_table_info(table_elt):
    """
    Get information about a table.
    @param IN table_elt Table XML element
    @return A TableInfo object
    """

    table_info = TableInfo()

    # Table name
    table_info.name = get_xy_table_name(table_elt)

    # Columns
    column_info_list = []
    columns_elt = table_elt.find("columns")
    if not columns_elt:
        raise Exception("No columns section for table %s" % table_info.name)
    column_elt_list = [c for c in columns_elt.findall("column")]
    if not column_elt_list:
        raise Exception("No columns for table %s" % table_info.name)
    for column_elt in column_elt_list:
        column_info = ColumnInfo()
        # - Column name
        column_info.name = get_xy_column_name(column_elt)
        # - Column type and type precision
        column_info.type = get_xy_column_type(column_elt)
        column_info.type_precision = get_xy_column_type_precision(column_elt)
        # - Column nullability
        column_info.nullable = is_nullable(column_elt)
        column_info_list.append(column_info)
    table_info.columns = column_info_list

    # Primary key
    primary_key_info = PrimaryKeyInfo()
    primary_key_elt = table_elt.find("primary_key")
    if not primary_key_elt:
        raise Exception("No primary key defined for table %s" % table_info.name)
    # - Primary key name
    primary_key_info.name = get_xy_primary_key_name(primary_key_elt)
    # - Primary key column names
    column_elt_list = [c for c in primary_key_elt.findall("column")]
    if not column_elt_list:
        raise Exception("No columns for the primary key of table %s" % table_info.name)
    primary_key_info.column_names = [get_xy_primary_key_column_name(c) for c in column_elt_list]
    table_info.primary_key = primary_key_info

    # Unique keys
    unique_key_info_list = []
    unique_keys_elt = table_elt.find("unique_keys")
    if unique_keys_elt:
        unique_key_elt_list = [u for u in unique_keys_elt.findall("unique_key")]
        for unique_key_elt in unique_key_elt_list:
            unique_key_info = UniqueKeyInfo()
            # - Unique key name
            unique_key_info.name = get_xy_unique_key_name(unique_key_elt)
            # - Unique key column names
            column_elt_list = [c for c in unique_key_elt.findall("column")]
            if not column_elt_list:
                raise Exception("No columns for unique key %s of table %s" % (unique_key_info.name,
                    table_info.name))
            unique_key_info.column_names = [get_xy_unique_key_column_name(c) for c in
                column_elt_list]
            unique_key_info_list.append(unique_key_info)
    table_info.unique_keys = unique_key_info_list

    # Foreign keys
    foreign_key_info_list = []
    foreign_keys_elt = table_elt.find("foreign_keys")
    if foreign_keys_elt:
        foreign_key_elt_list = [f for f in foreign_keys_elt.findall("foreign_key")]
        for foreign_key_elt in foreign_key_elt_list:
            foreign_key_info = ForeignKeyInfo()
            # - Foreign key name
            foreign_key_info.name = get_xy_foreign_key_name(foreign_key_elt)
            # - Foreign key target table name
            foreign_key_info.target_table_name = get_xy_foreign_key_target_table_name(
                foreign_key_elt)
            # - Foreign key column names and target column names
            column_elt_list = [c for c in foreign_key_elt.findall("column")]
            if not column_elt_list:
                raise Exception("No columns for foreign key %s of table %s" % (
                    foreign_key_info.name, table_info.name))
            foreign_key_info.column_names = [get_xy_foreign_key_column_name(c) for c in
                column_elt_list]
            foreign_key_info.target_column_names = [get_xy_foreign_key_target_column_name(c) for c
                in column_elt_list]
            foreign_key_info_list.append(foreign_key_info)
    table_info.foreign_keys = foreign_key_info_list

    # Indexes
    index_info_list = []
    indexes_elt = table_elt.find("indexes")
    if indexes_elt:
        index_elt_list = [i for i in indexes_elt.findall("index")]
        for index_elt in index_elt_list:
            index_info = IndexInfo()
            # - Index name
            index_info.name = get_xy_index_name(index_elt)
            # - Index column names
            column_elt_list = [c for c in index_elt.findall("column")]
            if not column_elt_list:
                raise Exception("No columns for index %s of table %s" % (index_info.name,
                    table_info.name))
            index_info.column_names = [get_xy_index_column_name(c) for c in column_elt_list]
            index_info_list.append(index_info)
    table_info.indexes = index_info_list

    return table_info


def get_table_creation_order(table_info_list):
    """
    Get the order in which tables need to be created.
    @param IN table_info_list List of table information objects
    @return List of table names in the order of creation
    """

    table_creation_order = []

    # Get the names of all the involved tables
    all_table_names = [table_info.name for table_info in table_info_list]

    # Determine the dependencies
    dependencies_by_table = {}
    for table_info in table_info_list:
        table_name = table_info.name
        dependencies = set([fk_info.target_table_name for fk_info in table_info.foreign_keys
            if fk_info.target_table_name != table_name])
        # Only keep the dependencies on the input tables
        dependencies.intersection_update(all_table_names)
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

    return table_creation_order


#---------------------
# Meta-data conversion
#---------------------


class MetaDataConverterAbstract(object):
    """
    Base class for converting meta-data into DB-specific data.
    """

    def _get_function_name_and_args(self, str_to_split):
        """
        Split a string of into a meta-function name and list of arguments.
        @param IN str_to_split String to split
        @return Function name and list of arguments, as a pair
        """

        parts = [s.strip() for s in str_to_split.split(" | ")]
        func_name = parts[0]
        func_args = parts[1:]
        return func_name, func_args

    def _convert_meta_function(self, func_name, func_args):
        """
        Convert a meta-function call into a DB-specific function call.
        THIS METHOD MUST BE OVERRIDDEN.
        @param IN func_name Meta-function name
        @param IN func_args Function arguments
        @return A string representing the DB-specific function call
        """

        raise Exception("Must be overridden")

    def meta_to_db_text(self, text):
        """
        Convert the meta-data in a text into the DB-specific equivalent.
        @param IN text Text with meta-data
        @return DB-specific text
        """

        # Convert the meta-functions into DB-specific functions
        regexp = re.compile("\${([^}]*)}") # inside-most "${...}"
        repl_func = lambda match_obj: self._convert_meta_function(
            *self._get_function_name_and_args(match_obj.group(1)))
        total_nb_subs = 0
        while True:
            text, nb_subs = regexp.subn(repl_func, text)
            if nb_subs == 0:
                break
            total_nb_subs += nb_subs

        return text

