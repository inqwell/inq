/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DateDiff.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-20 22:11:09 $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any;

import java.util.*;

/**
 * The <code>DateDiff</code> function.
 * Returns an LongI that is the numeric difference between the
 * given end and start dates (end - start) in units of the given
 * datepart.
 * <p>
 * Not all dateparts may be supported.  Unsupported dateparts throw
 * an exception
 */
public class DateDiff extends    AbstractFunc
								      implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any datepart_;
  private Any start_;
  private Any end_;
  private Any cal_;

	/**
	 * 
	 */
  public DateDiff(Any datepart, Any s, Any e, Any cal)
  {
    datepart_ = datepart;
    start_    = s;
    end_      = e;
    cal_      = cal;
  }

  public Any exec(Any a) throws AnyException
  {
		IntI datepart  = (IntI)EvalExpr.evalFunc(getTransaction(),
																a,
																datepart_,
																IntI.class);
		
		DateI start    = (DateI)EvalExpr.evalFunc(getTransaction(),
																a,
																start_,
																DateI.class);
    if (start == null)
      nullOperand(start_);

		DateI end      = (DateI)EvalExpr.evalFunc(getTransaction(),
																a,
																end_,
																DateI.class);

    if (end == null)
      nullOperand(end_);

    AnyCalendar cal = (AnyCalendar) EvalExpr.evalFunc(getTransaction(),
                                                      a,
                                                      cal_,
                                                      AnyCalendar.class);
    if (cal == null && cal_ != null)
      nullOperand(cal_);

		return new AnyLong(datediff(datepart.getValue(), 
                                start,
                                end,
                                cal));
		
  }

	private long datediff(int datepart, DateI start, DateI end, AnyCalendar cal) throws AnyException
	{
		long ret = 0;
		
    Calendar c = (cal != null) ? cal.getCalendar() : Calendar.getInstance();

    if (datepart == Calendar.DAY_OF_YEAR)
		{
			// Flatten the time elements smaller then a day
			// ie hour, min and sec
			Date startD = flattenToDay(start, c);
			Date endD = flattenToDay(end, c);
			
			long milliDiff = endD.getTime() - startD.getTime();
			ret = milliDiff / AnyDate.DAY_MILLI;
		}
		else if (datepart == Calendar.WEEK_OF_YEAR)
		{
      // Flatten the time elements smaller then a week
			// ie day, hour, min and sec
			Date startD = flattenToWeek(start, c);
			Date endD = flattenToWeek(end, c);
			
			long milliDiff = endD.getTime() - startD.getTime();
			ret = milliDiff / AnyDate.WEEK_MILLI;
		}
		else if (datepart == Calendar.HOUR)
		{
      Date startD = flattenToHour(start, c);
      Date endD = flattenToHour(end, c);
      ret = (endD.getTime() - startD.getTime()) / AnyDate.HOUR_MILLI;
		}
		else if (datepart == Calendar.MINUTE)
		{
      Date startD = flattenToMinute(start, c);
      Date endD = flattenToMinute(end, c);
      ret = (endD.getTime() - startD.getTime()) / AnyDate.MINUTE_MILLI;
		}
		else if (datepart == Calendar.SECOND)
		{
      Date startD = flattenToSecond(start, c);
      Date endD = flattenToSecond(end, c);
      ret = (endD.getTime() - startD.getTime()) / 1000;
		}
    else
    {
      throw new AnyException("Unsupported datepart " + datepart);
    }
    
		return ret;	
	}

	public static Date flattenToDay(DateI d, Calendar c)
	{
    c.setTimeInMillis(d.getTime());
		c.clear (Calendar.HOUR);
		c.clear (Calendar.HOUR_OF_DAY);
		c.clear (Calendar.MINUTE);
		c.clear (Calendar.SECOND);
		c.clear (Calendar.MILLISECOND);
		return c.getTime();
	}

  public static Date flattenToWeek(DateI d, Calendar c)
	{
		flattenToDay(d, c);
		c.clear (Calendar.DAY_OF_WEEK);
		return c.getTime();
	}

  public static Date flattenToSecond(DateI d, Calendar c)
  {
    c.setTimeInMillis(d.getTime());
    c.clear (Calendar.MILLISECOND);
    return c.getTime();
  }

  public static Date flattenToMinute(DateI d, Calendar c)
  {
    c.setTimeInMillis(d.getTime());
    c.clear (Calendar.SECOND);
    c.clear (Calendar.MILLISECOND);
    return c.getTime();
  }

  public static Date flattenToHour(DateI d, Calendar c)
  {
    c.setTimeInMillis(d.getTime());
    c.clear (Calendar.MINUTE);
    c.clear (Calendar.SECOND);
    c.clear (Calendar.MILLISECOND);
    return c.getTime();
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(datepart_);
		a.add(start_);
		a.add(end_);
    if (cal_ != null)
      a.add(cal_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    DateDiff dd = (DateDiff)super.clone();
    
    dd.datepart_    = datepart_.cloneAny();
    dd.start_       = start_.cloneAny();
    dd.end_         = end_.cloneAny();
    dd.cal_          = AbstractAny.cloneOrNull(cal_);

    return dd;
  }
}
