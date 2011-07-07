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
import com.inqwell.any.AnyNull;
import com.inqwell.any.BooleanI;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a durable subscriber from the given session and
 * optional arguments
 * @author tom
 *
 */
public class MqCreateDurableSubscriber extends    AbstractFunc
                                implements Cloneable
{

  private Any   sess_;
  private Any   topic_;
  private Any   name_;
  private Any   sel_;
  private Any   nolocal_;

  public MqCreateDurableSubscriber(Any sess,
                                   Any topic,
                                   Any name,
                                   Any sel,
                                   Any nolocal)
  {
    sess_     = sess;
    topic_    = topic;
    name_     = name;
    sel_      = sel;
    nolocal_  = nolocal;
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
    
    TopicI topic = (TopicI) EvalExpr.evalFunc(t,
                                             a,
                                             topic_,
                                             TopicI.class);

    if (topic == null)
      nullOperand(topic_);

    Any name   = EvalExpr.evalFunc(t,
                                   a,
                                   name_);

    if (name == null)
      nullOperand(name_);

    Any sel   = EvalExpr.evalFunc(t,
                                  a,
                                  sel_);

    BooleanI nolocal = (BooleanI)EvalExpr.evalFunc(t,
                                                   a,
                                                   nolocal_,
                                                   BooleanI.class);

    if (nolocal == null && nolocal_ != null)
      nullOperand(nolocal_);
    
    if (sel == null && sel_ != null)
      nullOperand(sel_);
    if (AnyNull.isNullInstance(sel))
      sel = null;
    
    if (nolocal == null && sel == null)
      return sess.createDurableSubscriber(topic, name);
    else
      return sess.createDurableSubscriber(topic, name, sel, nolocal.getValue());
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateDurableSubscriber m = (MqCreateDurableSubscriber)super.clone();
    
    m.sess_    = sess_.cloneAny();
    m.topic_   = topic_.cloneAny();
    m.name_    = name_.cloneAny();
    m.sel_     = AbstractAny.cloneOrNull(sel_);
    m.nolocal_ = AbstractAny.cloneOrNull(nolocal_);
        
    return m;
  }
}
