/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/SimpleSet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SimpleSet extends    AnySet
											 implements Set,
																	Cloneable
{
  public SimpleSet () { super(); }
  public SimpleSet (int initialCapacity) { super(initialCapacity); }
	protected void beforeAdd(Any item) {}
	protected void afterAdd(Any item) {}
	protected void beforeRemove(Any item) {}
	protected void afterRemove(Any item) {}
	protected void emptying() {}
}
