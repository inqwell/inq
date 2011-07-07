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
 * Explicitly acknowledge a message (for use in CLIENT_ACKNOWLEDGE
 * sessions).
 * @author tom
 *
 */
public class MqAcknowledge extends    AbstractFunc
                           implements Cloneable
{

  private Any   msg_;

  public MqAcknowledge(Any conn)
  {
    msg_     = conn;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    MessageI msg = (MessageI)EvalExpr.evalFunc(t,
                                               a,
                                               msg_,
                                               MessageI.class);

    if (msg == null)
      nullOperand(msg_);
    
    msg.acknowledge();
    
    return null;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqAcknowledge n = (MqAcknowledge)super.clone();
    
    n.msg_  = msg_.cloneAny();
        
    return n;
  }
}
