/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Notify.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Notify one of any processes waiting on the given object.
 * Returns <code>true</code> if a waiting process was notified,
 * throws an exception if no process was waiting.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Notify extends    AbstractFunc
                    implements Cloneable
{
	
	private Any     any_;
  private Any     expression_;
  private boolean all_;
	
	public Notify(Any any, Any expression, boolean all)
	{
    any_        = any;
    expression_ = expression;
    all_        = all;
	}

  public Any exec(Any a) throws AnyException
	{
    boolean notified = false;
    
		Any any = EvalExpr.evalFunc(getTransaction(),
																a,
																any_);
    if (any == null)
      nullOperand(any_);
      
    AnyFuncHolder.FuncHolder expression = (AnyFuncHolder.FuncHolder)
                                              EvalExpr.evalFunc(getTransaction(),
                                                                a,
                                                                expression_);

    if (expression == null && expression_ != null)
      nullOperand(expression_);
    
    // If there is an expression then execute it before doing the notification
    if (expression != null)
      expression.doFunc(getTransaction(), null, a);
      
    notified = Globals.lockManager__.notifyVia(any,
                                               getTransaction().getProcess(),
                                               all_);

// BB questions this.  Can't remember what for so removed for now.
//    if (!notified)
//      throw new AnyException("No process is waiting on " + any);
      
		return new AnyBoolean(notified);
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Notify n = (Notify)super.clone();
    n.any_        = any_.cloneAny();
    n.expression_ = AbstractAny.cloneOrNull(expression_);
		return n;
  }
}
