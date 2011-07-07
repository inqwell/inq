/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/CharAt.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the character of the string argument at the specified
 * position.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class CharAt extends    AbstractFunc
                    implements Cloneable
{
	private Any str_;
	private Any index_;
	
	public CharAt(Any str, Any index)
	{
		str_   = str;
		index_ = index;
	}
	
	public Any exec(Any a) throws AnyException
	{
		StringI str = (StringI)EvalExpr.evalFunc(getTransaction(),
                                             a,
                                             str_,
                                             StringI.class);

		Any index = EvalExpr.evalFunc(getTransaction(),
                                  a,
                                  index_);

		IntI i = new ConstInt(index);
    
    AnyChar ret = new AnyChar(str.toString().charAt(i.getValue()));

		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		CharAt c = (CharAt)super.clone();
		c.str_ = str_.cloneAny();
		c.index_ = index_.cloneAny();
		return c;
  }
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
    a.add(str_);
    a.add(index_);
  	return a.createIterator();
  }
}
