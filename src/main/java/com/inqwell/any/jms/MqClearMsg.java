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
 * Clear a JMS message's body.
 * <p/>
 * 
 * @author tom
 *
 */
public class MqClearMsg extends    AbstractFunc
                        implements Cloneable
{
  private Any   msg_;

  public MqClearMsg(Any msg)
  {
    msg_        = msg;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    MessageI msg = (MessageI) EvalExpr.evalFunc(t,
                                                a,
                                                msg_,
                                                MessageI.class);

    if (msg == null)
      nullOperand(msg_);

    msg.clearBody();
    
    return msg;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqClearMsg m = (MqClearMsg)super.clone();
    
    m.msg_           = msg_.cloneAny();

    return m;
  }
}
