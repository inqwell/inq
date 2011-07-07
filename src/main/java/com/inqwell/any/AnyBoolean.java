/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


/**
 * Concrete class AnyBoolean.  Generally copying semantics are defined
 * for scalar types.  Copying from <code>null</code> sets the value
 * to <code>false</code>, copying from a structure node (i.e.
 * an <code>array</code> or a <code>Map</code> yields <code>true</code>
 * on the understanding that the node exists.
 */
public class AnyBoolean extends    ConstBoolean
												implements BooleanI,
                                   Cloneable
{
	public static final BooleanI TRUE  = new ConstBoolean(true);
	public static final BooleanI FALSE = new ConstBoolean(false);
	
	public static final AnyObject class__ = new AnyObject(AnyBoolean.class);
	
  private transient AssignerVisitor copier_;

  public AnyBoolean() { super(); }
  public AnyBoolean(boolean b) { super(b); }
  public AnyBoolean(String s) { super(s); }
  public AnyBoolean(int i) { super(i); }
  public AnyBoolean(Any a) { super(a); }

  public static boolean booleanValue(String s)
  {
	  if (s.length() == 0)
	    return false;

    return true;
	}
	
	public void fromString(String s)
	{
		setFromString(s);
	}
	
  public Object clone() throws CloneNotSupportedException
  {
    AnyBoolean a = (AnyBoolean)super.clone();
    a.copier_ = null;

    return a;
  }

  public void setNull()
  {
  }

  public void setValue(boolean value) { setToValue(value); }

  public Any bestowConstness()
  {
    return new ConstBooleanDecor(this);
  }

  public Any copyFrom (Any a)
  {
		if (a == null || a == AnyNull.instance())
			setValue(false);
    else if (a != this)
    {
      if (copier_ == null)
        copier_ = makeCopier();
      copier_.copy (a);
    }
      
    return this;
  }

  public boolean isConst()
  {
    return false;
  }
}

