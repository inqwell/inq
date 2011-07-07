/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Not.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Evaluate the operand, convert its result to a boolean
 * and return the inverse
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Not extends    AbstractFunc
                 implements Cloneable
{
  private Any  op1_;

	/**
	 * Invert the expression and return as an AnyBoolean
	 */
  public Not(Any op1)
  {
    op1_      = op1;
  }

  public Any exec(Any a) throws AnyException
  {
		Any op1 = EvalExpr.evalFunc(getTransaction(),
                                a,
                                op1_);
    
//    if (op1 == null)
//      nullOperand(op1_);
    
    AnyBoolean ret = new AnyBoolean();
    
    ret.copyFrom(op1);
    ret.setValue(!ret.getValue());
    
	  return ret;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(op1_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Not n = (Not)super.clone();
    
    n.op1_     = op1_.cloneAny();
        
    return n;
  }
}
