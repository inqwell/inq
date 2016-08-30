/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NullTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-05-07 16:53:31 $
 */
 
package com.inqwell.any;

import com.inqwell.any.jms.SessionI;

//import com.inqwell.any.transaction.TransactionConstants;

/**
 * A dummy implementation where no transaction handling is required.
 * @author $Author: sanderst $
 * @version $Revision: 1.7 $
 * @see com.inqwell.any.Any
 */ 
public class NullTransaction extends    AbstractAny
														 implements Transaction,
																				Cloneable
{
	private Process process_;
	
	public NullTransaction() {}
	
	public NullTransaction(Process p) { process_ = p; }
	
	public void start(Any root) throws AnyException {}

	public void join (Map m) throws AnyException {}
	
  public boolean isActive() { return false; }
  
	public void copyOnWrite (Map m) throws AnyException {}

  public void fieldChanging(Map m, Any f, Any info) {}
  
	public void deleteIntent (Map m) throws AnyException {}
	
	public void resync(Map keyVal, Map cachedObject, Map readObject) throws AnyException
	{
	}
	
	public void createIntent (Map m, Any eventData) throws AnyException {}

	public boolean canCommit() throws AnyException { return true; }
	
	public void commit() throws AnyException {}
	
	public void abort() {}
	
	public void interrupt() {}

  public void exportEvents(Array eventBudle, Transaction t)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isAutoCommit() { return true; }

  
  public void addEvent(Event e)
  {
    //throw new UnsupportedOperationException();
  }
  
  public void setGatherEvents(boolean gather)
  {
    //throw new UnsupportedOperationException();
  }
  
  public boolean isGatheringEvents()
  {
    return false;
  }
  
  public boolean lock(Any a, long timeout) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public void unlock(Any a) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public void purgeKey(KeyDef kd)
  {
    throw new UnsupportedOperationException();
  }
  
	public void addAction(Func f, int when) {}
	
	public Map getTransInstance (Map m) { return m; }
	
	public void export(Map m, Transaction t) throws AnyException {}
  
	public Process getProcess()
	{
		return process_;
	}
	public void setProcess(Process p) { process_ = p; }
	
  public Transaction getParent()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setParent(Transaction t) {}
  
  public void setChild(Transaction t) {}

  public Map getContext() { return (process_ != null) ? process_.getContext() : null; }

	public Any getContextPath() { return (process_ != null) ? process_.getContextPath() : null; }
	
	public Map getCurrentStackFrame() { return process_.getCurrentStackFrame(); }

	public Map pushStackFrame() { return process_.pushStackFrame(); }
	
	public Map popStackFrame() { return process_.popStackFrame(); }
	
	public Iter setIter(Iter i) { return null; }
	
	public Iter getIter() { return null; }
	
  public Any setLoop(Any loop)
  {
    return null;
  }

  public Any getLoop()
  {
    return null;
  }
  
  public void setNotifyOld(Descriptor d, boolean notify)
  {
  }

  public void setMqSession(SessionI session)
  {
  }
  
  public void setMqCommit(boolean commit)
  {
  }

  public void mqDirty(boolean dirty)
  {
  }

  public boolean isMqDirty()
  {
    return false;
  }

  public SessionI getMqSession()
  {
    return null;
  }
  
  public Any getTemporary(Any a)
  {
    return null;
  }
  
  public Any readProperty(Any a)
  {
    return null;
  }
  
	public boolean isDegenerate() { return true; }
  
  public Any acquireResource(Any               spec,
                             ResourceAllocator allocator,
                             long              timeout) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isDeleteMarked(Map m)
  {
    throw new UnsupportedOperationException();
  }
  
  public Map isCreateMarked(Map m) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isModifying(Map m) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public Map getCreateList(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
  public Map getModifyList(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
  public Map getDeleteList(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

  public void setLineNumber(int line) {  }
  public void setColumn(int col)      {  }
  public int  getLineNumber()         { return -1; }
  public int  getColumn()             { return -1; }
  
  public void setIdentity(Descriptor d, Func f) {}

  public Any  getExecURL()            { return null;      }
  public void setExecURL(Any execUrl) { }
  
  public void setExecFQName(Any execFQName) {}
  public Any  getExecFQName() { return null; }

  public Stack getCallStack()         { return null;      }

  public void checkPrivilege(Any access, Map node, Any key)
  {
    //throw new AnyRuntimeException("No process");
  }

  public void setResolving(int resolving)
  {
  }
  
  public int getResolving()
  {
    return R_NOTHING;
  }

  public void setLastTMap(Map m)
  {
  }
  
  public void setLastTField(Any a)
  {
  }

  public void resetResolving()
  {
  }
  
  public Map getLastTMap()
  {
    return null;
  }
  
  public Any getLastTField()
  {
    return null;
  }
}
