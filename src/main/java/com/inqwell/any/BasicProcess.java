/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/BasicProcess.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.ref.AnyWeakReference;

/**
 * Provides stack and transaction support that subclasses can
 * build upon.  This class is not abstract and other methods
 * generally throw an UnsupportedOperationException.
 */
public class BasicProcess extends    AbstractProcess
                          implements Process
{
	private Transaction  transaction_;
  
  private Map          root_;     // All process data is stored under here

	private Map          context_;  // While a process is executing a
                                  // a service the execution context is
                                  // here.
  private Any          contextPath_;

	private Map          stackFrame_ = AbstractComposite.simpleMap(); // Our current stack
	private Queue        stackFrames_;

  // Something to synchronise on if us and our transaction could
  // be used by other threads outside Inq.
  protected Any        sync_;

  public BasicProcess()
  {
    setRoot(AbstractComposite.managedMap());
    initMembers();
  }
  
	public void setTransaction(Transaction t)
	{
		transaction_ = t;
		t.setProcess(this);
	}

	public Map getContext()
	{
		//System.out.println ("Getting Context: " + context_);
		return context_;
	}

	public Any getContextPath()
	{
    return contextPath_;
	}

	public void setContext(Map context)
	{
		//System.out.println ("Setting Context: " + context);
		context_        = context;
	}

	public void setContextPath(Any contextPath)
	{
	  contextPath_ = contextPath;
	}

	public Transaction getTransaction()
	{
		return transaction_;
	}

  // set Sync is accessed via properties from script
  // and effect mutual exclusion in a server environment
  public void setSync(Any sync)
  {
    if (sync == null)
      throw new AnyRuntimeException("sync cannot be null");
    
    if (AnyNull.isNullInstance(sync))
    {
      // Remove the sync item
      if (this.contains(sync__))
        this.remove(sync__);
      sync_ = null;
    }
    else
    {
      if (sync_ == null)
      {
        sync_ = sync;
        this.add(sync__, sync_);
      }
      else
        throw new AnyRuntimeException("sync already set: " + sync_);
    }
  }
  
  public Any getSync()
  {
    return sync_;
  }
  
  public void setWaitingObject(Any a, long timeout)
  {
    throw new UnsupportedOperationException("setWaitingObject");
  }
  
  public Any  getWaitingObject()
  {
    throw new UnsupportedOperationException("getWaitingObject");
  }

  public long getWaitingTimeout()
  {
    throw new UnsupportedOperationException("getWaitingTimeout");
  }

  public void setLockWaitObject(Any a, long timeout)
  {
    throw new UnsupportedOperationException("setLockWaitObject");
  }
  
  public Any  getLockWaitObject()
  {
    throw new UnsupportedOperationException("getLockWaitObject");
  }

  public long getLockWaitTimeout()
  {
    throw new UnsupportedOperationException("getLockWaitTimeout");
  }

  public void notifyUnlock(Any a)
  {
    throw new UnsupportedOperationException("notifyUnlock");
  }

  public void join()
  {
    join(0);
  }
  
  public void join(long waitTime)
  {
    throw new UnsupportedOperationException("join");
  }
  
  public ExceptionHandler getExceptionHandler()
  {
    throw new UnsupportedOperationException("getExceptionHandler");
  }
  
  public Map getRoot()
  {
    return root_;
  }
  
  protected void setRoot(Map root)
  {
    root_ = root;
  }

  protected void initMembers()
 	{
    if (stackFrames_ == null)
      stackFrames_ = AbstractComposite.queue();
 	}

	public Map getCurrentStackFrame() throws StackUnderflowException
	{
		if (stackFrame_ == null)
			throw new StackUnderflowException("No stack frame established");

		return stackFrame_;
	}

	public Map pushStackFrame()
	{
		// We use a simple map in case we pass arguments which have
		// parental restrictions
		Map newStack = AbstractComposite.simpleMap();

		if (stackFrame_ != null)
			stackFrames_.addLast(stackFrame_);

		stackFrame_     = newStack;

		return newStack;
	}

	public Map popStackFrame() throws StackUnderflowException
	{
		if (stackFrame_ == null)
			throw new StackUnderflowException("popStackFrame()");

		if (stackFrames_.isEmpty())
			stackFrame_ = null;
		else
			stackFrame_ = (Map)stackFrames_.removeLast();

		return stackFrame_;
	}

	public void emptyStack()
	{
		stackFrame_     = AbstractComposite.simpleMap();
		stackFrames_.empty();
	}

  public Any getCatalogPath()
	{
    throw new UnsupportedOperationException();
	}

  public AnyTimer getTimer()
	{
    throw new UnsupportedOperationException();
	}

	public boolean isAncestor(Process p)
	{
    throw new UnsupportedOperationException();
	}

	public void removeChildProcess(Process p, Transaction t)
	{
    throw new UnsupportedOperationException();
	}

	public void addChildProcess(Process p, Transaction t)
	{
    throw new UnsupportedOperationException();
	}

	public void setSupervisor (boolean b)
	{
    throw new UnsupportedOperationException();
	}

	public boolean isSupervisor ()
	{
    throw new UnsupportedOperationException();
	}

	public void kill(Process p) throws AnyException
	{
    throw new UnsupportedOperationException();
	}
  
  public void interrupt()
  {
    throw new UnsupportedOperationException();
  }

  public void startThread()
  {
    throw new UnsupportedOperationException();
  }

	public void deadlockVictim()
	{
    throw new UnsupportedOperationException();
	}

	public void send (Any a) throws AnyException
	{
    throw new UnsupportedOperationException();
	}

  /**
   * A specialisation to hold the Process for which an instance of
   * this class is the node-space root. Only used for the node-space
   * root and would be a proper inner class but for chicken/egg
   * as the code now stands.
   */
  public static class RootMap extends InstanceHierarchyMap
  {
    private static final long serialVersionUID = 1L;
    
    private transient AnyWeakReference p_;     // Process
    
    public void setProcess(Process p)
    {
      if (p_ != null)
        throw new IllegalStateException();
      
      p_ = new AnyWeakReference(p);
    }
    
    public Process getProcess()
    {
      return (Process)p_.getAny();
    }

    public Any buildNew(Any a)
    {
      Any ret = AbstractComposite.managedMap();
      if (a != null)
        ret.copyFrom(a);
      return ret;
    }

//    protected  void finalize() throws Throwable
//    {
//      System.out.println("***** Finalised " + getClass() + " " + System.identityHashCode(this));
//      super.finalize();
//    }
  }
}
