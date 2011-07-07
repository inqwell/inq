/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Concatenate.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * String concatenation.
 * Concatenate second operand to first and return result.  First operand
 * must be a string; second operand will be converted to a string if
 * necessary.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class Concatenate extends    AbstractFunc
												 implements Cloneable
{
	Any       op1_;
	Any       op2_;
	
	AnyString temp_ = new AnyString();
	
	public Concatenate (Any op1, Any op2)
	{
		op1_ = op1;
		op2_ = op2;
	}
	
  public Any exec (Any a) throws AnyException
  {
		Any       op1 = EvalExpr.evalFunc(getTransaction(),
																			a,
																			op1_);

		Any       op2 = EvalExpr.evalFunc(getTransaction(),
																			a,
																			op2_);

		temp_.copyFrom(op1);
		return temp_.concat(op2);
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
    Concatenate c = (Concatenate)super.clone();
    
    c.op1_  = op1_.cloneAny();
    c.op2_  = op2_.cloneAny();
    
    c.temp_ = (AnyString)temp_.cloneAny();
    
    return c;
  }
}
