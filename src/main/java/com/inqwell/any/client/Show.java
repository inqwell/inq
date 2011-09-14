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
	private Any     withResize_;
	private Any     target_;
	
	public Show (Any window, Any withResize, Any relativeTo)
	{
		window_     = window;
		withResize_ = withResize;
		target_     = relativeTo;
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
	  
	  if (window == null)
	    nullOperand(window_);

	  Any withResize = EvalExpr.evalFunc
																		(getTransaction(),
	                                   a,
	                                   withResize_);
	  
	  if (withResize == null && withResize_ != null)
	    nullOperand(withResize_);
	  
	  BooleanI b = new ConstBoolean(withResize);
	  boolean resize = b.getValue();

    AnyComponent locationTarget = (AnyComponent)EvalExpr.evalFunc
                                    (getTransaction(),
                                     a,
                                     target_,
                                     AnyComponent.class);

    if (locationTarget == null && target_ != null)
      nullOperand(target_);

		getTransaction().addAction(new OnCommit(window, resize, locationTarget),
                               Transaction.AFTER_EVENTS);

		return window;
	
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	Show s = (Show)super.clone();
  	
  	s.window_        = window_.cloneAny();
    s.withResize_    = AbstractAny.cloneOrNull(withResize_);
    s.target_        = AbstractAny.cloneOrNull(target_);
  	
    return s;
  }
  
  static private class OnCommit extends AbstractFunc
  {
    private WindowF      window_;
    private boolean      resize_;
    private AnyComponent target_;

    private OnCommit(WindowF window, boolean resize, AnyComponent target)
    {
      window_ = window;
      resize_ = resize;
      target_ = target;
    }
    
    @Override
    public Any exec(Any a) throws AnyException
    {
      window_.show(resize_, target_);
      
      return null;
    }
  }
}
