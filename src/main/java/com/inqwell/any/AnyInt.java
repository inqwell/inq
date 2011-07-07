/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyInt.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;


/**
 * Concrete class AnyInt.  Integer data type of Any.
 */
public class AnyInt extends    ConstInt
                    implements IntI,
                               Numeric,
                               Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyInt.class);

  private transient AssignerVisitor copier_;

  public AnyInt() { }
  public AnyInt(int i) { super(i); }
  public AnyInt(Any a) { super(a); }
  public AnyInt(String s) { super(s); }

	public void fromString(String s)
	{
		setFromString(s);
	}
	
  public Any bestowConstness()
  {
    return new ConstIntDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyInt a = (AnyInt)super.clone();

    a.copier_ = null;

    return a;
  }

  public Any copyFrom (Any a)
  {
    if (a != null)
    {
      if (a != this)
      {
        if (copier_ == null)
          copier_ = makeCopier();
        copier_.copy (a);
      }
    }
    else
      setNull();
      
    return this;
  }

  public void setValue(int value) { setToValue(value); }

  public void increment()
  {
    incrementValue();
  }
  
  public void decrement()
  {
    decrementValue();
  }
  
  public void setNull()
  {
    setToNull();
  }

  public boolean isConst()
  {
    return false;
  }
}

