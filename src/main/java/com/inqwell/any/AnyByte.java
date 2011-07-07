/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


/**
 * Concrete class AnyByte.  Byte data type of Any.
 */
public class AnyByte extends    ConstByte
                     implements ByteI,
                                Numeric,
                                Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyByte.class);
	
  private transient AssignerVisitor copier_;

  public AnyByte() { super(); }
  public AnyByte(byte b) { super(b); }
  public AnyByte(String s) { super(s); }
  public AnyByte(int i) { super((byte)i); }
  public AnyByte(Any a) { super(a); }

	public void fromString(String s)
	{
    setFromString(s);
	}
	
  public Any bestowConstness()
  {
    return new ConstByteDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyByte a = (AnyByte)super.clone();

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

  public void setValue(byte value) { setToValue(value); }

  public void setNull()
  {
    setToNull();
  }

  public boolean isConst()
  {
    return false;
  }
}

