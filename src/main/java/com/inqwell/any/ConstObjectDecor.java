/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

public class ConstObjectDecor extends    AbstractAny
                              implements ObjectI
{
  private static final long serialVersionUID = 1L;

  protected ObjectI delegate_; 

  public ConstObjectDecor(ObjectI d)
  {
    delegate_ = d;
  }

  public Object getValue()
  {
    return delegate_.getValue();
  }

  public void setValue(Object value)
  {
    constViolation(reason__);
  }

  public void accept(Visitor v)
  {
    v.visitAnyObject(this);
  }

  public Any bestowConstness()
  {
    return this;
  }

  public Any buildNew(Any a)
  {
    // Refer to delegate and don't bother to decorate seems to be the
    // appropriate thing to do.
    return delegate_.buildNew(a);
  }

  public Any copyFrom(Any a)
  {
    constViolation(reason__);
    return this; // not reached
  }

  public boolean equals(Any a)
  {
    return delegate_.equals(a);
  }

  public int hashCode()
  {
    return delegate_.hashCode();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public boolean isConst()
  {
    return true;
  }
}
