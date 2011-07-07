/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/TwoPhaseTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-07 16:53:31 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * Base class for a two-phase commit policy transaction implementation.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 * @see com.inqwell.any.Any
 */ 
public abstract class TwoPhaseTransaction  extends    AbstractTransaction
                                           implements Transaction,
                                                      Cloneable
{
  public static Map CREATE_NO   = null;
  public static Map CREATE_NOID = AbstractComposite.simpleMap();

  // Similar to above but for the objects abortable after phase 1
	private Map toBeAborted_;
	
	// The first phase commit action for a given object.  Every object in
	// the above maps should have a commit action.  This Map relates the
	// object to its commit (phase 1) function.
	private Map commitFuncPhase1_;
	
	// As above but for phase 2.
	private Map commitFuncPhase2_;
	
	// We keep a map of abort functions which is filled in as each
	// phase 1 commit function is executed.
	private Map abortFunc_;
  
  private boolean autoCommit_ = false;
  
  private Map createEventData_;
	
	// We need to know what process we are executing on behalf of
	protected Process process_;
	
	private boolean interrupted_  = false;
	private boolean active_       = false;
	private boolean commitActive_ = false;
	
	// Func objects to actually do the work for phase 1...
	private CommitBase   updatePhase1_;
	private CommitBase   createPhase1_;
	private CommitBase   deletePhase1_;
	private CommitBase   resyncPhase1_;
	private CommitBase   noOpPhase1_;
	 
	// ...and phase 2
	private CommitBase   updatePhase2_;
	private CommitBase   createPhase2_;
	private CommitBase   deletePhase2_;
	private CommitBase   resyncPhase2_;
	
	public TwoPhaseTransaction(Process p)
	{
		init();
		process_ = p;
	}
	
	public TwoPhaseTransaction()
	{
		this(null);
	}

	public void start(Any root) throws AnyException
	{
    active_ = true;
    doStart(root);
  }
  
  protected abstract void doStart(Any root) throws AnyException;
  
	public void copyOnWrite (Map m) throws AnyException
	{
		active_ = true;
		
    if (doCopyOnWrite(m))
		// *** commit func phase 1
      commitFuncPhase1_.add (m, updatePhase1_);
	}
	
	protected abstract boolean doCopyOnWrite(Map m) throws AnyException;
	
	protected void setUpdatePhase1(CommitBase f) { updatePhase1_ = f; }
	protected void setUpdatePhase2(CommitBase f) { updatePhase2_ = f; }
	protected void setCreatePhase1(CommitBase f) { createPhase1_ = f; }
	protected void setCreatePhase2(CommitBase f) { createPhase2_ = f; }
	protected void setDeletePhase1(CommitBase f) { deletePhase1_ = f; }
	protected void setDeletePhase2(CommitBase f) { deletePhase2_ = f; }
	protected void setResyncPhase1(CommitBase f) { resyncPhase1_ = f; }
	protected void setResyncPhase2(CommitBase f) { resyncPhase2_ = f; }
	protected void setNoOpPhase1(CommitBase f)   { noOpPhase1_ = f; }

	protected CommitBase getUpdatePhase1() { return updatePhase1_; }
	protected CommitBase getUpdatePhase2() { return updatePhase2_; }
	protected CommitBase getCreatePhase1() { return createPhase1_; }
	protected CommitBase getCreatePhase2() { return createPhase2_; }
	protected CommitBase getDeletePhase1() { return deletePhase1_; }
	protected CommitBase getDeletePhase2() { return deletePhase2_; }
	protected CommitBase getResyncPhase1() { return resyncPhase1_; }
	protected CommitBase getResyncPhase2() { return resyncPhase2_; }
	protected CommitBase getNoOpPhase1()   { return noOpPhase1_; }

  protected Any getPhase1Action(Any key)
  {
    return commitFuncPhase1_.get(key);
  }
  
  protected Any clearPhase1Action(Any key)
  {
    return commitFuncPhase1_.remove(key);
  }
  
	/**
	 * Mark the given object as 'to be created' when this transaction
	 * commits.
	 */
	public void createIntent (Map m, Any eventData) throws AnyException
	{
		active_ = true;

    Map uniqueKey = doCreateIntent(m);
    
		// *** commit func
		commitFuncPhase1_.add (uniqueKey, createPhase1_);
    
    // If there is some event data then remember it
    if (eventData != null)
      createEventData_.add(uniqueKey, eventData);
	}
  
  public boolean isActive()
  {
    return active_;
  }
  
  protected abstract Map doCreateIntent(Map m) throws AnyException;
  
  protected abstract Map doIsCreateMarked(Map m) throws AnyException;
  
	/**
	 * Mark the given object as 'to be deleted' when this transaction
	 * commits.
	 */
	public void deleteIntent (Map m) throws AnyException
	{
		active_ = true;
    
    Map uniqueKey = doDeleteIntent(m);
    
	  if (commitFuncPhase1_.contains(m) && m.isTransactional())
	  {
		  // Object has already been joined in this transaction
		  // for update.  Change its status to delete.
		  commitFuncPhase1_.remove(m);
		  commitFuncPhase1_.add (uniqueKey, deletePhase1_);
      m.getDescriptor().destroy(m, this);
		}
		else
		{
      if (commitFuncPhase1_.contains(uniqueKey) &&
        commitFuncPhase1_.get(uniqueKey) == createPhase1_)
      {
        // The object has already been joined in this transaction
        // for create. Just remove it from the structures and
        // perform any unlocks it has. 
        commitFuncPhase1_.remove(uniqueKey);
        
        if (createEventData_.contains(uniqueKey))
          createEventData_.remove(uniqueKey);
      }
      else
      {
        // normal delete
        commitFuncPhase1_.add (uniqueKey, deletePhase1_);
        m.getDescriptor().destroy(m, this);
      }
		}
	}

  public boolean isDeleteMarked(Map m)
  {
    Any uniqueKey = m.getUniqueKey();
    boolean ret = isDeleteMarkedUnique(uniqueKey);
    
    if (!ret)
    {
      // Not here, try any parent
      
      Transaction parent = getParent();
      if (parent != null)
        return parent.isDeleteMarked(m);
    }
    
    return ret;
  }
  
  protected boolean isDeleteMarkedUnique(Any pk)
  {
    if (commitFuncPhase1_.getIfContains(pk) == deletePhase1_)
      return true;
    return false;
  }
  
  public Map isCreateMarked(Map m) throws AnyException
  {
    Map createMarked = doIsCreateMarked(m);
    if (createMarked == CREATE_NO)
      return null;
    if (createMarked != CREATE_NOID)
      return createMarked;
      
    // If there's no handling of identity by the subclass then assume
    // unique key and do it here.
    Descriptor d = m.getDescriptor();
    Map        k = d.getUniqueKey(m);
    
    if (commitFuncPhase1_.contains(k) &&
        commitFuncPhase1_.get(k) == createPhase1_)
    {
      Map active = getActiveObjects();
      return (Map)active.get(k);
    }
    
    // Check again, this time with the key value that prevails when
    // we are re-creating a deleted instance. Check if we are deleting one
    // first, just to save a bit of time
    if (isDeleteMarkedUnique(k))
    {
      k = (Map)k.cloneAny();
      k.add(Transaction.recreate__, AnyBoolean.TRUE);
      
      if (commitFuncPhase1_.contains(k) &&
          commitFuncPhase1_.get(k) == createPhase1_)
      {
        Map active = getActiveObjects();
        return (Map)active.get(k);
      }
    }
    
    return null;
  }
  
  public Map getCreateList(Descriptor d)
  {
    // Note - doesn't consider any additions to the transaction context
    // that may have happened by stuff going on in mutate.
    
    Map ret = null;
    if (commitFuncPhase1_.entries() != 0)
    {
      Map active = getActiveObjects();
      
      ret = AbstractComposite.orderedMap();
      
      Iter i = commitFuncPhase1_.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        if (commitFuncPhase1_.get(k) == createPhase1_)
        {
          Map m = (Map)active.get(k);
          if (d == null || m.getDescriptor() == d)
          {
            // Make a new map containing the actual fields of the
            // instance in creation, making any of those that are
            // key fields const to protect them
            Map m1 = AbstractComposite.simpleMap();
            Iter ii = m.createKeysIterator();
            while (ii.hasNext())
            {
              Any kk = ii.next();
              Descriptor dd = m.getDescriptor();
              if (dd.isKeyField(kk))
                m1.add(kk, m.get(kk).bestowConstness());
              else
                m1.add(kk, m.get(kk));
            }
            ret.add(k, m1);  // No bestowConstness for maps, see AbstractMap.java
          }
        }
      }
      if (ret.entries() == 0)
        ret = null;
    }
    return ret;
  }

  public Map getModifyList(Descriptor d)
  {
    // Note - doesn't consider any additions to the transaction context
    // that may have happened by stuff going on in mutate.
    
    Map ret = null;
    if (commitFuncPhase1_.entries() != 0)
    {
      ret = AbstractComposite.orderedMap();
      
      Iter i = commitFuncPhase1_.createKeysIterator();
      while (i.hasNext())
      {
        Any a = i.next(); // instance in the update case
        if (commitFuncPhase1_.get(a) == updatePhase1_)
        {
          Map m = (Map)a;
          
          if (d == null || m.getDescriptor() == d)
            ret.add(m.getUniqueKey(), m);  // No need to protect - it's a managed instance
        }
      }
      if (ret.entries() == 0)
        ret = null;
    }
    return ret;
  }

  public Map getDeleteList(Descriptor d)
  {
    // Note - doesn't consider any additions to the transaction context
    // that may have happened by stuff going on in mutate.
    
    Map ret = null;
    if (commitFuncPhase1_.entries() != 0)
    {
      Map active = getActiveObjects();
      
      ret = AbstractComposite.orderedMap();
      
      Iter i = commitFuncPhase1_.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        if (commitFuncPhase1_.get(k) == deletePhase1_)
        {
          Map m = (Map)active.get(k);
          if (d == null || m.getDescriptor() == d)
            ret.add(k, m);  // It's managed and scheduled for delete. No const reqd.
        }
      }
      if (ret.entries() == 0)
        ret = null;
    }
    return ret;
  }

	public Process getProcess()
	{
		return process_;
	}
	
	public void setProcess(Process p)
	{
		process_ = p;
	}
	
	protected abstract Map doDeleteIntent (Map m) throws AnyException;
  
  protected boolean isCommitting()
  {
    return commitActive_;
  }
	
	public void resync(Map    keyVal,
                     Map    cachedObject,
                     Map    readObject) throws AnyException
	{
    if (cachedObject == null && readObject == null)
      return;
      
    Any participantsKey = doResync(keyVal, cachedObject, readObject);
    
		// Take the trouble to determine which fields have
		// changed, in the case of an update.
		if (cachedObject != null && readObject != null)
		{
      Iter i = cachedObject.createKeysIterator();
      while (i.hasNext())
      {
        Any f = i.next();
        if (!cachedObject.get(f).equals(readObject.get(f)))
          fieldChanging(cachedObject, f, null);
      }
    }
		
		// *** commit func phase 1
		commitFuncPhase1_.add (participantsKey, resyncPhase1_);
	}

	protected abstract Any doResync(Map    keyVal,
                                  Map    cachedObject,
                                  Map    readObject) throws AnyException;
  
  protected abstract boolean phase1Complete();
                          
	public void commit() throws AnyException
	{
    commitActive_ = true;
    
    this.getCallStack().push(new Call.CallStackEntry(Transaction.transaction__, Transaction.commit__));

    // Any child transaction will have already been committed
    // in its Try block
    do
    {
      stopOnInterrupt();
      
      Map m = getPhase1Objects();
      // Check if we are on a subsequent set of phase 1 objects
      // and if do add them into the overall participants
      if (m != getActiveObjects())
      {
        Map participants = getActiveObjects();
        Iter i = m.createKeysIterator();
        while (i.hasNext())
        {
          Any k = i.next();
          participants.add(k, m.get(k));
        }
      }
      
      validateKeyChanges(m, commitFuncPhase1_);
      
      doPhase1(m, commitFuncPhase1_);
    }
    while (!phase1Complete());
		
		// last chance saloon
		stopOnInterrupt();
    
    // The i/o is done, so if we get this far and we're not autocommit
    // release any resources
    if (!isAutoCommit())
    {
      releaseResources(false);
    }

		doPhase2(commitFuncPhase2_);
		
		mqCommit();
		
		execBeforeActions();
		doEvents(createEventData_);
		execAfterActions();
    
		unlockAll();
		
		reset();
		
    getCallStack().pop();
	}

	public void abort() throws AnyException
	{
    doAbort();

		// If this method throws then we're in big trouble.  Maybe we should
		// throw a different exception if the abort functions fail, since
		// this invalidates the external storage.  
		// *** process abort funcs
		try
		{
			runFuncs (toBeAborted_, abortFunc_, true);
			mqRollback();
		}
		
		finally
		{
			// unlock all our objects
      releaseResources(true);
			unlockAll();
			reset();
		}
	}
	
	protected abstract void doAbort() throws AnyException;
	
	public boolean canCommit() throws AnyException
	{
		// Since this model requires that we have acquired all locks necessary
		// by the time we get to the commit stage we can return true here.
		stopOnInterrupt();
		return true;
	}
  
  public boolean isAutoCommit()
  {
    return autoCommit_;
  }
	
	public void export(Map m, Transaction t) throws AnyException
	{
    boolean croak = false;
    // Check whether this transaction is doing anything with
    // the specified object and if so what type of operation it is.
    if (commitFuncPhase1_.contains(m) &&
        commitFuncPhase1_.get(m) == updatePhase1_)
    {
      // modify
      //participants_.remove(m);
      //locked_.remove(m);
      croak = true;
    }
    else
    {
      Descriptor d = m.getDescriptor();
      Map uniqueKey = d.getUniqueKey(m);
      if (commitFuncPhase1_.contains(uniqueKey))
      {
        if (commitFuncPhase1_.get(uniqueKey) == createPhase1_)
        {
          // create
          croak = true;
        }
        else
        {
          // delete. Note - no need to check for recreate as there will
          // be a delete as well in this case.
          croak = true;
        }
      }
    }
    
    if (croak)
      throw new TransactionException("Participating objects cannot be exported to a nested transaction yet");
    
    //if (parent_ != null)
      //parent_.export(m, t);
	}
	
  /**
   * Returns the Map of phase 1 objects.  This will either be
   * the participants or the set of objects that are added to
   * the transaction during a call to a mutator.  In the
   * latter case, once called the alternate set is also cleared,
   * so this method can only be called by subclasses when they
   * wish to perform final processing on this set.  For continued
   * access to whichever set is in effect
   * call <code>getActiveObjects()</code>.
   */
  protected abstract Map getPhase1Objects();

  protected abstract Map getActiveObjects();
  
  protected Map getCreateEventData()
  {
    return createEventData_;
  }

  /**
   *
   * Determine whether any key fields have changed on the objects
   * in set m and if so whether the changes violate any uniqueness.
   */
	protected abstract void validateKeyChanges(Map m, Map commitFuncP1) throws AnyException;
	
  /**
   * Perform the phase 1 commit action on the objects in set m
   */
	protected abstract void doPhase1(Map m, Map commitFuncPhase1) throws AnyException;

	protected abstract void doPhase2(Map commitFuncPhase2) throws AnyException;

	protected abstract void doEvents(Map createEventData) throws AnyException;

	protected abstract void unlockAll() throws AnyException;

  protected abstract void releaseResources(boolean aborting);
  
	public synchronized void interrupt() throws AnyException
	{
		if (active_)
			interrupted_ = true;
	}
	
	public Map getContext() { return process_.getContext(); }

	public Any getContextPath() { return process_.getContextPath(); }
	
	public Map getCurrentStackFrame() throws StackUnderflowException
	{
		return process_.getCurrentStackFrame();
	}

	public Map pushStackFrame() { return process_.pushStackFrame(); }
	
	public Map popStackFrame() throws StackUnderflowException
	{
		return process_.popStackFrame();
	}
	
	public boolean isDegenerate() { return false; }
	
	private void stopOnInterrupt() throws AnyException
	{
		if (interrupted_)
		{
			abort();
			throw new TransactionException ("Interrupted!");
		}
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
	
	protected void finalize() throws Throwable
	{
		// If we are unlucky enough to get finalized while in progress
		// then do our best to unwind anything we might have done.
		abort();
	}

	protected abstract boolean isJoined(Map m);
	
  public boolean lock(Any a, long timeout) throws AnyException
  {
    active_ = true;
    return doLock(a, timeout);
  }
  
  protected abstract boolean doLock(Any a, long timeout) throws AnyException;
  
  protected void doReset()
  {
		// Empty all our tables in case this transaction will be reused
		toBeAborted_.empty();
		commitFuncPhase1_.empty();
		commitFuncPhase2_.empty();
		abortFunc_.empty();
		createEventData_.empty();
		interrupted_  = false;
		active_       = false;
		commitActive_ = false;
    
    super.doReset();
  }
  
	protected void runFuncs(Map objectList, Map funcList, boolean aborting) throws AnyException
	{
    if (funcList == null)
      return;
      
		// we don't know what funcs we are doing so just set them all.
		// We could set during the iteration but the instances are the same
		// for all usages.
		updatePhase1_.setList(objectList, aborting);
		createPhase1_.setList(objectList, aborting);
		deletePhase1_.setList(objectList, aborting);
		updatePhase2_.setList(objectList, aborting);
		createPhase2_.setList(objectList, aborting);
		deletePhase2_.setList(objectList, aborting);
		resyncPhase1_.setList(objectList, aborting);
		resyncPhase2_.setList(objectList, aborting);
		noOpPhase1_.setList(objectList, aborting);
		
		Iter i;
		
		// do the work
		i = objectList.keys().createIterator();
		while (i.hasNext())
		{
		  Any a = i.next();
		  
		  Func f = (Func)funcList.get(a);
		  f.exec(a);
		}
	}
  
	private void init()
	{
		process_      = null;

    toBeAborted_      = AbstractComposite.orderedMap();
		commitFuncPhase1_ = AbstractComposite.simpleMap();
		commitFuncPhase2_ = AbstractComposite.simpleMap();
		abortFunc_        = AbstractComposite.simpleMap();
    
    createEventData_  = AbstractComposite.simpleMap();
	}
	
	// Classes to implement the various actions on commit/abort
	protected abstract class CommitBase extends AbstractFunc
                                      implements Func
	{
		protected Map     processing_;
		private   boolean aborting_;
		
		void setList(Map m, boolean aborting)
		{
			processing_ = m;
			aborting_   = aborting;
		}
		
		protected void setAbortFunc(Func p2,
																Func abort,
																Map  key,
																Map  value) throws AnyException
		{
			if (!aborting_)
			{
        
        if (isAutoCommit())
        {
          toBeAborted_.add(key, value);
          abortFunc_.add(key, abort);
        }
        
        commitFuncPhase2_.add(key, p2);
				stopOnInterrupt();
			}
		}
	}
}

