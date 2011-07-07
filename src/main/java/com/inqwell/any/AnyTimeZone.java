/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

import java.util.Date;
import java.util.TimeZone;

public class AnyTimeZone extends    DefaultPropertyAccessMap
                         implements Cloneable
{
  public static final AnyTimeZone null__ = new AnyTimeZone((TimeZone)null);
  
  public static final Any timezone__     = AbstractValue.flyweightString("timezone");  
  
  private TimeZone timeZone_;
  
  public AnyTimeZone()
  {
    timeZone_ = TimeZone.getDefault();
  }
  
  /**
   * Construct to wrap a pre-loaded TimeZone
   */
  public AnyTimeZone(TimeZone t)
  {
    timeZone_ = t;
  }
  
  public AnyTimeZone(Any source)
  {
    processTimeZone(source.toString());
  }
  
  public AnyTimeZone(String source)
  {
    processTimeZone(source);
  }
  
  public TimeZone getTimeZone()
  {
    return timeZone_;
  }
  
  // Properties
  
  public Any getDisplayName()
  {
    return new AnyString(timeZone_.getDisplayName());
  }
  
  public Any getShortDisplayName()
  {
    return new AnyString(timeZone_.getDisplayName(false, TimeZone.SHORT));
  }
  
  public Any  getDSTSavings()
  {
    return new AnyInt(timeZone_. getDSTSavings());
  }
  
  public Any  getRawOffset()
  {
    return new AnyInt(timeZone_. getRawOffset());
  }
  
  public Any getID()
  {
    return new AnyString(timeZone_.getID());
  }
  
  public Any getDSTToday()
  {
    return timeZone_.inDaylightTime(new Date()) ? AnyBoolean.TRUE : AnyBoolean.FALSE;
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
      if (a instanceof StringI)
      {
        processTimeZone(a.toString());
        return this;
      }
      
      if (!(a instanceof AnyTimeZone))
        throw new IllegalArgumentException();
      
      AnyTimeZone i = (AnyTimeZone)a;
      this.timeZone_ = i.timeZone_;
    }
    return this;
  }
  
  // Properties

  public Object getPropertyBean()
  {
    return timeZone_;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  public String toString()
  {
    if (timeZone_ != null)
      return timeZone_.getDisplayName();
    else
      return "<no zone>";
  }

  public boolean equals(Any a)
  {
    return (a instanceof AnyTimeZone) &&
         (((AnyTimeZone)a).timeZone_.equals(timeZone_));
  }

  private void processTimeZone(String tz)
  {
    timeZone_ = TimeZone.getTimeZone(tz);
  }
}
