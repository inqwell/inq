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
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a queue from the given session and
 * queue name arguments
 * @author tom
 *
 */
public class MqCreateProducer extends    AbstractFunc
                              implements Cloneable
{

  private Any   sess_;
  private Any   dest_;

  public MqCreateProducer(Any sess, Any qname)
  {
    sess_     = sess;
    dest_     = qname;
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
    
    DestinationI dest = (DestinationI)EvalExpr.evalFunc(t,
                                                        a,
                                                        dest_,
                                                        DestinationI.class);

    if (dest == null && dest_ != null)
      nullOperand(dest_);

    // Check if the script is creating an unassigned producer.
    if (AnyNull.isNullInstance(dest))
      dest = null;
    
    MessageProducerI producer = sess.createProducer(dest);
    
    return producer;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateProducer m = (MqCreateProducer)super.clone();
    
    m.sess_   = sess_.cloneAny();
    m.dest_     = AbstractAny.cloneOrNull(dest_);
        
    return m;
  }
}
