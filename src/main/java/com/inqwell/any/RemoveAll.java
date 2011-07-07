/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/RemoveAll.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

/**
 * Accepts two <code>Composite</code> operands and removes
 * from the first any children that are contained within
 * the second.  Returns the first operand as the result of
 * this function.
 */
public class RemoveAll extends    AbstractFunc
								       implements Cloneable
{
	
  private Any op1_;
  private Any op2_;

	/**
	 * 
	 */
  public RemoveAll(Any op1, Any op2)
  {
    op1_  = op1;
    op2_  = op2;
  }

  public Any exec(Any a) throws AnyException
  {
		Composite op1 = (Composite)EvalExpr.evalFunc(getTransaction(),
																								 a,
																								 op1_,
																								 Composite.class);
    if (op1 == null)
      nullOperand(op1_);
		
		Composite op2 = (Composite)EvalExpr.evalFunc(getTransaction(),
																								 a,
																								 op2_,
																								 Composite.class);
    if (op2 == null)
      nullOperand(op2_);
    
		op1.removeAll(op2);
		
		return op1;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(op1_);
		a.add(op2_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    RemoveAll r = (RemoveAll)super.clone();
    
    r.op1_     = op1_.cloneAny();
    r.op2_     = op2_.cloneAny();
    
    return r;
  }
}
