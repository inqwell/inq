"""
Utils for decimal manipulations.

$Header: /home/inqwell/cvsroot/dev/scripts/python/FotechUtils/decimal_utils.py,v 1.1 2009/05/22 22:16:34 sanderst Exp $
$Author: sanderst $
$DateTime: 2009/05/20 03:35:32 $
$Change: 166987 $
"""

import decimal


#---------------------------------------------------------------------------------------------------
# Type-specific functions
#---------------------------------------------------------------------------------------------------


def decimalToString(val):
    """
    Convert a decimal into a string.
    The advantage over applying "str" on a decimal is that the string representation returned here
    is minimal, i.e. no unnecessary trailing zeros or dot

    Params:
    - val (decimal.Decimal): Decimal to convert

    Return (str): String representation
    """

    # NOTE: We do not use the "normalize" method on decimal.Decimal as it forces scientific notation
    val = str(val)
    if "." in val:
        lhs, rhs = val.split(".", 1)
        lhs = lhs.lstrip("0") or "0"
        rhs = rhs.rstrip("0")
        if rhs:
            val = ".".join((lhs, rhs))
        else:
            val = lhs
    return val


def floatToDecimal(val):
    """
    Convert a float f into a decimal d such that:
    - float(d) = f
    - d has the smallest number of significant places

    Params:
    - val (float): Float to convert

    Return (decimal.Decimal): Decimal conversion
    """

    if val < 0:
        decimal_sign = 1
        val = -val
    else:
        decimal_sign = 0
    decimal_exponent = 0
    factor = 1
    scaled_val_round = round(val, 0)
    while (scaled_val_round / factor) != val:
        decimal_exponent -= 1
        factor *= 10
        scaled_val_round = round(val * factor, 0)
    scaled_val_int = int(scaled_val_round)
    decimal_digits = map(int, str(scaled_val_int))
    return decimal.Decimal((decimal_sign, decimal_digits, decimal_exponent))


def floatToString(val):
    """
    Convert a float f to a string s such that:
    - float(s) = f
    - f has the smallest number of significant places

    Params:
    - val (float): Float to convert

    Return (str): String representation
    """

    dec = floatToDecimal(val)
    return decimalToString(dec)


def round(val, digits, mode = decimal.ROUND_HALF_UP):
    """
    Round a decimal value to the given number of decimal places,
    using the given rounding mode, or the standard ROUND_HALF_UP
    if not specified
    """
    return val.quantize(decimal.Decimal("10") ** -digits, mode)

#---------------------------------------------------------------------------------------------------
# Generic functions
#---------------------------------------------------------------------------------------------------


def toString(obj):
    """
    Convert an object to a string.

    Params:
    - obj (any): Object to convert

    Return (str): String representation
    """

    if isinstance(obj, decimal.Decimal):
        return decimalToString(obj)
    elif isinstance(obj, float):
        return floatToString(obj)
    else:
        return str(obj)


def toDecimal(obj):
    """
    Convert an object representing a numerical value to a decimal.
    The object itself is returned if already a decimal.

    Params:
    - obj (any): Object to convert

    Return (str): Decimal conversion
    """

    if isinstance(obj, decimal.Decimal):
        return obj
    elif isinstance(obj, float):
        return floatToDecimal(obj)
    elif isinstance(obj, str):
        return decimal.Decimal(obj)
    elif isinstance(obj, int):
        return decimal.Decimal(obj)
    elif isinstance(obj, tuple):
        return decimal.Decimal(obj)
    else:
        float_val = float(obj)
        return floatToDecimal(float_val)

