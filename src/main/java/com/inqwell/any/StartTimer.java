/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/StartTimer.java $
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
public class StartTimer extends    AbstractFunc
                         implements Cloneable
{
	private static final long serialVersionUID = 1L;

	private Any timer_;
	private Any delay_;
	
	public StartTimer(Any timer, Any delay)
	{
		timer_ = timer;
		delay_ = delay;
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
		
		Any delay = EvalExpr.evalFunc(getTransaction(),
													        a,
													        delay_);

		if (delay == null && delay_ != null)
			nullOperand(delay_);

    timer.startTimer(new ConstLong(delay));
    
		return timer;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		StartTimer s = (StartTimer)super.clone();
		s.timer_ = timer_.cloneAny();
		s.delay_ = AbstractAny.cloneOrNull(delay_);
		return s;
  }
	
}
