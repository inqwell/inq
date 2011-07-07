/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/ref/AnyReferenceQueue.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.ref;

import java.lang.ref.ReferenceQueue;
import com.inqwell.any.Any;
import com.inqwell.any.Iter;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Visitor;

/**
 * Extends the standard JDK ReferenceQueue class to make it
 * a collectable Any and to return enqueued reference objects
 * as AnySoftReference instances.
 */
public class AnyReferenceQueue extends    ReferenceQueue
                               implements Any
{
  public AnyReferenceQueue()
  {
  }
  
  /**
   * Returns the enqueued reference as an Any.
   */
  public Reference pollAny()
  {
    return (Reference)super.poll();
  }
  
  public Reference removeAny() throws InterruptedException
  {
	return (Reference)super.remove();
  }
  
  public Reference removeAny(long timeout) throws InterruptedException
  {
	return (Reference)super.remove(timeout);
  }
  
  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    throw new IllegalArgumentException ("accept() not supported");
  }

  public Any copyFrom (Any a)
  {
    throw new IllegalArgumentException ("copyFrom() not supported");
  }
  
  public Any buildNew (Any a)
  {
    throw new IllegalArgumentException ("buildNew() not supported");
  }

  public boolean like (Any a)
  {
    return false;
  }

  public boolean isTransactional()
  {
		return false;
  }

  public boolean isConst()
  {
    return false;
  }
    
  public Any bestowConstness()
  {
    return this;
  }
  
  public final Any cloneAny ()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw (new IllegalArgumentException ("cloneAny exception: " +
                                           getClass().getName()));
    }
    return a;
  }
}
