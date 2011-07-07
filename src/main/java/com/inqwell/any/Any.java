/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Any.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;
import java.io.Serializable;

/**
 * Any defines the interface for all Inq collectable classes.
 */
public interface Any extends Serializable
{

  /**
   * Create an iterator which traverses the nodes rooted at
   * this.  If this is a leaf node then a
   * degenerate iterator is returned.
   * @return an implementation of Iter
   */
  public Iter createIterator ();

  /**
   * The Visitor Pattern from [GOF].  Arbitrary functionality is
   * defined in the particular implementation of the Visitor interface,
   * thus removing the need to define such functionality in
   * conventional methods.  accept() implementations can call
   * the visitor method appropriate to their type.
   */
  public void accept (Visitor v);

  /**
   * Assignment.  Throws IllegalArgumentException if this cannot be
   * copied from argument.
   */
  public Any copyFrom (Any a);

  /**
   * Factory method instantiation.  Throws IllegalArgumentException if this
   * cannot create new instances of itself. If the implementation can
   * copy the argument to initialise itself then it will do so.
   */
  public Any buildNew (Any a);

  /**
   * Cloning for Anys.  Attempts to call the standard method clone()
   * so implementing classes must also conform to the rules for
   * handling cloning.  Throws a runtime exception if a
   * CloneNotSupportedException is trapped.
   */
  public Any cloneAny ();

  /**
   * Whether the implementation has transactional semantics. This method
   * returns <code>true</code> if the implementation is prepared to
   * take part in a transaction, <code>false</code> otherwise.
   * @return <code>true</code> if the instance is transactional, in which
   * case it is submitted to the transaction when operations that are
   * defined as mutating are performed upon it. <code>false</code> if
   * the instance has no transactional semantics.
   */
  public boolean isTransactional();

  /**
   * Whether this instance is <code>const</code>. If <code>const</code> then
   * an instance is likely to throw a runtime exception
   * if {@link #copyFrom(Any)} is called.
   * @return <code>true</code> if the instance
   * is <code>const</code>, <code>false</code> otherwise.
   */
  public boolean isConst();

  /**
   * If possible, return a <code>const</code> version of </code>this</code>.
   * This method may return <code>this</code> even if <code>this</code> is
   * non-const, that is this method is not bound to return a <code>const</code>
   * version of the instance or even throw if it cannot (at the moment).
   * @return A <code>const</code> version of <code>this</code>. The return
   * value has the same property values and behaviour as the original;
   * <p>
   * <code>this</code> if the instance is already <code>const</code>;
   * <p>
   * <code>this</code> if the instance does not properly implement
   * constness, in which case {@link #isConst()} may still
   * return <code>false</code>.
   */
  public Any bestowConstness();
}
