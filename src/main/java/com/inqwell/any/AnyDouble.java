/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


/**
 * Concrete class AnyDouble.  Double data type of Any.
 */
public class AnyDouble extends    ConstDouble
                       implements DoubleI,
                                  Numeric,
                                  Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyDouble.class);
	
  private transient AssignerVisitor copier_;

  public AnyDouble() { super(); }
  public AnyDouble(double d) { super(d); }
  public AnyDouble(String s) { super(s); }
  public AnyDouble(Any a) { super(a); }

	public void fromString(String s)
	{
		setFromString(s);
	}

  public Any bestowConstness()
  {
    return new ConstDoubleDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyDouble a = (AnyDouble)super.clone();

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

  public void setValue(double value) { setToValue(value); }

  public void setNull()
  {
    setToNull();
  }

  public boolean isConst()
  {
    return false;
  }
}

