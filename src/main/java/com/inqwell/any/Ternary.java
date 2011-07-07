/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Ternary.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Accepts three operands whereby the first is evaluated if the third
 * evaluates to <code>true</code>, otherwise the second is evaluated.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.OperatorVisitor
 */
public class Ternary extends    AbstractFunc
										 implements Cloneable
{
  private Any op1_;
  private Any op2_;
  private Any op3_;

	/**
	 * Returns op1 if op3 is true, otherwise returns op2.  Thus
	 * op3 must evaluate to a boolean
	 */
  public Ternary(Any op1, Any op2, Any op3)
  {
    op1_  = op1;
    op2_  = op2;
    op3_  = op3;
  }

  public Any exec(Any a) throws AnyException
  {
		Any cond = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 op3_);
		
    BooleanI res = new ConstBoolean(cond);
    
		Any ret;
		
		if (res.getValue())
			ret = EvalExpr.evalFunc(getTransaction(),
                              a,
                              op1_.cloneAny());
		else
			ret = EvalExpr.evalFunc(getTransaction(),
                              a,
                              op2_.cloneAny());
    return ret;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(op1_);
  	a.add(op2_);
  	a.add(op3_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Ternary t = (Ternary)super.clone();
    
    //t.op1_ = AbstractAny.cloneOrNull(op1_);
    //t.op2_ = AbstractAny.cloneOrNull(op2_);
    t.op3_ = op3_.cloneAny();

    return t;
  }
}
