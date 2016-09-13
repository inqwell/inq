#!/usr/local/bin/bash
"""
Convert a XML database table files into a Django model file.

$Header: /home/inqwell/cvsroot/dev/scripts/python/xml_to_django_model.py,v 1.1 2009/05/22 22:15:59 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/05/07 19:53:05 $
$Change: 165960 $
"""

import re
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


def _get_class_name(table_name):
    """
    Get the class name for a given Xylinq table name.
    @param IN table_name Xylinq table name
    @return Class name
    """

    # Just make sure the first letter is upper case
    cls_name = "%s%s" % (table_name[0].upper(), table_name[1:])
    return cls_name


def _get_field_name(column_name):
    """
    Get the field name for a given Xylinq column name.
    @param IN column_name Xylinq column name
    @return Field name
    """

    # All lower-case, with underscores to separate words
    # Words are determined from the upper-case letters
    field_name = column_name
    field_name = re.sub("([a-z])([A-Z])", lambda m: "%s_%s" % (m.group(1), m.group(2).lower()),
        field_name)
    field_name = "%s%s" % (field_name[0].lower(), field_name[1:])
    field_name = re.sub("([A-Z])([a-z])", lambda m: "_%s%s" % (m.group(1).lower(), m.group(2)),
        field_name)
    field_name = field_name.lower()

    return field_name


def _get_field_type(column_info):
    """
    Get the Django field type from the column's information.
    @param IN column_info Xylinq column information object
    @return A 2-element tuple: the name of the Django type class, and its attributes as a string
    """

    xy_type = column_info.type
    xy_type_precision = column_info.type_precision

    # Integer
    if xy_type in ("big_integer", "integer", "small_integer"):
        return "IntegerField", ""

    # Decimal
    if xy_type == "decimal":
        return "DecimalField", "decimal_places=%s, max_digits=%s, " % (xy_type_precision[1],
            xy_type_precision[0])

    # Date
    if xy_type == "date":
        return "DateField", ""

    # Datetime
    if xy_type == "date_time":
        return "DateTimeField", ""

    # String
    if xy_type in ("char", "var_char"):
        return "CharField", "max_length=%s, " % xy_type_precision[0]

    # Blob
    if xy_type == "blob":
        return "TextField", ""

    raise Exception("Column type not supported: %s" % xy_type)


