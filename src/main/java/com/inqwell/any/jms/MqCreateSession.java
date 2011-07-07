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
import com.inqwell.any.BooleanI;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.IntI;
import com.inqwell.any.Transaction;

/**
 * Create a session from the given connection and
 * other arguments
 * @author tom
 *
 */
public class MqCreateSession extends    AbstractFunc
                             implements Cloneable
{

  private Any   conn_;
  private Any   ack_;
  private Any   txn_;

  public MqCreateSession(Any conn, Any ack, Any txn)
  {
    conn_     = conn;
    ack_      = ack;
    txn_      = txn;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    ConnectionI conn = (ConnectionI)EvalExpr.evalFunc(t,
                                                      a,
                                                      conn_,
                                                      ConnectionI.class);

    if (conn == null)
      nullOperand(conn_);
    
    BooleanI txn   = (BooleanI)EvalExpr.evalFunc(t,
        a,
        txn_,
        BooleanI.class);
    
    if (txn == null)
      nullOperand(txn_);
    
    IntI ack   = (IntI)EvalExpr.evalFunc(t,
                                         a,
                                         ack_,
                                         IntI.class);

    if (ack == null && ack_ != null)
      nullOperand(ack_);

    SessionI ret = conn.createSession(txn.getValue(),
                                      (ack != null) ? ack.getValue() : 0);

    // If the session is transacted then hand it over to our transaction
    // to manage
    if (txn.getValue())
      t.setMqSession(ret);
    
    return ret;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateSession n = (MqCreateSession)super.clone();
    
    n.conn_  = conn_.cloneAny();
    n.ack_   = AbstractAny.cloneOrNull(ack_);
    n.txn_   = txn_.cloneAny();
        
    return n;
  }
}
