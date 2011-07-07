/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any.jms;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyObject;
import com.inqwell.any.BasicProcess;
import com.inqwell.any.Call;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Process;
import com.inqwell.any.Transaction;

public class MqSetMessageListener extends AbstractFunc implements Cloneable
{
  private Any f_;
  private Any cons_;
  
  public MqSetMessageListener(Any session, Any f)
  {
    cons_ = session;
    f_ = f;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    MessageConsumerI mc = (MessageConsumerI)EvalExpr.evalFunc(t,
                                                              a,
                                                              cons_,
                                                              MessageConsumerI.class);

    AnyFuncHolder.FuncHolder f = (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc
                                      (t,
                                       a,
                                       f_,
                                       AnyFuncHolder.FuncHolder.class);
    
    if (mc == null)
      nullOperand(cons_);
    
    if (f == null)
      nullOperand(f_);
    
    if (AnyNull.isNullInstance(f))
      mc.setMessageListener(null);
    else
    {
      Call c = verifyCall(f, true);
      AnyMessageListener l = new AnyMessageListener(c, t.getProcess());
      mc.setMessageListener(l);
      
      // Assuming we get here (i.e. there wasn't an exception)
      // force syncext on our process, just in case script
      // did not do so.
      Process p = getTransaction().getProcess();
      if (!p.contains(Process.sync__) && p instanceof BasicProcess)
      {
        BasicProcess bp = (BasicProcess)p;
        bp.setSync(new AnyObject());
      }
    }
    
    return null;
  }

  public Object clone () throws CloneNotSupportedException
  {
    MqSetMessageListener s = (MqSetMessageListener)super.clone();
    
    s.f_    = f_.cloneAny();
    s.cons_ = cons_.cloneAny();
   
    return s;
  }
}
