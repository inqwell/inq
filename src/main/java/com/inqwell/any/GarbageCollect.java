/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GarbageCollect.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Runs the garbage collector. Takes no arguments and 
 * returns <code>null</code>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GarbageCollect extends    AbstractFunc
                            implements Cloneable
{
	public GarbageCollect()
	{
	}

	public Any exec(Any a) throws AnyException
	{
    System.gc();
		return null;
	}

  public Object clone () throws CloneNotSupportedException
  {
		GarbageCollect g = (GarbageCollect)super.clone();
    return g;
  }
}
