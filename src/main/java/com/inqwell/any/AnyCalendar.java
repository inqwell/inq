/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.1 $
 * $Date: 2011-04-20 22:12:27 $
 */

package com.inqwell.any;

import java.util.Calendar;
import java.util.Locale;

/**
 * Carry calendars as Any
 * 
 * @author tom
 *
 */
public class AnyCalendar extends    DefaultPropertyAccessMap
                         implements Cloneable
{
  public static final AnyCalendar null__ = new AnyCalendar((Calendar)null);
  
  public static final Any calendar__     = AbstractValue.flyweightString("calendar");  
  
  private Calendar calendar_;
  
  public AnyCalendar()
  {
    //calendar_ = Calendar.getInstance(Locale.getDefault());
  }
  
  /**
   * Construct to wrap a pre-loaded Calendar
   */
  public AnyCalendar(Calendar c)
  {
    calendar_ = c;
  }
  
  public Calendar getCalendar()
  {
    return calendar_;
  }
  
  public void setCalendar(Calendar c)
  {
    calendar_ = c;
  }
  
  public void setTime(long time)
  {
    if (calendar_ == null)
      throw new IllegalStateException();
    
    synchronized(this)
    {
      calendar_.setTimeInMillis(time);
    }
  }

  public void setTime(DateI time)
  {
    if (calendar_ == null)
      throw new IllegalStateException();
    
    synchronized(this)
    {
      calendar_.setTimeInMillis(time.getTime());
    }
  }

  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // See AnyFile.accept also
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public Iter createKeysIterator()
  {
    return DegenerateIter.i__;
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (a instanceof AnyCalendar)
      {
        AnyCalendar i = (AnyCalendar)a;
        this.calendar_ = (Calendar)i.calendar_.clone();
      }
      else if (a instanceof DateI)
      {
        if (calendar_ == null)
          throw new IllegalStateException();
        
        synchronized(this)
        {
          calendar_.setTimeInMillis(((DateI)a).getTime());
        }
      }
      else if (a instanceof LongI)
      {
        if (calendar_ == null)
          throw new IllegalStateException();
        
        synchronized(this)
        {
          calendar_.setTimeInMillis(((LongI)a).getValue());
        }
      }
      else
        throw new IllegalArgumentException();
    }
    return this;
  }
  
  // Properties

  public Any getTimeZone()
  {
    if (calendar_ == null)
      return AnyNull.instance();
    
    return new AnyTimeZone(calendar_.getTimeZone());
  }
  public Object getPropertyBean()
  {
    return calendar_;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  public String toString()
  {
    if (calendar_ != null)
      return calendar_.toString();
    else
      return "<no calendar>";
  }

  public boolean equals(Any a)
  {
    return (a instanceof AnyCalendar) &&
         (((AnyCalendar)a).calendar_.equals(calendar_));
  }
}
