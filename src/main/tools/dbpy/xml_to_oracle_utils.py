#!/usr/local/bin/bash
"""
Utility functions for conversion from an XML table file to an Oracle file.

$Header: /home/inqwell/cvsroot/dev/scripts/python/xml_to_oracle_utils.py,v 1.1 2009/05/22 22:16:06 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/05/01 17:04:46 $
$Change: 165582 $
"""

from xml_to_db_utils import MetaDataConverterAbstract


#--------------------------------------------------------------------------------------------------
# DB-specific configuration variables
#--------------------------------------------------------------------------------------------------


# Renamings due to clashes with reserved database keywords
_renamings = {
    "Parent" : "Parent_",
    "Session": "Session_",
    "Source" : "Source_",
    "User"   : "User_",
    "Version": "Version_",
}


# Mapping from the general database types to Oracle types
_general_type_to_db_type = {
    "big_integer"  : "NUMBER(38)",
    "blob"         : "BLOB",
    "char"         : "VARCHAR2",
    "date"         : "DATE",
    "date_time"    : "TIMESTAMP",
    "decimal"      : "NUMBER",
    "integer"      : "NUMBER(38)",
    "small_integer": "NUMBER(38)",
    "var_char"     : "VARCHAR2",
}


#--------------------------------------------------------------------------------------------------
# Main functionality
#--------------------------------------------------------------------------------------------------


def get_db_renamings():
    """
    Get the names that clash with reserved database keywords along with their renamings.
    @return A dictionary mapping clashing names to their renamings
    """

    return dict(_renamings)


def get_db_compatible_name(name):
    """
    Get an Oracle compatible entity name for the provided name.
    @param IN name Proposed name
    @return The corresponding Oracle-compatible name
    """

    try:
        db_name = _renamings[name]
    except KeyError:
        db_name = name
    return db_name


def _get_db_name(elt, elt_type, attr_name="name"):
    """
    Get the database-compatible name of an XML element.
    @param IN elt       XML element
    @param IN elt_type  XML element type
    @param IN attr_name XML attribute name; 'name' by default
    @return The name as a string
    """

    name = elt.get(attr_name)
    if not name:
        raise Exception("Missing %s '%s' attribute" % (elt_type, attr_name))
    return get_db_compatible_name(name)


def get_db_column_name(column_elt):
    """
    Get the database-compatible name of a column.
    @param IN column_elt Column XML element
    @return The name as a string
    """

    return _get_db_name(column_elt, "column")


def get_db_column_type(column_elt):
    """
    Get the database type of a column.
    @param IN column_elt Column XML element
    @return The database type as a string
    """

    col_type = column_elt.get("type")
    if not col_type:
        raise Exception("Missing column 'type' attribute")
    try:
        db_type = _general_type_to_db_type[col_type]
    except KeyError:
        raise Exception("Unknown type: %s" % col_type)
    precision_list = []
    precision_level = 1
    precision = column_elt.get("type_precision_%d" % precision_level)
    while precision is not None:
        precision_list.append(precision)
        precision_level += 1
        precision = column_elt.get("type_precision_%d" % precision_level)
    if precision_list:
        db_type = "%s(%s)" % (db_type, ",".join(precision_list))
    return db_type


def get_db_foreign_key_column_target(column_elt):
    """
    Get the database-compatible name of the column targeted by foreign key column.
    @param IN column_elt Foreign key column XML element
    @return The name as a string
    """

    return _get_db_name(column_elt, "foreign key column", attr_name="to")


def get_db_foreign_key_name(foreign_key_elt):
    """
    Get the database-compatible name of a foreign key.
    @param IN foreign_key_elt Foreign key XML element
    @return The name as a string
    """

    return _get_db_name(foreign_key_elt, "foreign key")


def get_db_foreign_key_target_table(foreign_key_elt):
    """
    Get the database-compatible name of the table targeted by foreign key.
    @param IN foreign_key_elt Foreign key XML element
    @return The name as a string
    """

    return _get_db_name(foreign_key_elt, "foreign key", attr_name="to")


