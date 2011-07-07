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
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.jms;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.ConstLong;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.Transaction;

/**
 * Send a JMS message.
 * <p/>
 * The first argument, <code>destOrProd</code> may be a
 * {@link MessageProducerI} or a {@link DestinationI}. In each case
 * the following must prevail:
 * <ul>
 * <li>If a {@link MessageProducerI} the producer must have
 * been created with an associated destination;</li>
 * <li>if a {@link DestinationI} the destination must have
 * had its <code>producer</code> property set to a producer
 * that does <strong>not</strong> have an associated destination.</li>
 * </ul>
 * <p/>
 * The delivery mode, priority and time-to-live arguments
 * are optional.
 * 
 * @author tom
 *
 */
public class MqSend extends    AbstractFunc
                                implements Cloneable
{
  private Any   destOrProd_;
  private Any   msg_;
  private Any   delMode_;
  private Any   priority_;
  private Any   ttl_;

  public MqSend(Any destOrProd,
                Any msg,
                Any delMode,
                Any priority,
                Any ttl)
  {
    destOrProd_ = destOrProd;
    msg_        = msg;
    delMode_    = delMode;
    priority_   = priority;
    ttl_        = ttl;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any destOrProd = EvalExpr.evalFunc(t,
                                       a,
                                       destOrProd_);

    if (destOrProd == null)
      nullOperand(destOrProd_);
    
    MessageI msg = (MessageI) EvalExpr.evalFunc(t,
                                                a,
                                                msg_,
                                                MessageI.class);

    if (msg == null)
      nullOperand(msg_);

    IntI delMode   = (IntI)EvalExpr.evalFunc(t,
                                             a,
                                             delMode_,
                                             IntI.class);

    IntI priority  = (IntI)EvalExpr.evalFunc(t,
                                             a,
                                             priority_,
                                             IntI.class);

    Any ttl = EvalExpr.evalFunc(t,
                                a,
                                ttl_);
    
    if (ttl == null && ttl_ != null)
      nullOperand(ttl_);
    
    if (priority == null && priority_ != null)
      nullOperand(priority_);
    
    if (delMode == null && delMode_ != null)
      nullOperand(delMode_);
    
    LongI lTtl = null;
    if (ttl != null)
      lTtl = new ConstLong(ttl);
    
    if (destOrProd instanceof MessageProducerI)
    {
      MessageProducerI prod = (MessageProducerI)destOrProd;
      if (lTtl == null)
        prod.send(msg);
      else
        prod.send(msg, delMode.getValue(), priority.getValue(), lTtl.getValue());
    }
    else
    {
      DestinationI dest = (DestinationI)destOrProd;
      
      MessageProducerI p = dest.getProducer();
      
      if (lTtl == null)
        p.send(dest, msg);
      else
        p.send(dest,
               msg,
               delMode.getValue(),
               priority.getValue(),
               lTtl.getValue());
    }

    t.mqDirty(true);

    return null;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqSend m = (MqSend)super.clone();
    
    m.destOrProd_    = destOrProd_.cloneAny();
    m.msg_           = msg_.cloneAny();
    m.delMode_       = AbstractAny.cloneOrNull(delMode_);
    m.priority_      = AbstractAny.cloneOrNull(priority_);
    m.ttl_           = AbstractAny.cloneOrNull(ttl_);
        
    return m;
  }
}
