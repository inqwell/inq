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
 * Reset a JMS bytes or stream message.
 * <p/>
 * 
 * @author tom
 *
 */
public class MqReset extends    AbstractFunc
                     implements Cloneable
{
  private Any   msg_;

  public MqReset(Any msg)
  {
    msg_ = msg;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    BytesMessageI msg = (BytesMessageI) EvalExpr.evalFunc(t,
                                                          a,
                                                          msg_,
                                                          BytesMessageI.class);

    if (msg == null)
      nullOperand(msg_);

    msg.reset();
    
    return msg;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqReset m = (MqReset)super.clone();
    
    m.msg_           = msg_.cloneAny();

    return m;
  }
}
