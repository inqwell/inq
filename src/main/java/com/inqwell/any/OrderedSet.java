/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/OrderedSet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.LinkedHashSet;

/**
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class OrderedSet extends    AnySet
											  implements Set,
																	 Cloneable
{
  public OrderedSet () { super(new LinkedHashSet()); }
	protected void beforeAdd(Any item) {}
	protected void afterAdd(Any item) {}
	protected void beforeRemove(Any item) {}
	protected void afterRemove(Any item) {}
	protected void emptying() {}
}
