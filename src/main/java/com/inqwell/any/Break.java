/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Break.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $ 
 * $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

/**
 * Terminate a loop operation by throwing a <code>BreakException</code>.
 * <p>
 * This function effects a change of control flow to prematurely
 * terminate a loop.  If there is an operand that operand is
 * evaluted and passed to the <code>BreakException</code> so
 * it can be picked up by the parent loop and returned as the
 * loop result.
 */
public class Break extends    AbstractFunc
										 implements Cloneable
{
	
  private Any expression_;

  public Break()
  {
  }

	/**
	 * 
	 */
  public Break(Any expression)
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
			throw new BreakException(res);
		else
			throw new BreakException();
		
		// not reached
		// return res;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
    if (expression_ != null)
      a.add(expression_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Break b = (Break)super.clone();
    
    b.expression_ = AbstractAny.cloneOrNull(expression_);
    
    return b;
  }
}
