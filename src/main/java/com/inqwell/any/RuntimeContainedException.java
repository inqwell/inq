/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import com.inqwell.any.channel.RuntimeMasqueradeException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Runtime version of <code>ContainedException</code>.
 * @see com.inqwell.any.ContainedException
 */
public class RuntimeContainedException extends    AnyRuntimeException
																			 implements ExceptionContainer
{
	public  Throwable throwable_;

	public RuntimeContainedException (Throwable t)
  {
    throwable_ = t;
    inheritHandler();
  }

  public RuntimeContainedException (Throwable t, String message)
  {
    super(message);
    throwable_ = t;
    inheritHandler();
  }

  public RuntimeContainedException (Throwable t,
                                    String    message,
                                    Any       userInfo)
  {
    super(message, userInfo);
    throwable_ = t;
    inheritHandler();
  }

  public RuntimeContainedException (Throwable                t,
                                    String                   message,
                                    Any                      userInfo,
                                    AnyFuncHolder.FuncHolder handler)
  {
    super(message, userInfo, handler);
    throwable_ = t;
    inheritHandler();
  }

  public RuntimeContainedException (Throwable                t,
                                    String                   message,
                                    Any                      userInfo,
                                    AnyFuncHolder.FuncHolder handler,
                                    boolean                  isUser)
  {
    super(message, userInfo, handler, isUser);
    throwable_ = t;
    inheritHandler();
  }

  public String getMessage()
  {
    String s = null;
    
    if (throwable_ != null)
      s = throwable_.getMessage();

    if (s != null && super.getMessage() != null)
      s = super.getMessage() + ": " + s;
    else if (super.getMessage() != null)
      s = super.getMessage();

    return s;
  }

  public Any getExceptionName()
  {
    if (throwable_ instanceof ExceptionI)
      return ((ExceptionI)throwable_).getExceptionName();
    
    return AbstractValue.flyweightString(throwable_.getClass().getSimpleName());
  }

  public Any getUserInfo()
  {
    if (throwable_ instanceof ExceptionI)
    {
      ExceptionI e = (ExceptionI)throwable_;
      return e.getUserInfo();
    }
    
    return super.getUserInfo();
  }
  
  public boolean isUser()
  {
    if (throwable_ instanceof ExceptionI)
    {
      ExceptionI e = (ExceptionI)throwable_;
      return e.isUser();
    }
    
    return super.isUser();
  }
  
	public void printStackTrace()
  {
    if (super.getMessage() != null)
      System.err.println(super.getMessage());

    if (throwable_ != null)
      throwable_.printStackTrace();
    printInqStackTrace();
  }

	public void printStackTrace(PrintStream s)
  {
    if (super.getMessage() != null)
      s.println(super.getMessage());

    if (throwable_ != null)
      throwable_.printStackTrace(s);
      
    printInqStackTrace(s);
  }

	public void printStackTrace(PrintWriter s)
  {
    if (super.getMessage() != null)
      s.println(super.getMessage());

    if (throwable_ != null)
      throwable_.printStackTrace(s);
      
    printInqStackTrace(s);
  }

  public String toString()
  {
    String s = null;
    
    if (throwable_ != null)
      s = throwable_.toString();

    if (super.getMessage() != null)
      if (s != null)
        s += super.getMessage();
      else
        s = super.getMessage();

    return s;
  }

	public Object clone() throws CloneNotSupportedException { return super.clone(); }
  
  public Throwable getThrowable()
  {
    if (throwable_ instanceof ExceptionContainer)
    {
      ExceptionContainer ec = (ExceptionContainer)throwable_;
      return ec.getThrowable();
    }
    return throwable_;
  }

	public Any collapseException()
	{
		return new RuntimeMasqueradeException(this, getUserInfo(), getHandler(), isUser());
	}
  
  public void setThrowable(Throwable t)
  {
    throwable_ = t;
  }
  
  private void inheritHandler()
  {
    if (throwable_ instanceof ExceptionI)
    {
      ExceptionI ei = (ExceptionI)throwable_;
      this.setHandler(ei.getHandler());
      ei.setHandler(null);
    }
  }
}
