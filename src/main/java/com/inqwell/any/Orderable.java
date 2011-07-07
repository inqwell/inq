/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/Orderable.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * Defines an interface by which implementing collections may order
 * themselves.
 */
public interface Orderable extends Any
{
	/**
	 * Reorder this Orderable's immediate children.  The contents of the
	 * collection will be reordered according
	 * to the natural ordering of the element contents.  This method uses the
	 * <code>AnyComparator</code> implementation to do the ordering.
	 * <p>
	 * Any previously held comparator within the <code>Orderable</code>
	 * implementation is diacarded and this instance is then no longer
	 * capable of intrinsic reordering or maintaining correct the
	 * correct order as new nodes are added.
	 * @param orderBy an array of <code>NodeSpecification</code> objects which
	 * locate the desired elements under the this array defining the order
	 * criteria.
	 */
  public void sort (Array orderBy);
  
	/**
	 * Reorder this Orderable's immediate children.  The contents of the
	 * collection will be reordered according to the given comparator
	 * implementation.
	 * <p>
	 * Implementations are at liberty to remember
	 * the <code>OrderComparator</code> that they are passed for
	 * future reordering and for maintaining the current order
	 * as new elements are inserted into them.  Clients should
	 * arrange to clone the comparator to avoid problems of
	 * inadvertant object sharing.
	 */
  public void sort (OrderComparator c);
  
	/**
	 * Reorder this Orderable's immediate children.  The contents of the
	 * collection will be reordered according to the node specifications and
	 * comparator implementation given.
	 * <p>
	 * Implementations are at liberty to remember
	 * the <code>OrderComparator</code> that they are passed for
	 * future reordering and for maintaining the current order
	 * as new elements are inserted into them.  Clients should
	 * arrange to clone the comparator to avoid problems of
	 * inadvertant object sharing.
	 */
  public void sort (Array orderBy, OrderComparator c);

  /**
   * Return a comparator that an Orderable maintains, if it
   * wishes to define its ordering intenally
   */
  public OrderComparator getOrderComparator();

  /**
   * Ensure that the given element is in the correct order position
   * in this collection, if there is an <code>OrderComparator</code>
   * in effect
   * @return the insertion position or -1 if there is no comparator
   */
  public int reorder(Any a);
  
  /**
   * Return the underlying order. If the implementation is intrinsically
   * ordered then can return <code><b>this</b></code>. The return value
   * is strictly read-only
   */
  public Array getArray();
  
  /**
   * Expose the underlying java List implementation - required for access
   * to other Java core API classes.
   */
  public java.util.List getList();
}
