/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Defines the characteristics of vectoring for an Any composite.
 * If the implementation of a <code>Composite</code> interface or
 * derived can support vectored access then it can implement this
 * interface.
 */
public interface Vectored extends Composite
{
  public void removeByVector (int at);
  public void removeByVector (Any at);
  
  public int indexOf(Any a);

  /**
   * Determine whether this vector contains the given value.
   * This method has the same signature
   * as <code>Map.containsValue(Any)</code> and is redefined
   * here for those <code>Map</code>s that are
   * also <code>Vector</code>s.
   */
  public boolean containsValue (Any value);

  /**
   * Return the element at the specified position
   * @exception IndexOutOfBoundsException
   */
  public Any getByVector (int at);
  public Any getByVector (Any at);
  
  /**
   * Returns the key by which the same child would be retrieved
   * if this Vectored supports key access, such as that provided
   * by implementations of both Map and Vectored.
   * @param at the vector index
   * @return the key
   */
  public Any getKeyOfVector(int at);

  /**
   * Returns the key by which the same child would be retrieved
   * if this Vectored supports key access, such as that provided
   * by implementations of both Map and Vectored.
   * @param at the vector index
   * @return the key
   */
  public Any getKeyOfVector(Any at);
  
  /**
	 * Places the given value at the end of the collection.  If the
	 * implementation is a map then a suitable key will be invented
	 * for the value, for example its identity.
	 */
  public void addByVector(Any value);
  
  /**
	 * Places the given value at the specified location in the collection.
	 * If the location is not in the range <code>0</code>
	 * to <code> entries()</code> then an exception is generated unless
	 * the collection can be sparse.  If <code>setSparse(true)</code> has
	 * previously been called then place-holders are inserted as required.
	 * <p>
	 * If the implementation is a map then a suitable key will be invented
	 * for the value, for example its identity.
	 */
  public void addByVector(int at, Any value);
  
  /**
	 * Places the given value at the specified location in the collection.
	 * If the location is not in the range <code>0</code>
	 * to <code> entries()</code> then an exception is generated unless
	 * the collection can be sparse.  If <code>setSparse(true)</code> has
	 * previously been called then place-holders are inserted as required.
	 */
  public void addByVector(int at, Any key, Any value);
  
  /**
   * May perform any initialisation required to set up ordering
   * @return the order of the contents
   */
  public Array initOrderBacking();
  
  /**
   * Reverse the order of the elements in this Vectored.
   */
  public void reverse();
  
  public void setSparse(boolean isSparse);

  /**
   * Convert contents to a Java object array.  Useful for interfacing
   * to JDK core classes which require such primitive constructs!
   */
  public Object[] toArray();
  
}
