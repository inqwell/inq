/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/Stack.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

/**
 * A stack of objects
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */

public interface Stack extends Composite
{
  /**
   * Add an object to the stack.
   */
  public void push (Any a);
  
  /**
   * Remove an object.
   */
  public Any pop ();

  public Any peek ();
  
  /**
   * Expose the underlying java Stack implementation.
   */
  public java.util.Stack getStack ();

  /**
   * Checks for the existence of the given object.
   * Overrides contains() in com.inqwell.any.Composite
   * @return true if this contains the given object; false otherwise
   */
  public boolean contains (Any a);

  /**
   * Return a shallow copy of self.  New Stack object contains same object
   * references as this.
   */
  public Stack shallowCopy();
}
