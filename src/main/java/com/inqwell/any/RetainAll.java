/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/RetainAll.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any;

/**
 * Accepts two <code>Composite</code> operands and retains
 * in the first only those children that are contained within
 * the second.  All other children of the first operand are
 * removed. Returns the first operand as the result of
 * this function.
 * <p/>
 * If the second operand is the equals constant then the
 * first operand is unaffected.
 */
public class RetainAll extends    AbstractFunc
								       implements Cloneable
{
	
  private Any op1_;
  private Any op2_;

	/**
	 * 
	 */
  public RetainAll(Any op1, Any op2)
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
		
		Any op2 = EvalExpr.evalFunc(getTransaction(),
																								 a,
																								 op2_);
		
		if (!AnyAlwaysEquals.isAlwaysEquals(op2))
		{
		  if (!(op2 instanceof Composite))
		    throw new IllegalArgumentException(op2.getClass().toString() + " not a collection");

		  op1.retainAll((Composite)op2);
		}
		
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
    RetainAll r = (RetainAll)super.clone();
    
    r.op1_     = op1_.cloneAny();
    r.op2_     = op2_.cloneAny();
    
    return r;
  }
}
