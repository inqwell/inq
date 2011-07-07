/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


/**
 * Concrete class AnyFloat.  Float data type of Any.
 */
public class AnyFloat extends    ConstFloat
                      implements FloatI,
                                 Numeric,
                                 Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyFloat.class);
	
  private transient AssignerVisitor copier_;

  public AnyFloat() { }
  public AnyFloat(float f) { super(f); }
  public AnyFloat(String s) { super(s); }
  public AnyFloat(Any a) {super(a);}

	public void fromString(String s)
	{
		setFromString(s);
	}
	
  public Any bestowConstness()
  {
    return new ConstFloatDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyFloat a = (AnyFloat)super.clone();

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

  public void setValue(float value) { setToValue(value); }

  public void setNull()
  {
    setToNull();
  }

  public boolean isConst()
  {
    return false;
  }
}

