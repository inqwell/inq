#!/usr/local/bin/bash
"""
Base class for converting a meta-filter file into a database-specific filter file.
"""

import re


def _get_function_name_and_args(str_to_split):
    """
    Split a string of into a meta-function name and list of arguments.
    @param IN str_to_split String to split
    @return Function name and list of arguments, as a pair
    """

    parts = [s.strip() for s in str_to_split.split(" | ")]
    if len(parts) < 2:
        raise Exception("Invalid meta function string: %s" % str_to_split)
    func_name = parts[0]
    func_args = parts[1:]
    return func_name, func_args


class MetaToDbFilter:
    """
    Base class for converting a meta-filter file into a database-specific filter file.
    """

    def _convert_meta_function(self, func_name, func_args):
        """
        Convert a meta-function call into a DB-specific function call.
        THIS METHOD MUST BE OVERRIDDEN.
        @param IN func_name Meta-function name
        @param IN func_args Function arguments
        @return A string representing the DB-specific function call
        """

        raise Exception("Must be overridden")

    def _get_db_renamings(self):
        """
        Get the names that clash with reserved database keywords along with their renamings.
        @return A dictionary mapping clashing names to their renamings
        """

        raise Exception("Must be overridden")

    def meta_to_db_text(self, text):
        """
        Convert a meta-filter text into a database-specific filter text.
        @param IN text Meta-filter text
        @return Database-specific text
        """

        lines = text.splitlines()
        nb_lines = len(lines)

        # Find the "read-sql" section
        # - 'read-sql' line
        regexp = re.compile("^\s*\"read-sql\",\s*$", re.IGNORECASE)
        for line_num in xrange(0, nb_lines):
            line = lines[line_num]
            match_obj = regexp.match(line)
            if match_obj:
                break
        else:
            raise Exception("'read-sql' section not found")
        # - Opening double-quote line
        regexp = re.compile("^\s*\"\s*$", re.IGNORECASE)
        line_num += 1
        if line_num >= nb_lines:
            raise Exception("End of file not expected after 'read-sql' line")
        line = lines[line_num]
        match_obj = regexp.match(line)
        if match_obj is None:
            raise Exception("Opening double-quote line expected after 'read-sql' line:\n%s" % line)
        first_line_num = line_num + 1
        # - Closing double-quote line
        regexp = re.compile("^\s*\",?\s*$", re.IGNORECASE)
        for line_num in xrange(first_line_num, nb_lines):
            line = lines[line_num]
            match_obj = regexp.match(line)
            if match_obj:
                break
        else:
            raise Exception("Closing double-quote line not found")
        last_line_num = line_num - 1
        if last_line_num < first_line_num:
            raise Exception("Empty 'read-sql' section")

        # Prefix and postfix the text with " " so that regular expressions using "\W" work
        section_text = " %s " % ("\n".join(lines[first_line_num:last_line_num + 1]))

        # Convert the table/column names into DB-compatible names
        renamings = self._get_db_renamings()
        for xml_name, db_name in renamings.iteritems():
            regexp = re.compile("(\W)%s(\W)" % xml_name)
            repl_str = "\\1%s\\2" % db_name
            section_text, nb_subs = regexp.subn(repl_str, section_text)
            if nb_subs > 0:
                print "%d renamings of '%s' to '%s'" % (nb_subs, xml_name, db_name)

        # Convert the meta-functions into DB-specific functions
        regexp = re.compile("\${([^}]*)}") # inside-most "${...}"
        repl_func = lambda match_obj: self._convert_meta_function(
            *_get_function_name_and_args(match_obj.group(1)))
        total_nb_subs = 0
        while True:
            section_text, nb_subs = regexp.subn(repl_func, section_text)
            if nb_subs == 0:
                break
            total_nb_subs += nb_subs
        if total_nb_subs > 0:
            print "%d meta-function substitutions" % total_nb_subs

        # Remove the " " prefix and postfix
        section_text = section_text[1:-1]

        # Build the result string
        result_lines = lines[:first_line_num]
        result_lines.append(section_text)
        result_lines.extend(lines[last_line_num + 1:])
        return "\n".join(result_lines)

    def meta_to_db_file(self, input_file, output_file):
        """
        Convert a meta-filter file into a database-specific filter file.
        @param IN input_file  Meta-filter file to read
        @param IN output_file DB-specific filter file to write
        """

        fh = open(input_file)
        try:
            input_text = fh.read()
        finally:
            fh.close()

        output_text = self.meta_to_db_text(input_text)

        fh = open(output_file, "w")
        try:
            fh.write(output_text)
        finally:
            fh.close()

