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
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.WindowF;


/**
 * Bring the given window to the front (hopefully).
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class ToFront extends    AbstractFunc
                  implements Cloneable
{
  private Any     window_;
  private WindowF rWindow_;

  private boolean postTransaction_;
  
  public ToFront (Any window)
  {
    window_     = window;
  }

  /**
   * Resolve the argument and call <code>show()</code>.
   *
   * @return The resolved argument.
   */
  public Any exec(Any a) throws AnyException
  {
    if (!postTransaction_)
    {
      rWindow_  = (WindowF)EvalExpr.evalFunc
                                      (getTransaction(),
                                       a,
                                       window_,
                                       WindowF.class);

      postTransaction_ = true;
      getTransaction().addAction(this, Transaction.AFTER_EVENTS);

      return rWindow_;
    
    }

    rWindow_.toFront();
    Any ret  = rWindow_;
    rWindow_ = null;

    return ret;
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    ToFront s = (ToFront)super.clone();
    
    s.window_        = window_.cloneAny();
    s.rWindow_       = null;
    
    return s;
  }
}
