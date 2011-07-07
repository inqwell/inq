/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Show.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.WindowF;


/**
 * A function which calls the <code>show</code> method on its operand
 * and returns the operand.  The operand must thus resolve to an object
 * implementing the <code>WindowF</code> interface.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Show extends    AbstractFunc
									implements Cloneable
{
	private Any     window_;
	private WindowF rWindow_;
	
	private Any     withResize_;
	private boolean rWithResize_;
	
	private boolean postTransaction_;
	
	public Show (Any window)
	{
		this(window, null);
	}

	public Show (Any window, Any withResize)
	{
		window_     = window;
		withResize_ = withResize;
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
		  
		  if (rWindow_ == null)
		    nullOperand(window_);

		  Any withResize = EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   withResize_);
		  
		  if (withResize == null && withResize_ != null)
		    nullOperand(withResize_);
		  
		  BooleanI b = new ConstBoolean(withResize);
		  rWithResize_ = b.getValue();

			postTransaction_ = true;
			getTransaction().addAction(this, Transaction.AFTER_EVENTS);

			return rWindow_;
		
		}

		rWindow_.show(rWithResize_);
		Any ret  = rWindow_;
		rWindow_ = null;

	  return ret;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	Show s = (Show)super.clone();
  	
  	s.window_        = window_.cloneAny();
    s.withResize_    = AbstractAny.cloneOrNull(withResize_);
    s.rWindow_       = null;
    s.rWithResize_   = false;
  	
    return s;
  }
}
