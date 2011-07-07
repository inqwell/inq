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
 * Read an item of data from a JMS stream or bytes message.
 * <p/>
 * 
 * @author tom
 *
 */
public class MqRead extends    AbstractFunc
                    implements Cloneable
{
  private Any   msg_;
  private Any   value_;

  public MqRead(Any msg,
                Any value)
  {
    msg_    = msg;
    value_  = value;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    // We've made stream messages bytes messages too
    BytesMessageI msg = (BytesMessageI)EvalExpr.evalFunc(t,
                                                         a,
                                                         msg_,
                                                         BytesMessageI.class);

    if (msg == null)
      nullOperand(msg_);
    
    Any ret = EvalExpr.evalFunc(t,
                                a,
                                value_);
    
    if (ret == null)
      nullOperand(value_);
    
    msg.read(ret);
    
    return ret;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqRead m = (MqRead)super.clone();
    
    m.msg_    = msg_.cloneAny();
    m.value_  = value_.cloneAny();
        
    return m;
  }
}
