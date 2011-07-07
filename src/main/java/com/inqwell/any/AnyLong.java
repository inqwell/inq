/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


/**
 * Concrete class AnyLong.  Long integer data type of Any.
 */
public class AnyLong extends    ConstLong
                     implements LongI,
                                Numeric,
                                Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyLong.class);
	
  private transient AssignerVisitor copier_;

  public AnyLong() { super(); }
  public AnyLong(long l) { super(l); }
  public AnyLong(String s) { super(s); }
  public AnyLong(Any a) { super(a); }

	public void fromString(String s)
	{
		setFromString(s);
	}
	
  public Any bestowConstness()
  {
    return new ConstLongDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyLong a = (AnyLong)super.clone();

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

  public void  setValue(long value) { setToValue(value); }

  public void setNull()
  {
    setToNull();
  }

  public boolean isConst()
  {
    return false;
  }
}

