/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/cache/MappedCacheSoftReference.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.server.cache;

import com.inqwell.any.ref.AnyReferenceQueue;
import com.inqwell.any.Any;

public class MappedCacheSoftReference extends CacheSoftReference
{
  private Cache cache_;
  
  public MappedCacheSoftReference (Any data, Any referent)
  {
    super (data, referent);
  }
  
  public MappedCacheSoftReference (Any data, Any referent, AnyReferenceQueue q)
  {
    super (data, referent, q);
  }
  
  public MappedCacheSoftReference (Any               data,
                                   Any               referent,
                                   AnyReferenceQueue q,
                                   Cache             cache)
  {
    super (data, referent, q);
    cache_ = cache;
  }
  
  /**
   * If this reference is associated with a cache then return it.
   */
  public Cache getCache()
  {
    return cache_;
  }
}
