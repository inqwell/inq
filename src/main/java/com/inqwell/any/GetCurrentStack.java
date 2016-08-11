/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetCurrentStack.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Gets the current Inq stack, if available
 */
public class GetCurrentStack extends    AbstractFunc
                             implements Cloneable
{
	
	public GetCurrentStack()
	{
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any stack = getTransaction().getProcess().getCurrentStack();
                                       
    return stack;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		GetCurrentStack g = (GetCurrentStack)super.clone();
		return g;
  }
}
