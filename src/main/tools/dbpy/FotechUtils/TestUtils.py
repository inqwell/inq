#!/usr/local/bin/python
# $Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/TestUtils.py,v 1.1 2009/05/22 22:16:47 sanderst Exp $
# $Author: sanderst $
# $Revision: 1.1 $
# $DateTime: 2008/10/22 16:12:54 $
# $Change: 143536 $

from xml.dom.minidom import parseString

#------------------------------------------------------------------------------
def parse_xml_to_dictform(text):
    """
    Parses the text as XML and converts it to "dict" form, whereby each element is represented by a python
    tuple, consisting of the node name, a dictionary of attributes and a list of sub-elements.

    Such "dict" form can be compared for equality directly. If two XML files' dict forms are equal then
    the XML files are equivalent, i.e. have all the same elements in the same order, and all the same
    attributes (in any order).
    """
    return xml_to_dictform(parseString(text).documentElement)

#------------------------------------------------------------------------------
def xml_to_dictform(node):
    """ Converts a minidom node to "dict" form. See parse_xml_to_dictform. """
    if node.nodeType != node.ELEMENT_NODE:
        raise Exception("Expected element node")

    result = (node.nodeName, {}, [])  # name, attrs, items

    if node.attributes != None:
        attrs = node.attributes # hard to imagine a more contrived way of accessing attributes...
        for key, value in ((attrs.item(i).name, attrs.item(i).value) for i in xrange(attrs.length)):
            result[1][key] = value

    for child in node.childNodes:
        if child.nodeType == child.ELEMENT_NODE:
            result[2].append(xml_to_dictform(child))

    return result

#------------------------------------------------------------------------------
def dictform_to_string(dictform, _indent = 0):
    """
    Prints a "dict" form XML as a tree of nodes, listing each node's attributes alphabetically. Should
    only be used for unit testing - it is not intended to be parseable back into a tree.
    """
    result = ["    " * _indent, dictform[0], '[' + ' '.join((k+'="'+dictform[1][k]+'"' for k in sorted(dictform[1].iterkeys()))) + ']\n']
    for item in dictform[2]:
        val = dictform_to_string(item, _indent+1)
        if isinstance(val, list): result.extend(val)
        else:                     result.append(val)
    if _indent == 0:
        result = ''.join(result)
    return result
