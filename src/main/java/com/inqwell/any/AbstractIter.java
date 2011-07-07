/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractIter.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

public abstract class AbstractIter extends    AbstractAny
                                   implements Iter
{
  // What we are iterating over
  private Any iterRoot_;
  
	public void add(Any a)
	{
		throw  new UnsupportedOperationException(getClass() + ".add()");
	}
	
  public Any previous()
	{
		throw  new UnsupportedOperationException(getClass() + ".previous()");
	}
  
  public Any getIterRoot()
  {
    return iterRoot_;
  }
  
  protected void setIterRoot(Any root)
  {
    iterRoot_ = root;
  }
}
