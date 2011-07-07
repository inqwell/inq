/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.io.PrintStream;
import java.io.PrintWriter;

public class AnyRuntimeException extends    RuntimeException
																 implements ExceptionI,
																						Cloneable
{
	private static final long serialVersionUID = 1L;
	
	private static Any name__ = AbstractValue.flyweightString(AnyRuntimeException.class.getSimpleName());

	private Any callStack_;

  private Any                      userInfo_;
  private AnyFuncHolder.FuncHolder handler_;
  private boolean                  isUser_;
  private boolean                  isCommit_;
  private DateI                    time_;
  
  private int                      line_;

  /**
   * Create a non-user and unhandled exception with no message
   */
  public AnyRuntimeException ()
  {
    super();
    initTime();
  }

  /**
   * Create a non-user and unhandled exception with the
   * given message.
   */
  public AnyRuntimeException (String s)
  {
    super(s);
    initTime();
  }

  public AnyRuntimeException(String s,
                             Any    userInfo)
  {
    this(s, userInfo, null, false);
  }
  
  /**
   * Create a non-user exception that will be handled by the
   * specified function and carries the given user information.
   */
  public AnyRuntimeException(String                   s,
                             Any                      userInfo,
                             AnyFuncHolder.FuncHolder handler)
  {
    this(s, userInfo, handler, false);
  }
  
  /**
   * Create a user or non-user exception that will be handled
   * by the specified function and carries the given user information.
   */
  public AnyRuntimeException(String                   s,
                             Any                      userInfo,
                             AnyFuncHolder.FuncHolder handler,
                             boolean                  isUser)
  {
    super(s);
    isUser_   = isUser;
    handler_  = handler;
    userInfo_ = userInfo;
    initTime();
  }
  
  public void setHandler(AnyFuncHolder.FuncHolder handler)
  {
    handler_ = handler;
  }
  
  public AnyFuncHolder.FuncHolder getHandler()
  {
    return handler_;
  }
  
  public Any getUserInfo()
  {
    return userInfo_;
  }
  
  public boolean isUser()
  {
    return isUser_;
  }
  
  public boolean isChecked()
  {
    return false;
  }
  
  public void setCommit(boolean commit)
  {
    isCommit_ = commit;
  }
  
  public boolean isCommit()
  {
    return isCommit_;
  }
  
  public Any getExceptionMessage()
  {
  	return new AnyString(getMessage());
  }
  
  public String getMessage()
  {
    String s = super.getMessage();
    
//    if (userInfo_ != null)
//      s += " " + userInfo_.toString();
    
    return s;
  }

  public void setLineNumber(int line)
  {
    line_ = line;
  }
  
  public int getLineNumber()
  {
    return line_;
  }

  /**
   * Return the time this exception was generated
   */
  public DateI getTime()
  {
    return time_;
  }
  
  public Any getExceptionName()
  {
    return name__;
  }
  
  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitUnknown(this);
  }

  public Any copyFrom (Any a)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any buildNew (Any a)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isConst()
  {
    return false;
  }
    
  public Any bestowConstness()
  {
    return this;
  }
  
  public boolean like (Any a)
  {
    return false;
  }

  public boolean isTransactional()
  {
		return false;
  }

  public Any fillInCallStack(Transaction t)
  {
    if (!t.getCallStack().isEmpty() && callStack_ == null)
    {
			Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
			se.setLineNumber(t.getLineNumber());
    }
    
    if (callStack_ == null)
      callStack_  = new ConstString(t.getCallStack().toString());
    
    return callStack_;
  }
  
  public Any topOfStack(Transaction t)
  {
    if (!t.getCallStack().isEmpty() && callStack_ == null)
    {
      Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
      se.setLineNumber(t.getLineNumber());
      callStack_  = new ConstString(se.toString());
    }
    
    if (callStack_ == null)
      return AnyString.EMPTY;
    
    return callStack_;
  }

  public Any getCallStack()
  {
    return callStack_;
  }

  public void printStackTrace()
  {
    if (!isUser())
    	super.printStackTrace();
  	printInqStackTrace();
  }
  
  public void printStackTrace(PrintStream s)
  {
    if (!isUser())
  	  super.printStackTrace(s);
  	printInqStackTrace(s);
  }
  
  public void printStackTrace(PrintWriter s)
  {
    if (!isUser())
    	super.printStackTrace(s);
  	printInqStackTrace(s);
  }
           
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
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

  protected void printInqStackTrace()
  {
  	printInqStackTrace(System.err);
  }
  
  protected void printInqStackTrace(PrintStream s)
  {
  	if (callStack_ != null)
  	{
    	s.println(callStack_);
  	}
    //else
      //s.println("Inq call stack not available");
  }
  
  protected void printInqStackTrace(PrintWriter s)
  {
  	if (callStack_ != null)
  	{
    	s.println(callStack_);
  	}
    //else
      //s.println("Inq call stack not available");
  }
  
  private void initTime()
  {
    time_ = new ConstDate();
  }
}
