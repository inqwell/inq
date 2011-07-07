/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * AnyArray supports access to elements by index.
 */
public class AnyArray extends    AbstractArray
                      implements Array,
																 Vectored,
																 Orderable,
                                 Cloneable
{
  public AnyArray () { super(); }
  public AnyArray (int initialCapacity) { super(initialCapacity); }
	protected void beforeAdd(int at, Any item) {}
	protected void afterAdd(int at, Any item) {}
	protected void beforeRemove(int at) {}
	protected void afterRemove(int at) {}
	protected void emptying() {}
	protected void replacing(int at, Any item) {}
}
