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
 * $Date: 2011-04-20 22:11:09 $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any;

import java.util.*;

/**
 * The <code>DateFlat</code> function.
 * Returns a DateI that is flattened to the given
 * datepart.
 * <p>
 * Not all dateparts may be supported.  Unsupported dateparts throw
 * an exception
 */
public class DateFlat extends    AbstractFunc
                      implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any datepart_;
  private Any date_;
  private Any cal_;

  /**
   * 
   */
  public DateFlat(Any datepart, Any d, Any cal)
  {
    datepart_ = datepart;
    date_     = d;
    cal_      = cal;
  }

  public Any exec(Any a) throws AnyException
  {
    IntI datepart  = (IntI)EvalExpr.evalFunc(getTransaction(),
                                a,
                                datepart_,
                                IntI.class);
    
    DateI date    = (DateI)EvalExpr.evalFunc(getTransaction(),
                                a,
                                date_,
                                DateI.class);
    if (date == null)
      nullOperand(date_);

    AnyCalendar cal = (AnyCalendar) EvalExpr.evalFunc(getTransaction(),
                                                      a,
                                                      cal_,
                                                      AnyCalendar.class);
    if (cal == null && cal_ != null)
      nullOperand(cal_);

    return dateflat(datepart.getValue(), 
                    date,
                    cal);
    
  }

  private DateI dateflat(int datepart, DateI date, AnyCalendar cal) throws AnyException
  {
    DateI ret = null;
    
    Calendar c = (cal != null) ? cal.getCalendar() : Calendar.getInstance();

    if (datepart == Calendar.DAY_OF_YEAR)
    {
      // Flatten the time elements smaller then a day
      // ie hour, min and sec
      Date dateD = DateDiff.flattenToDay(date, c);
      
      ret = new AnyDate(dateD.getTime());
    }
    else if (datepart == Calendar.WEEK_OF_YEAR)
    {
      // Flatten the time elements smaller then a week
      // ie day, hour, min and sec
      Date dateD = DateDiff.flattenToWeek(date, c);

      ret = new AnyDate(dateD.getTime());
    }
    else if (datepart == Calendar.HOUR ||
             datepart == Calendar.HOUR_OF_DAY)
    {
      Date dateD = DateDiff.flattenToHour(date, c);
      ret = new AnyDate(dateD.getTime());
    }
    else if (datepart == Calendar.MINUTE)
    {
      Date dateD = DateDiff.flattenToMinute(date, c);
      ret = new AnyDate(dateD.getTime());
    }
    else if (datepart == Calendar.SECOND)
    {
      Date dateD = DateDiff.flattenToSecond(date, c);
      ret = new AnyDate(dateD.getTime());
    }
    else
    {
      throw new AnyException("Unsupported datepart " + datepart);
    }
    
    return ret; 
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(datepart_);
    a.add(date_);
    if (cal_ != null)
      a.add(cal_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    DateFlat dd = (DateFlat)super.clone();
    
    dd.datepart_   = datepart_.cloneAny();
    dd.date_       = date_.cloneAny();
    dd.cal_        = AbstractAny.cloneOrNull(cal_);

    return dd;
  }
}
