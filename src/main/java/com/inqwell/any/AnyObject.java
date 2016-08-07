/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyObject.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;


/**
 * The <code>AnyObject</code> class simply wraps any <code>java.lang.Object</code>
 * (or its sub class) into the Any framework.
 */
public class AnyObject extends    AbstractAny
											 implements ObjectI,
                                  Cloneable
{
	private static final long serialVersionUID = 1L;

	private Object value_;
	
	public AnyObject() {  }
	public AnyObject(Object o) { value_ = o; }
	
  public Any bestowConstness()
  {
    return new ConstObjectDecor(this);
  }

	public String toString()
	{
		return (value_ == null) ? "" : value_.toString();
	}
	
	public int hashCode()
	{
		return (value_ == null) ? 0 : value_.hashCode();
	}
	
	public boolean equals(Any a)
	{
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (AnyNull.isNullInstance(a))
    {
      if (value_ == null)
        return true;
      else
        return false;
    }
    
    if (a instanceof AnyObject)
    {
      ObjectI ao = (ObjectI)a;
      if (value_ == ao.getValue())
        return true;
      
      if (value_ != null)
        return value_.equals(ao.getValue());
    }
		return false;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		AnyObject a = (AnyObject)super.clone();
		return a;
	}
	
	public void accept (Visitor v)
	{
		v.visitAnyObject(this);
	}
	
	public Any copyFrom (Any a)
	{
		if (a != this)
		{
			if (a == null || AnyNull.isNullInstance(a))
			{
				setValue(null);
			}
			else
			{
				if (a instanceof ObjectI)
				{
          ObjectI ao = (ObjectI)a;
					setValue(ao.getValue());
				}
        else if (a instanceof StringI)
        {
          setValue(((StringI)a).getValue());
        }
        else 
				{
          // Just take it whatever
          setValue(a);
				}
			}
		}
		return this;
	}
		
	public Object  getValue() { return value_; }
	public void setValue(Object value) { value_ = value; }
}
	
