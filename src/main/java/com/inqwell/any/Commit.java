/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Commit.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;



/**
 * Commit the current transaction.  The semantics of committing a
 * transaction depend on the implementation of the transaction in
 * effect.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Commit extends    AbstractFunc
												 implements Cloneable
{
	public Commit()
	{
	}
	
	public Any exec(Any a) throws AnyException
	{
		getTransaction().commit();
		return a;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		return super.clone();
  }
	
}
