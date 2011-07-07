/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/identity/Identity.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 
package com.inqwell.any.identity;

import com.inqwell.any.Any;
import com.inqwell.any.Map;

/**
 * Access to the identity package.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public abstract class Identity
{
	public static Map bestowIdentity (Map m)
	{
		if (m instanceof HasIdentity)
			return m;
		else
			return new AnyMapDecor (m);
	}
	
	public static boolean hasIdentity(Any a)
	{
		if (a instanceof HasIdentity)
			return true;
		else
			return false;
	}
}

