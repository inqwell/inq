/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NullF.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 * @version  $
 * @see 
 */

package com.inqwell.any;

/**
 * A <code>Func</code> that just returns <code>null</code>.  This
 * function can be used as a place-holder for service arguments that
 * are Funcs (for local invocations) that may not be supplied and
 * have no sensible default
 */
public class NullF extends    AbstractFunc
									implements Cloneable
{
	/**
	 * 
	 */
  public NullF()
  {
  }

  public Any exec(Any a) throws AnyException
  {
		return null;
  }
  
  public Iter createIterator ()
  {
  	return DegenerateIter.i__;
  }

  public Object clone () throws CloneNotSupportedException
  {
    NullF f = (NullF)super.clone();
    
    return f;
  }
}
