/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/ref/AnyWeakReference.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.ref;

import com.inqwell.any.Any;
import com.inqwell.any.Iter;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Visitor;

/**
 * Extends the standard JDK SoftReference class to make it
 * a collectable Any and to form a base for other classes
 * wishing to store extra data with the reference.
 */
public class AnyWeakReference extends    java.lang.ref.WeakReference<Any>
                              implements WeakReference
{
  public AnyWeakReference (Any referent)
  {
    super (referent);
  }
  
  public AnyWeakReference (Any referent, AnyReferenceQueue q)
  {
    super (referent, q);
  }

  /**
   * Returns the referent as an Any.
   */
  public Any getAny()
  {
    return super.get();
  }
  
  public void clearAny()
  {
		super.clear();
  }

  public boolean isAnyEnqueued()
  {
		return super.isEnqueued();
  }
        
  public boolean enqueueAny()
  {
	return super.enqueue();
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
  
  public int hashCode()
  {
  	Object o = super.get();

  	if (o != null)
  	  return o.hashCode();
  	
  	return 0;
  }
  
  public boolean equals(Object o)
  {
  	Object r = super.get();

  	if (r == null)
  	  return false;
  	
  	return r.equals(o);
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
  
  public Object clone() throws CloneNotSupportedException
  {
    Any a = getAny();
    
    if (a != null)
      a = a.cloneAny();
    
    return new AnyWeakReference(a);
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
