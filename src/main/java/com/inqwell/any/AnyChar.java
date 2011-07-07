/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


/**
 * Concrete class AnyChar.  Char data type of Any.
 */
public class AnyChar extends    ConstChar
                     implements CharI,
                                Numeric,
                                Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyChar.class);
	
  private transient AssignerVisitor copier_;

  public AnyChar() { super(); }
  public AnyChar(char c) { super(c); }
  public AnyChar(String s) { super(s); }
  public AnyChar(int i) { super((char)i); }

	public void fromString(String s)
	{
		setFromString(s);
	}

  public Object clone() throws CloneNotSupportedException
  {
    AnyChar a = (AnyChar)super.clone();

    a.copier_ = null;

    return a;
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public Any bestowConstness()
  {
    return new ConstCharDecor(this);
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

  public void setValue(char value) { setToValue(value); }

  public void setNull()
  {
    setToNull();
  }

  public boolean isConst()
  {
    return false;
  }
}

