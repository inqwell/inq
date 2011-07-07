/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $
 * @see 
 */

package com.inqwell.any;

/**
 * Implements <code>for</code> loop as in
 * <pre>
 * for([initial-expr] ; [test-expr] ; [final-expr]])
 *   statement
 * </pre>
 */
public class For extends    AbstractFunc
                   implements Cloneable
{
  private Any init_;
  private Any test_;
  private Any final_;
  private Any expr_;

  /**
   * 
   */
  public For(Any init, Any test, Any eFinal, Any expr)
  {
    init_  = init;
    test_  = test;
    final_ = eFinal;
    expr_  = expr;
  }
  
  public Any exec(Any a) throws AnyException
  {
    // Unusually, we don't care what the result of the initial
    // expression is...
    if (init_ != null)
      EvalExpr.evalFunc(getTransaction(),
                        a,
                        init_);

    BooleanI b = new AnyBoolean();
    
    Any ret = b; // return false if the loop does not execute at all
    
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
        // Continue must run for loop's final expression
        if (final_ != null)
          EvalExpr.evalFunc(getTransaction(),
                            a,
                            final_);
        
        continue;
      }

      // After each iteration perform the final expression
      if (final_ != null)
        EvalExpr.evalFunc(getTransaction(),
                          a,
                          final_);
    }

    return ret;
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    if (init_ != null)
      a.add(init_);
    if (test_ != null)
      a.add(test_);
    if (final_ != null)
      a.add(final_);
    
    a.add(expr_);
    
    return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    For f = (For)super.clone();
    
    f.init_     = AbstractAny.cloneOrNull(init_);
    f.test_     = AbstractAny.cloneOrNull(test_);
    f.final_    = AbstractAny.cloneOrNull(final_);
    f.expr_     = expr_.cloneAny();
    
    return f;
  }
  
  private boolean doTest(Any test, Any a, BooleanI result) throws AnyException
  {
    // for loops don't require a test. In this case the result is always true 
    if (test == null)
      return true;
    
    Any res = EvalExpr.evalFunc(getTransaction(),
                                a,
                                test);
    
    // whatever the result is, convert it to a boolean
    result.copyFrom(res);

    return result.getValue();
  }
}
