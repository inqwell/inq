/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractProcess.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

/**
 *
 */
public abstract class AbstractProcess extends    InstanceHierarchyMap
                                      implements Process
{
  short   effectivePrivilegeLevel_ = Process.DEFAULT_PRIVILEGE;
  short   realPrivilegeLevel_      = Process.DEFAULT_PRIVILEGE;
  boolean realSet_                 = false;

	private int       lineNumber_;
	private int       columnNumber_;

	private Stack     callStack_ = AbstractComposite.callStack();

	private Any       execUrl_;

  public void setWaitingObject(Any a, long timeout)
  {
    throw new UnsupportedOperationException();
  }

  public Any getSync()
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

  public boolean isAlive()
  {
    throw new UnsupportedOperationException();
  }
  
  public void join()
  {
    join(0);
  }

  public void join(long waitTime)
  {
    throw new UnsupportedOperationException();
  }

  public short getRealPrivilegeLevel()
  {
    return realPrivilegeLevel_;
  }

  public short getEffectivePrivilegeLevel()
  {
    return effectivePrivilegeLevel_;
  }

  public void setRealPrivilegeLevel(short level)
  {
    if (!realSet_)
    {
      realPrivilegeLevel_ = level;
      realSet_            = true;
    }
    else
      throw new AnyRuntimeException("Privilege level is already set");
  }

  public void setEffectivePrivilegeLevel(short level)
  {
    effectivePrivilegeLevel_ = level;
  }

  public ExceptionHandler getExceptionHandler()
  {
    throw new UnsupportedOperationException();
  }

  public void setLineNumber(int line) { lineNumber_   = line; }
  public void setColumn(int col)      { columnNumber_ = col;  }
  public int  getLineNumber()         { return lineNumber_;   }
  public int  getColumn()             { return columnNumber_; }
  public Any  getExecURL()            { return execUrl_;      }
  public void setExecURL(Any execUrl) { execUrl_ = execUrl;   }

  public Stack getCallStack()         { return callStack_;    }

  public Any copyFrom (Any a)
  {
    return this;
  }

	public boolean equals(Object o)
	{
		return this == o;
	}

	public boolean equals(Any a)
	{
		return this == a;
	}

	public int hashCode()
	{
		return System.identityHashCode(this);
	}

	public Object clone() throws CloneNotSupportedException
	{
		return this;
  }

  public String toString()
  {
  	StringBuffer s = new StringBuffer();
    s.append("{");
    // We need this synchronized block to prevent ConcurrentModificationExceptions
    // occurring when child processes come and go, should anyone call toString().
    synchronized(this)
    {
      Iter i = createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Any v = this.get(k);
        if (v instanceof Process)
          continue;
  
        s.append(k.toString());
        s.append("=");
        s.append(v.toString());
        if (i.hasNext())
          s.append(", ");
      }
    }
    s.append("}");
    return s.toString();
  }

  protected void reset()
  {
    effectivePrivilegeLevel_ = Process.DEFAULT_PRIVILEGE;
    realPrivilegeLevel_      = Process.DEFAULT_PRIVILEGE;

    realSet_ = false;
  }

  public boolean isRealSet()
  {
    return realSet_;
  }
}
