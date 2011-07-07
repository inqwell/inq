/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Abort.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;



/**
 * Abort the current transaction.  This function aborts the current
 * transaction.
 * <p>
 * A transaction may also be aborted by errors it encounters during
 * the commit phase, such as duplicate object creation.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Abort extends    AbstractFunc
									 implements Cloneable
{
	public Abort()
	{
	}
	
	public Any exec(Any a) throws AnyException
	{
		getTransaction().abort();
		
		return null;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Abort a = (Abort)super.clone();
		return a;
  }
}
