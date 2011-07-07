/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/ProxyProcess.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

/**
 * An implementation of the <code>Process</code> interface
 * which acts as a replacement for the various process
 * objects in a server when they are read from a serialized
 * stream in another JVM.  
 */
public class ProxyProcess extends    AbstractProcess
										      implements Process
{
	private Any catalogPath_;
	
	public ProxyProcess(Any catalogPath)
	{
		catalogPath_ = catalogPath;
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
    throw new UnsupportedOperationException();
	}
	
  public void interrupt()
  {
    throw new UnsupportedOperationException();
  }

	public boolean isSupervisor ()
	{
		return false;
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
	
	public Any getCatalogPath()
	{
		return catalogPath_;
	}
	
	public void setContext(Map context)
	{
	}
	
	public void setContextPath(Any contextPath)
	{
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
    throw new UnsupportedOperationException();
  }
}
