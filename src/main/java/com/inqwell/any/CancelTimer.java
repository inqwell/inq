/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/CancelTimer.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Cancel a timer task previously created with <code>ScheduleAt</code>.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class CancelTimer extends    AbstractFunc
                         implements Cloneable
{
	
  private static final long serialVersionUID = 1L;

  private Any timer_;
	
	public CancelTimer(Any timer)
	{
		timer_ = timer;
	}
	
	public Any exec(Any a) throws AnyException
	{
		AnyTimerTask timer = (AnyTimerTask)EvalExpr.evalFunc(getTransaction(),
                                                         a,
                                                         timer_);

		if (timer == null)
			nullOperand(timer_);
		
		// put the owner process in, just in case timer was created as a
		// data type.
		timer.ownerProcess(getTransaction().getProcess());
		
    timer.cancel();
    
		return timer;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		CancelTimer c = (CancelTimer)super.clone();
		c.timer_ = timer_.cloneAny();
		return c;
  }
	
}
