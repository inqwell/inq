/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Trim.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Trim leading and trailing spaces from the given <code>StringI</code>.
 * If there is nothing to trim then the same string is returned.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Trim extends    AbstractFunc
									implements Cloneable
{
	
	private Any any_;
	
	public Trim(Any any)
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
    
    String trimmed = anyString.toString().trim();
    
    if (trimmed == anyString.toString())
      return anyString;
    else
      return new AnyString(trimmed);
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Trim t = (Trim)super.clone();
		t.any_ = AbstractAny.cloneOrNull(any_);
		return t;
  }
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	if (any_ != null)
      a.add(any_);
  	return a.createIterator();
  }
}
