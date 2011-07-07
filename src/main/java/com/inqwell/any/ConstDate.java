/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstDate.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.io.ObjectStreamException;

/**
 * An immutable date
 */
public class ConstDate extends    AbstractValue
                       implements DateI,
                                  Cloneable
{
	public static final AnyObject class__ = new AnyObject(ConstDate.class);
	
  public static long MAX_VALUE = java.lang.Long.MAX_VALUE;
  public static long MIN_VALUE = 0;

  private java.util.Date value_;

  public ConstDate() { value_ = new java.util.Date(); }
  public ConstDate(java.util.Date date) { value_ = new java.util.Date(date.getTime()); }
  public ConstDate(long date) { value_ = new java.util.Date(date); }
  public ConstDate(DateI date)
  {
    if (date == null || date.isNull())
      this.setToNull();
    else
      value_ = new java.util.Date(date.getTime());
  }

  public ConstDate(String s)
  {
		value_ = new java.util.Date();
		setFromString(s);
	}

  public String toString()
  {
    if (isNull())
      return AnyString.EMPTY.toString();
    else
    {
      synchronized(this)
      {
        return (value_.toString());
      }
    }
  }

	public void fromString(String s)
	{
    constViolation();
	}

  public int hashCode()
  {
    synchronized(this)
    {
      return value_.hashCode();
    }
  }

  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == null && isNull())
      return true;
      
    return (a instanceof DateI) &&
           (((DateI)a).getTime() == (getTime()));
  }

  public Any bestowConstness()
  {
    return this;
  }

  public boolean isConst()
  {
    return true;
  }

  public Object clone() throws CloneNotSupportedException
  {
    if (this.getClass() == ConstDate.class) 
      return this;

    ConstDate ret = (ConstDate)super.clone();
    
    if (isNull())
    {
      ret.setNull();
    }
    else
    {
      // Allocate a new Date object in the clone
      synchronized(this)
      {
        ret.value_ = new java.util.Date (value_.getTime());
      }
    }

    return ret;
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyDate(this);
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this;
  }

  public boolean isNull()
  {
    return value_ == DateNull.instance();
  }
  
  public void setNull()
  {
    constViolation();
  }
  
  protected void setToNull()
  {
    value_ = DateNull.instance();
  }
  
  public java.util.Date  getValue() { return value_; }
  
  public void setValue(java.util.Date value)
  {
    constViolation();
  }

  protected void setToValue(DateI value)
  {
    if (value == null)
      setToNull();
    else
      setToTime(value.getTime());
  }

  public long getTime()
  {
    synchronized(this)
    {
      return value_.getTime();
    }
  }
  
  public void setTime(long t)
  {
    constViolation();
  }

  public int compareTo(DateI other)
  {
    // For thread safety
    DateI otherD = (DateI)other.cloneAny();
    
    synchronized(this)
    {
      return this.value_.compareTo(otherD.getValue());
    }
  }
  
  protected void setToTime(long t)
  {
    if (isNull())
      value_ = new java.util.Date(t);
      
    synchronized(this)
    {
      value_.setTime(t);
    }
  }

  protected void initialiseFrom(Any a)
  {
    AssignerVisitor v = makeCopier();
    v.copy(a);
  }
  
  public void setFromString(String s)
  {
    // Relatively expensive as we need to create a formatting object

    /*
    DateFormat df = DateFormat.getDateInstance();
    */
    SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy H:m:s");			      
    df.setLenient (true);

    boolean worked = false;
    Exception ex = null;
    try
    {
      ConstDate.this.setValue (df.parse(s));
      worked = true;
    }
    catch (ParseException e)
    {
      worked = false;
      ex = e;
    }

    // try another date format
    if (!worked)
    {
      SimpleDateFormat df2 = new SimpleDateFormat("dd MMM yyyy");
      df2.setLenient (true);
      try
      {
        ConstDate.this.setValue (df2.parse(s));
        worked = true;
      }
      catch (ParseException e)
      {
        worked = false;
        ex = e;
      }
    }
    
    if (!worked)
    {
      throw (new IllegalArgumentException ("Parse exception converting " +
                                           s +
                                           " to ConstDate: " +
                                           ex.getMessage()));
    }
  }
  
  protected AssignerVisitor makeCopier()
  {
		return new CopyFrom();
  }

  protected class CopyFrom extends AssignerVisitor
  {
    protected void copy (Any from)
    {
      from.accept (this);
    }

    // Its only OK to copy to a ConstDate from a string or another ConstDate

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstDate.this.setToNull();
      else
        setFromString(s.getValue());
    }

    public void visitAnyDate (DateI d)
    {
      if (d.isNull())
        ConstDate.this.setToNull();
      else
        ConstDate.this.setToValue (d);
    }
    
    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstDate.this.setToNull();
      else
        ConstDate.this.setToTime (l.getValue());
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstDate.this.setToNull();
      else
        ConstDate.this.setToTime (d.longValue());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstDate.this.setToNull();
      else
        unsupportedOperation (o);
    }
  }
}

