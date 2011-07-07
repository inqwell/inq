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
 * Stop a connection. Disables receipt of incoming messages
 * @author tom
 *
 */
public class MqStop extends    AbstractFunc
                    implements Cloneable
{

  private Any   conn_;

  public MqStop(Any conn)
  {
    conn_     = conn;
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
    
    conn.stop();
    
    return null;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqStop n = (MqStop)super.clone();
    
    n.conn_  = conn_.cloneAny();
        
    return n;
  }
}
