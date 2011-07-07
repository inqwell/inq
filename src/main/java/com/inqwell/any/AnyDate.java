/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.io.ObjectStreamException;

/**
 * Concrete class AnyDate.  Date data type of Any.  Holds date and time
 * by wrapping by java.util.Date
 */
public class AnyDate extends    ConstDate
                     implements DateI,
                                Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyDate.class);
	
  public static long SEC_MILLI = 1000;
  public static long MINUTE_MILLI = (SEC_MILLI * 60);
  public static long HOUR_MILLI = (MINUTE_MILLI * 60);
  public static long DAY_MILLI = (HOUR_MILLI * 24);
  public static long WEEK_MILLI = (DAY_MILLI * 7);

  public static IntI AM                   = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.AM));
  public static IntI AM_PM                = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.AM_PM));
  public static IntI APRIL                = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.APRIL));
  public static IntI AUGUST               = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.AUGUST));
  public static IntI DATE                 = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.DATE));
  public static IntI DAY_OF_MONTH         = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.DAY_OF_MONTH));
  public static IntI DAY_OF_WEEK          = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.DAY_OF_WEEK));
  public static IntI DAY_OF_WEEK_IN_MONTH = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.DAY_OF_WEEK_IN_MONTH));
  public static IntI DAY_OF_YEAR          = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.DAY_OF_YEAR));
  public static IntI DECEMBER             = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.DECEMBER));
  public static IntI DST_OFFSET           = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.DST_OFFSET));
  public static IntI ERA                  = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.ERA));
  public static IntI FEBRUARY             = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.FEBRUARY));
  public static IntI FIELD_COUNT          = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.FIELD_COUNT));
  public static IntI FRIDAY               = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.FRIDAY));
  public static IntI HOUR                 = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.HOUR));
  public static IntI HOUR_OF_DAY          = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.HOUR_OF_DAY));
  public static IntI JANUARY              = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.JANUARY));
  public static IntI JULY                 = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.JULY));
  public static IntI JUNE                 = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.JUNE));
  public static IntI MARCH                = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.MARCH));
  public static IntI MAY                  = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.MAY));
  public static IntI MILLISECOND          = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.MILLISECOND));
  public static IntI MINUTE               = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.MINUTE));
  public static IntI MONDAY               = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.MONDAY));
  public static IntI MONTH                = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.MONTH));
  public static IntI NOVEMBER             = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.NOVEMBER));
  public static IntI OCTOBER              = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.OCTOBER));
  public static IntI PM                   = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.PM));
  public static IntI SATURDAY             = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.SATURDAY));
  public static IntI SECOND               = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.SECOND));
  public static IntI SEPTEMBER            = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.SEPTEMBER));
  public static IntI SUNDAY               = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.SUNDAY));
  public static IntI THURSDAY             = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.THURSDAY));
  public static IntI TUESDAY              = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.TUESDAY));
  public static IntI UNDECIMBER           = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.UNDECIMBER));
  public static IntI WEDNESDAY            = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.WEDNESDAY));
  public static IntI WEEK_OF_MONTH        = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.WEEK_OF_MONTH));
  public static IntI WEEK_OF_YEAR         = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.WEEK_OF_YEAR));
  public static IntI YEAR                 = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.YEAR));
  public static IntI ZONE_OFFSET          = (IntI)AbstractValue.flyweightConst(new ConstInt(Calendar.ZONE_OFFSET));

  private transient AssignerVisitor copier_;

  public AnyDate() { super(); }
  public AnyDate(java.util.Date date) { super(date); }
  public AnyDate(long date) { super(date); }
  public AnyDate(DateI date)
  {
    super(date);
  }

  public AnyDate(String s)
  {
		super(s);
	}

	public void fromString(String s)
	{
		setFromString(s);
	}

  public Any bestowConstness()
  {
    return new ConstDateDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyDate a = (AnyDate)super.clone();
    
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

  public void setNull()
  {
    setToNull();
  }
  
  public void setValue(java.util.Date value)
  {
    if (value == null)
      setNull();
    else
      setTime(value.getTime());
  }
  
  public void setTime(long t)
  {
    setToTime(t);
  }

  public boolean isConst()
  {
    return false;
  }
}

