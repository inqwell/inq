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
 * Create a queue from the given session and
 * queue name arguments
 * @author tom
 *
 */
public class MqCreateTopic extends    AbstractFunc
                           implements Cloneable
{

  private Any   sess_;
  private Any   topic_;

  public MqCreateTopic(Any sess, Any topic)
  {
    sess_     = sess;
    topic_    = topic;
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
    
    Any topic   = EvalExpr.evalFunc(t,
                                    a,
                                    topic_);

    if (topic == null)
      nullOperand(topic_);

    return sess.createTopic(topic);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateTopic n = (MqCreateTopic)super.clone();
    
    n.sess_   = sess_.cloneAny();
    n.topic_   = topic_.cloneAny();
        
    return n;
  }
}
