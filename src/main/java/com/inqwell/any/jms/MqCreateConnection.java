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

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a connection from the given connection factory and
 * optional user and password arguments
 * @author tom
 *
 */
public class MqCreateConnection extends    AbstractFunc
                                implements Cloneable
{

  private Any   conn_;
  private Any   user_;
  private Any   passwd_;

  public MqCreateConnection(Any conn, Any user, Any passwd)
  {
    conn_     = conn;
    user_     = user;
    passwd_   = passwd;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    ConnectionFactoryI conn =
            (ConnectionFactoryI)EvalExpr.evalFunc(t,
                                                  a,
                                                  conn_,
                                                  ConnectionFactoryI.class);

    if (conn == null)
      nullOperand(conn_);
    
    Any user   = EvalExpr.evalFunc(t,
                                   a,
                                   user_);

    Any passwd = EvalExpr.evalFunc(t,
                                   a,
                                   passwd_);

    if (user == null && user_ != null)
      nullOperand(user_);
    
    if (passwd == null && passwd_ != null)
      nullOperand(passwd_);
    
    if (user == null)
      return conn.createConnection();
    else
      return conn.createConnection(user, passwd);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateConnection n = (MqCreateConnection)super.clone();
    
    n.conn_   = conn_.cloneAny();
    n.user_   = AbstractAny.cloneOrNull(user_);
    n.passwd_ = AbstractAny.cloneOrNull(passwd_);
        
    return n;
  }
}
