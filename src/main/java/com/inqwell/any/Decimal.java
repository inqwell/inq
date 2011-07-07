/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Decimal.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

import java.math.BigDecimal;

/**
 *
 */
public interface Decimal extends Value
{
	public static final AnyObject class__ = new AnyObject(Decimal.class);
  
  public void setScale(int scale);
  
  public int scale();
  
  public BigDecimal getValue();
  public void setValue(BigDecimal d);
  
  public double doubleValue();
  
  public long longValue();
  
  public int signum();
  
  public void fromString(String s);
}
