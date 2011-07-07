/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Length.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the string length of the specified node.  Operand
 * must resolve to an StringI
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Length extends    AbstractFunc
									 implements Cloneable
{
	
	private Any any_;
	
	public Length(Any any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		StringI anyString = (StringI)EvalExpr.evalFunc(getTransaction(),
																a,
																any_,
																StringI.class);
    
    if (anyString == null)
      nullOperand(any_);

		AnyInt ret = new AnyInt();
		
		ret.setValue(anyString.getValue().length());

		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Length l = (Length)super.clone();
		l.any_ = AbstractAny.cloneOrNull(any_);
		return l;
  }
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	if (any_ != null)
      a.add(any_);
  	return a.createIterator();
  }
}
