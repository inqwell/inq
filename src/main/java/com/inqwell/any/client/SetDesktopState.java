/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Map;
import com.inqwell.any.client.swing.SwingInvoker;

/**
 * Ask the desktop for its current state
 */
public class SetDesktopState extends    AbstractFunc
                             implements Cloneable
{
  private Any state_;
  
  public SetDesktopState (Any state)
  {
    state_ = state;
  }

  public Any exec(Any a) throws AnyException
  {
    final Map m  = (Map)EvalExpr.evalFunc(getTransaction(),
                                          a,
                                          state_,
                                          Map.class);
    
    SwingInvoker s = new SwingInvoker()
    {

      @Override
      protected void doSwing()
      {
        AnyWindow.restoreDesktop(m);
      }
    };
    
    s.maybeSync();
    
    return m;
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
