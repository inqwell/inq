/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ScheduleAt.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Schedule the future execution of an expression.  Implements the scripting
 * statement <code>createtimer(f, when|delay, [period], [delayOrRate]);</code>
 * where:
 * <ul>
 * <li><b>f</b> is the function that will be called when the timer
 * matures</li>
 * <li><b>whenOrDelay</b> is either a <code>DateI</code> for an absolute time
 * execution or a (convertible to) <code>LongI</code> for a relative delay</li>
 * <li><b>period</b> is the repeat interval.  If not specified or zero then
 * the timer is a one-off execution.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class ScheduleAt extends    AbstractFunc
									 implements Cloneable
{
	private Any func_;
	private Any whenOrDelay_;
	private Any period_;
	private Any delayOrRate_;
	private Any start_;
	
  /**
   * 
   */
	public ScheduleAt(Any func,
                    Any whenOrDelay,
                    Any period,
                    Any delayOrRate,
                    Any start)
	{
		func_        = func;
		whenOrDelay_ = whenOrDelay;
		period_      = period;
		delayOrRate_ = delayOrRate;
		start_        = start;
	}
	
	public Any exec(Any a) throws AnyException
	{
		AnyFuncHolder.FuncHolder func = (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc
                               (getTransaction(),
																a,
																func_,
                                AnyFuncHolder.FuncHolder.class);

    if (func == null)
    	nullOperand(func_);
    
		Any  whenOrDelay = EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         whenOrDelay_);

    if (whenOrDelay == null)
    	nullOperand(whenOrDelay_);
    
		Any  period      = EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         period_);

    if (period == null)
    	nullOperand(period_);
    
		IntI  delayOrRate = (IntI)EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         delayOrRate_,
                                         IntI.class);
    
		if(delayOrRate == null)
			nullOperand(delayOrRate_);
		
		BooleanI start  = (BooleanI)EvalExpr.evalFunc(getTransaction(),
																					        a,
																					        start_,
																					        BooleanI.class);

    DateI   when    = null;
    LongI   delay   = null;
    LongI   aPeriod = null;
    
    if (whenOrDelay instanceof DateI)
    {
      when = (DateI)whenOrDelay;
    }
    else
    {
      delay = new ConstLong(whenOrDelay);
    }
    
    if (period != null)
    {
      aPeriod = new ConstLong(period);
    }
    
    AnyTimerTask tt = null;
    tt = new AnyTimerTask(func,
                          when,
                          delay,
                          aPeriod,
                          delayOrRate,
                          start,
                          getTransaction().getProcess());
    
		return tt;
	}
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(func_);
		a.add(whenOrDelay_);
		if (period_ != null)
      a.add(period_);
		if (delayOrRate_ != null)
      a.add(delayOrRate_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		ScheduleAt s = (ScheduleAt)super.clone();
		s.func_        = func_.cloneAny();
		s.whenOrDelay_ = whenOrDelay_.cloneAny();
		s.period_      = AbstractAny.cloneOrNull(period_);
		s.delayOrRate_ = AbstractAny.cloneOrNull(delayOrRate_);
		s.start_        = AbstractAny.cloneOrNull(start_);
		
		return s;
  }
  
  private Map resolveArgs(Map args, Any root) throws AnyException
  {
		if (args != null)
		{
			Iter i = args.keys().createIterator();
			while (i.hasNext())
			{
				Any argkey = i.next();
				Any argval = args.get(argkey);
				Any newArgval = EvalExpr.evalFunc (getTransaction(),
                                           root,
                                           argval);
        if (newArgval != argval)
          args.replaceItem(argkey, newArgval);
				
				// handle the case where the argument is itself a
				// map (for example the parameters in XSLT)
				if (argval instanceof Map)
				{
					resolveArgs((Map)argval, root);
				}
			}
		}
		//System.out.println ("args = " + args);
		return args;
  }
}
