/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/PrimaryTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.io.inq.RemoteTransaction;

/**
 * Base class for a two-phase commit policy transaction implementation.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.7 $
 * @see com.inqwell.any.Any
 */ 
public class PrimaryTransaction  extends    LocalTransaction
                                 implements Transaction,
                                            Cloneable
{
  private static final long serialVersionUID = 1L;

  private static Any identity__    = new ConstString("<identity>");

  // Map of the objects this transaction has locked
	private Set locked_  = AbstractComposite.set();
	
	// Map of the objects explicitly locked by the user
	private Set userLocked_  = AbstractComposite.set();
	
  private Transaction parent_ = null;
  private Transaction child_  = null;
  
  private Map         mutatorVars_ = AbstractComposite.simpleMap();
  
  private Map         remoteTransactions_ = AbstractComposite.simpleMap();
  
  private Map         identityFuncs_ = AbstractComposite.simpleMap();
  private Map         identityVals_  = AbstractComposite.simpleMap();
  
	public PrimaryTransaction(Process p)
	{
    super(p);
		init();
	}
	
	public PrimaryTransaction()
	{
		this(null);
	}

	/**
	 * Lock the object to give this transaction exclusive access.
	 * When the implementation calls <code>join()</code> defines
	 * whether the transaction's nature is optimistic or pessimistic.
	 */
	public void join (Map m) throws AnyException
	{
		if (!m.hasIdentity())
		{
			// objects which don't yet have identity semantics must be
			// created, not joined
			throw new TransactionException("Object is not yet created! " +
																		 m);
		}
			
		if (!m.isTransactional())
		{
			// may be the object was deleted while we were suspended
			// waiting to obtain the lock.
			throw new TransactionException("Object has been deleted! " +
																		 m);
		}
			
		// Lock the object
		lock(m);
	}
	
	/**
	 * Calls <code>join()</code> and creates a private instance for this
	 * transaction to work on.
	 */
	protected boolean doCopyOnWrite(Map m) throws AnyException
	{
    // See comments in super().
    if (getTransInstance(m) != m)
      return false;

    if (isDeleteMarked(m))
      throw new AnyException("Cannot modify instances scheduled for deletion");
    
    // Further, this will not fail any more as the check above includes
    // any parent transactions.
    if (parent_ != null)
      parent_.export(m, this);
    
    boolean ret = super.doCopyOnWrite(m);
    
    // When calling the mutator function later we have a problem in
    // that the 'old' value cannot be the txn public instance,
    // because transaction resolution always returns the 'new'
    // (i.e. private) instance as expected generally.  To avoid
    // this we have to pass a copy of the old (public) value but
    // it would be to slow and greedy to clone every time.  Keep
    // a prototype value to copy to for each distinct type of object
    // joined into the transaction.
    if (ret)
    {
      Descriptor d = m.getDescriptor();
      if (!mutatorVars_.contains(d))
      {
        Any mm = d.newInstance();
        mutatorVars_.add(d, mm);
      }
      
      // May be remember the old value as well for the event notification
      // phase. This is potentially expensive too but is configured
      // on a per typedef, per transaction basis from script so the
      // application is in control.
      rememberOld(m);
    }
    
    return ret;
	}
	
	/**
	 * Mark the given object as 'to be created' when this transaction
	 * commits.  If some event data has been provided, this will be
   * put into the creation event.
	 */
	protected Map doCreateIntent (Map m) throws AnyException
	{
    if (parent_ != null)
      parent_.export(m, this);
      
		if (m.hasIdentity())
		{
			// objects which have yet to be created should not have
			// identity semantics!
			throw new TransactionException("Object is already created! " +
																		 m);
		}
			
		// This could croak if the client deletes and then
		// creates the same object.  Maybe we should check for this
		// but leave it for the time being
    // And so...see below
		
	  Descriptor d = m.getDescriptor();
	  if (d == Descriptor.degenerateDescriptor__)
	    throw new AnyException("Not a typedef instance");
	  
		d.construct(m, this);
	  Map uniqueKey = d.getUniqueKey(m);
	  
    // Put the unique key value in now. Useful for script that wants
    // to access it before the instance is managed properly
    m.setUniqueKey(uniqueKey);
    
    Any deleted = null;
	  // Is there an instance that has been deleted?
    if (isDeleteMarkedUnique(uniqueKey))
    {
      deleted = getActiveObject(uniqueKey);
      
      // When there is a deleted instance already in the transaction
      // we have to avoid duplicate child problems when adding the
      // pk-->instance mapping to the active object and commit function.
      // Do this by adding an extra "system" entry to the key map.
      // This becomes the key in the transaction's state maps only, not
      // in the instance's uniquekey holder (set already above) so
      // there are a few places where we have to check twice.
      uniqueKey = (Map)uniqueKey.cloneAny();
      uniqueKey.add(Transaction.recreate__, AnyBoolean.TRUE);
    }
    
    // Check if the instance is already joined in the transaction.
    // This will be the case if it has been deleted, so check for
    // this and allow to proceed if so. (Actually, now that we
    // have added the recreate__ flag to the pk map this can no
    // longer happen. Usefully, this reinstates the error of
    // creating the same instance twice, even in the face of
    // recreating a deleted one)
    if (isJoined(uniqueKey))// && deleted == null)
	  {
			// Already marked this unique key for creation.  This is an
			// error within the scope of this transaction
			throw new TransactionException("Unique Key Violation within transaction " +
																		 d +
																		 " with unique key " +
																		 uniqueKey);
		}
	  
		// Lock all the unique keys at the outset.  This ensures we are
		// safe throughout the transaction with respect to the
		// object we are trying to create.
    // Further, if we are creating an instance whose primary key is
    // the same as one already marked for deletion , check unique key
    // values of the deleted instance. If a key
    // value is the same as the one being created then all is well. If
    // not then the normal checks are performed to ensure unique key
    // integrity
	  Map allKeys = d.getAllKeys();
	  Iter i = allKeys.createIterator();
	  while (i.hasNext())
	  {
			KeyDef thisKey = (KeyDef)i.next();
      
      if (thisKey.isUnique())
      {
        Map thisKeyVal = thisKey.makeKeyVal(m);
        // Deletion only locks the instance, not the primary key, so this
        // test is still valid in the recreate case and with the "normal"
        // key value.
        if (alreadyLocked(thisKeyVal))
        {
          // Indicates an error whereby the primary unique key is not violated
          // (or it would already be joined) but one of the
          // other unique keys has been by a previous createIntent operation
          // within this transaction.
          throw new TransactionException("Unique Key Violation while creating " +
                                         d +
                                         " with unique key " +
                                         thisKey +
                                         " with a value of " + thisKeyVal);
        }
  
        lock(thisKeyVal);
  
        // Read the candidate object to see if it is already created but
        // not cached. 
        //if (thisKey.shouldCache() &&
        //    (d.read(process_, thisKey, thisKeyVal) != null))
        Any r = null;
        if ((r = d.read(process_, thisKey, thisKeyVal, 0)) != null)
        {
          // If we read the deleted instance then that's OK
          if (r != deleted)
          {
            // If the key value of the returned instance does not have the
            // same value as candidate object then still OK.  This means
            // we are either using an eligibility expression or not caching
            // but that the underlying i/o is using an inequality expression
            // that matches another object.
            Map readKeyVal = thisKey.makeKeyVal((Map)r);
            if (readKeyVal.equals(thisKeyVal))
            {
              // We can't continue this transaction because we are trying
              // to create an object which already exists.  We leave the clean-up
              // operation to our client.
              throw new TransactionException("Unique Key Violation while creating " +
                                             d +
                                             " with unique key " +
                                             thisKey + 
                                             " with a value of " +
                                             thisKeyVal);
            }
          }
        }
      }
      else
      {
        // For non-unique keys (that are cached) lock the key value to
        // protect the cache
        if (thisKey.shouldCache())
        {
          Map thisKeyVal = thisKey.makeKeyVal(m);
          lock(thisKeyVal);
        }
      }
		}
    
    // If there is an identity function then call it to ascertain
    // that function's idea of what the identity of the instance
    // we are creating.
    Any id = null;
    if (identityFuncs_.contains(d))
    {
  	  // Clone the candidate instance now so the identity
    	// function can't modify it, seeing as we've done all
    	// the unique key stuff
    	m = (Map)m.cloneAny();
      Func f = (Func)identityFuncs_.get(d);
      id = execIdentityFunc(d, f, m);
      if (identityVals_.contains(id))
        throw new AnyException("Identity violation of " + m +
                               " with " + identityVals_.get(id));
    }
		
		// Now we know we know we can create the object put it in
		// the participants_ list.  Clone the original map if not
    // already done so in case the client wants to reuse it to
    // create subsequent objects.
    if (id != null)
    {
      identityVals_.add(id, m);
	    addActiveObject(uniqueKey, m);
    }
    else
	    addActiveObject(uniqueKey, m = (Map)m.cloneAny());
    
    return uniqueKey;
	}
	
  protected Map doIsCreateMarked(Map m) throws AnyException
  {
    Descriptor d = m.getDescriptor();
    if (!identityFuncs_.contains(d))
      return CREATE_NOID;
      
    Func f = (Func)identityFuncs_.get(d);
    Any id = execIdentityFunc(d, f, m);
    if (identityVals_.contains(id))
      return (Map)identityVals_.get(id);
    else
      return CREATE_NO;
  }
  
	/**
	 * Mark the given object as 'to be deleted' when this transaction
	 * commits.
	protected Map doDeleteIntent (Map m) throws AnyException
	{
    if (parent_ != null)
      parent_.export(m, this);
    
    return super.doDeleteIntent(m);
	}
	 */

	/**
	 * Mark the given object as 'to be deleted' when this transaction
	 * commits.
	 */
	protected Map doDeleteIntent (Map m) throws AnyException
	{
    if (parent_ != null)
      parent_.export(m, this);
    
    // Check if we are deleting an object that was previosuly created
    // in this transaction. If so, the action is simply to remove it
    // from the transaction
	  Descriptor d = m.getDescriptor();
    Map m1 = isCreateMarked(m);
    if (m1 != null)
    {
      // The object has already been joined in this transaction
      // for create. Just remove it from the structures and
      // perform any unlocks it has. 

      Map k = null;
      
      Map allKeys = d.getAllKeys();
      Iter i = allKeys.createIterator();
      while (i.hasNext())
      {
        KeyDef thisKey = (KeyDef)i.next();
        
        if (thisKey.isUnique())
        {
          Map thisKeyVal = thisKey.makeKeyVal(m1);
          removeTransactionLock(thisKeyVal);
          if (thisKey.isPrimary())
            k = thisKeyVal;  // we need to return the primary key
        }
        else
        {
          if (thisKey.shouldCache())
          {
            Map thisKeyVal = thisKey.makeKeyVal(m1);
            removeTransactionLock(thisKeyVal);
          }
        }
      }
      
      // Check for (real) delete followed by recreate.
      if (isDeleteMarkedUnique(k))
      {
        k = (Map)k.cloneAny();
        k.add(Transaction.recreate__, AnyBoolean.TRUE);
        removeActiveObject(k);
      }
      else
  		  removeActiveObject(k);
      
      // If there's an identity function, remove the id value
      if (identityFuncs_.contains(d))
      {
        Func f = (Func)identityFuncs_.get(d);
        Any id = execIdentityFunc(d, f, m1);
        identityVals_.remove(id);
      }

      // Run the destructor in case this is required to delete any
      // dependent objects that were created. Note that while the
      // method isCreateMarked can accept a key or an instance, this
      // really means that we should be passed an instance, as that is
      // what a destructor will expect. Not sure what this means for
      // destroying dependent objects as we could only obtain their
      // reference by reading them out and that doesn't happen for
      // objects that have not been managed (that is not yet, anyway).
      d.destroy(m1, this);
      return k;
    }
    
		if (!m.isTransactional())
		{
			throw new TransactionException("Attempt to delete non-transactional object " +
																		 m);
		}
			
	  //Map uniqueKey = d.getUniqueKey(m);
	  Map uniqueKey = (Map)m.getUniqueKey();
	  
//		System.out.println ("deleteIntent participants_: " + participants_);
	  if (isJoined(uniqueKey))
	  {
			// Already marked this unique key for deletion.  This is an
			// error within the scope of this transaction but shouldn't
			// generally happen as the reference to deleted objects should
			// be removed from the user structure prior to entering this
			// method.
			throw new TransactionException("Double-delete within transaction " +
																		 d +
																		 " of object " +
																		 m);
		}
	  
	  if (isJoined(m))
	  {
		  // Object has already been joined in this transaction
		  // for update.  Change its status to delete.
		  removeActiveObject(m);
		}
		else
		{
		  join(m);
//		  System.out.println ("deleteIntent participants_: " + participants_);
		
		}
    addActiveObject(uniqueKey, m);
    
	  return uniqueKey;
	}


	protected Any doResync(Map    keyVal,
                         Map    cachedObject,
                         Map    readObject) throws AnyException
	{
    // readObject == null implies external object deletion
    
    if (alreadyLocked(keyVal))
    {
      // Indicates an error whereby the primary unique key is not violated
      // (or it would already be in the participants_ list) but one of the
      // other unique keys has been by a previous createIntent operation
      // within this transaction.
      throw new TransactionException("Unique Key Violation while resyncing " +
                                     readObject +
                                     " with unique key " +
                                     keyVal);
    }

    lock(keyVal);      // stop anyone creating the object or resync collision

    if (cachedObject != null)
      join(cachedObject);  // already cached (may be referred to) and will be updated

    Any participantsKey = (cachedObject != null) ? cachedObject : readObject;
    
    addActiveObject(participantsKey, readObject);
    
    return participantsKey;
	}

	protected void doAbort() throws AnyException
	{
    // Belt-and-braces to ensure any locks are released on exception.
    if (child_ != null)
      child_.abort();
	}
	
	// Determine whether any key fields have changed and if so whether
	// the changes violate any uniqueness.
	protected void validateKeyChanges(Map objs,
                                    Map commitFuncPhase1) throws AnyException
	{
    Map fieldsChanging = getFields();
    
    Map c = AbstractComposite.simpleMap();
    
    // Drive the list by the supplied set of objects (see below also). The
    // exact set to process is contained in fieldsChanging, this set
    // could change when calls to mutators are made.  Also, we don't make
    // special provision for a repeating alternate set of fields-changing
    // objects in these circumstances, whereas we do with the active
    // objects. Note that once we enter this method a new set of objects
    // can be created as a consequence of mutator fns.
    Iter i = objs.createKeysIterator();
    
    while (i.hasNext())
    {
      Any a = i.next();
      // Further to the above, check if the current object is contained
      // in the fieldsChanging set.  The supplied set can contain information
      // relating to creations and deletions as well.
      if (!fieldsChanging.contains(a))
        continue;
        
      Map o = (Map)a;
      Set f = (Set)fieldsChanging.get(o);
      Set k = null;
      
      Descriptor d = o.getDescriptor();
      
      // Fetch the transaction's private instance
      Map m = (Map)objs.get(o);

      // true when resync and no existing cache entry.
      // Note that this means that resync is only valid on
      // the hosting server (i.e. cannot be performed on
      // a remote client server) as the any given remote client
      // may not have a local cache entry when the hosting server
      // does have one.
      if (m == o)
        continue;
      
      // Check if no fields changed.  If so, change phase 1 func
      // to no-op
      if (f.entries() == 0)
      {
        commitFuncPhase1.replaceItem(o, getNoOpPhase1());
        continue;
      }
      
      // Run the mutator
      Map oo = (Map)mutatorVars_.get(d);
      oo.copyFrom(o);
      d.mutate(o, oo, c, this);
        
      // If there is an identity expression in the transaction
      // for this typedef, check for identity violation
      if (identityFuncs_.contains(d))
      {
        Func idf = (Func)identityFuncs_.get(d);
        Any id = execIdentityFunc(d, idf, o);
        if (identityVals_.contains(id))
          throw new AnyException("Identity violation of " + m +
                                 " with " + identityVals_.get(id));
      }

      // Check each unique key's fields to see they comprise
      // any of the fields we are changing.
      
      Map uniqueKeys = d.getAllKeys();
      Iter ik = uniqueKeys.createIterator();
      while (ik.hasNext())
      {
        KeyDef thisKey = (KeyDef)ik.next();
        boolean keyModified = false;
        
        //if (f.containsAny(thisKey.getFields()) && thisKey.shouldCache())
        if (f.containsAny(thisKey.getFields()) ||
            thisKey.getFields().isEmpty())
        {
          if (thisKey.isUnique())
          {
            // We are changing a unique key.  In fact, the field(s)
            // may have been assigned, but could still be the same
            // value as before, so check by reading by the key.
            // If we get the same object the key hasn't changed.
            // Also check if we are changing the key value to something
            // we have created in this transaction.  This ensures
            // we catch such errors earlier than we otherwise would.
            // Make the key value from the changed object
            Map newKeyVal = thisKey.makeKeyVal(m);
            Map oldKeyVal = thisKey.makeKeyVal(o);

            if (newKeyVal.equals(oldKeyVal))
            {
              continue;
            }
            
            if (thisKey == d.getPrimaryKey())
              throw new TransactionException("Modifying primary key (" + d.getFQName() + ") from " +
                                             oldKeyVal + " to " +
                                             newKeyVal);
              
            if (alreadyLocked(newKeyVal))
            {
              // Indicates an error whereby the unique key is violated
              // by some other operation within this transaction.
              throw new TransactionException("Unique Key Violation while modifying to " +
                                             m +
                                             " with unique key " +
                                             thisKey);
            }
            lock(newKeyVal);
            // Read the candidate object to see if it is already created but
            // not cached
            Any existing = d.read(process_, thisKey, newKeyVal, 0);
            if (existing != null && existing != o)
            {
              // We can't continue this transaction because we are trying
              // to create an object which already exists.  We leave the clean-up
              // operation to our client.
              throw new TransactionException("Unique Key Violation while modifying to " +
                                             m +
                                             " with unique key " +
                                             thisKey);
            }
            keyModified = true;
          }
          else
          {
            // For non-unique keys just take the trouble to verify whether
            // the key has changed.  Optimises phase 2
            if (thisKey.shouldCache())
            {
              Map okv = thisKey.makeKeyVal(o);
              Map nkv = thisKey.makeKeyVal(m);
              if (!nkv.equals(okv))
                keyModified = true;
              
              // Lock the new key value to protect the cache.  This
              // could be done later, in UpdatePhase1 just before writing
              // out the object, but we have the key value here so its
              // more convenient to do it now.
              if (keyModified && thisKey.shouldCache() && !thisKey.isVolatile())
                lock(nkv);
            }
          }
          // Remember the (cached) keys that have changed for commit
          // phase 2. Keys that are volatile are expired, so we don't need
          // to remember them.
          if (keyModified)
          {
            if (k == null && !thisKey.isVolatile())
            {
              k = AbstractComposite.set();
              keysChanged_.add(o, k);
            }
            if (thisKey.isVolatile())
              thisKey.expire(null);  // can't designate primary key as volatile anyway.
            else
              k.add(thisKey);
          }
        }
      }
		}
	}
	
  protected boolean doLock(Any a, long timeout) throws AnyException
  {
    return lock(a, true, timeout);
  }
  
  public void unlock(Any a) throws AnyException
  {
    // Only for user locks. If the lock is a transaction lock then do nothing.
    // The transaction commit or abort will release the lock and we do not
    // want user actions interfering with that.
    if (locked_.contains(a))
      return;
    
    if (!userLocked_.contains(a))
      throw new AnyException ("You do not hold the lock on " + a);
    
    if (Server.instance().unlock(process_, a) == 0)
      userLocked_.remove(a);
  }
  
  public void setChild(Transaction t)
  {
    if (t != null)
    {
      t.setProcess(getProcess());
      t.setParent(this);
    }
    child_ = t;
    if (child_ != null)
      child_.setGatherEvents(this.isGatheringEvents());
  }
  
  public Transaction getParent()
  {
    return parent_;
  }
  
  public void setParent(Transaction t)
  {
    if (parent_ != null && t != null)
      throw new IllegalArgumentException("Parent is already set");
      
    parent_ = t;
  }
  
  public void setIdentity(Descriptor d, Func f)
  {
//    if (identityFuncs_.contains(d))
//      throw new AnyRuntimeException("Already have an identity function for " + d);
    
    //System.out.println("Setting identity 1 " + d);
    //System.out.println("Setting identity 2 " + f);
    identityFuncs_.replaceItem(d, f);
  }
  
	protected void unlockAll() throws AnyException
	{
		Server.instance().unlockList(process_, locked_, true);
		Server.instance().unlockList(process_, userLocked_, true);
	}
	
	/**
	 * In fact a factory method the result of which will be a new empty
	 * transaction
	 */
	public Object clone() throws CloneNotSupportedException
	{
		Transaction t = (Transaction)super.clone();
		init();
		return t;
	}
	
	protected void updatingPhase2(Map pubInstance,
                                Map pvtInstance,
                                Set keysChanged) throws AnyException
	{
    RemoteTransaction rt = getRemoteTransaction(pubInstance.getDescriptor());
    if (rt != null)
    {
      rt.setKeyChanges(pubInstance, keysChanged);

      // Because this method gets called after phase 2 has been
      // done for this object these two objects will be equal,
      // [They wouldn't compare pubInstance.equals(pvtInstance)
      // because pubInstance operates identity semantics]
      // However, when the RemoteTransaction (and therefore the
      // participants) are sent to the host for commit the
      // serialization policy for identity-decorated objects is that
      // the decorator is the keyed class and the underlying
      // unique key of the object is substituted.  This is resolved
      // to the cached object in the server host, which still has
      // the old values. Altogether spooky!
      rt.copyOnWrite(pubInstance);
    }
	}
	
	protected void creatingPhase2(Any uniqueKey,
                                Map pvtInstance) throws AnyException
	{
    // The map we are passed is the unmanaged one (the map has been
    // managed but we are given the one inside the decorator, so its
    // not really private)
    RemoteTransaction rt = getRemoteTransaction(pvtInstance.getDescriptor());
    if (rt != null)
    {
      Any eventData = null;
      Map m = getCreateEventData();
      if (m.contains(uniqueKey))
        eventData = m.get(uniqueKey);
        
      rt.createIntent(pvtInstance, eventData);
    }
	}
	
	protected void deletingPhase2(Any uniqueKey,
                                Map unmgInstance) throws AnyException
	{
	}
	
  protected void doEvents(Map createEventData) throws AnyException
  {
    super.doEvents(createEventData);
    if (parent_ != null)
      parent_.exportEvents(getEventBundle(), this);
  }
  
	protected void doReset()
	{
		// Empty all our tables in case this transaction will be reused
		locked_.empty();
		userLocked_.empty();
		remoteTransactions_.empty();
    setParent(null);
    setChild(null);
    mutatorVars_.empty();
    identityFuncs_.empty();
    identityVals_.empty();
    super.doReset();
	}
	
  private boolean lock(Any a) throws AnyException
  {
    return lock(a, false, -1);
  }
	
	private boolean lock(Any a, boolean userLock, long timeout) throws AnyException
	{
    Set lockTable = userLock ? userLocked_
                             : locked_;

    //System.out.println("PrimaryTransaction.lock() " + a);
    //System.out.println("PrimaryTransaction.lock() " + userLock);
    boolean ret = true;
    
    // Although the LockManager allows us to nest locks we don't
    // do that when locking for transaction purposes.  Makes things
    // easier with nested transactions.
		if (!lockTable.contains(a))
		{
      // If there is already a transaction lock then don't enter as
      // a user lock.  If there is a user lock and we want a transaction
      // lock then transfer as such.  Note that this could mean that
      // a nested user lock becomes a transaction lock, which we said
      // above we don't nest....
      if (userLock && locked_.contains(a))
        return true;
      
      if (!userLock && userLocked_.contains(a))
      {
        userLocked_.remove(a);
        locked_.add(a);
        //System.out.println("PROMOTING LOCK " + a);
        return true;
      }
      
      // Check if object is already held by us in the lock manager. This
      // could be the case if we are importing an object from an ancestor
      // transaction.
      if (Server.instance().locker(a) != getProcess())
        ret = Server.instance().lock(process_, a, timeout);
      else
        ret = true;
      
      if (ret)
			  lockTable.add(a);
	  }
	  else if (userLock)
    {
      // Nest user locks
			ret = Server.instance().lock(process_, a, timeout);
    }
			
	  return ret;
	}
	
	private boolean alreadyLocked(Any a) throws AnyException
	{
    // We are only interested in the transaction locks, not the
    // user locks, so this is assumed.  We check the local lock
    // table as, even if the object is locked with the LockManager,
    // the object is not considered transaction locked if it is
    // not contained within this transaction.  Supports import of
    // object from an ancestor transaction.
		if (locked_.contains(a) && Server.instance().locker(a) == process_)
			return true;
		else
			return false;
	}
	
  private void removeTransactionLock(Any a) throws AnyException
  {
    Server.instance().unlock(process_, a);
    locked_.remove(a);
  }
  
	private RemoteTransaction getRemoteTransaction(Descriptor d)
	{
    RemoteTransaction rt = null;
    
    DomainAgent da = d.getDomainAgent();
    
    if (da != null)
    {
      Any hostId = da.getHostId();
      if (remoteTransactions_.contains(hostId))
      {
        rt = (RemoteTransaction)remoteTransactions_.get(hostId);
      }
      else
      {
        // while abstract rt = new RemoteTransaction(getProcess(), hostId);
      }
    }
    return rt;
  }
	
  private Any execIdentityFunc(Descriptor d, Func f, Map instance) throws AnyException
  {
    Any ret = null;
    
    BOTDescriptor bd = (BOTDescriptor)d;
    
    // There's no need to clone the function as its exclusive to this
    // transaction and therefore thread.

    f.setTransaction(this);
    Call.CallStackEntry se = (Call.CallStackEntry)this.getCallStack().peek();
    int curLine = se.getLineNumber();
    se.setLineNumber(this.getLineNumber());
    this.getCallStack().push(new Call.CallStackEntry(bd.getBaseURL(), this.identity__));
    ret = f.execFunc(instance);
    this.getCallStack().pop();
    se.setLineNumber(curLine);

    return ret;
  }

  private void init()
	{
    setUpdatePhase1(new UpdatePhase1());
    setUpdatePhase2(new UpdatePhase2());
    setCreatePhase1(new CreatePhase1());
    setCreatePhase2(new CreatePhase2());
    setNoOpPhase1(new NoOpPhase1());

    setDeletePhase1(new DeletePhase1());
    setDeletePhase2(new DeletePhase2());
    setResyncPhase1(new ResyncPhase1());
    setResyncPhase2(new ResyncPhase2());
	}
	
	class UpdatePhase1 extends    CommitBase
										 implements Func
	{
		
		
		public Any exec(Any a) throws AnyException
		{
		  // We are passed the Map public instance...
			Map tm = (Map)a;
			
			// ...this maps to the item we should write
			Map  m  = (Map)processing_.get(tm);

      // write the private instance to primary persistent storage
			m.getDescriptor().write(process_, m);
			
			// if write succeeds then set up the abort function to restore
			// this object should that be necessary (map value should be
			// original Map)    (also set event function)

			setAbortFunc(getUpdatePhase2(), getUpdatePhase1(), tm, tm);

			return a;
		}
	}

	private class CreatePhase1 extends    CommitBase
                             implements Func
	{
		public Any exec(Any a) throws AnyException
		{
		  // We are passed a map representing the primary unique key of the
		  // object we are creating...
			Map k = (Map)a;
			
			// ...this maps to the object we should create.
			Map o  = (Map)processing_.get(k);

			Descriptor d = o.getDescriptor();
      
      // Lock all non-unique

			//d.write(process_, k, o);
			d.write(process_, o);
			
			// if write succeeds abort function should delete the object
			// we've just created
			// (also set the event function)
			
			setAbortFunc(getCreatePhase2(), getDeletePhase1(), k, o);

			return a;
		}
	}

	private class DeletePhase1 extends    CommitBase
                             implements Func
	{
		public Any exec(Any a) throws AnyException
		{
		  // We are passed a map representing the primary unique key of the
		  // object we are deleting...
			Map k = (Map)a;

			// ...this maps to the object we should delete.
			Map o  = (Map)processing_.get(k);
			
			Descriptor d = o.getDescriptor();

			d.delete(process_, k, o);

			// if delete succeeds abort function should create the object
			// we've just deleted
			// (also set the event function)
			
			setAbortFunc(getDeletePhase2(), getCreatePhase1(), k, o);

			return a;
		}
	}
	
	private class ResyncPhase1 extends CommitBase
                             implements Func
  {
		public Any exec(Any a) throws AnyException
		{
      // Nothing to do
      Map m = (Map)a;
			setAbortFunc(getResyncPhase2(), getResyncPhase1(), m, m);
      return a;
    }
  }
	
	private class NoOpPhase1 extends CommitBase
                           implements Func
  {
		public Any exec(Any a) throws AnyException
		{
      // Does nothing - takes the place of UpdatePhase1 if there
      // were no fields changed (detected when keys are validated)
      // Just remove the object from the active list so that
      //   1) it will not take part in phase 2
      //   2) it can rejoin if some other mutator subsequently
      //      does actually update the object
      // A bit round-about but its concurrent-modification safe
      // to manipulate the collections here.
      removeActiveObject(a);
      clearPhase1Action(a);
      return a;
    }
  }
}

