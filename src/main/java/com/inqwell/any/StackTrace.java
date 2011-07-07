/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/StackTrace.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the current Inq call stack
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class StackTrace extends    AbstractFunc
									      implements Cloneable
{
	public StackTrace() {}

	public Any exec(Any a) throws AnyException
	{
		Transaction t = getTransaction();
		
    if (!t.getCallStack().isEmpty())
    {
			Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
			se.setLineNumber(getLineNumber());
    }
  	return new ConstString(t.getCallStack().toString());
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		return super.clone();
  }
	
  public Iter createIterator ()
  {
  	return DegenerateIter.i__;
  }
}
