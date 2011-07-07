/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetDate.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return a new <code>AnyDate</code> instance initialised to the current
 * system date and time.  Used where cloning an <code>AnyDate</code>,
 * which preserves its current value, is inappropriate
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetDate extends    AbstractFunc
									   implements Cloneable
{
	public GetDate()
	{
	}
	
	public Any exec(Any a) throws AnyException
	{
		return new AnyDate();
	}
	
  public Object clone () throws CloneNotSupportedException
  {
  	return super.clone();
  }
}
