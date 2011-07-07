/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/ref/Reference.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.ref;

import com.inqwell.any.Any;

/**
 * Extends the standard JDK ReferenceQueue class to make it
 * a collectable Any and to return enqueued reference objects
 * as AnySoftReference instances.
 */
public interface Reference extends Any
{
  /**
   * Returns this reference object's referent.  If this reference object has
   * been cleared, either by the program or by the garbage collector, then
   * this method returns <code>null</code>.
   *
   * @return	 The Any to which this reference refers, or
   * <code>null</code> if this reference object has been cleared
   */
  public Any getAny();

  /**
   * Clears this reference object.  Invoking this method will not cause this
   * object to be enqueued.
   */
  public void clearAny();

    /**
     * Tells whether or not this reference object has been enqueued, either by
     * the program or by the garbage collector.	 If this reference object was
     * not registered with a queue when it was created, then this method will
     * always return <code>false</code>.
     *
     * @return	 <code>true</code> if and only if this reference object has
     * been enqueued
     */
  public boolean isAnyEnqueued();
    
    
  /**
   * Adds this reference object to the queue with which it is registered,
   * if any.
   *
   * @return	 <code>true</code> if this reference object was successfully
   *		 enqueued; <code>false</code> if it was already enqueued or if
   *		 it was not registered with a queue when it was created
   */
  public boolean enqueueAny();
}

