/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Dispose.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.WindowF;


/**
 * A function which calls the <code>dispose</code> method on its operand
 * and returns the operand.  The operand must thus resolve to an object
 * implementing the <code>WindowF</code> interface.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Dispose extends    AbstractFunc
									implements Cloneable
{
	private Any window_;
	
	public Dispose (Any window)
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
		window.dispose(false);
		                                   
	  return window;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	Dispose d = (Dispose)super.clone();
  	
  	d.window_        = window_.cloneAny();
  	
    return d;
  }
}
