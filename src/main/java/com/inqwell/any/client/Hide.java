/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Hide.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.WindowF;


/**
 * A function which calls the <code>hide</code> method on its operand
 * and returns the operand.  The operand must thus resolve to an object
 * implementing the <code>WindowF</code> interface.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Hide extends    AbstractFunc
									implements Cloneable
{
	private Any window_;
	
	public Hide (Any window)
	{
		window_ = window;
	}

	/**
	 * Resolve the argument and call <code>show()</code>.
	 *
	 * @return The resolved argument.
	 */
	public Any exec(Any a) throws AnyException
	{
		WindowF window  = (WindowF)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   window_,
		                                   WindowF.class);
    if (window != null)
    {
      window.hide();
      window.getComponent().setVisible(false);
    }
		                                   
	  return window;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	Hide h = (Hide)super.clone();
  	
  	h.window_  = window_.cloneAny();
  	
    return h;
  }
}
