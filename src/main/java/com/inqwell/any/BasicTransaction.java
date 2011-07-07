/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/BasicTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

import com.inqwell.any.Process;

/**
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class BasicTransaction extends    AbstractTransaction
														  implements Transaction,
																				 Cloneable
{	
	private Process process_;

	public BasicTransaction() {}
		
	public void start(Any root) throws AnyException {}

	public void join (Map m) throws AnyException
	{
    throw new AnyException("BasicTransaction.join()");
  }

	
	public void copyOnWrite (Map m) throws AnyException
	{
    throw new AnyException("BasicTransaction.copyOnWrite()");
  }

	public void deleteIntent (Map m) throws AnyException
	{
    throw new AnyException("BasicTransaction.deleteIntent()");
  }
	
	public void createIntent (Map m, Any eventData) throws AnyException
	{
    throw new AnyException("BasicTransaction.createIntent()");
  }

	public void resync(Map keyVal, Map cachedObject, Map readObject) throws AnyException
	{
	}

	public void commit() throws AnyException {}
	
	public void abort() {}
	
	public void interrupt() {}

	public Map getTransInstance (Map m) { return m; }
	
	public Process getProcess()
	{
		return process_;
	}
	
	public void setProcess(Process p)
  {
    process_ = p;
  }
	
	public Map getContext() { return process_.getContext(); }

	public Any getContextPath() { return process_.getContextPath(); }
	
  public Map getCurrentStackFrame() { return process_.getCurrentStackFrame(); }

	public Map pushStackFrame() { return process_.pushStackFrame(); }
	
	public Map popStackFrame() { return process_.popStackFrame(); }
	
	public boolean isDegenerate() { return true; }
	
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
