/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Throw.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Throw an exception.  The <code>Throw</code> function can be
 * used to throw an exception.  Unless caught at a higher level
 * in an Inq script the exeception will be processed by the
 * process's top level exception handler.  In a server environment
 * this means propagating the exception to the peer client
 * process.
 * <p>
 * <code>Throw</code> may be supplied with no arguments.
 * To be valid in this case, the throw tag must appear
 * in the body of the second operand to a <code>try</code>
 * expression (the catch block).  If no exception is available on
 * the call stack, then Throw itself throws an exception.
 * <p>
 * If supplied with a single argument, the argument is placed
 * as the message of a new exception that is thrown.
 * 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Throw extends    AbstractFunc
									 implements Cloneable
{
  private Any    message_;
  private Any    userInfo_;
  private Any    handler_;
  
	public Throw()
	{
    this(null, null, null);
	}
	
	public Throw(Any message)
	{
    this(message, null, null);
	}
	
	public Throw(Any message, Any userInfo)
	{
    this(message, userInfo, null);
	}
	
	public Throw(Any message, Any userInfo, Any handler)
  {
    message_  = message;
    userInfo_ = userInfo;
    handler_  = handler;
  }
  
	public Any exec(Any a) throws AnyException
	{
		Any message =  EvalExpr.evalFunc(getTransaction(),
                                     a,
                                     message_);

    if (message == null && message_ != null)
      nullOperand(message_);
    
		Any userInfo = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    userInfo_);
    
    if (userInfo == null && userInfo_ != null)
      nullOperand(userInfo_);
    
		AnyFuncHolder.FuncHolder handler = (AnyFuncHolder.FuncHolder)
                  EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    handler_,
                                    AnyFuncHolder.FuncHolder.class);
                                    
    if (handler == null && handler_ != null)
      nullOperand(handler_);
    if (handler != null && handler.isNull())
      handler = null;
    
    if (message == null)
    {
      Map stackFrame = getTransaction().getCurrentStackFrame();
      if (stackFrame.contains(NodeSpecification.atException__))
      {
        // Rethrowing a caught exception
        Any ex = stackFrame.get(NodeSpecification.atException__);
        if (ex instanceof AnyException)
        {
          AnyException aex = (AnyException)ex;
          getTransaction().setLineNumber(aex.getLineNumber());
          throw aex;
        }
        else
        {
          AnyRuntimeException arex = (AnyRuntimeException)ex;
          getTransaction().setLineNumber(arex.getLineNumber());
          throw arex;
        }
      }
      else
        throw new AnyException("No current exception");
    }
    else
    {
      throw new AnyException(message.toString(),
                             userInfo,
                             handler,
                             true);
    }
		// return a;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    Throw t = (Throw)super.clone();
    t.message_  = AbstractAny.cloneOrNull(message_);
    t.userInfo_ = AbstractAny.cloneOrNull(userInfo_);
    t.handler_  = AbstractAny.cloneOrNull(handler_);
    return t;
  }
}
