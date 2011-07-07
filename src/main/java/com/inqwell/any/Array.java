/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * The base interface for array type composites. 
 */
public interface Array extends Composite
{
  /**
   * Places the given element at the end of the array.
   */
  public void add (Any element);

  /**
   * Places the given element at the specified position in the array.
   */
  public void add (int at, Any element);
  public void add (Any at, Any element);

  /**
   * Add all the elements of <code>c</code> to <code>this</code>.
   * The composite <code>c</code> remains unchanged.
   */
  public void addAll(Composite c);

  /**
   * Set the object at the specified position to the given item
   * @exception IndexOutOfBoundsException
   */
  public void replaceItem (int at, Any item);
  public void replaceItem (Any at, Any item);

  /**
   * Set the value at the specified position to the given value
   * @exception IndexOutOfBoundsException
   */
  public void replaceValue (int at, Any value);
  public void replaceValue (Any at, Any value);

  public Any remove (int at);
  public Any remove (Any at);
  
  public int indexOf(Any a);

  /**
   * Return the element at the specified position
   * @exception IndexOutOfBoundsException
   */
  public Any get (int at);
  public Any get (Any at);

  /**
   * Return a shallow copy of self.  New Array object contains same object
   * references as this
   */
  public Array shallowCopy();

  /**
   * Convert contents to a Java object array.  Useful for interfacing
   * to JDK core classes which require such primitive constructs!
   */
  public Object[] toArray();
  
  /**
   * Expose the underlying java List implementation - required for access
   * to other Java core API classes.
   */
  public java.util.List getList();
  
  /**
   * Reverse the order of the elements in this Array.
   */
  public void reverse();
  
  /**
   * Create a iterator which iterates in reverse - Obviously!
   */
	public Iter createReverseIterator();
	
  /**
	 * Adds identity semantics, if supported, to this object.
	 * @return For intents and purposes, the original object but now with
	 * identity semantics.
	 */
	public Array bestowIdentity();
}
