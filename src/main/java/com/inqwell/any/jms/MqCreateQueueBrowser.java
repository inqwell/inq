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
package com.inqwell.any.jms;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a queue browser from the given session, queue and
 * optional selector arguments
 * 
 * @author tom
 */
public class MqCreateQueueBrowser extends    AbstractFunc
                                  implements Cloneable
{

  private Any   sess_;
  private Any   q_;
  private Any   sel_;

  public MqCreateQueueBrowser(Any sess, Any q, Any sel)
  {
    sess_ = sess;
    q_    = q;
    sel_  = sel;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    SessionI sess = (SessionI)EvalExpr.evalFunc(t,
                                                a,
                                                sess_,
                                                SessionI.class);

    if (sess == null)
      nullOperand(sess_);
    
    QueueI q   = (QueueI)EvalExpr.evalFunc(t,
                                           a,
                                           q_,
                                           QueueI.class);

    if (q == null)
      nullOperand(q_);

    Any sel   = EvalExpr.evalFunc(t,
                                  a,
                                  sel_);

    if (sel == null && sel_ != null)
      nullOperand(sel_);
    if (AnyNull.isNullInstance(sel))
      sel = null;
    
    return sess.createBrowser(q, sel);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateQueueBrowser n = (MqCreateQueueBrowser)super.clone();
    
    n.sess_   = sess_.cloneAny();
    n.q_   = q_.cloneAny();
        
    return n;
  }
}
