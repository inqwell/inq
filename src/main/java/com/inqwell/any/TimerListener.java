/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/TimerListener.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * An <code>EventListener</code> whose purpose is to dispatch
 * a received TIMER_TASK event by executing the contained function
 */
public class TimerListener extends    AbstractAny
                           implements EventListener
{
  private static final long serialVersionUID = 1L;

  static private Array eventTypes__ = AbstractComposite.array();
  
  static private Any lastRan__  = new ConstString("lastRan");
  static private Any nextRuns__ = new ConstString("nextRuns");
  static private Any timer__    = new ConstString("fromTimer");
  
  // The root node and transaction of the process that
  // this TimerListener is associated with.
	private Any          root_;
	private Transaction  t_;
  
  // Records the arguments passed to the function supplied in the
  // TIMER_TASK event.  Just to ensure that if they are mutated
  // by the function that the original AnyTimerTask is not corrupted.
  private DateI        lastRan_  = new AnyDate();
  private DateI        nextRuns_ = new AnyDate();
  private Map          args_     = AbstractComposite.simpleMap();

  static
  {
    eventTypes__.add(EventConstants.TIMER_TASK);
  }
  
	public TimerListener(Any root, Transaction t)
	{
		root_ = root;
		t_    = t;
    args_.add(lastRan__,  lastRan_);
    args_.add(nextRuns__, nextRuns_);
	}
	
  public boolean processEvent(Event e) throws AnyException
  {
    AnyFuncHolder.FuncHolder fh = (AnyFuncHolder.FuncHolder)e.getContext();
    
    AnyTimerTask.TimerEvent te = (AnyTimerTask.TimerEvent)e;
    lastRan_.copyFrom(te.getLastRan());
    nextRuns_.copyFrom(te.getNextRuns());
    args_.replaceItem(timer__, te.getTimer());
    
    try
    {
	    fh.doFunc(t_, args_, root_);
    }
    finally
    {
    	args_.remove(timer__);
    }
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
}
