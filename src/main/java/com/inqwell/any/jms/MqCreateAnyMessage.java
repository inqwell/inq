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

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a message to carry an Any from the given session
 * and optional any instance
 * @author tom
 *
 */
public class MqCreateAnyMessage extends    AbstractFunc
                                implements Cloneable
{
  private Any   sess_;
  private Any   any_;

  public MqCreateAnyMessage(Any sess, Any any)
  {
    sess_     = sess;
    any_      = any;
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
    
    Any any = EvalExpr.evalFunc(t,
                                a,
                                any_);
    
    if (any == null && any_ != null)
      nullOperand(any_);
    
    if (any == null)
      return sess.createObjectMessage();
    else
      return sess.createObjectMessage(any);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateAnyMessage n = (MqCreateAnyMessage)super.clone();
    
    n.sess_   = sess_.cloneAny();
    n.any_    = AbstractAny.cloneOrNull(any_);
        
    return n;
  }
}
