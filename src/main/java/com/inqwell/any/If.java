/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/If.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any;

/**
 * The <code>if</code> function.  Executes the second
 * operand if the first operand evaluates to <code>true</code>,
 * otherwise executes the third operand.
 * The first operand, the test expression, is evaluated and
 * its result converted to a <code>boolean</code>. If true
 * the second operand is evaluated. If false the optional third
 * operand is executed.
 * <p>
 * The return value is the result of the second operand, or
 * <code>null</code> if the test evaluates to <code>false</code>
 * after conversion to a boolean.
 */
public class If extends    AbstractFunc
								implements Cloneable
{
	
  private Any test_;
  private Any expr_;
  private Any else_;

	/**
	 * 
	 */
  public If(Any test, Any expr)
  {
    test_  = test;
    expr_  = expr;
  }

  public If(Any test, Any expr, Any elseExpr)
  {
    test_  = test;
    expr_  = expr;
    else_  = elseExpr;
  }

  public Any exec(Any a) throws AnyException
  {
		Any res = EvalExpr.evalFunc(getTransaction(),
																a,
																test_);
		
		AnyBoolean b = new AnyBoolean(false);
		
		// whatever the result is, convert it to a boolean
		b.copyFrom(res);

		Any ret = b;
		
		if (b.getValue())
		{
			ret = EvalExpr.evalFunc(getTransaction(),
															a,
															expr_.cloneAny());
		}
		else
		{
      if (else_ != null)
      {
        ret = EvalExpr.evalFunc(getTransaction(),
                                a,
                                else_.cloneAny());
      }
    }

		return ret;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(test_);
		a.add(expr_);
		if (else_ != null)
      a.add(else_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    If i = (If)super.clone();
    
    i.test_     = test_.cloneAny();
    
    // Clone the one we execute (if any)
    //i.expr_     = expr_.cloneAny();
    //i.else_     = AbstractAny.cloneOrNull(else_);
    
    return i;
  }
}
