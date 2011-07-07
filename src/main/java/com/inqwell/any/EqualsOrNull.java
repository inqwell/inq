/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EqualsOrNull.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * EqualsOrNull: returns <code>true</code> if the first operand is
 * equal to the second operand or if the second operand is null.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */ 
public class EqualsOrNull extends    AbstractFunc
                          implements Cloneable
{
	Any       op1_;
	Any       op2_;
	
	public EqualsOrNull (Any op1, Any op2)
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

    if (op2 == null)
      return AnyBoolean.TRUE;
    
    if (op2 instanceof Value)
    {
      Value v = (Value)op2;
      if (v.isNull())
        return AnyBoolean.TRUE;
    }
    
    EvalExpr equals = new EvalExpr(op1, op2, new Equals());
    equals.setTransaction(getTransaction());
    
    return equals.exec(a);
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
    EqualsOrNull e = (EqualsOrNull)super.clone();
    
    e.op1_  = op1_.cloneAny();
    e.op2_  = op2_.cloneAny();
    
    
    return e;
  }
}
