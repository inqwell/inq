/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyShort.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;


/**
 * Concrete class AnyShort.  Short integer data type of Any.
 */
public class AnyShort extends    ConstShort
                      implements ShortI,
                                 Numeric,
                                 Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyShort.class);

  private transient AssignerVisitor copier_;

  public AnyShort() { super(); }
  public AnyShort(short i) { super(i); }
  public AnyShort(String s) {super(s); }
  public AnyShort(Any a) { super(a); }
  public AnyShort(int i) { super(i); }

	public void fromString(String s)
	{
		setFromString(s);
	}
	
  public Any bestowConstness()
  {
    return new ConstShortDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyShort a = (AnyShort)super.clone();

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

  public void setValue(short value) { setToValue(value); }

  public void setNull()
  {
    setToNull();
  }

  public boolean isConst()
  {
    return false;
  }
}

