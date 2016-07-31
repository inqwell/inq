/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractFunc.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.io.IOException;
import java.io.ObjectInputStream;


/**
 * Provides default implementations for the methods defined
 * in AnyFunc.
 */
public abstract class AbstractFunc implements NamedFunc
{
	private Transaction transaction_ = Transaction.NULL_TRANSACTION;
	
	private int lineNumber_    = -1;
	private int columnNumber_  = -1;

  public static boolean debugTxn__ = false;
  
  // Any must be a Call statement or a func holder and that contains a
  // call statement.
  public static Call verifyCall(Any a)
  {
    if (a instanceof Call)
      return (Call)a;

    if (!(a instanceof AnyFuncHolder.FuncHolder))
      throw new IllegalArgumentException("Not a function");
    
    AnyFuncHolder.FuncHolder func = (AnyFuncHolder.FuncHolder)a;
    Func f = func.getFunc();
    if (!(f instanceof Call))
      throw new IllegalArgumentException("Not a Call statement");
    
    return (Call)f;
  }
  
  public static Call verifyCall(Any a, boolean clone)
  {
    Call c = verifyCall(a);
    if (clone)
      c = (Call)c.cloneAny();
    
    return c;
  }
  
  public final void setTransaction(Transaction t)
  {
    if (t == null)
      t = Transaction.NULL_TRANSACTION;
      
    if (AbstractFunc.debugTxn__ &&
        transaction_ != Transaction.NULL_TRANSACTION &&
        t != Transaction.NULL_TRANSACTION &&
        transaction_ != t)
      throw new AnyRuntimeException("Illegal func state");

    
		transaction_ = t;
		int line = getLineNumber();
		int col  = getColumn();
		if (line >= 0)
		{
			t.setLineNumber(line);
			t.setColumn(col);
		}
	}
	
  public Transaction getTransaction()
  {
		return transaction_;
	}
	
  public Any getFQName()
  {
  	return null;
  }
  
  public void setFQName(Any fqName)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setBaseURL(Any url)
  {
    throw new UnsupportedOperationException();
  }

  public Any getBaseURL()
  {
    return null;
  }

  public Any doTransactionHandling (Any root, Any a) throws AnyException
  {
	  if (a.isTransactional())
	    a = getTransaction().getTransInstance((Map)a);
	
	  return a;
  }

  public Map buildArgs()
  {
		return null;
	}
  
  public final Any execFunc (Any a) throws AnyException
  {
    Process p = getTransaction().getProcess();
    if (p != null && p.killed())
      throw new ProcessKilledException(new InterruptedException());

    beforeExecute(a);
    Any ret = exec(a);
    return afterExecute(ret, a);
  }
  
  public Map buildArgs(Map m)
  {
		return null;
  }
  
  public void setParam(Any a)
  {
  }
  
  public Any getParam()
  {
    return null;
  }
  
  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitFunc (this);
  }

  public boolean like(Any a)
  {
    return false;
  }
  
  public Any copyFrom (Any a)
  {
    throw new IllegalArgumentException
      ("copyFrom: not supported for " + getClass().getName());
  }

  public final Any cloneAny ()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw (new IllegalArgumentException ("cloneAny exception: " +
                                           getClass().getName()));
    }
    return a;
  }

  public boolean equals(Object o)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(o))
			return true;

    if (o instanceof Any)
      return equals ((Any)o);
    return false;
  }

  public boolean equals(Any a)
  {
		return this == a;
  }
  
  public boolean isTransactional()
  {
		return false;
  }

  public boolean isConst()
  {
    return false;
  }
  
  public Any bestowConstness()
  {
    return this;
  }
  
  public Any buildNew (Any a)
  {
    throw new IllegalArgumentException
      ("buildNew: not supported for " + getClass().getName());
  }

  public void setLineNumber(int line) { lineNumber_   = line; }
  public void setColumn(int col)      { columnNumber_ = col;  }
  public int  getLineNumber()         { return lineNumber_;   }
  public int  getColumn()             { return columnNumber_; }

  protected void beforeExecute(Any a)
  {
    getTransaction().resetResolving();
  }
  
  protected Any afterExecute(Any ret, Any a)
  {
    getTransaction().resetResolving();
    return ret;
  }
  
	protected void nullOperand() throws AnyException
	{
		nullOperand((Any)null);
	}
	
	protected void nullOperand(String auxInfo) throws AnyException
	{
		throw new AnyException("Null operand " +
		                       ((auxInfo != null) ? auxInfo : "") +
		                       " from class " + getClass().toString());
	}
  
  protected void nullOperand(Any operand)
  {
  	Transaction t = getTransaction();
  	
    if (!t.getCallStack().isEmpty())
    {
      Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
      se.setLineNumber(t.getLineNumber());
      throw new AnyRuntimeException("Operand " + operand + " could not be resolved at " + se.toString());
    }
    throw new AnyRuntimeException("Operand " + operand + " could not be resolved");
  }
  
  protected void operandError(Any operand, String auxInfo) throws AnyException
  {
    throw new AnyException(operand.toString() + ": " + auxInfo);
  }
  
  private void readObject(ObjectInputStream instr) throws IOException,
                                                          ClassNotFoundException
  {
    instr.defaultReadObject();
    
    // Ensure instances serialized in have the local NULL_TRANSACTION
    // object or we get an Illegal func state exception thrown
    // from setTransaction
    transaction_ = Transaction.NULL_TRANSACTION;
  }

}
