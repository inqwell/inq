/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.server;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Map;
import com.inqwell.any.Transaction;

/**
 * If the argument is being modified in the transaction then
 * return truee, otherwise return false.
 */
public class IsModifying extends    AbstractFunc
                        implements Cloneable
{
  private Any modifying_;
  
  public IsModifying(Any modifying)
  {
    modifying_  = modifying;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Map modifying = (Map)EvalExpr.evalFunc(t,
                                           a,
                                           modifying_,
                                           Map.class);

    if (modifying == null)
      nullOperand(modifying_);

    // When map is in the transaction get the public copy.
    if (t.getResolving() == Transaction.R_MAP)
      modifying = t.getLastTMap();
    
    Any ret = AnyBoolean.FALSE;
    
    if (getTransaction().isModifying(modifying))
      ret = AnyBoolean.TRUE;
    
    return ret;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    IsModifying c = (IsModifying)super.clone();
    
    c.modifying_  = modifying_.cloneAny();
    
    return c;
  }
}
