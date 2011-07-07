/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Exceptions defined in the Any framework are, themselves, Anys allowing
 * them to be collected, transmitted through channels and so forth.
 */
public class AnyException extends    Exception
													implements ExceptionI,
																		 Cloneable
{
  private static final long serialVersionUID = 1L;

  private static Any name__ = AbstractValue.flyweightString(AnyException.class.getSimpleName());

  private Any callStack_;
  
  private Any                      userInfo_;
  private AnyFuncHolder.FuncHolder handler_;
  private boolean                  isUser_;
  private boolean                  isCommit_;
  private ConstDate                time_;
  
  private int                      line_;
  
	/**
	 * Convenience function to throw an exception caught from outside
	 * the Any framework.  The exception is wrapped inside an
	 * com.inqwell.any.ContainedException instance, which forwards public methods
	 * to the java.lang.Exception delegate.
	 */
	public static void throwExternalException (Exception e) throws AnyException
	{
		throw (new ContainedException(e));
	}
	
  /*
	public static AnyException wrapExternalException (Exception e)
	{
		return new ContainedException(e);
	}
	*/
  
  /**
   * Create a non-user and unhandled exception with no message
   */
  public AnyException ()
  {
    super();
    initTime();
  }
  
  /**
   * Create a non-user and unhandled exception with the
   * given message.
   */
  public AnyException (String s)
  {
    super(s);
    initTime();
  }

  /**
   * Create a non-user exception that will be handled by the
   * specified function and carries the given user information.
   */
  public AnyException(String                   s,
                      Any                      userInfo,
                      AnyFuncHolder.FuncHolder handler)
  {
    this(s, userInfo, handler, false);
  }
  
  /**
   * Create a user or non-user exception that will be handled
   * by the specified function and carries the given user information.
   */
  public AnyException(String                   s,
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

  public boolean isChecked()
  {
    return true;
  }
  
  public void setCommit(boolean commit)
  {
    isCommit_ = commit;
  }
  
  public boolean isCommit()
  {
    return isCommit_;
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

  public boolean like (Any a)
  {
    return false;
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
