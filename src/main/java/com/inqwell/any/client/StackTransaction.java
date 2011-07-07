/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/StackTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class StackTransaction extends    AbstractTransaction
														  implements Transaction,
																				 Cloneable
{	
	private Map             stackFrame_ = AbstractComposite.simpleMap(); // Our current stack
	private Queue           stackFrames_;

	public StackTransaction() { init(); }
  public StackTransaction(Process p) { setProcess(p); init(); }
		
	public void start(Any root) throws AnyException {}

	public void join (Map m) throws AnyException
	{
    throw new AnyException("StackTransaction.join()");
  }
	
	public void copyOnWrite (Map m) throws AnyException
	{
    throw new AnyException("StackTransaction.copyOnWrite()");
  }

	public void deleteIntent (Map m) throws AnyException
	{
    throw new AnyException("StackTransaction.deleteIntent()");
  }
	
  public Any acquireResource(Any               spec,
                             ResourceAllocator allocator,
                             long              timeout) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

	public void createIntent (Map m, Any eventData) throws AnyException
	{
    throw new AnyException("StackTransaction.createIntent()");
  }

	public void resync(Map keyVal, Map cachedObject, Map readObject) throws AnyException
	{
	}

	public void commit() throws AnyException
  {
    reset();
  }
	
  public boolean isAutoCommit() { return true; }

	public void abort()
  {
    reset();
  }
	
	public void interrupt() {}

	public Map getTransInstance (Map m) { return m; }
	
	public Process getProcess()
	{
		return null;
	}
	
	public void setProcess(Process p) {}
	
	public Map getContext() { return null; }

	public Any getContextPath() { return null; }
	
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
	
	public boolean isDegenerate() { return true; }
	
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
	
	private void init()
	{
		stackFrames_ = AbstractComposite.queue();
	}
}
