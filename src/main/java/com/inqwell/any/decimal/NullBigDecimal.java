/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/decimal/NullBigDecimal.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.decimal;

import com.inqwell.any.ConstBigDecimal;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * A tagging class that ensures that <code>AnyBigDecimal</code>s
 * whose value is null will be correctly deserialized.
 * <p>
 * Instances of NullBigDecimal are created to represent null for
 * an AnyBigDecimal value.  One instance is created within the VM
 * for each scale encountered.  This class has deserialization
 * semantics such that if a null value is received the object
 * is replaced with this VM's instance.
 */
public class NullBigDecimal extends java.math.BigDecimal
{
  public NullBigDecimal(BigInteger val)
  {
    super(val);
  }

  public NullBigDecimal(BigInteger unscaledVal, int scale)
  {
    super(unscaledVal, scale);
  }
  
  public NullBigDecimal(double val)
  {
    super(val);
  }
  
  public NullBigDecimal(String val) 
  {
    super(val);
  }

  protected Object readResolve() throws ObjectStreamException
  {
    return ConstBigDecimal.nullForScale(this);
  }
}
