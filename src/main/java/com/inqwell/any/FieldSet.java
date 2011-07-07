/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/FieldSet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * A <code>Set</code> implementation intended for use in places where
 * tests for equality should yield <code>true</code> when the two sets
 * have overlapping members.  Such instances include components of
 * complex event types, where an event listener may be interested in
 * a <i>node updated</i> event only if it contains certain fields.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class FieldSet extends    SimpleSet
											implements Set,
																 Cloneable
{
  public FieldSet () { super(); }
  public FieldSet (int initialCapacity) { super(initialCapacity); }

	// hashCode() must return a fixed value since it cannot be based on
	// our contents and still maintain the standard contract with
	// equals()
	public int hashCode() { return 1; }
	
	/**
	 * Returns <code>true</code> if the given object is a <code>Set</code>
	 * and its set of objects overlaps that of this
	 */
	public boolean equals (Any a)
	{
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == this)
      return true;

    if (!(a instanceof Set))
      return false;
		 
    Iter i = a.createIterator();
    
    boolean contains = false;
    
    while (!contains && i.hasNext())
    {
			Any member = i.next();
			contains = this.contains(member);
		}
    return contains;
	}
}
