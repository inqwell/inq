/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/SerializedProcess.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * An implementation of the <code>Process</code> interface
 * which acts as a replacement for the various process
 * objects in a server when they are placed in a serialization
 * stream.
 * <p>
 * In general, server side <code>Process</code> implementations
 * should not, indeed cannot, be serialized.  However, a
 * representation in a stream allows them to be manipulated 
 */
public class SerializedProcess extends    SimpleMap
										           implements Process
{
	private Any catalogPath_;
	
	public SerializedProcess(Any catalogPath)
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
  
  public boolean killed()
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
	
  public boolean isAlive()
  {
    throw new UnsupportedOperationException();
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
  public void setWaitingObject(Any a, long timeout)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any  getWaitingObject()
  {
    throw new UnsupportedOperationException();
  }

  public long getWaitingTimeout()
  {
    throw new UnsupportedOperationException();
  }

  public void setLockWaitObject(Any a, long timeout)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any  getLockWaitObject()
  {
    throw new UnsupportedOperationException();
  }

  public long getLockWaitTimeout()
  {
    throw new UnsupportedOperationException();
  }

  public void notifyUnlock(Any a)
  {
    throw new UnsupportedOperationException();
  }

  public void join()
  {
    join(-1);
  }
  
  public void join(long waitTime)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setLineNumber(int line)
  {
    throw new UnsupportedOperationException();
  }
  
  public Stack getCallStack()
  {
    throw new UnsupportedOperationException();
  }
  
  public Any getCurrentStack()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setColumn(int col)
  {
    throw new UnsupportedOperationException();
  }
  
  public int  getLineNumber()
  {
    throw new UnsupportedOperationException();
  }
  
  public int  getColumn()
  {
    throw new UnsupportedOperationException();
  }
  
  public Any getSync()
  {
    throw new UnsupportedOperationException();
  }
  
  public Any  getExecURL()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setExecURL(Any execUrl)
  {
    throw new UnsupportedOperationException();
  }
  
  public short getRealPrivilegeLevel()
  {
    throw new UnsupportedOperationException();
  }
  
  public short getEffectivePrivilegeLevel()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setRealPrivilegeLevel(short level)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setEffectivePrivilegeLevel(short level)
  {
    throw new UnsupportedOperationException();
  }

  public ExceptionHandler getExceptionHandler()
  {
    throw new UnsupportedOperationException();
  }

  public boolean isRealSet()
  {
    throw new UnsupportedOperationException();
  }
  
  public void startThread()
  {
    throw new UnsupportedOperationException();
  }
}
