/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstBigDecimalDecor.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.math.BigDecimal;

/**
 * A decorator that bestows const characteristics on the
 * underlying <code>Decimal</code>.
 */
public class ConstBigDecimalDecor extends AbstractValue
                                  implements Decimal,
                                             Cloneable
{  
  private Decimal delegate_;
  
	public ConstBigDecimalDecor(Decimal d)
	{
    delegate_ = d;
	}
  
  public Any bestowConstness()
  {
    return this;
  }

  public boolean isConst()
  {
    return true;
  }
  
  public void setScale(int scale)
  {
    delegate_.setScale(scale);
  }
  
  public int scale()
  {
    return delegate_.scale();
  }
  
  public BigDecimal getValue()
  {
    return delegate_.getValue();
  }
  
  public void setValue(BigDecimal d)
  {
    constViolation(reason__);
  }
  
  public double doubleValue()
  {
    return delegate_.doubleValue();
  }
  
  public long longValue()
  {
    return delegate_.longValue();
  }
  
  public int signum()
  {
    return delegate_.signum();
  }
  
  public void fromString(String s)
  {
    constViolation(reason__);
  }
  
  public boolean isNull()
  {
    return delegate_.isNull();
  }
  
  public Any copyFrom (Any a)
  {
    constViolation(reason__);
    return this; // not reached
  }

  public Any buildNew (Any a)
  {
    // Refer to delegate and don't bother to decorate seems to be the
    // appropriate thing to do.
    return delegate_.buildNew(a);
  }
  
  public Iter createIterator () {return DegenerateIter.i__;}
  
  public void setNull()
  {
    constViolation(reason__);
  }

  public boolean equals(Any a)
  {
    return delegate_.equals(a);
  }

  public int hashCode()
  {
    return delegate_.hashCode();
  }

  public void accept (Visitor v)
  {
    v.visitDecimal(this);
  }
  
  public String toString()
  {
    return delegate_.toString();
  }

  public Object clone() throws CloneNotSupportedException
  {
    return this;
  }

  protected void initialiseFrom(Any a)
  {
    throw new UnsupportedOperationException();
  }
}
