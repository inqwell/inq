/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DateAdd.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-20 22:11:09 $
 * @version $Revision: 1.4 $
 * @see 
 */

package com.inqwell.any;

import java.util.*;

/**
 * The <code>DateAdd</code> function. Adds the given value (use -ve for
 * subtract) to the given date where the value is granuals of the
 * given datepart.
 * <p>
 * Returns a new DateI.
 */
public class  DateAdd extends    AbstractFunc
								      implements Cloneable
{
	
  private Any datepart_;
  private Any val_;
  private Any date_;
  private Any cal_;

	/**
	 * 
	 */
  public DateAdd(Any datepart, Any val, Any date, Any cal)
  {
    datepart_ = datepart;
    val_      = val;
    date_     = date;
    cal_      = cal;
  }

  public Any exec(Any a) throws AnyException
  {
		IntI datepart  = (IntI)EvalExpr.evalFunc(getTransaction(),
                                             a,
                                             datepart_,
                                             IntI.class);
		
		Any val          = EvalExpr.evalFunc(getTransaction(),
																				 a,
																				 val_);
		if (val == null)
		  nullOperand(val_);
	
		DateI date     = (DateI)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              date_,
                                              DateI.class);
    if (date == null)
      nullOperand(date_);

    AnyCalendar cal  = (AnyCalendar)EvalExpr.evalFunc(getTransaction(),
                                                      a,
                                                      cal_,
                                                      AnyCalendar.class);
    if (cal == null && cal_ != null)
      nullOperand(cal_);
  
    LongI toAdd = new ConstLong(val);
		return new AnyDate(dateadd(datepart.getValue(), 
                               toAdd.getValue(),
                               date,
                               cal));
		
  }

	private Date dateadd(int datepart, long value, DateI date, AnyCalendar cal)
	{
	  Calendar d;
		
	  if (cal == null)
		  d = Calendar.getInstance();
		else
		  d = cal.getCalendar();
		
		d.setTimeInMillis(date.getTime());	
    d.add(datepart, (int)value);
    return d.getTime();
	}


  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(datepart_);
		a.add(val_);
		a.add(date_);
		if (cal_ != null)
		  a.add(cal_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    DateAdd da = (DateAdd)super.clone();
    
    da.datepart_     = datepart_.cloneAny();
    da.val_          = val_.cloneAny();
    da.date_         = date_.cloneAny();
    da.cal_          = AbstractAny.cloneOrNull(cal_);
    
    return da;
  }
}
