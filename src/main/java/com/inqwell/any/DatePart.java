/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DatePart.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-20 22:11:09 $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any;

import java.util.*;

/**
 * The <code>DatePart</code> function.
 * Returns an LongI
 */
public class  DatePart extends    AbstractFunc
                       implements Cloneable
{
	
  private Any datepart_;
  private Any date_;
  private Any cal_;

	/**
	 * 
	 */
  public DatePart(Any datepart, Any d, Any cal)
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
		
		DateI date     = (DateI)EvalExpr.evalFunc(getTransaction(),
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

    IntI ret = null;
    
    if (date.isNull())
      ret = new AnyInt((Any)null);
    else
      ret = new AnyInt(datepart(datepart.getValue(), 
                                date,
                                cal));
		
    return ret;
  }

	private int datepart(int datepart, DateI date, AnyCalendar cal)
	{
		int ret = 0;
		
    Calendar d = (cal != null) ? cal.getCalendar() : Calendar.getInstance();
		d.setTimeInMillis(date.getTime());	

		// the wrapped datepart correspond exactly with the Java Calendar
		ret = d.get(datepart);

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
    DatePart dp = (DatePart)super.clone();
    
    dp.datepart_    = datepart_.cloneAny();
    dp.date_        = date_.cloneAny();
    dp.cal_        = AbstractAny.cloneOrNull(cal_);

    return dp;
  }
}
