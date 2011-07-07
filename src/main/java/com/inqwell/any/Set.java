/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/Set.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

/**
 * A unique set of objects
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */

public interface Set extends Composite
{

  /**
   * Add an object to the set.
   * @exception DuplicateChildException if the object already exists in the
   * collection.
   */
  public void add (Any a);
  
  /**
	 * Add all the objects of the specified composite to this set.
	 * If <code>excludeDuplicates</code> is <code>true</code>
	 * then any elements of <code>c</code> which are already
	 * contained within this are skipped.  Otherwise we attempt
	 * to add all elements with the
	 * resulting <code>DuplicateChildException</code> if there are
	 * any duplicates
	 */
  public void addAll (Composite c, boolean excludeDuplicates);
  
  /**
   * Remove an object.
   */
  public Any remove (Any a);

  /**
   * Expose the underlying java Set implementation.
   */
  public java.util.Set getSet ();

  /**
   * Checks for the existence of the given object.
   * Overrides contains() in com.inqwell.any.Composite
   * @return true if this contains the given object; false otherwise
   */
  public boolean contains (Any a);

  /**
   * Return a shallow copy of self.  New Set object contains same object
   * references as this.
   */
  public Set shallowCopy();
}
