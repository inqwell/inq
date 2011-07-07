/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/WaitReady.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * A utility class that supports resource state and synchronisation.
 * This class may be used to track whether a resource is busy,
 * to wait for that resource to become idle and signify whether
 * a resource is ready for use.  In addition, a defunct state
 * can be entered, when the resource will never become available.
 * <p>
 */
public class WaitReady extends AbstractAny
{
  private Any     name_;

  private volatile boolean ready_   = true;
  private volatile int     inUse_   = 0;
  private boolean defunct_ = false;
  
  
  public WaitReady(Any name)
  {
    name_ = name;
  }

  public synchronized void startUse()
  {
    startUse(true);
  }
  
	public synchronized void startUse(boolean checkDefunct)
	{
    if (checkDefunct && isDefunct())
      throw new AnyRuntimeException("Resource " + name_ + " is defunct");
      
    waitReady();
    inUse_++;
	}
	
	public synchronized void endUse()
	{
    inUse_--;
    notifyAll();
	}
	
	// Wait for the resource to be ready
	public synchronized void waitReady()
	{
    while(true)
    {
      if (ready_)
        return;
      
      try
      {
        wait();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
	}
	
  /**
   * Wait for this resource to become idle and then clear its ready status
   */
	public synchronized void waitIdle()
	{
    while(true)
    {
      if (inUse_ == 0)
      {
        ready_ = false;
        return;
      }
      try
      {
        wait();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
  
  public synchronized void signalReady()
  {
    if (ready_)
      throw new IllegalStateException("Resource " + name_ + " is currently ready");
      
    ready_ = true;
    notifyAll();
  }
  
  public boolean isDefunct()
  {
    return defunct_;
  }
  
  /**
   * A one-way operation that sets the defunct state.  Once set,
   * other operations generate exceptions
   */
  public void setDefunct()
  {
    defunct_ = true;
  }
}
