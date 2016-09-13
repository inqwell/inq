#!/usr/local/bin/bash
"""
Convert a meta-filter file into a Oracle filter file.
"""

from meta_to_db_filter_files import MetaToDbFilter
from xml_to_oracle_utils import get_db_renamings


def _convert_strip_time(arg_list):
    """
    Handler for the "strip-time" meta-function.
    @param IN arg_list List of arguments
    @return DB function call string
    """

    nb_args = len(arg_list)
    if nb_args != 1:
        raise Exception("The 'strip_time' meta-function should take exactly 1 argument "
            "(%d provided)" % nb_args)
    return "TRUNC(%s)" % arg_list[0]


def _convert_concatenate(arg_list):
    """
    Handler for the "concatenate" meta-function.
    @param IN arg_list List of arguments
    @return DB function call string
    """

    return " || ".join(arg_list)


# Mapping from meta-function names to handlers
_meta_func_handlers = {
    "strip_time" : _convert_strip_time,
    "concatenate": _convert_concatenate,
}


class MetaToOracleFilter(MetaToDbFilter):
    """
    Class for converting a meta-filter file into a Oracle filter file.
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

    def _get_db_renamings(self):
        """
        Get the names that clash with reserved database keywords along with their renamings.
        @return A dictionary mapping clashing names to their renamings
        """

        return get_db_renamings()


def main():
    from optparse import OptionParser
    import os

    parser = OptionParser()
    parser.add_option("--in", dest="input_file", help="Input meta-filter file")
    parser.add_option("--in_dir", dest="input_dir", help="Input directory for meta-filter files")
    parser.add_option("--out", dest="output_file", help="Output Oracle file")
    parser.add_option("--out_dir", dest="output_dir", help="Output directory for Oracle files")
    options, dummy = parser.parse_args()

    converter = MetaToOracleFilter()
    if options.input_file is not None:
        output_file = options.output_file
        if output_file is None:
            output_file = os.path.join(options.output_dir, os.path.basename(options.input_file))
        converter.meta_to_db_file(options.input_file, output_file)
    else:
        import glob
        import os
        input_files = glob.glob(os.path.join(options.input_dir, "*.sql"))
        for input_file in input_files:
            try:
                print "Processing %s" % input_file
                output_file = os.path.join(options.output_dir, os.path.basename(input_file))
                converter.meta_to_db_file(input_file, output_file)
            except Exception, err:
                print "!!! FAILED: %s !!!" % err


if __name__ == "__main__":
    main()

