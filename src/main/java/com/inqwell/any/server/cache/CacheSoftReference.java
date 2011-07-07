/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/cache/CacheSoftReference.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.server.cache;

import com.inqwell.any.ref.AnySoftReference;
import com.inqwell.any.ref.AnyReferenceQueue;
import com.inqwell.any.Any;

/**
 * An extension of com.inqwell.any.SoftReference which can hold an
 * Any instance as additional data.
 */
public class CacheSoftReference extends AnySoftReference
{
  private Any any_;
  
  public CacheSoftReference (Any data, Any referent)
  {
    super (referent);
    any_ = data;
  }
  
  public CacheSoftReference (Any data, Any referent, AnyReferenceQueue q)
  {
    super (referent, q);
    any_ = data;
  }
  
  /**
   * If this reference is associated with a cache then return it.
   */
  public Cache getCache()
  {
    return null;
  }
  
  public Any getData()
  {
    return any_;
  }
}
