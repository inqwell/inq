/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Count.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the number of children of the specified node.
 * If the node is not a composite then this function
 * returns zero.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Count extends    AbstractFunc
									 implements Cloneable
{
	
	private Any any_;
	
	public Count(Any any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any any = EvalExpr.evalFunc(getTransaction(),
																a,
																any_);

    if (any == null)
      nullOperand(any_);
    
    // If the node does not support children then return -1
		AnyInt ret = new AnyInt(-1);
		
		if (any instanceof Composite)
		{
			ret.setValue(((Composite)any).entries());
		}

		return ret;
	}
	
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(any_);
    return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
		Count c = (Count)super.clone();
		c.any_ = any_.cloneAny();
		return c;
  }
	
}
