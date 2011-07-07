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
 */

package com.inqwell.any;

public class Reverse extends AbstractFunc implements Cloneable
{
  private Any any_;
  
  public Reverse(Any any)
  {
    any_ = any;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Vectored any = (Vectored)EvalExpr.evalFunc(getTransaction(),
                                               a,
                                               any_,
                                               Vectored.class);

    if (any == null)
      nullOperand(any_);
    
    any.reverse();
    
    return any;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Reverse r = (Reverse)super.clone();
    r.any_ = AbstractAny.cloneOrNull(any_);
    return r;
  }

}
