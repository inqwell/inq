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
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * CUnsubscribe from a durable subscription
 * @author tom
 *
 */
public class MqUnsubscribe extends    AbstractFunc
                           implements Cloneable
{

  private Any   sess_;
  private Any   name_;

  public MqUnsubscribe(Any sess, Any name)
  {
    sess_     = sess;
    name_    = name;
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
    
    Any name   = EvalExpr.evalFunc(t,
                                   a,
                                   name_);

    if (name == null)
      nullOperand(name_);

    sess.unsubscribe(name);
    
    return null;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqUnsubscribe n = (MqUnsubscribe)super.clone();
    
    n.sess_   = sess_.cloneAny();
    n.name_   = name_.cloneAny();
        
    return n;
  }
}
