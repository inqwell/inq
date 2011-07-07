/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Collate.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

public class Collate extends    AbstractFunc
                     implements Cloneable
{
	Any       collator_;
	Any       op1_;
	Any       op2_;
	
	/**
	 * Collate two strings. Two modes of opertation are supported:
	 * <ol><li>If <code>op1</code> and <code>op2</code> are both specified
	 * a one-off comparison is performed directly using the collator.</li>
	 * <li>If only <code>op1</code> is specified then the collator is asked
	 * for a collation key for the operand.</li></ol>
	 * Returns the comparison result as an integer or the collation key
	 * as appropriate.  
	 * @param collator
	 * @param op1
	 * @param op2
	 */
	public Collate(Any collator, Any op1, Any op2)
	{
		collator_ = collator;
		op1_      = op1;
		op2_      = op2;
	}
	

	public Any exec(Any a) throws AnyException
	{
		AnyCollator collator = (AnyCollator)EvalExpr.evalFunc(getTransaction(),
																													a,
																													collator_,
																													AnyCollator.class);

		if (collator == null)
			nullOperand(collator_);

		StringI     op1 = (StringI)EvalExpr.evalFunc(getTransaction(),
																								 a,
																							   op1_,
																								 StringI.class);

		if (op1 == null)
			nullOperand(op1_);

		StringI     op2 = (StringI)EvalExpr.evalFunc(getTransaction(),
																								 a,
																							   op2_,
																								 StringI.class);

		if (op2 == null && op2_ != null)
			nullOperand(op2_);

		if (op2 == null)
			return collator.getCollationKey(op1);
		
		return new AnyInt(collator.compare(op1, op2));
	}
  
  public Object clone () throws CloneNotSupportedException
  {
    Collate c = (Collate)super.clone();
    
    c.collator_  = collator_.cloneAny();
    c.op1_       = op1_.cloneAny();
    c.op2_       = AbstractAny.cloneOrNull(op2_);
    
    return c;
  }
}
