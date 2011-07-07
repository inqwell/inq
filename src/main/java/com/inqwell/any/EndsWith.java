/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EndsWith.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Returns boolean <code>true</code> if first operand ends with the
 * second operand, <code>false</code> otherwise.  Operands
 * must resolve to <code>StringI</code>s.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class EndsWith extends    AbstractFunc
                      implements Cloneable
{
	
	private Any s1_;
	private Any s2_;
	
	public EndsWith(Any s1, Any s2)
	{
		s1_ = s1;
		s2_ = s2;
	}
	
	public Any exec(Any a) throws AnyException
	{
		StringI s1 = (StringI)EvalExpr.evalFunc(getTransaction(),
																a,
																s1_,
																StringI.class);

		StringI s2 = (StringI)EvalExpr.evalFunc(getTransaction(),
																a,
																s2_,
																StringI.class);

    if (s1 == null)
      nullOperand(s1_);
   
    if (s2 == null)
      nullOperand(s2_);

		AnyBoolean ret = new AnyBoolean(s1.toString().endsWith(s2.toString()));
		
		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		EndsWith e = (EndsWith)super.clone();
		e.s1_ = s1_.cloneAny();
		e.s2_ = s2_.cloneAny();
		return e;
  }
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
    
  	if (s1_ != null)
      a.add(s1_);
  	if (s2_ != null)
      a.add(s2_);
      
  	return a.createIterator();
  }
}
