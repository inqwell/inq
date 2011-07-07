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
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.jms;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.BooleanI;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Establish whether the current transaction will commit any transactional
 * messaging session there is. A messaging session will be committed
 * by the root transaction at all times, but may be committed by a nested
 * transaction if so established using this function.
 * <p/>
 * 
 * @author tom
 *
 */
public class MqSetCommit extends    AbstractFunc
                         implements Cloneable
{
  private Any   setCommit_;

  public MqSetCommit(Any setCommit)
  {
    setCommit_ = setCommit;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    BooleanI setCommit = (BooleanI)EvalExpr.evalFunc(t,
                                a,
                                setCommit_);

    if (setCommit == null)
      nullOperand(setCommit_);
    
    t.setMqCommit(setCommit.getValue());

    return null;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqSetCommit m = (MqSetCommit)super.clone();
    
    m.setCommit_    = setCommit_.cloneAny();

    return m;
  }
}
