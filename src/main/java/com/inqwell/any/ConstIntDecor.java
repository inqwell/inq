/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstIntDecor.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * A decorator that bestows const characteristics on the
 * underlying <code>IntI</code>.
 */
public class ConstIntDecor extends AbstractValue
                            implements IntI,
                                       Cloneable
{  
  private IntI delegate_;
  
	public ConstIntDecor(IntI d)
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
  
  public int getValue()
  {
    return delegate_.getValue();
  }
  
  public void setValue(int d)
  {
    constViolation(reason__);
  }
  
  public void decrement()
  {
    constViolation(reason__);
  }
  
  public void increment()
  {
    constViolation(reason__);
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

  public Any roundFrom(Any a)
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
    v.visitAnyInt(this);
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
