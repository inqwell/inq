/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/cache/SoftCacheMap.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.server.cache;

import com.inqwell.any.*;
import com.inqwell.any.ref.*;

/**
 * The SoftCacheMap class is a Map whose values are kept as soft references.  No
 * special handling is required of the client - values are wrapped in soft
 * references as they are added to the cache and extracted as they are
 * retrieved.
 * <P>
 * A SoftCacheMap can be constructed to auto-clean itself, in which case
 * values are automatically removed when they become unreferenced.
 * Otherwise the cache is tidied when next accessed.
 * <p>
 * The SoftCacheMap class has a number of characteristics which differ from
 * conventional Maps:
 * <ul>
 * <li>
 * Unlike other <code>Map</code> implementations, the <code>Cache.get(k)</code>
 * class returns <code>null</code> for non-existant keys, rather than throwing
 * a runtime exception.
 * <li>
 * The <code>contains(k)</code> method can return <code>true</code> for
 * a given key while later <code>get(k)</code> can return <code>null</code>
 * for the same key.  This makes <code>contains(k)</code> of dubious
 * usefulness.  Indeed, <code>contains(k)</code> is not overridden in this
 * class so does not take account of the fact that the associated soft
 * reference might have been cleared, effectively meaning that the key
 * is a candidate for removal.  The <code>get(k)</code> method does so
 * clients can test for <code>null</code>.
 * </ul>
 * <p>
 * Cloning is not supported.  The mutating methods <code>add()</code> and
 * <code>remove()</code>, and the access method <code>get()</code> are
 * synchronized.  This ensures thread-safety given that any of these
 * methods can remove unreferenced entries from the cache.
 * By default the other methods of the <code>SoftCacheMap</code> class are not
 * synchronized.  Instances may
 * be wrapped in a com.inqwell.any.SynchronizedMap object to effect thread
 * safety overall but things like <code>entries()</code> are generally
 * not important enough to warrant the overhead of synchronization and
 * <code>contains()</code> cannot be relied on either.
 * <P>
 * A Cache can accept single or multi-valued entries.  If multi-valued then the
 * items added to the cache must be objects implementing com.inqwell.any.Array.
 * The SoftCacheMap class creates its own array object and wraps each child from
 * the given array in a soft reference.  Thus the client is free to
 * mutate its original array without affecting the SoftCacheMap.  The contents of
 * a Cache instance must be homogenous - it is illegal to mix single
 * and multi-valued entries.
 * <p>
 * A multi-valued cache entry will be removed when any of the values held against
 * the key become unreferenced.  This is distinct from deleting the value
 * from the set, for which the new method <code>remove (key, value)</code>
 * is provided.  This method preserves the cache entry if there are still
 * items in the multi-value set.
 */
