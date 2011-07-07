/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/cache/CacheMap.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.server.cache;

import com.inqwell.any.*;
import com.inqwell.any.ref.*;

/**
 * The CacheMap class is a Map whose values are kept as normal (i.e. strong)
 * references.
 * <P>
 * Cloning is not supported.  The mutating methods <code>add()</code> and
 * <code>remove()</code>, and the access method <code>get()</code> are
 * synchronized.  This ensures thread-safety.
 * <p>
 * By default the other methods of the <code>CacheMap</code> class are not
 * synchronized.  Instances may
 * be wrapped in a com.inqwell.any.SynchronizedMap object to effect thread
 * safety overall but things like <code>entries()</code> are generally
 * not important enough to warrant the overhead of synchronization and
 * cannot be relied on either.
 * <P>
 * A Cache can accept single or multi-valued entries.  If multi-valued then the
 * items added to the cache must be objects implementing com.inqwell.any.Array.
 * The CacheMap class creates its own array objects.  Thus the client is
 * free to mutate its original array without affecting the CacheMap.
 * The contents of
 * a Cache instance must be homogenous - it is illegal to mix single
 * and multi-valued entries.
 * <p>
 * A multi-valued cache entry will be removed when all of the values held
 * against the key are removed.  This is distinct from deleting the value
 * from the set, for which the new method <code>remove (key, value)</code>
 * is provided.  This method preserves the cache entry if there are still
 * items in the multi-value set.
 */
public final class CacheMap extends    AnyMap
														implements Cache
{
	private boolean           singleValued_;
  
  // The number of loaded objects in this cache
  private IntI            loaded_ = new AnyInt();

	public CacheMap (boolean singleValued)
	{
		singleValued_ = singleValued;
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
    Any ret = (Any)getMap().get(key);
    
    // If this key represents a null entry, just return it.
    if (ret == AnyNull.instance())
      return ret;
      
    if (ret != null)
    {
			// Check for single or multi-value entry
			if (!singleValued_)
			{
				ret = getArray((Array)ret);
			}
		}
    return ret;
  }

  public synchronized Any remove (Any key)
  {
		// Its OK to remove an object more than once? 
		//System.out.println ("CacheMap.add remove(Any) " + key);
		Any a = super.remove(key);
    
    if (a != null)
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
		//System.out.println ("CacheMap.remove(Any,Any) " + value + " as " + key);
	  Any a = super.get(key);
	  if (a != null)
	  {
	    if (singleValued_)
	    {
	      remove(key);
        // Decrement is done by remove(key)
	    }
	    else
	    {
	      // this could be a performance problem with large arrays.
	      // Investigate Set backed collections...
	      Array array = (Array)a;
	      int i = array.indexOf(value);
				//System.out.println ("CacheMap.remove(Any,Any) looking for " + value + " in array and found index " + i);
	      if (i >= 0)
        {
	        array.remove(i);
          loaded_.decrement();
        }
	    }
	  }
	}
	
  public void setStaticticsVariables(IntI loaded)
  {
    loaded_  = loaded;
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException("Cloning not supported for " + getClass());
  }

	private Any getArray (Array a)
	{
		// Each item is held within a soft reference within the
		// cache's array.  Create a new strongly referenced version
		// for the client
		
		return a.shallowCopy();
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
    // Allow a null entry. But don't include it in the count of objects
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
			//System.out.println ("CacheMap.add adding " + value + " as " + key);
			addSingle(key, value);
		}
		else
		{
			if (contains(key))
			{
				if (value instanceof Array)
					throw new DuplicateChildException ("Adding key: " + key);
					
				//System.out.println ("CacheMap.add adding " + value + " as " + key);
				// Hmmm... important to call super.get() the way things are
				// implemented (based on AnyMap) as we want access to the
				// cache's actual array.
				Array a = (Array)super.get(key);
				a.add (value);
			}
			else
			{
				if (value instanceof Array)
				{
					//System.out.println ("CacheMap.add adding " + value + " as " + key);
					addArray (key, value);
				}
				else
				{
					// Its not an array and the cache doesn't contain the value.
					// Since BOTs that use this cache are not persistent
					// (see CachingKey) this case represents the first
					// object ever to be created, so we add it to the cache as the
					// only member of this key.
					//System.out.println ("CacheMap.add adding non-persistent" + value + " as " + key);
					Array a = AbstractComposite.array();
					a.add(value);
					addArray (key, a);
					//System.out.println ("CacheMap.add non-persistent contents is " + this);
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

		Any a = get(key);
		if (a == null)
		{
			//System.out.println ("CacheMap.uniqueReference entering" + value);
			//System.out.println ("CacheMap.uniqueReference against" + key);
			add(key, value);
			a = value;
		}
		return a;
  }
  
  public synchronized void expire(Transaction t) throws AnyException
  {
  	if (t != null)
  	{
	  	Iter i = createIterator();
	  	while (i.hasNext())
	  	{
	  		Any a = i.next();
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
  
	public void destroy()
	{
	}

  public void purge()
  {
  }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	
  protected void emptying()
  {
    loaded_.setValue(0);
  }
  
	private void addSingle (Any key, Any value)
	{
		getMap().put (key, value);
    loaded_.increment();
	}
	
	private void addArray (Any key, Any value)
	{
		Array clientArray = (Array)value;
		
		if (clientArray.entries() == 0)
			return;

		getMap().put (key, clientArray.shallowCopy());
    loaded_.setValue(loaded_.getValue() + clientArray.entries());
	}
}
