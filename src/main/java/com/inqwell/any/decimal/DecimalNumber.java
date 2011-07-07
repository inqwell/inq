/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/decimal/DecimalNumber.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.decimal;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Support alternative implementations of fixed scale decimal numbers.
 * This interface exists solely to allow alternative implementations
 * of objects representing BigDecimals.  The (non-functionally extending)
 * derivation of java.math.BigDecimal implements this interface,
 * as does the Inq representation of null for this type.
 * <p>
 * The crux of the problem is that the scale of an AnyDecimal is
 * held within the underlying BigDecimal, yet we don't want to lose
 * this information in the null representation since it is
 * maintained when an assignment (via setValue) is made.
 */
public interface DecimalNumber
{
  BigDecimal abs();
  BigDecimal add(BigDecimal val);
  int compareTo(BigDecimal val);
  int compareTo(Object o);
  BigDecimal divide(BigDecimal val, int roundingMode);
  BigDecimal divide(BigDecimal val, int scale, int roundingMode);
  double doubleValue();
  boolean equals(Object x);
  float floatValue();
  int hashCode();
  int intValue();
  long longValue();
  BigDecimal max(BigDecimal val);
  BigDecimal min(BigDecimal val);
  BigDecimal movePointLeft(int n);
  BigDecimal movePointRight(int n);
  BigDecimal multiply(BigDecimal val);
  BigDecimal negate();
  int scale();
  BigDecimal setScale(int scale);
  BigDecimal setScale(int scale, int roundingMode);
  int signum();
  BigDecimal subtract(BigDecimal val);
  BigInteger toBigInteger();
  String toString();
  BigInteger unscaledValue();
}

