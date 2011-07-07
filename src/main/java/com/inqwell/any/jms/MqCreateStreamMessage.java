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
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a stream message from the given session
 * @author tom
 *
 */
public class MqCreateStreamMessage extends    AbstractFunc
                                  implements Cloneable
{
  private Any   sess_;
  private Any   bytes_;

  public MqCreateStreamMessage(Any sess, Any bytes)
  {
    sess_     = sess;
    bytes_    = bytes;
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
    
    AnyByteArray bytes = (AnyByteArray)EvalExpr.evalFunc(t,
                                                         a,
                                                         bytes_,
                                                         AnyByteArray.class);

    if (bytes == null && bytes_ != null)
      nullOperand(bytes_);

    StreamMessageI m = sess.createStreamMessage();
    
    if (bytes != null)
      m.write(bytes);
    
    return m;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateStreamMessage n = (MqCreateStreamMessage)super.clone();
    
    n.sess_   = sess_.cloneAny();
    n.bytes_  = AbstractAny.cloneOrNull(bytes_);
    
    return n;
  }
}
