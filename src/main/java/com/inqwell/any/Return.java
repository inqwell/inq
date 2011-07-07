/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Return.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $ 
 * $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

/**
 * Terminate the current called expression by throwing
 * a <code>ReturnException</code>.
 * <p>
 * This function effects a change of control flow to prematurely
 * terminate an expression.  If there is an operand that operand is
 * evaluted and passed to the <code>ReturnException</code> so
 * it can be picked up by the parent loop and returned as the
 * loop result.
 */
public class Return extends    AbstractFunc
										implements Cloneable
{
	private static String msg__ = "Return statement out of context";
  private Any expression_;

  public Return()
  {
  }

	/**
	 * 
	 */
  public Return(Any expression)
  {
    expression_ = expression;
  }

  public Any exec(Any a) throws AnyException
  {
		Any res = null;
		if (expression_ != null)
		{
			res = EvalExpr.evalFunc(getTransaction(),
															a,
															expression_);
		}
		
		if (res != null)
			throw new ReturnException(res, msg__);
		else
			throw new ReturnException(msg__);
		
		// not reached
		// return res;
  }
  
  public Iter createIterator ()
  {
  	if (expression_ != null)
    {
	  	Array a = AbstractComposite.array();
			a.add(expression_);
	  	return a.createIterator();
    }
    
    return DegenerateIter.i__;
  }

  public Object clone () throws CloneNotSupportedException
  {
    Return r = (Return)super.clone();
    
    r.expression_ = AbstractAny.cloneOrNull(expression_);
    
    return r;
  }
}
