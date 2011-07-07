/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/UniqueKey.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * The UniqueKey function.  Returns the unique key of the
 * single <code>Map</code> operand.  Thus the <code>Map</code>
 * implementation must support the <code>getUniqueKey</code> method
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class UniqueKey extends    AbstractFunc
											 implements Cloneable
{
	private Any map_;
	
	public UniqueKey(Any map)
	{
		map_ = map;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Map map = (Map)EvalExpr.evalFunc(getTransaction(),
																		 a,
																		 map_,
																		 Map.class);
		
		return map.getUniqueKey();
	}

  public Object clone () throws CloneNotSupportedException
  {
		UniqueKey u = (UniqueKey)super.clone();
		
    u.map_   = map_.cloneAny();
    
    return u;
  }
}
