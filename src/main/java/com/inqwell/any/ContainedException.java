/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


import com.inqwell.any.channel.MasqueradeException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Simply contains one instance of a checked exception allowing base class
 * methods to specify a throws clause.  Derived classes do not actually need
 * to throw anything but if they do they cannot violate the base class's
 * exception specification.  This can cause a problem when the base
 * specification is an interface and there is no knowledge of what
 * kind of exceptions an implementation might end up throwing.
 * <p>
 * Presumably, where the ContainedException is caught the 'real' exception
 * type is known and dealt with.
 */
public class ContainedException extends    AnyException
																implements ExceptionContainer
{
	private static final long serialVersionUID = 1L;

	/**
   * Make a contained exception.  The container (this) has
   * no message itself, so only that of the contained
   * throwable (if any) will appear in stack traces etc.
   */
	public ContainedException (Throwable t) { throwable_ = t; }
  
	public ContainedException (Throwable t, String s)
  {
    super(s);
    throwable_ = t;
  }

  public ContainedException(Throwable                t,
                            String                   s,
                            Any                      userInfo)
  {
    this(t, s, userInfo, null, false);
  }
  
  public ContainedException(Throwable                t,
                            String                   s,
                            Any                      userInfo,
                            AnyFuncHolder.FuncHolder handler)
  {
    this(t, s, userInfo, handler, false);
  }
  
  /**
   * Create a user or non-user exception that will be handled
   * by the specified function and carries the given user information.
   */
  public ContainedException(Throwable                t,
                            String                   s,
                            Any                      userInfo,
                            AnyFuncHolder.FuncHolder handler,
                            boolean                  isUser)
  {
    super(s, userInfo, handler, isUser);
    throwable_ = t;
  }

  public String getMessage()
  {
    String s = throwable_.getMessage();
    
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

  public Throwable getThrowable()
  {
    if (throwable_ instanceof ExceptionContainer)
    {
      ExceptionContainer ec = (ExceptionContainer)throwable_;
      return ec.getThrowable();
    }
    return throwable_;
  }  
  
  public void setThrowable(Throwable t)
  {
    throwable_ = t;
  }
  
  public AnyFuncHolder.FuncHolder getHandler()
  {
    if (throwable_ instanceof ExceptionI)
    {
      ExceptionI e = (ExceptionI)throwable_;
      return e.getHandler();
    }
    
    return super.getHandler();
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
      
    throwable_.printStackTrace();
    printInqStackTrace();
  }
  
	public void printStackTrace(PrintStream s)
  {
    if (super.getMessage() != null)
      s.println(super.getMessage());
      
    throwable_.printStackTrace(s);
    
    // Check for multiple contained?
    printInqStackTrace(s);
  }
  
	public void printStackTrace(PrintWriter s)
  {
    if (super.getMessage() != null)
      s.println(super.getMessage());
      
    throwable_.printStackTrace(s);
    printInqStackTrace(s);
  }

  public String toString()
  {
    String s = throwable_.toString();
    
    if (super.getMessage() != null)
      s += super.getMessage();
      
    return s;
  }
  
	public Object clone() throws CloneNotSupportedException { return super.clone(); }
	
	public Any collapseException()
	{
		return new MasqueradeException(this, getUserInfo(), getHandler(), isUser());
	}
	
	public  Throwable throwable_;
}
