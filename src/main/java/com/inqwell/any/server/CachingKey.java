/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any.server;

import com.inqwell.any.Descriptor;
import com.inqwell.any.KeyDef;
import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.IntI;
import com.inqwell.any.FloatI;
import com.inqwell.any.AnyFloat;
import com.inqwell.any.ConstString;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Array;
import com.inqwell.any.Map;
import com.inqwell.any.Iter;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.BooleanI;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractKeyDef;
import com.inqwell.any.Transaction;
import com.inqwell.any.server.cache.*;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.NullIO;
import com.inqwell.any.io.csv.CsvIO;
import com.inqwell.any.io.AnyIOException;
import com.inqwell.any.client.ClientKey;
import com.inqwell.any.client.StackTransaction;
import com.inqwell.any.ref.SweptReferenceQueue;
import java.io.ObjectStreamException;

//import com.inqwell.any.util.Util;

/**
 * A key definition implementation where the results are held in a
 * cache for future use by multiple threads.  Instances are
 * created according to the prototype held in an associated
 * <code>Descriptor</code> and they are <i>managed</i>, that is
 * they are given identity semantics, so that they can be held as
 * <code>keys</code> in <code>Map</code>s and still mutated.
 */
public class CachingKey extends    IoKey
												implements KeyDef
{
  private transient  Cache      cache_;
  private            CachingKey primary_;
  
  // May not be set but if it is then defines logic for
  // whether a key value is eligible for a given cache
  // entry.  When an object is managed then it may be
  // that its key value defined by this instance is eligible
  // for more than the single cache entry.
  // If the underlying persistence mechanism
  // supports arbitrary queries (for example, SQL) then
  // such a query could contain expressions like
  // "columnA <= x".  It would be possible for a given
  // value of x that instances should be entered into
  // multiple cache entries, any of those currently loaded
  // that satisfy this condition and not only the entry
  // whose key value == x. This expression is executed for
  // all currently loaded key values, so this facility
  // should be used with care as it causes a scan of the
  // cache entries each time an object is managed and
  // unmanaged and this scan is synchronized.
  // Each cache entry is tested for eligibility against
  // the supplied instance by generating its key value
  // and placing this and the cache value in a temporary
  // context. This context is then passed to the expression
  // so that paths like cache.x and key.x are viable.
  // The expression should return AnyBoolen(true) if the
  // key is eligible and AnyBoolen(false) if it is not.
  private Any        eligibilityExpr_;
  
  // When modelling several types in a single table it
  // may be necessary to further qualify instance
  // existence - when a single row in a table is used
  // to model instances of two or more types simultaneously.
  private Any        existsWhenExpr_;
  
  //private AnyInt   loaded_;
  private FloatI   hitRate_ = new AnyFloat();
  private int      reads_;
  private int      hits_;

  static private Any cache__    = new ConstString("cache");
  static private Any key__      = new ConstString("key");
  static private Any instance__ = new ConstString("instance");
  
	/**
	 * Make a new Caching Key definition.  There is a relationship between
	 * all keys for a given item and the primary key whereby the primary key's
	 * cache is used as the reference repository.  If instance(s) are read
	 * from persistent storage for this key definition because they not
	 * contained in this cache then, once we have those instance(s) we must
	 * check if they are already in the primary cache.  If they are then
	 * we must use those instances instead of the new ones we just read to
	 * achieve the goal of a single shared instance of a given object.  If
	 * they are not then, as the first creators of the instances, we add them
	 * to the primary cache.
	 */
  public CachingKey(String name, Any primary, boolean unique, boolean cached)
  {
    super(name, unique);
		primary_ = (CachingKey)primary;
    setShouldCache(cached);
		//setUnique (unique);
		//setName (new ConstString(name));
  }
  
  /**
	 * We need to be able to create the primary!
	 */
  public CachingKey(String name)
  {
		this(name, null, true, true);
		primary_ = this;
  }
  
  public Any read (Map keyVal, int maxCount) throws AnyException
  {
    // NB
    // could check for the system field "descriptor__" here just
    // to make sure that the argument is a genuine key.  Ensures
    // cache is not corrupted!
    
    // Precondition assumed is that the keyVal is locked
    // by the caller, so our own cache is protected by that lock,
    // that is amongst competing threads only one would perform
    // the physical read and the others would enjoy a cache hit.
    
    // Further, however, is the fact that non-primary keys reference
    // the primary cache to ensure their instances are common
    // across the VM. If any instance is in the primary cache then
    // that is used. If not then the instance becomes the common
    // one on its first occurrance and is lodged in the primary cache.
    // Thus, mutual exclusion of the primary and non-primary keys
    // is required:
    //   primary:     synchronize on this for the duration of the read
    //   non-primary: synchronize on primary_ while ensuring the
    //                unique reference (see ensureUniqueReference())
    
    Any ret;
    
    if (this == primary_)
    {
      synchronized(this)
      {
        ret = doRead(keyVal, maxCount);
      }
    }
    else
      ret = doRead(keyVal, maxCount);
    
    return ret;
  }
  
  private Any doRead(Map keyVal, int maxCount) throws AnyException
  {
    maxCount = getMaxCount(maxCount);
    
    // May be check the cache first
    Any ret = shouldCache() ? cache_.get(keyVal) : null;
    
    // If we decide to cache an object that is in persistent
    // storage but has values that means it does not exists then
    // check it here.
    if (ret != null && this == primary_ && !checkExists(ret))
      return null;
    
    // Its OK now to say we have committed to the read
    reads_++;
    
    if (ret == null)
    {
      if (isUnique())
      {
        // instances read from cache or persistent storage are by definition
        // managed, so we give them identity.
        //System.out.println ("CachingKey.read " + io_);
        //System.out.println ("CachingKey.read " + keyVal);
        ret = io_.read (keyVal,
                        ((Map)getDescriptor().newInstance()).bestowIdentity());
      }
      else
      {
        Array a = AbstractComposite.array();
        // putting identity semantics on to the prototype we pass
        // into the read method ensures that all the instances created
        // come out as such
        //System.out.println ("CachingKey.read multi " + io_);
        //System.out.println ("CachingKey.read multi" + keyVal);
        //System.out.println ("CachingKey.read multi" + getDescriptor());
        int i = io_.read(keyVal,
                         ((Map)getDescriptor().newInstance()).bestowIdentity(),
                         a,
                         maxCount);
        if (i != 0)
          ret = a;
      }
      ret = ensureUniqueReference(ret);
      
      // Lodge the returned map in the cache against the given key
      // If it is null then put in the AnyNull instance.
      if (shouldCache())
      {
        Any v = (ret == null) ? AnyNull.instance()
                              : ret;
        //System.out.println ("CachingKey (read) " + getName()  + Thread.currentThread() + " is adding " + keyVal + ", " + v);
        cache_.add(keyVal, v);
      }
    }
    else
    {
      //System.out.println ("Cache Hit ");// + ret);
      hits_++;
    }
    
    // Check for the null cache entry
    if (ret == AnyNull.instance())
      ret = null;
    
    hitRate_.setValue(((float)hits_ / reads_) * 100);
      
    // Even though we may have cached an object it still might
    // not 'exist' (never mind about the stats)
    if (ret != null && this == primary_ && !checkExists(ret))
      ret = null;
      
    return ret;
  }

	private Any ensureUniqueReference(Any value)
	{
		Any a = value;
		
		if (a != null)
		{
			if (this != primary_)
			{
        synchronized(primary_)
        {
  				if (isUnique())
  				{
  					a = primary_.uniqueReference((Map)value);
  					if (!primary_.checkExists(a))
              a = null;
          }
  				else
  				{
  					Array ar    = (Array)value;
  					Array newAr = AbstractComposite.array();
  					for (int i = 0; i < ar.entries(); i++)
  					{
  						Map m = (Map)ar.get(i);
  						Any u = primary_.uniqueReference(m);
  						if (primary_.checkExists(u))
                newAr.add(u);
  					}
            if (newAr.entries() != 0)
  					  a = newAr;
            else
              a = null;
  				}
        }
			}
			else
			{
				Map m = (Map)value;
				Map primaryUnique = m.getDescriptor().getUniqueKey(m);
				m.setUniqueKey(primaryUnique);
				//System.out.println ("CachingKey.ensureUniqueReference() setting Transactional....");
				m.setTransactional(true);
			}
		}
		return a;
	}
	
	private Any uniqueReference(Map value)
	{
		if (this == primary_)
		{
			Map primaryUnique = value.getDescriptor().getUniqueKey(value);
			//System.out.println ("CachingKey.uniqueReference() checking unique key " + primaryUnique);
			Any cached = cache_.uniqueReference(primaryUnique, value);
			if (cached == value)
			{
				// We just put this instance in the unique cache so put
				// the unique key into the map
				value.setUniqueKey(primaryUnique);
				//System.out.println ("CachingKey.uniqueReference() setting Transactional....");
				value.setTransactional(true);
			}
      else if (AnyNull.isNullInstance(cached))
      {
        // Getting this far means we've encountered the window in which
        // one thread has create the instance in external storage but not
        // yet completed managing it, while another has read the instance
        // from external storage.  Return null in this case.
        cached = null;
      }
			return cached;
		}
		else
		{
			return primary_.uniqueReference(value);
		}
	}
	
	public void manage (Map instanceVal)
	{
		//System.out.println ("CachingKey " + getName()  + " is managing " + instanceVal);
		if (this == primary_)
		{
			Map primaryUnique = instanceVal.getDescriptor().getUniqueKey(instanceVal);
			instanceVal.setUniqueKey(primaryUnique);
		}
		
		if (!shouldCache())
      return;
      
  	if (eligibilityExpr_ != null)
  	  addAllEligible(makeKeyVal(instanceVal), instanceVal);
  	else
    {
      Any k = makeKeyVal(instanceVal);
      //System.out.println ("CachingKey (manage) " + getName()  + Thread.currentThread() + " is adding " + k + ", " + instanceVal);
  	  cache_.add (k, instanceVal);
    }
	}
	
	public void unmanage (Map instanceVal)
	{
    if (!shouldCache())
      return;
      
		//System.out.println ("CachingKey " + getName()  + " is UNMANAGING " + instanceVal);
		if (eligibilityExpr_ != null)
  	  removeAllEligible(makeKeyVal(instanceVal), instanceVal);
  	else
	    cache_.remove (makeKeyVal(instanceVal), instanceVal);
	}
	
	public void resync (Transaction t, Map m) throws AnyException
  {
		if (this != primary_)
      throw new UnsupportedOperationException("resync() not on primary key");
    
    Map kv = makeKeyVal(m);
    
    synchronized(cache_)
    {
      // See if it is cached
      Map co  = (Map)cache_.get(kv);

      // Remove to force re-read
      cache_.remove(kv);

      // Now it isn't cached see if we can read it in.
      Map ro  = (Map)this.read (kv, 0);
      
      // if there was a value in the cache, put it back in case
      // any processes have references to it.  The transaction
      // will update its fields and raise the event on it.
      if (co != null)
      {
        cache_.remove(kv);
        cache_.add(kv, co);
      }
      else if (ro != null)
      {
      	// This state will result in instance 'creation' so
      	// remove the instance we have just read from the
      	// cache for when the object is subsequently managed
      	cache_.remove(kv);
      }

      t.resync(kv, co, ro);
    }
  }

	public void expire(Transaction t) throws AnyException
	{
    if (shouldCache())
      cache_.expire(t);
	}

  public void purge()
  {
    if (shouldCache())
      cache_.purge();
  }

  public void destroy()
  {
    if (shouldCache())
      cache_.destroy();
  }
  
  public Any getFromPrimary(Any key)
  {
    if (this != primary_)
      throw new IllegalArgumentException("Only supported on primary key");

    return cache_.get(key);
  }
  
//  public String toString()
//  {
//    StringBuffer s = new StringBuffer();
//    
//    s.append (super.toString());
//    
//    s.append ("IO Class: ").append(_io.toString()).append
//                                                (Util.lineSeparator());
//    return s.toString();
//  }
  
  public void setIO (PhysicalIO io, Descriptor d)
  {
    io_ = io;
    if (auxInfo_ != null)
    {
      if (!auxInfo_.contains(PhysicalIO.KEY_FIELDS))
        auxInfo_.add(PhysicalIO.KEY_FIELDS, this.getAllFields());
      
      if (!auxInfo_.contains(Descriptor.fieldOrder__))
      {
        //System.out.println("*** ADDING 1 " + d.getClass());
        if (d instanceof BOTDescriptor)
        {
          BOTDescriptor bd = (BOTDescriptor)d;
          //System.out.println("*** ADDING 2 " + bd.getFieldOrder());
          auxInfo_.add(Descriptor.fieldOrder__, bd.getFieldOrder());
        }
      }
      
      io_.setAuxInfo(auxInfo_, (Map)primary_.getAuxInfo());
    }
    
    if (io instanceof CsvIO)
    {
      CsvIO csvIo = (CsvIO)io;
      csvIo.setKey(this);
    }
    
    if (!shouldCache())
      return;
      
    //System.out.println ("cachingKey.setIO " + io_);
    isPersistent_ = (!(io instanceof NullIO));
    if (isPersistent_)
			cache_ = new SoftCacheMap(SweptReferenceQueue.getSweptReferenceQueue(),
                                isUnique());
		else
			cache_ = new CacheMap(isUnique());
  }

  public void setStaticticsVariables(IntI loaded, FloatI hitRate)
  {
    //loaded_  = loaded;
    if (shouldCache())
      cache_.setStaticticsVariables(loaded);
    hitRate_ = hitRate;
  }
  
  public void setEligibilityExpr(Any eligibilityExpr) throws AnyException
  {
  	//System.out.println("Setting Eligibility Expr " + eligibilityExpr);
  	//if (isUnique())
  	//  throw new AnyException("Eligibility expressions only for non-unique keys");
  	eligibilityExpr_ = eligibilityExpr;
  }

  public void setExistsWhenExpr(Any existsWhenExpr) throws AnyException
  {
  	//System.out.println("Setting Eligibility Expr " + eligibilityExpr);
  	if (!isPrimary())
  	  throw new AnyException("Exists expression only for primary key");
  	existsWhenExpr_ = existsWhenExpr;
  }

  public boolean isPrimary()
  {
    if (primary_ == null)
      throw new AnyRuntimeException("Primary Key not set!");
      
    return this == primary_;
  }
  
  public boolean isValid()
  {
//    if (!super.isValid())
//      return false;
//      
//    if ((isNative() || isUpdate()) && io_ == null)
//      return false;
      
    return true;
  }   

  private void addAllEligible(Map key, Map instanceVal)
  {
    if (!shouldCache())
      return;
      
  	Map context = AbstractComposite.simpleMap();
    
  	context.replaceItem(instance__,   instanceVal);
  	Transaction stackXaction = new StackTransaction();
    Any eligibilityExpr = eligibilityExpr_.cloneAny();
    
    synchronized(cache_)
    {
      Iter i = cache_.keys().createIterator();
      
      try
      {
        while (i.hasNext())
        {
          Any inCache;
          Any cache = i.next();
          context.replaceItem(key__, cache);
  
          BooleanI res = (BooleanI)EvalExpr.evalFunc
                                     (stackXaction,
                                      context,
                                      eligibilityExpr,
                                      BooleanI.class);
          if (res.getValue())
          {
            if (!isUnique() ||
                (inCache = cache_.get(cache)) == null ||
                (AnyNull.isNullInstance(inCache)))
            {
              //System.out.println ("CachingKey (eligible) " + getName()  + Thread.currentThread() + " is adding " + key + ", " + instanceVal);
              cache_.add (cache, instanceVal);
            }
            else
              throw new AnyRuntimeException
                ("Eligibility expression or key fields incorrectly configured adding " + key +
                    " whose cache entry is " + inCache + " attempting to add " + instanceVal);
          }
        }
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
		
  private void removeAllEligible(Map key, Map instanceVal)
  {
    if (!shouldCache())
      return;
      
  	Map context = AbstractComposite.simpleMap();
  	context.replaceItem(instance__,   instanceVal);
    
    Transaction stackXaction = new StackTransaction();
    Any eligibilityExpr = eligibilityExpr_.cloneAny();
      
    synchronized(cache_)
    {
    	Iter i = cache_.keys().createIterator();
    	try
    	{
		  	while (i.hasNext())
		  	{
		  		Any cache = i.next();
		  		context.replaceItem(key__, cache);
	
					BooleanI res = (BooleanI)EvalExpr.evalFunc
					                           (stackXaction,
																			context,
                                      eligibilityExpr,
																			BooleanI.class);
                                      
					if (res.getValue())
	  	      cache_.remove (cache, instanceVal);
		  	}
	  	}
    	catch (AnyException e)
    	{
    	  throw new RuntimeContainedException(e);
    	}
  	}
  }
  
  public boolean checkExists(Any instanceVal, Transaction t)
  {
    if (instanceVal == null)
      return false;
      
    boolean ret = true;
    
    if (existsWhenExpr_ == null)
      return ret;

    if (t == null)
    {
        t = new StackTransaction();
    }

    try
  	{
      BooleanI res = (BooleanI)EvalExpr.evalFunc
                                 (t,
                                  instanceVal,
                                  existsWhenExpr_,
                                  BooleanI.class);
			ret = res.getValue();
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
    return ret;
  }
		
  private boolean checkExists(Any instanceVal)
  {
    return checkExists(instanceVal, null);
  }
		
//	protected Object writeReplace() throws ObjectStreamException
//	{
//		ClientKey ck = new ClientKey(getName(),
//																 getDescriptor(),
//																 getFields(),
//																 isUnique(),
//																 isNative(),
//																 isForeign());
//		
//		return ck;
//	}
}
