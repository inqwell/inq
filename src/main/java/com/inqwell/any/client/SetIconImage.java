/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/SetIconImage.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.beans.FrameF;
import com.inqwell.any.*;


/**
 * A function which calls the <code>setIconImage</code> method on its operand
 * and returns the operand.  The operand must thus resolve to an object
 * implementing the <code>FrameF</code> interface.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SetIconImage extends    AbstractFunc
													implements Cloneable
{
	private Any window_;
	private Any icon_;
	
	public SetIconImage (Any window, Any icon)
	{
		window_ = window;
		icon_ =   icon;
	}

	/**
	 * Resolve the argument and call <code>show()</code>.
	 *
	 * @return The resolved argument.
	 */
	public Any exec(Any a) throws AnyException
	{
		FrameF window  = (FrameF)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   window_,
		                                   FrameF.class);

		AnyIcon icon  = (AnyIcon)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   icon_,
		                                   AnyIcon.class);
		window.setIconImage(icon);
		                                   
	  return window;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	SetIconImage s = (SetIconImage)super.clone();
  	
  	s.window_        = window_.cloneAny();
  	s.icon_          = icon_.cloneAny();
  	
    return s;
  }
}