def table_info_list_to_django_model_text(table_info_list, xy_to_db_name_func):
    """
    Convert a list of table info objects into a Django model text.
    @param IN table_info_list    List of table info objects
    @param IN xy_to_db_name_func Function taking a Xylinq name and returning its DB-compatible name
    @return The Django model text as a string
    """

    # Using """""" so that Perforce does not try any substitution here when committing this file
    result_lines = [
        """\"\"\"
Django model for Xylinq.

$Header"""""": $
$Author"""""": $
$DateTime"""""": $
$Change"""""": $
\"\"\"

from django.db import models""",
    ]

    table_info_by_name = dict([(table_info.name, table_info) for table_info in table_info_list])
    table_order = get_table_creation_order(table_info_list)
    for table_name in table_order:
        table_info = table_info_by_name[table_name]

        # Determine the field to use as primary key:
        # - the table's primary key if it is single-column
        # - the first single-column table's unique key otherwise
        # Also identify the other unique fields and the composite unique keys
        primary_field = None
        unique_fields = []
        unique_composites = []
        pk_col_names = table_info.primary_key.column_names
        if len(pk_col_names) == 1:
            primary_field = _get_field_name(pk_col_names[0])
        else:
            unique_composites.append([_get_field_name(col_name) for col_name in pk_col_names])
        for uk_info in table_info.unique_keys:
            uk_col_names = uk_info.column_names
            if len(uk_col_names) == 1:
                if primary_field is None:
                    primary_field = _get_field_name(uk_col_names[0])
                else:
                    unique_fields.append(_get_field_name(uk_col_names[0]))
            else:
                unique_composites.append([_get_field_name(col_name) for col_name in uk_col_names])
        if primary_field is None:
            raise Exception("Cannot find a single field to use as primary key for table %s" %
                table_name)

        # One-to-one and many-to-one fields
        # Map a field name to a (target class name, target field name) tuple
        one_to_one_fields = {}
        many_to_one_fields = {}
        for fk_info in table_info.foreign_keys:
            fk_col_names = fk_info.column_names
            if len(fk_col_names) == 1:
                field_name = _get_field_name(fk_col_names[0])
                target_cls_name = _get_class_name(fk_info.target_table_name)
                target_field_name = _get_field_name(fk_info.target_column_names[0])
                if field_name == primary_field or field_name in unique_fields:
                    one_to_one_fields[field_name] = (target_cls_name, target_field_name)
                else:
                    many_to_one_fields[field_name] = (target_cls_name, target_field_name)

        # Class header line
        cls_name = _get_class_name(table_name)
        result_lines.append("""
class %s(models.Model):""" % cls_name)

        # Meta class lines
        db_table_name = xy_to_db_name_func(table_name)
        result_lines.append("""    class Meta:
        db_table = u"%s\"""" % db_table_name)
        # The "managed" attribute is only supported by Django's dev version for now, so commenting
        # out the below line
        #result_lines.append("""        managed = False""")
        if unique_composites:
            result_lines.append("""        unique_together = (""")
            for composite in unique_composites:
                result_lines.append("""            ("%s"),""" % "\", \"".join(composite))
            result_lines.append("""        )""")

        # Fields lines
        for col_info in table_info.columns:
            col_name = col_info.name
            field_name = _get_field_name(col_name)
            db_col_name = xy_to_db_name_func(col_name)
            pk_str = field_name == primary_field and ", primary_key=True" or ""
            # NOTE:
            # The Django doc mentions the following about the "null" attribute:
            # "
            # Avoid using null on string-based fields such as CharField and TextField unless you
            # have an excellent reason. If a string-based field has null=True, that means it has
            # two possible values for "no data": NULL, and the empty string. In most cases, it's
            # redundant to have two possible values for "no data"; Django convention is to use
            # the empty string, not NULL.
            # "
            # However, I believe this is only relevant if we plan to enter data through Django's
            # admin site, which is not our case.
            # On top of that, some DBMS do not consider empty string and null as the same.
            # Here, we set the "null" attribute as it is in the DB independently of the type.
            if field_name == primary_field:
                null_str = ""
            else:
                null_str = ", null=%s" % (col_info.nullable and "True" or "False")

            if field_name in one_to_one_fields:
                target_cls_name, target_field_name = one_to_one_fields[field_name]
                if target_cls_name == cls_name:
                    target_cls_name = "\"self\""
                result_lines.append(
                    """    %s = models.OneToOneField(%s, to_field="%s", db_column="%s"%s%s)""" % (
                    field_name, target_cls_name, target_field_name, db_col_name, pk_str, null_str))
            elif field_name in many_to_one_fields:
                target_cls_name, target_field_name = many_to_one_fields[field_name]
                if target_cls_name == cls_name:
                    target_cls_name = "\"self\""
                result_lines.append(
                    """    %s = models.ForeignKey(%s, to_field="%s", db_column="%s"%s%s)""" % (
                    field_name, target_cls_name, target_field_name, db_col_name, pk_str, null_str))
            else:
                type_cls_name, type_attrs_str = _get_field_type(col_info)
                unique_str = field_name in unique_fields and ", unique=True" or ""
                result_lines.append(
                    """    %s = models.%s(%sdb_column="%s"%s%s%s)""" % (
                    field_name, type_cls_name, type_attrs_str, db_col_name, pk_str, unique_str,
                    null_str))

    return "\n".join(result_lines)


def table_xml_files_to_django_model_file(input_files, output_file, xy_to_db_name_func):
    """
    Convert table XML files into a Django model file.
    @param IN input_files        Table XML files
    @param IN output_file        Django model file
    @param IN xy_to_db_name_func Function taking a Xylinq name and returning its DB-compatible name
    """

    table_info_list = []
    for input_file in input_files:
        table_elt_tree = ET.parse(input_file)
        table_elt = table_elt_tree.getroot()
        table_info = get_table_info(table_elt)
        table_info_list.append(table_info)
    output_text = table_info_list_to_django_model_text(table_info_list, xy_to_db_name_func)
    fh = open(output_file, mode="w")
    try:
        fh.write(output_text)
    finally:
        fh.close()


def main():
    import glob
    from optparse import OptionParser
    import os

    parser = OptionParser()
    parser.add_option("--in_dir", dest="input_dir", help="Input directory for XML files")
    parser.add_option("--out", dest="output_file", help="Output Django model file")
    parser.add_option("--db", dest="db_type", help="DB type: MySQL, Oracle, ...")
    options, dummy = parser.parse_args()

    input_dir = options.input_dir
    if input_dir is None:
        raise Exception("Missing mandatory argument '--in_dir'")
    output_file = options.output_file
    if output_file is None:
        raise Exception("Missing mandatory argument '--out'")
    db_type = options.db_type
    if db_type is None:
        raise Exception("Missing mandatory argument '--db'")
    try:
        xy_to_db_name_func = _name_func_by_db_type[db_type.lower()]
    except KeyError:
        raise Exception("DB type not supported: '%s'" % db_type)

    input_files = glob.glob(os.path.join(input_dir, "*.xml"))
    table_xml_files_to_django_model_file(input_files, output_file, xy_to_db_name_func)


if __name__ == "__main__":
    main()

