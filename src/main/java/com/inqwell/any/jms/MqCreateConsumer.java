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
import com.inqwell.any.AnyNull;
import com.inqwell.any.BooleanI;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;

/**
 * Create a consumer from the given session and
 * optional arguments
 * @author tom
 *
 */
public class MqCreateConsumer extends    AbstractFunc
                              implements Cloneable
{

  private Any   sess_;
  private Any   dest_;
  private Any   sel_;
  private Any   nolocal_;

  public MqCreateConsumer(Any sess, Any dest, Any sel, Any nolocal)
  {
    sess_     = sess;
    dest_     = dest;
    sel_      = sel;
    nolocal_  = nolocal;
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
    
    DestinationI dest = (DestinationI) EvalExpr.evalFunc(t,
                                                         a,
                                                         dest_,
                                                         DestinationI.class);

    if (dest == null)
      nullOperand(dest_);

    Any sel   = EvalExpr.evalFunc(t,
                                  a,
                                  sel_);

    BooleanI nolocal = (BooleanI)EvalExpr.evalFunc(t,
                                                   a,
                                                   nolocal_,
                                                   BooleanI.class);

    if (nolocal == null && nolocal_ != null)
      nullOperand(nolocal_);
    
    if (sel == null && sel_ != null)
      nullOperand(sel_);
    if (AnyNull.isNullInstance(sel))
      sel = null;
    
    if (nolocal == null && sel == null)
      return sess.createConsumer(dest);
    else if (sel != null && nolocal == null)
      return sess.createConsumer(dest, sel);
    else
      return sess.createConsumer(dest, sel, nolocal.getValue());
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    MqCreateConsumer m = (MqCreateConsumer)super.clone();
    
    m.sess_    = sess_.cloneAny();
    m.dest_    = dest_.cloneAny();
    m.sel_     = AbstractAny.cloneOrNull(sel_);
    m.nolocal_ = AbstractAny.cloneOrNull(nolocal_);
        
    return m;
  }
}
