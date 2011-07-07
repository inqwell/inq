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
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a queue from the given session and
 * queue name arguments
 * @author tom
 *
 */
public class MqCreateQueue extends    AbstractFunc
                           implements Cloneable
{

  private Any   sess_;
  private Any   qname_;

  public MqCreateQueue(Any sess, Any qname)
  {
    sess_     = sess;
    qname_      = qname;
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
    
    Any qname   = EvalExpr.evalFunc(t,
                                    a,
                                    qname_);

    if (qname == null)
      nullOperand(qname_);

    return sess.createQueue(qname);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateQueue n = (MqCreateQueue)super.clone();
    
    n.sess_   = sess_.cloneAny();
    n.qname_   = qname_.cloneAny();
        
    return n;
  }
}
