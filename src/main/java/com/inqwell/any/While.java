/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/While.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any;

/**
 * Implements <code>while</code> and <code>do</code> loops.
 * 
 */
public class While extends    AbstractFunc
                   implements Cloneable
{
	
  private Any test_;
  private Any expr_;

  private boolean isDo_   = false;

  /**
   * Execute the given expression while the test is true.  The
   * test is performed prior to the first execution of the
   * expression.
   */
  public While(Any test, Any expr)
  {
    this(test, expr, false);
  }
  
  /**
   * Execute the given expression while the test is true.  The
   * test is performed prior to the first execution of the
   * expression if <code>isDo</code> is <code>false</code>.
   * Otherwise the expression will be performed at least once
   * and the test performed then (as in a do-while loop).
   */
  public While(Any test, Any expr, boolean isDo)
  {
    test_  = test;
    expr_  = expr;
    setDo(isDo);
  }

  public Any exec(Any a) throws AnyException
  {
		BooleanI b = new AnyBoolean();
		
		Any ret = b; // for while, return false if the loop does not execute at all
		
		if (!isDo_)
		{
      while (doTest(test_, a, b))
      {
        try
        {
          ret = EvalExpr.evalFunc(getTransaction(),
                                  a,
                                  expr_);
        }
        catch (BreakException bex)
        {
          ret = bex.getResult();
          break;
        }
        catch (ContinueException cex)
        {
          continue;
        }
      }
    }
    else
    {
      do
      {
        try
        {
          ret = EvalExpr.evalFunc(getTransaction(),
                                  a,
                                  expr_);
        }
        catch (BreakException bex)
        {
          ret = bex.getResult();
          break;
        }
        catch (ContinueException cex)
        {
          continue;
        }
      }
      while (doTest(test_, a, b));
    }

		return ret;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(test_);
		a.add(expr_);
  	return a.createIterator();
  }
  
  public void setDo(boolean isDo)
  {
    isDo_ = isDo;
  }

  public Object clone () throws CloneNotSupportedException
  {
    While w = (While)super.clone();
    
    w.test_     = test_.cloneAny();
    w.expr_     = expr_.cloneAny();
    
    return w;
  }
  
  private boolean doTest(Any test, Any a, BooleanI result) throws AnyException
  {
		Any res = EvalExpr.evalFunc(getTransaction(),
																a,
																test);
		
		// whatever the result is, convert it to a boolean
		result.copyFrom(res);

    return result.getValue();
  }
}
