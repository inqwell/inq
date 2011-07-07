/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SimpleMap.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * A no-frills map which does not impose any restrictions like single
 * hierarchy (see <code>AnyPMap</code>)
 * <p>
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SimpleMap extends    AnyMap
											 implements Map,
											 Cloneable
{
  public Object clone() throws CloneNotSupportedException
  {
		return super.clone();
	}
	
	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}
}
