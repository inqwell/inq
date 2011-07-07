/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/CallStack.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class CallStack extends    SimpleStack
											 implements Stack,
																	Cloneable
{
	public String toString()
	{
		StringBuffer ret = new StringBuffer(64);
		Iter i = this.createIterator();
		while (i.hasNext())
		{
			Any a = i.next();
			ret.append(a.toString());
			ret.append('\n');
		}
		return ret.toString();
	}
}
