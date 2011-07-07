/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * DegenerateIter is returned by any non-composite Any derived.  Used by
 * more than one type of Any derived but not available except via Iter
 * interface, hence package access.
 */
public class DegenerateIter extends AbstractIter implements Iter
{
	public static DegenerateIter i__ = new DegenerateIter();

	private DegenerateIter() {}
	
  /**
   * Always returns null as there are never any elements
   */
  public boolean hasNext() { return false; }

  /**
   * Always throws an exception as there are never any elements
   */
  public Any next()
  {
    throw new java.util.NoSuchElementException ("DegenerateIter");
  }

  public void remove()
  {
    throw (new java.lang.UnsupportedOperationException(getClass().toString()));
  }
};