def get_db_index_name(index_elt):
    """
    Get the database-compatible name of an index.
    @param IN index_elt Index XML element
    @return The name as a string
    """

    return _get_db_name(index_elt, "index")


def get_db_primary_key_name(primary_key_elt):
    """
    Get the database-compatible name of a primary key.
    @param IN primary_key_elt Primary key XML element
    @return The name as a string
    """

    return _get_db_name(primary_key_elt, "primary key")


def get_db_table_name(table_elt):
    """
    Get the database-compatible name of a table.
    @param IN table_elt Table XML element
    @return The name as a string
    """

    return _get_db_name(table_elt, "table")


def get_db_unique_key_name(unique_key_elt):
    """
    Get the database-compatible name of a unique key.
    @param IN unique_key_elt Unique key XML element
    @return The name as a string
    """

    return _get_db_name(unique_key_elt, "unique key")


#---------------------
# Meta-data conversion
#---------------------


def _convert_concatenate(arg_list):
    """
    Handler for the "concatenate" meta-function.
    @param IN arg_list List of arguments
    @return DB function call string
    """

    return " || ".join(arg_list)


def _convert_now(arg_list):
    """
    Handler for the "concatenate" meta-function.
    @param IN arg_list List of arguments
    @return DB function call string
    """

    nb_args = len(arg_list)
    if nb_args != 0:
        raise Exception("The 'now' meta-function does not take arguments (%d provided)" % nb_args)
    return "SYSDATE"


# Conversion between the meta-data date format components and the Oracle equivalent
# The meta-data date format components are the same as in MySQL
_date_format_conversion = {
    # Day:
    # - Abbreviated weekday name (3 letters)
    "%a": "DY",
    # - Day of the month, numeric (2 digits)
    "%d": "DD",
    # - Weekday name
    "%W": "DAY",
    # Month:
    # - Abbreviated month name (3 letters)
    "%b": "MON",
    # - Month name
    "%M": "MONTH",
    # - Month, numeric (2 digits)
    "%m": "MM",
    # Year:
    # - Year, numeric (4 digits)
    "%Y": "YYYY",
    # - Year, numeric (2 digits)
    "%y": "YY",
    # Hour (24h)
    "%H": "HH24",
    # Minutes
    "%i": "MI",
    # Seconds
    "%s": "SS",
}

def _convert_str_to_date(arg_list):
    """
    Handler for the "str_to_date" meta-function.
    @param IN arg_list List of arguments
    @return DB function call string
    """

    nb_args = len(arg_list)
    if nb_args != 2:
        raise Exception("The 'strip_time' meta-function should take exactly 2 argument "
            "(%d provided)" % nb_args)
    date_str, format_str = arg_list
    for meta_format_component, db_format_component in _date_format_conversion.iteritems():
        format_str = format_str.replace(meta_format_component, db_format_component)
    if "%" in format_str:
        raise Exception("Cannot fully interpret the format string: %s" % format_str)
    return "TO_DATE(%s, %s)" % (date_str, format_str)


def _convert_strip_time(arg_list):
    """
    Handler for the "strip_time" meta-function.
    @param IN arg_list List of arguments
    @return DB function call string
    """

    nb_args = len(arg_list)
    if nb_args != 1:
        raise Exception("The 'strip_time' meta-function should take exactly 1 argument "
            "(%d provided)" % nb_args)
    return "TRUNC(%s)" % arg_list[0]


_meta_func_handlers = {
    "concatenate": _convert_concatenate,
    "now":         _convert_now,
    "str_to_date": _convert_str_to_date,
    "strip_time" : _convert_strip_time,
}


class MetaDataConverter(MetaDataConverterAbstract):
    """
    Class for converting meta-data into Oracle-specific data.
    """

    def _convert_meta_function(self, func_name, func_args):
        """
        Convert a meta-function call into a DB-specific function call.
        @param IN func_name Meta-function name
        @param IN func_args Function arguments
        @return A string representing the DB-specific function call
        """

        try:
            handler = _meta_func_handlers[func_name]
        except KeyError:
            raise Exception("Unhandled meta-function '%s'" % func_name)
        return handler(func_args)

