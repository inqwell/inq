/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstString.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * An immutable string
 */
public class ConstString extends AbstractValue
                         implements StringI, Cloneable, Comparable

{
  public final static String null__   = "__null__";

  private String value_;

  transient private static final String empty__ = "";

  public ConstString() { value_ = empty__; }
  public ConstString(char[] c) { value_ = new String(c); }
  public ConstString(byte[] b) { value_ = new String(b); }
  public ConstString(String s) { value_ = s; }
  public ConstString(StringI s) { value_ = s.getValue(); }

  public Any bestowConstness()
  {
    return this;
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this; // not reached
  }

  public String toString()
  {
    if (this.isNull())
      return AnyString.EMPTY.toString();

    return value_;
  }

  public int hashCode()
  {
    return value_.hashCode();
  }

  public boolean equals(Any a)
  {
    if (this == a)
      return true;
    
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == null && isNull())
      return true;

    if (a instanceof StringI)
    {
      return (((StringI)a).getValue().equals(getValue()));
    }
    return false;
  }

  public boolean isNull()
  {
    // java null
    if (value_ == null)
      return true;

    // inq null
    if (value_ == null__)
      return true;

    // just in case serialized in
    if (value_.equals(null__))
    {
      value_ = null__;
      return true;
    }
    return false;
  }

  public int length()
  {
    if (isNull())
      return 0;

    return value_.length();
  }

  public void setNull()
  {
    constViolation();
  }

  public void setValue(String value)
  {
    constViolation();
  }

  public void setValue(char[] c)
  {
    constViolation();
  }

  public int compareTo (Any a)
  {
    ConstString op2 = (ConstString)likeMe(a);
    return (getValue().compareTo(op2.getValue()));
  }

  public String  getValue()
  {
    if (this.isNull())
      return AnyString.EMPTY.toString();

    return value_;
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyString(this);
  }

  public boolean isConst()
  {
    return true;
  }

  public Object clone() throws CloneNotSupportedException
  {
    // If we're a ConstString then don't clone
    //if (AnyString.class.isAssignableFrom(this.getClass()))
    //System.out.println("CLONE " + this.getClass());
    if (this.getClass() == ConstString.class)
      return this;

    //System.out.println("Real Clone of " + this.getClass());
    Object o = super.clone();

    // The underlying String object is immutable so no need to
    // allocate a new one to the clone.

    return o;
  }

  public int compareTo(Object o)
  {
    StringI s = (StringI)o;
    
    return getValue().compareTo(s.getValue());
  }
  
  // The non-const version is derived from us.
  protected void setString(String s)
  {
    if (this.getClass() == ConstString.class)
      constViolation();

    if (s == null)
      setNullValue();
    else
      value_ = s;
  }

  protected void setNullValue()
  {
    if (this.getClass() == ConstString.class)
      constViolation();
    value_ = null__;
  }

  protected void initialiseFrom(Any a)
  {
    if (a == null)
    {
      //setNullValue();
      value_ = null__;
    }
    else
    {
      // Inconsistent because derived uses visitor. But all the
      // other values have their visitor in the base class. This one
      // should have too.  I'm a twit.
      //setString(a.toString());
    	if (a instanceof Value && ((Value)a).isNull())
    		setNull();
    	else
        value_ = a.toString();
    }
  }

  /*
  protected Object readResolve() throws ObjectStreamException
  {
    if (value_.equals(null__))
      value_ = null__;

    return this;
  }
  */
}
