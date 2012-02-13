/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyTimerTask.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.util.Timer;

import com.inqwell.any.client.swing.SwingInvoker;

/**
 * Represents an expression to be run an at some time in the
 * future.  All the possibilities described in
 * <a href="http://java.sun.com/j2se/1.4.1/docs/api/java/util/Timer.html">Timer</a>
 * are possible.
 * <p>
 * When the timer matures a TIMER_TASK event is sent to the given input
 * channel. This event is dispatched to execute the function contained
 * within the timer task.
 */
 
public class AnyTimerTask extends    PropertyAccessMap
                          implements Value,
                                     Cloneable
{
	private static final long serialVersionUID = 1L;

	public static IntI FIXED_DELAY = new ConstInt(0);
  public static IntI FIXED_RATE  = new ConstInt(1);
  
  private static Any timer__    = new ConstString("fromTimer");

  private AnyFuncHolder.FuncHolder func_;
  private transient Map                      propertyMap_;

  private transient TimerTask                tt_;
  private transient Process                  p_;
  private transient boolean                  dispatchToGfx_;
  
  private DateI     lastRan_  = new AnyDate((DateI)null);
  private DateI     nextRuns_ = new AnyDate((DateI)null);
  private LongI     period_   = new AnyLong((Any)null);
  private IntI      delayOrRate_ = FIXED_DELAY;  // FIXED_DELAY or FIXED_RATE
  private Any       userInfo_;
 
   
  /**
   * Create and possibly schedule a timer task.
   * @param The function to execute.
   * @param p The process to which the TIMER_TASK event will be written
   * when the timer matures.  Usually the process input channel.
   * @param when The absolute time when the function should be executed.
   * If <code>null</code> then <code>delay</code> must be specified.
   * @param delay The relative delay before the function should be executed.
   * Must be supplied if <code>when</code> is null, ignored otherwise.
   * @param period optional if the timer task is FIXED_DELAY, mandatory
   * if the timer is FIXED_RATE. The repeat interval.
   * @param delayOrRate If AnyTimerTask.FIXED_DELAY then any period
   * represents the interval between the last execution ending and the
   * next one commencing. If AnyTimerTask.FIXED_RATE then period
   * represents the interval between successive executions.  Moot as
   * the execution of the timer is just to send the TIMER_TASK event
   * so the timer interval is decoupled from function execution.
   * @param start if true start the timer.
   */
  public AnyTimerTask(AnyFuncHolder.FuncHolder func,
                      DateI                    when,
                      LongI                    delay,
                      LongI                    period,
                      IntI                     delayOrRate,
                      BooleanI                 start,
                      Process                  p) throws AnyException
  {
  	setFunc(func);
    ownerProcess(p);
    setNextRuns(when);
    setFixedOrDelay(delayOrRate);
    setPeriod(period);
    
    if (start.getValue())
      startTimer(delay);
  }
  
  /**
   * No-args constructor for use as a scripted data type.
   */
  public AnyTimerTask()
  {
  	setFixedOrDelay(FIXED_DELAY);
  }
  
  /**
   * Cancel this timer task.  The next scheduled run (if any) will
   * not take place. All property settings (except <code>scheduled</code)
   * remain, so the timer can be rescheduled again subsequently.
   */
  public synchronized void cancel()
  {
  	cancelTimer();
  }
  
  /**
   * Forces this timer to fire immediately regardless of whether it is
   * scheduled to run at some time in the future. The <code>lastRan</code>
   * property is set to the current system time.
   * <p>
   * Any scheduling of this timer is not affected and it will fire again
   * at <code>nextRuns</code>, if non-null.  
   * @throws AnyException
   */
  public synchronized void manualFire() throws AnyException
  {
  	if (func_ == null)
  		throw new IllegalStateException("Function is not set");
  	
  	fire(System.currentTimeMillis());
  }

  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;
      
    return false;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public synchronized Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }
        
      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }
  
  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

 	/**
	 * Returns the string representation of the URL of this file.  By
	 * standardising on URLs we bridge files to all other URL specified
	 * stream types.
	 */
	public synchronized String toString()
	{
		StringBuffer sb = new StringBuffer();
    sb.append((func_ == null) ? "<no function>" : func_.toString());
    sb.append(" last ran: ");
    sb.append(lastRan_.toString());
    sb.append(" next runs: ");
    sb.append(nextRuns_.toString());
    sb.append(" period: ");
    sb.append(period_.toString());
    // Can cause an infinite loop when userInfo_ legitimately contains this timer.
    // Script can always fetch userInfo_ by property access itself
    //sb.append(" userInfo: ");
    //sb.append((userInfo_ == null) ? "<none>" : userInfo_.toString());

    return sb.toString();
	}
	
  public synchronized Any copyFrom (Any a)
  {
    if (a == AnyNull.instance())
    {
      // Cancel ourselves. If the properties are OK we could be secheduled.
      this.cancel();
      setNull();
      return this;
    }
    
  	if (!(a instanceof AnyTimerTask))
  		throw new IllegalArgumentException(a.getClass().toString() + " is not a timer");
  	
  	AnyTimerTask tt = (AnyTimerTask)a;
  	
  	// Cancel ourselves. If the properties are OK we could be secheduled.
  	this.cancel();
  	
  	this.func_ = tt.func_;  // cloned if executed, so OK
  	
  	this.period_.copyFrom(tt.period_);
  	this.lastRan_.copyFrom(tt.lastRan_);
  	this.nextRuns_.copyFrom(tt.nextRuns_);
  	
  	// If we have a process, leave it as it is
  	
		return this;
  }

	public Object clone() throws CloneNotSupportedException
	{
		AnyTimerTask tt = (AnyTimerTask)super.clone();
		
		// Can't share properties
		tt.propertyMap_ = null;
		
		// no need to clone func_. It is cloned in FuncHolder.doFunc
		
		// Copy the property values. The new timer will not be scheduled, but
		// may be eligible for scheduling without further setup if the prevailing
		// property values are OK.
		tt.period_   = new AnyLong(period_);
		tt.lastRan_  = new AnyDate(lastRan_);
		tt.nextRuns_ = new AnyDate(nextRuns_);

		// Not owned by anyone yet.
		tt.p_ = null;
		
		// Not scheduled
		tt.tt_ = null;
    
    // Clear userInfo_
    tt.userInfo_ = null;
		
		// delayOrRate_ is only ever read-only to a static value
		
		// er, that's it.
		return tt;
	}
	
  // Properties
	
	/**
	 * Returns the time this timer last fired.
	 * @return When the timer last fired (const). The null value if the
	 * timer has never fired.
	 */
  public synchronized DateI getLastRan()
  {
    return (DateI)lastRan_.bestowConstness();
  }
  
  /**
   * Returns when this timer will next run.
   * @return When the timer will next run (const). The value could be in
   * the past if that is what was set previously and the timer has not
   * yet been scheduled. The null value if no time has been set.
   */
  public synchronized Any getNextRuns()
  {
    return nextRuns_.bestowConstness();
  }
  
  /**
   * Establish the time when this timer will next run. If this timer is
   * already scheduled then it is cancelled and must be restarted by
   * calling {@link#startTimer}.
   * @param nextRuns the time when this timer will next run when
   * rescheduled by {@link#startTimer} or immediately, if <code>nextRuns</code>
   * is in the past.
   */
  public synchronized void setNextRuns(Any nextRuns)
  {
  	cancelTimer();
  	nextRuns_.copyFrom(nextRuns);
  }
  
  public void setPeriod(Any period)
  {
  	cancelTimer();
  	period_.copyFrom(period);
  	if (period_.getValue() == 0)
  		period_.setNull();
  }
  
  public void setSyncGui(boolean syncGui)
  {
    if (Globals.isServer())
      throw new AnyRuntimeException("Property not supported in server environment");
    
    dispatchToGfx_ = syncGui;
  }
  
  public synchronized Any getPeriod()
  {
  	return period_;
  }
  
  public synchronized void setFixedOrDelay(IntI fixedOrDelay)
  {
  	cancelTimer();
  	if (fixedOrDelay.equals(FIXED_DELAY))
  		delayOrRate_ = FIXED_DELAY;
  	else if (fixedOrDelay.equals(FIXED_RATE))
  		delayOrRate_ = FIXED_RATE;
  	else
  		throw new IllegalArgumentException();
  }
  
  public synchronized IntI getFixedOrDelay()
  {
  	return delayOrRate_;
  }
  
  /**
   * Set user-supplied information that will be carried in this timer.
   * The timer is passed to the function called when it fires, so this
   * property can be used to carry any additional data required. 
   * @param userInfo Any desired information to be carried to the handler
   * function.
   */
  public void setUserInfo(Any userInfo)
  {
    // Check if there is information in the transaction that says
    // we would be aliasing a private map instance. We don't want
    // this to happen as the userInfo property is stored and returned
    // as an opaque map. Fields also.
    Transaction t = Globals.getProcessForCurrentThread().getTransaction();
    userInfo = AbstractAny.ripSafe(userInfo, t);
    
    userInfo_ = userInfo;
  }
  
  /**
   * Get any user information.
   * @return The user information.
   */
  public Any getUserInfo()
  {
    return userInfo_;
  }
  
  /**
   * Sets the function that this time will call when fired. This property
   * cannot be set if the timer is currently scheduled.
   * @param func The function called when the timer fires.
   */
  public synchronized void setFunc(AnyFuncHolder.FuncHolder func)
  {
  	if (isScheduled())
  		throw new IllegalStateException("Cannot set function while timer is scheduled");

  	if (func.isNull())
  		throw new IllegalArgumentException("function cannot be null");
  	
  	func_ = func;
  }
  
  /**
   * Sets the owning process of this timer. The method is named so that
   * it will not be examined for property reflection.
   * <p>
   * The owner process can only be set once and is the creator of the
   * timer, to whom timer events will be dispatched.
   * @param p
   */
  public void ownerProcess(Process p)
  {
  	if (p_ != null && p_ != p)
  		throw new IllegalStateException("Process already set: " + p_);
  	
  	p_ = p;
  }

  /**
   * Indicates whether this timer is scheduled to run at some future time.
   * 
   * @return <code>true</code> if the timer will fire in the
   * future, <code>false</code> if it will not.
   */
  public synchronized boolean isScheduled()
  {
  	return tt_ != null;
  }
  
  /**
   * Start the timer with the prevailing properties as follows:
   * <p>
   * 
   * @param delay
   * @throws IllegalStateException if the timer is already scheduled.
   */
  public synchronized void startTimer(LongI delay) throws AnyException
  {
    if (tt_ != null)
    	throw new IllegalStateException("Timer is already scheduled");
    
    if (func_ == null)
      throw new IllegalStateException("No dispatch function set");

    // Get the owner process's timer.
    Timer timer = p_.getTimer().getTimer();
    
    // Ready a TimerTask
    TimerTask tt = new TimerTask();
     
    if (!nextRuns_.isNull())
    {
      if (delayOrRate_ == FIXED_DELAY)
      {
        if (period_.isNull())
        {
          timer.schedule(tt, nextRuns_.getValue());
        }
        else
        {
          timer.schedule(tt, nextRuns_.getValue(), period_.getValue());
        }
      }
      else
      {
        if (period_.isNull())
          throw new AnyException("Fixed rate timer must have a period");
        
        timer.scheduleAtFixedRate(tt, nextRuns_.getValue(), period_.getValue());
      }
    }
    else
    {
      if (delay == null || delay.isNull())
        throw new AnyException("An absolute or relative start time must be specified");

      if (delayOrRate_ == FIXED_DELAY)
      {
        if (period_.isNull())
        {
          timer.schedule(tt, delay.getValue());
          nextRuns_.setTime(System.currentTimeMillis() + delay.getValue());
        }
        else
        {
          timer.schedule(tt, delay.getValue(), period_.getValue());
          nextRuns_.setTime(System.currentTimeMillis() + delay.getValue());
        }
      }
      else
      {
        if (period_.isNull())
          throw new AnyException("Fixed rate timer must have a period");
        
        timer.scheduleAtFixedRate(tt, delay.getValue(), period_.getValue());
        nextRuns_.setTime(System.currentTimeMillis() + delay.getValue());
      }
    }
    tt_ = tt;
  }
  
  /**
   * Returns whether this timer is schedulable (with an assumed delay
   * if one would be required). If this method returns <code>false</code>
   * then calling {@link #startTimer(LongI)} would throw an exception.
   * 
   * @return <code>true</code> if this timer is in a state where it
   * can be scheduled, <code>false</code> if it cannot.
   */
  public synchronized boolean isSchedulable()
  {
    boolean ret = false;
    
    if (!nextRuns_.isNull())
      ret = true;
    else if (!period_.isNull())
      ret = true;
    
//    if (!nextRuns_.isNull() && delayOrRate_ == FIXED_DELAY)
//      ret = true;
//    else if (!nextRuns_.isNull() && delayOrRate_ == FIXED_RATE && !period_.isNull())
//      ret = true;
//    else if (nextRuns_.isNull() && delayOrRate_ == FIXED_DELAY)
//      ret = true;
//    else if (nextRuns_.isNull() && delayOrRate_ == FIXED_RATE && !period_.isNull())
//      ret = true;
    
    return ret;
  }
  
  /**
   * A timer is null if it is not schedulable.
   */
  public boolean isNull()
  {
    return !isSchedulable();
  }
  
  /**
   * Puts this timer into a non-schedulable state. The implementation
   * cancels any scheduling of this timer and sets the
   * properties <code>nextRuns</code> and <code>period</code>
   * to <code>null</code> and <code>fixedOrDelay</code>
   * to <code>FIXED_RATE</code>.
   * <p>
   * In general, the timer properties should all be considered undefined
   * after calling this method. 
   */
  public synchronized void setNull()
  {
    cancel();
    nextRuns_.setNull();
    period_.setNull();
    delayOrRate_ = FIXED_DELAY;
  }
  
	public Iter createIterator () {return DegenerateIter.i__;}
	
  public boolean isEmpty() { return false; }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  protected void finalize() throws Throwable
	{
    // There must be a reference to us in the process's timer
    // so who knows if we ever get GC?  Anyway...
		cancel();
	}
	  
  /**
   * Fire the timer event to the process that originally created this
   * timer.
   * 
   * @param fireTime the time this method was called. Zero indicates
   * the firing is from a timer schedule and the system time will
   * be (approximately) the same as the value of <code>nextRuns</code>.
   * <p>
   * A non-zero value is the time from a manual fire. This will also
   * be the current system time but is used to indicate that the firing
   * is manual.
   * @throws AnyException
   */
  private synchronized void fire(long fireTime) throws AnyException
  {
  	if (fireTime != 0)
  		lastRan_.setTime(fireTime);
  	else
  	{
  		lastRan_.setTime(System.currentTimeMillis());
  		
  		// Evaluate the time the timer will next fire.
  		nextRunTime();
  	}
  	
    Map args = AbstractComposite.simpleMap();
    args.add(timer__, this);
    if (dispatchToGfx_)
    {
      final AnyFuncHolder.FuncHolder fFunc = func_;
      final Map                      fArgs = args;
      final Transaction              fT    = Globals.process__.getTransaction();
      
      SwingInvoker ss = new SwingInvoker()
      {
        protected void doSwing()
        {
          // Hmmm, must drains-up the use of checked exceptions sometime.
          try
          {
            fFunc.doFunc(fT, fArgs, Globals.process__.getRoot());
          }
          catch(AnyException e)
          {
            throw new RuntimeContainedException(e);
          }
        }
      };
      
      ss.serviceAsync(Globals.process__.getTransaction());
    }
    else
    {
      Event e = new DispatchedEvent(EventConstants.DISPATCHED, func_, args);
      p_.send(e);
    }
    //System.out.println("FIRED " + this);
  }
  
  private void cancelTimer()
  {
    if (tt_ != null)
    {
      tt_.cancel();
      tt_ = null;
    }
    //nextRuns_.setNull();
  }
  
  /**
   * If this timer has a period, calculate the next time it will run 
   * and set this into the property state. If it is a one-shot timer,
   * that is it has no period, the <code>nextRuns</code> property is
   * set to value null and the underlying TimerTask is discarded.
   */
  private void nextRunTime()
  {
  	// Check there is a timer task as well, in case this is a manual firing
  	// of a currently unscheduled timer.
    if (!period_.isNull() && tt_ != null)
    {
      if (delayOrRate_ == FIXED_DELAY)
        nextRuns_.setTime(System.currentTimeMillis() + period_.getValue());
      else
        nextRuns_.setTime(lastRan_.getTime() + period_.getValue());
    }
    else
    {
      nextRuns_.setNull();  // Not scheduled to repeat...
      tt_ = null;           // ...so throw the timer task away
    }
  }
  
  // The only reason for defining the TimerTask derived as a
  // separate (in this case inner) class is because we can't
  // easily extend TimerTask and at the same time make it an
  // Any, because TimerTask is not an interface.
  private class TimerTask extends java.util.TimerTask
  {
    public void run()
    {
      try
      {
      	synchronized (AnyTimerTask.this)
				{
      		// Final check that we have not been cancelled
      		if (isScheduled())
            AnyTimerTask.this.fire(0);
				}
      }
      catch(Exception e)
      {
        // If we get an error then just cancel the timeout.
        // Since we only send to the process i/p channel the
        // only likely error is that the channel is closed.
        AnyTimerTask.this.cancel();
      }
    }
  }
  
  public static class TimerEvent extends SimpleEvent
  {
		private static final long serialVersionUID = 1L;

		private DateI        lastRan_;
    private DateI        nextRuns_;
    private AnyTimerTask tt_;
    
    TimerEvent(DateI        lastRan,
               DateI        nextRuns,
               AnyTimerTask tt,
               Any          eventType,
               Any          context)
    {
      super(eventType, context);
      lastRan_  = lastRan;
      nextRuns_ = nextRuns;
      tt_       = tt;
    }
    
    DateI        getLastRan()  { return lastRan_;  }
    DateI        getNextRuns() { return nextRuns_; }
    AnyTimerTask getTimer()    { return tt_; }
  }
}
 
