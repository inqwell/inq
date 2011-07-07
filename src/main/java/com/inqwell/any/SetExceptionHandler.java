/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 * @version  $
 * @see 
 */

package com.inqwell.any;

/**
 * Set the exception handler expression. Note that presently the only
 * ExceptionHandler implementation that supports this method is ExceptionToFunc
 * and that is only used by spawned processes in the server.
 * 
 * @author Tom
 * 
 */
public class SetExceptionHandler extends AbstractFunc implements Cloneable
{
  private Any f_;
  
  public SetExceptionHandler(Any f)
  {
    f_ = f;
  }
  
  public Any exec(Any a) throws AnyException
  {
    AnyFuncHolder.FuncHolder f = (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc
                                      (getTransaction(),
                                       a,
                                       f_,
                                       AnyFuncHolder.FuncHolder.class);
    
    ExceptionHandler eh = getTransaction().getProcess().getExceptionHandler();
    
    eh.setDefaultFunc(f);
    
    return null;
  }

  public Object clone () throws CloneNotSupportedException
  {
    SetExceptionHandler s = (SetExceptionHandler)super.clone();
    
    s.f_ = f_.cloneAny();
    
    return s;
  }
}