public final class SoftCacheMap extends    AnyMap
																implements Cache
{
  static String cacheSweeper__ = "CacheSweeper";
  
	private CacheSweeper      sweeper_;
  
  // Internally created
	private AnyReferenceQueue q_;
  
  // Externally supplied
	private SweptReferenceQueue qExt_;
  
	private boolean         singleValued_;
  
  // This gets overwritten when the containing type is
  // fully resolved (and we are supporting metadata).
  // This initialisation is temporary but we can access
  // the loaded_ variable during various primordial
  // phases so the easiest thing to do is set it anyway.
  private IntI            loaded_ = new AnyInt();

  /**
   * Create a SoftCacheMap that auto cleans itself.
   * @param singleValued <code>true</code> if the cache accepts
   * single values, false if it accepts multi values.
   */
	public SoftCacheMap (boolean singleValued)
	{
		this(true, singleValued);
	}
	
  /**
   * Create a SoftCacheMap that holds single or multi value
   * objects and may auto clean itself.
   * @param autoClean when <code>true</code> the cache starts a
   * thread that will remove unreferenced objects from the cache.
   * Expensive on threads if there are many instances of this class
   * class constructed this way.
   * @param singleValued <code>true</code> if the cache accepts
   * single values, false if it accepts multi values.
   */
	public SoftCacheMap (boolean autoClean, boolean singleValued)
	{
		singleValued_ = singleValued;
		q_ = new AnyReferenceQueue();
		if (autoClean)
		{
			sweeper_ = new CacheSweeper();
			sweeper_.setDaemon(true);
      sweeper_.setName(cacheSweeper__);
			sweeper_.start();
		}
	}
	
	public SoftCacheMap (SweptReferenceQueue qExt, boolean singleValued)
  {
		singleValued_ = singleValued;
    qExt_ = qExt;
  }
  
	/**
	 * Fetch a value from the cache.
	 * @return <code>null</code> if there is no entry for the supplied key;
	 * a suitable Any otherwise.  If the key maps to a multi-valued object
	 * then a new com.inqwell.any.Array is returned which the client is free to
	 * mutate as it requires.
	 */
  public synchronized Any get (Any key)
  {
		processQueue();
    Any ret = (Any)getMap().get(key);
    
    if (ret != null)
    {
      // If this key represents a null entry, just return it.
      if (ret == AnyNull.instance())
        return ret;
        
			// Check for single or multi-value entry
			if (singleValued_)
			{
				ret = getSingle((AnySoftReference)ret, key);
			}
			else
			{
				ret = getArray((Array)ret, key);
			}
		}
    return ret;
  }

  public synchronized Any remove (Any key)
  {
		// Its OK to remove an object more than once.  This
		// could happen if more than one multi-valued item was
		// unreferenced.
		//System.out.println ("SoftCacheMap.add remove(Any) " + key);
		Any a = super.remove(key);
    
    if (a != null && !AnyNull.isNullInstance(a))
    {
      if (a instanceof Array)
      {
        Array arr = (Array)a;
        loaded_.setValue(loaded_.getValue() - arr.entries());
      }
      else
        loaded_.decrement();
    }
		return null;
  }
  
  /**
	 * Remove a (potentially) multi-valued element from the cache.
	 * This is a <code>Cache</code> specific removal method which
	 * accepts a value as well as a key.  If the key maps to
	 * anything other than an array the entry is removed.  If the
	 * map value is an array then the item in the array comparing
	 * for equality with the given value is removed and the cache
	 * entry is kept.
	 * <P>
	 * Note that the array should not hold duplicates as only a
	 * single item is removed from it.
	 */
	public synchronized void remove (Any key, Any value)
	{
		//System.out.println ("SoftCacheMap.remove(Any,Any) 1 " + value + " as " + key);
		processQueue();  // recursive but should be OK
	  Any a = super.get(key);
	  if (a != null)
	  {
	    if (singleValued_)
	    {
        //System.out.println ("SoftCacheMap.remove(Any,Any) 2 " + value + " as " + key);
        remove(key);
        // Decrement is done by remove(key)
	    }
	    else
	    {
        // Check for the null entry
        Object o = getMap().get(key);
        if (o == AnyNull.instance())
          getMap().remove(key);
        else
        {
          
  	      // this could be a performance problem with large arrays.
  	      // Investigate Set backed collections...
          //System.out.println ("SoftCacheMap.remove(Any,Any) 3 " + value + " as " + key);
  	      Array array = (Array)a;
  	      int i = array.indexOf(value);
  	      if (i >= 0)
  	      {
            //System.out.println ("SoftCacheMap.remove(Any,Any) 4 " + value + " as " + key);
  	        array.remove(i);
            loaded_.decrement();
          }
        }
	    }
	  }
	}
	
	public void destroy()
	{
    if (sweeper_ != null)
      sweeper_.interrupt();
	}
	
  public synchronized void purge()
  {
    empty();
  }

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException("Cloning not supported for " + getClass());
  }

  // Identity semantics for sweeper thread mapping
  public boolean equals(Object o)
  {
    return (o == this);
  }

  public boolean equals(Any a)
  {
    return (a == this);
  }

  public int hashCode()
  {
		return identity();
	}

	private Any getSingle (AnySoftReference sr, Any key)
	{
		Any ret = sr.getAny();
		if (ret == null)
		{
			// reference has been cleared so remove entry from map
			remove (key);
		}
		return ret;
	}
		 
	private Any getArray (Array a, Any key)
	{
		// Each item is held within a soft reference within the
		// cache's array.  Create a new strongly referenced version
		// for the client
		
		Array ret = AbstractComposite.array();
		
		Iter i = a.createIterator();
		while ((ret != null) && (i.hasNext()))
		{
			AnySoftReference sr = (AnySoftReference)i.next();
			
			Any item = sr.getAny();
			
			if (item == null)
			{
				// a child soft reference has been cleared, so remove it from
				// the cache as a whole and return null.
				remove(key);
				ret = null;
			}
			else
			{
				ret.add (item);
			}
		}
		return ret;
	}
	
	/**
	 * Adds <code>value</code> to the cache against <code>key</code>.
	 * The semantics of <code>add</code> are somewhat different for
	 * caches than other <code>Map</code> implementations:
	 * <ul>
	 * <li>If the cache is single valued the an exception is thrown for duplicate
	 * keys.  This is the same as for normal maps.
	 * <li>If the cache is multi-valued and the key is already present
	 * the value being added must not be an array or an exception is thrown.
	 * The value will be added to the existing multi-value entry.
	 * <li>If the cache is multi-valued and the key is not present the value
	 * being added must be an array.  Otherwise the add operation is ignored.
	 * </ul>
	 * <p>
	 */
  public synchronized void add (Any key, Any value)
  {
		processQueue();
    
    // Allow a null entry.  This will never be removed by a cache
    // cleanup because it is not a SoftReference value.
    // It doesn't count towards the number of loaded objects.
    if (value == AnyNull.instance())
    {
			handleDuplicates(key);			
      getMap().put (key, value);
      return;
    }
    else
    {
      // Its OK to replace an existing mapping to the null value with
      // non-null.
      Object o = getMap().get(key);
      if (o == AnyNull.instance())
        getMap().remove(key);
    }
    
		if (singleValued_)
		{
			handleDuplicates(key);			
			//System.out.println ("SoftCacheMap.add adding " + value + " as " + key);
			addSingle(key, value);
		}
		else
		{
			if (contains(key))
			{
				if (value instanceof Array)
					throw new DuplicateChildException ("Adding key: " + key);
					
//				System.out.println ("SoftCacheMap.add adding " + value + " as " + key);
				// Hmmm... important to call super.get() the way things are
				// implemented (based on AnyMap) as we want access to the
				// cache's actual array.
				Array a = (Array)super.get(key);
				a.add (wrapInSoftReference(key, value, q_));
        loaded_.increment();
			}
			else
			{
				if (value instanceof Array)
				{
//					System.out.println ("SoftCacheMap.add adding " + value + " as " + key);
					addArray (key, value);
				}
			}
		}
  }
  
	/**
	 * Query the cache and add the object if not present returning what
	 * was given.  This method is only supported if the cache is operating
	 * in single-value mode.  If an entry is present then that entry is
	 * returned.  Otherwise the object is added and returned.  This allows a
	 * particular cache to act as a reference list for all the objects within
	 * it.
	 */
  public synchronized Any uniqueReference(Any key, Any value)
  {
		if (!singleValued_)
		{
			throw new UnsupportedOperationException
				("uniqueReference() only for unique keys");
		}

		Any a = get(key);  // we want the real object, not its soft reference
		if (a == null)
		{
			//System.out.println ("SoftCacheMap.uniqueReference entering" + value);
			//System.out.println ("SoftCacheMap.uniqueReference against" + key);
			add(key, value);
			a = value;
		}
		return a;
  }
  
  
  public synchronized void expire(Transaction t) throws AnyException
  {
  	if (t != null)
  	{
	  	Iter i = createKeysIterator();
	  	while (i.hasNext())
	  	{
	  		Any a = i.next();
	  		a = this.get(a);
	  		if (a == null)
	  		  continue;
	  		if (a.isTransactional())
	  		{
	  			Map m = (Map)a;
	  			m.setTransactional(false);
          m.getDescriptor().expire(m, t);
	  		}
	  	}
  	}
  	empty();
  }
  
  public void setStaticticsVariables(IntI loaded)
  {
    loaded_ = loaded;
  }
  
	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void handleNotExist(Any key) {}
	
  protected void emptying()
  {
    loaded_.setValue(0);
  }
  
	private void addSingle (Any key, Any value)
	{
		CacheSoftReference csr = wrapInSoftReference(key, value, q_);
		getMap().put (key, csr);
    loaded_.increment();
	}
	
	private void addArray (Any key, Any value)
	{
		Array clientArray = (Array)value;
		
		if (clientArray.entries() == 0)
			return;

		Array cacheArray  = new CacheArray();
		
		Iter i = clientArray.createIterator();
		
		while (i.hasNext())
		{
			CacheSoftReference csr;
			Any item = i.next();
		
			csr = wrapInSoftReference (key, item, q_);
			cacheArray.add (csr);
      loaded_.increment();
		}
		getMap().put (key, cacheArray);
	}
  
  // Replace a previously null cache entry with a real object
  private void replaceNullEntry(Any key, Any value)
  {
    this.remove(key);
    
    if (singleValued_)
    {
      addSingle(key, value);
    }
    else
    {
      Array cacheArray  = new CacheArray();
			CacheSoftReference csr = wrapInSoftReference (key, value, q_);
			cacheArray.add (csr);
      getMap().put (key, cacheArray);
      loaded_.increment();
		}
  }
	
	protected void finalize()
	{
		// if we are finalized then kill off our sweeper thread, if any
		if (sweeper_ != null)
			sweeper_.interrupt();
	}
	
	private CacheSoftReference wrapInSoftReference(Any key, Any value, AnyReferenceQueue q)
	{
    // The arg q is the internally allocated queue (where there is one).
    // There should only be one or the other
    if (qExt_ == null)
    {
      if (q != null)
        return new CacheSoftReference (key, value, q);
      else
        return new CacheSoftReference (key, value);
    }
    else
    {
      return new MappedCacheSoftReference (key,
                                           value,
                                           qExt_,
                                           this);
    }
	}
	
	private void processQueue()
	{
    if (q_ != null)
    {
      CacheSoftReference csr;
      while ((csr = (CacheSoftReference)q_.pollAny()) != null)
      {
        // the user data in the csr is the original key
        remove (csr.getData());
      }
    }
//    else if (qExt_ != null)
//      qExt_.processQueue();
	}
	
	private static class CacheArray extends AnyArray
	{
    // Override since array contains CacheSoftReferences and
    // this method is called with the referent.
    public int indexOf(Any a)
    {
      int max = this.entries();
      for (int i = 0; i < max; i++)
      {
        AnySoftReference asr = (AnySoftReference)this.get(i);
        if (asr.getAny().equals(a))
          return i;
      }
      return -1;
    }
	}

  // Use this class if the cache cleans itself with its own thread.
	private class CacheSweeper extends Thread
	{
		private CacheSweeper ()
		{
			super ();
		}
		
		// Wait at the reference queue and remove any keys that come through
		public void run() 
		{
			try
			{
				CacheSoftReference csr;
        while (true)
        {
          csr = (CacheSoftReference)SoftCacheMap.this.q_.removeAny();
          synchronized(SoftCacheMap.this)
          {
            SoftCacheMap.this.remove (csr.getData());
          }
        }
			}
			catch (InterruptedException e) {}
		}
	}
}
