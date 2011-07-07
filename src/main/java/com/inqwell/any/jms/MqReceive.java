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

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.ConstLong;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.LongI;
import com.inqwell.any.Transaction;

/**
 * Receive a JMS message.
 * <p/>
 * The first argument, <code>destOrCons/code> may be a
 * {@link MessageConsumerI} or a {@link DestinationI}. In each case
 * the following must prevail:
 * <ul>
 * <li>If a {@link MessageConsumerI} the producer must have
 * been created with an associated destination;</li>
 * <li>if a {@link DestinationI} the destination must have
 * had its <code>consumer</code> property set to a consumer
 * that does <strong>not</strong> have an associated destination.</li>
 * </ul>
 * <p/>
 * The timeout argument is optional.
 * 
 * @author tom
 *
 */
public class MqReceive extends    AbstractFunc
                       implements Cloneable
{
  private Any   destOrCons_;
  private Any   timeout_;

  public MqReceive(Any destOrProd,
                   Any timeout)
  {
    destOrCons_ = destOrProd;
    timeout_    = timeout;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any destOrCons = EvalExpr.evalFunc(t,
                                       a,
                                       destOrCons_);

    if (destOrCons == null)
      nullOperand(destOrCons_);
    
    Any timeout = (LongI)EvalExpr.evalFunc(t,
                                           a,
                                           timeout_);
    if (timeout == null && timeout_ != null)
      nullOperand(timeout_);
    
    LongI lTimeout = null;
    if (timeout != null)
      lTimeout = new ConstLong(timeout);
    
    MessageI ret = null;
    MessageConsumerI msgCons;
    if (destOrCons instanceof DestinationI)
    {
      DestinationI dest = (DestinationI)destOrCons;
      msgCons = dest.getConsumer();
      if (msgCons == null)
        throw new AnyRuntimeException("No consumer established for destination");
    }
    else
      msgCons = (MessageConsumerI)destOrCons;

    if (lTimeout == null)
      ret = msgCons.receive();
    else
    {
      long lTo = lTimeout.getValue();
      if (lTo > 0)
        ret = msgCons.receive(lTo);
      else if (lTo < 0)
        ret = msgCons.receiveNoWait();
      else
        ret = msgCons.receive();
    }
    
    t.mqDirty(true);

    return ret;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqReceive m = (MqReceive)super.clone();
    
    m.destOrCons_    = destOrCons_.cloneAny();
    m.timeout_       = AbstractAny.cloneOrNull(timeout_);
        
    return m;
  }
}
