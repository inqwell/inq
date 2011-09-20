/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/DeadlockScanner.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.inqwell.any.AbstractProcess;
import com.inqwell.any.AddTo;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyTimer;
import com.inqwell.any.Array;
import com.inqwell.any.ConstDate;
import com.inqwell.any.ConstString;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.PermissionException;
import com.inqwell.any.Process;
import com.inqwell.any.RemoveFrom;
import com.inqwell.any.StackUnderflowException;
import com.inqwell.any.Transaction;

/**
 * Start a new thread
 * and wait at a specified server socket port.  Execute the given
 * function 
 */
public class DeadlockScanner extends    AbstractProcess
										         implements Process,
										                    Runnable
{
  private static Logger logger = Logger.getLogger("inq");
  
	private Thread           thread_;
	
	private boolean          killed_;
	
	private Any              catalogPath_;
	
	/**
	 * 
	 */
	public DeadlockScanner()
	{
		init();
	}
	
	public void run()
	{
		// make sure our members are not garbage collected!
		Process p = this;

		logger.log(Level.INFO, "Deadlock scanner");
		
		initInThread();

	  while (!killed_)
	  {
		  try
		  {
		    Thread.sleep(1000);
		    Array processList = Server.instance().scanForDeadlock();
		    if (processList != null && processList.entries() != 0)
		    {
			    Process dp = (Process)processList.get(0);
			    logger.log(Level.INFO, "DeadlockScanner victim: " + dp);
			    dp.deadlockVictim();
			  }
		  }
		  catch (Exception e)
		  {
        logger.log(Level.SEVERE, "DeadlockScanner caught exception", e);
			}
	  }
    logger.log(Level.INFO, "DeadlockScanner terminating......");
    RemoveFrom removeFrom = new RemoveFrom(this);
    removeFrom.setTransaction(getTransaction());
    try
    {
      removeFrom.exec(this);
    }
    catch(AnyException e)
    {
    	e.printStackTrace();
    }
	}

	/**
	 * Send data to this process.  No-operation
	 */
	public void send (Any a) throws AnyException
	{
	}
	
	public void setTransaction(Transaction t)
	{
	}
	
	public Transaction getTransaction()
	{
		return Transaction.NULL_TRANSACTION;
	}
	
	/**
	 * No-operation
	 */
	public void deadlockVictim()
	{
	}
	
	/**
	 * Called by another process <code>p</code> (say, the LockManager) to bump this
	 * process out of a <code>wait()</code> in which it is deadlocked
	 * with one or more competing processes.
	 */
	public void kill(Process p) throws AnyException
	{
		if (!p.isSupervisor())
			throw new PermissionException("Can't kill if not supervisor");

		killed_ = true;
		thread_.interrupt();
	}
	
  public void interrupt()
  {
    throw new UnsupportedOperationException();
  }

	public boolean isSupervisor ()
	{
		return false;
	}
	
  public boolean isAlive()
  {
    if (thread_ == null)
      return false;
    
    return thread_.isAlive();
  }
  
	public void setSupervisor (boolean b) {}
	
	public Map getRoot()
	{
		return null;
	}
	
	public AnyTimer getTimer()
	{
    throw new UnsupportedOperationException();
	}
	
	public Map getContext()
	{
		return null;
	}
	
	public Any getContextPath()
	{
		return null;
	}
	
	public void setContext(Map context)
	{
	}
	
	public void setContextPath(Any contextPath)
	{
	}
	
	public Any getCatalogPath()
	{
		return catalogPath_;
	}
	
	public Map getCurrentStackFrame() throws StackUnderflowException
	{
    throw new UnsupportedOperationException();
	}

	public Map pushStackFrame()
	{
    throw new UnsupportedOperationException();
	}
	
	public Map popStackFrame() throws StackUnderflowException
	{
    throw new UnsupportedOperationException();
	}
	
	public void emptyStack()
	{
    throw new UnsupportedOperationException();
	}
	
	public boolean isAncestor(Process p)
	{
		return false;
	}
	
	public void addChildProcess(Process p, Transaction t)
	{
    throw new UnsupportedOperationException();
	}
	
	public void removeChildProcess(Process p, Transaction t)
	{
    throw new UnsupportedOperationException();
	}
	
  public void startThread()
  {
    if (thread_ == null)
    {
      // off we go
      thread_ = new Thread(this);
      thread_.setDaemon(true);
      thread_.setName("DeadlockScanner");
      thread_.start();
    }
  }

	protected void init() //throws AnyException
	{
		killed_ = false;
    startThread();
	}

 	private void initInThread()
 	{
    try
    {
      this.add(Process.STARTED, new ConstDate());
      catalogPath_ = new ConstString(NodeSpecification.catalog__ +
                                     ".processes.DeadlockScanner");
      NodeSpecification n = new NodeSpecification(catalogPath_.toString());

      AddTo addTo = new AddTo(this, n);
      addTo.exec(null);
    }
    catch (Exception e)
    {
      // bit weak but inlikely to happen!
      e.printStackTrace();
    }
 	}
}
