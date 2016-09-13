#!/usr/local/bin/python
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/kbcoptparse.py,v 1.1 2009/05/22 22:16:40 sanderst Exp $
# $Author: sanderst $
# $Revision: 1.1 $
# $DateTime: 2008/10/21 10:33:52 $
# $Change: 138111 $

# Wishlist:
# - Display required args in default usage string
# - Improved help formatter, printing defaults & whether the argument is required
# - Log parsed values to kbclog
# - Automatically add --verbose, which modifies global kbclog settings

import sys
import optparse

__all__ = ['KBCOption', 'KBCOptionParser']



#------------------------------------------------------------------------------
class KBCOption(optparse.Option):

    ATTRS = optparse.Option.ATTRS + ['required']
    ACTIONS = optparse.Option.ACTIONS + ('test',)

    #------------------------------------------------------------------------------
    def __init__(self, *opts, **attrs):
        self._present = False
        return optparse.Option.__init__(self, *opts, **attrs)

    #------------------------------------------------------------------------------
    def process(self, opt, value, values, parser):
        self._present = True
        return optparse.Option.process(self, opt, value, values, parser)

    #------------------------------------------------------------------------------
    def take_action(self, action, dest, opt, value, values, parser):
        if action == 'test':
            sys.argv = [sys.argv[0]]
            import unittest
            unittest.main()
            parser.exit()
        else:
            optparse.Option.take_action(self, action, dest, opt, value, values, parser)



#------------------------------------------------------------------------------
class KBCOptionParser(optparse.OptionParser):
    """
    Extends OptionParser to allow required arguments to be defined. See "optparse"
    documentation for more info on how to use this class.

    Example of a successfully parsed set of arguments (-t is required and is supplied):

    >>> parser = KBCOptionParser()
    >>> dummy = parser.add_option('-t', '--test', required = True, help = "This arg is required")
    >>> dummy = parser.add_option('-s', '--something', default = 'ok')
    >>> by_name, by_pos = parser.parse_args(['-t', 'blah'])
    >>> by_name.test
    'blah'
    >>> by_name.something
    'ok'

    Same example but this time the required argument is not supplied:

    >>> sys.stderr = StringIO()       # capture the error message printed
    >>> parser = KBCOptionParser()
    >>> dummy = parser.add_option('-t', '--test', required = True)
    >>> dummy = parser.add_option('-s', '--something', default = 'ok')
    >>> by_name, by_pos = parser.parse_args(['-s', 'not ok'])
    Traceback (most recent call last):
        ...
    SystemExit: 2
    >>>
    >>> print sys.stderr.getvalue()   # this is the error message printed
    usage: kbcoptparse.py [options]
    <BLANKLINE>
    kbcoptparse.py: error: The following required arguments were not supplied:
        -t/--test
    <BLANKLINE>
    """

    #------------------------------------------------------------------------------
    def __init__(self,
                 usage=None,
                 option_list=None,
                 option_class=KBCOption,  # changing default option class here
                 version=None,
                 conflict_handler="error",
                 description=None,
                 formatter=None,
                 add_help_option=True,
                 prog=None,
                 add_test_option=False):

        optparse.OptionParser.__init__(self, usage, option_list, option_class, version, conflict_handler, description, formatter, add_help_option, prog)
        if add_test_option:
            self.add_option("--test", action="test", help="Perform self-test and exit")

    #------------------------------------------------------------------------------
    def check_values(self, values, args):

        # Check that all required options were supplied
        required_notfound = []
        for opt in self._get_all_options():
            if hasattr(opt, 'required') and opt.required and not opt._present:
                required_notfound.append(opt)

        if len(required_notfound) > 0:
            errstr = 'The following required arguments were not supplied:'
            for opt in required_notfound:
                errstr += '\n    ' + '/'.join(opt._short_opts + opt._long_opts)
            self.error(errstr)

        # Everything is looking ok, return the values/args
        return (values, args)



#------------------------------------------------------------------------------
KBCOptionGroup = optparse.OptionGroup # it is currently fully compatible so just use the same class



#------------------------------------------------------------------------------
if __name__ == '__main__':
    # Test using examples in the doc strings
    import doctest
    from StringIO import StringIO
    doctest.testmod()
