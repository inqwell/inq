/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * To support composite traversal we define a generic Iterator interface.
 * Note that java.util.Enumeration is not used as we want the return types
 * to be Any.
 */
public interface Iter extends Any
{
  /**
   * Advance to the next element.  Returns null if there are no further
   * elements.
   */
  public boolean hasNext();

  /**
   * Return the current element.  Returns nil if there are no further
   * elements or iterator has just been initialised.
   * @exception java.util.NoSuchElementException If there are no more
   * elements.
   */
  public Any next();
  
  /**
   * Removes from the underlying collection the last element returned by the
   * iterator (optional operation). This method can be called only once per
   * call to next. The behavior of an iterator is unspecified if the
   * underlying collection is modified while the iteration is in progress in
   * any way other than by calling this method.
   * @exception java.lang.UnsupportedOperationException if the
   * <code>remove</code> operation is not supported by this Iter
   * @exception java.lang.IllegalStateException if the next method has
   * not yet been called, or the remove method has already been called
   * after the last call to the next method.
   */
  public void remove();

  public Any previous();
  
  /**
   * Inserts the specified element into the
   * collection (optional operation).
   */
  public void add(Any a);

  public Any getIterRoot();
}


