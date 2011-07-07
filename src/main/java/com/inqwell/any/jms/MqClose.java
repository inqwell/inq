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
 * Close a JMS entity of some sort.
 * <p/>
 * 
 * @author tom
 *
 */
public class MqClose extends    AbstractFunc
                     implements Cloneable
{
  private Any   jms_;

  public MqClose(Any msg)
  {
    jms_        = msg;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any jms = EvalExpr.evalFunc(t,
                                a,
                                jms_);

    if (jms == null)
      nullOperand(jms_);

    if (jms instanceof ConnectionI)
      ((ConnectionI)jms).close();
    else if (jms instanceof SessionI)
    {
      SessionI session = (SessionI)jms;
      
      // When closing a transacted session remove it from our transaction
      if (session.isTransactional())
        t.setMqSession(null);
      
      session.close();
    }
    else if (jms instanceof MessageConsumerI)
      ((MessageConsumerI)jms).close();
    else if (jms instanceof MessageProducerI)
      ((MessageProducerI)jms).close();
    else if (jms instanceof QueueBrowserI)
      ((QueueBrowserI)jms).close();
    else
      throw new IllegalArgumentException("Unsupported class " + jms.getClass());
      
    return null;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqClose m = (MqClose)super.clone();
    
    m.jms_    = jms_.cloneAny();

    return m;
  }
}
