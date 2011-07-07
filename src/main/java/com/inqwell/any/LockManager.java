/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LockManager.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;


/**
 * Support object locking and deadlock detection.  Process may
 * negotiate for exclusive access to an object by using a
 * LockManager to acquire, await and notify the release of objects.
 * In addition, the LockManager allows a process to wait on an
 * arbitrary object until another process notifies it, thus
 * allowing processes to synchronise with each other.
 */
public final class LockManager
{
	// Something to synchronise access to lock tables and to
  // wait on for lock release notifications.
	private Object mutex_  = new Object();
  
  // well-known value through which a process waiting for a lock
  // to become free and process freeing a lock synchronize their
  // state
	//private Object waitingUnlockNotify_  = new Object();

	// Maps the objects that are locked to the process that holds the
	// lock, to the count of locks on the object and the lock agent
  // that was used.
	private Map locked_    = AbstractComposite.simpleMap();
	private Map lockCount_ = AbstractComposite.simpleMap();
	private Map domainAgent_ = AbstractComposite.simpleMap();

  // Maps the object a process is waiting for notification on
  // to the process(es) that is(are) waiting.
  private Map wait_      = AbstractComposite.simpleMap();

  // Maps the object being notified to the process that is
  // responsible for raising the notification as it unlocks
  // the object.
  private Map pendingNotify_ = AbstractComposite.simpleMap();
  
  // List of process in deadlock with each other.
  Array list_ = AbstractComposite.array();

	// Maps the processes waiting for lock release to the object that
	// they are waiting for
	private Map lockWait_  = AbstractComposite.simpleMap();

  // Maps the objects that are being waited for to the
  // processes that are waiting for them (the opposite of
  // the above).  Note that this map is only necessary because
  // of the remote locking strategy not requiring a thread
  // in the host server.
  private Map lockWaitObjects_ = AbstractComposite.simpleMap();

  public boolean lock(Process p, Any a) throws AnyException
  {
    return this.lock(p, a, -1, DomainAgent.null__);
  }

  public boolean lock(Process p, Any a, DomainAgent domainAgent) throws AnyException
  {
    return this.lock(p, a, -1, domainAgent);
  }

  public boolean lock(Process p, Any a, long timeout) throws AnyException
  {
    return this.lock(p, a, timeout, DomainAgent.null__);
  }

  /**
   * Process <code>p</code> tries to lock the given
   * object <code>a</code>. If the lock can be obtained then
   * <code>p</code> is returned. Otherwise the process
   * currently holding the lock is returned.  If the timeout
   * is non-zero (i.e. indefinite or finite) and the lock
   * cannot be obtained then a lock-wait entry will be made.
   * <p>
   * This method is used by implementations
   * of <code>Process</code> that do not have their own thread,
   * such as <code>LockerProxy</code> in the case of remote locks.
   * This <code>LockManager</code> is not responsible for
   * running any timeout that was specified. Some other thread,
   * possibly in another Inq environment, will perform this task
   * and subsequently call <code>exitWait(p, a)</code> for the
   * same process and object.
   */
  public Process lockOrEnterWait(Process p, Any a, long timeout) throws AnyException
  {
	  synchronized (mutex_)
	  {
      if (lock(p, a))
        return p;

      if (timeout != 0)
        inLockWait(p, a);

      return locker(a);
	  }
  }

  public void exitWait(Process p, Any a)
  {
    synchronized (mutex_)
	  {
      removeLockWait(p, a);
	  }
  }

	/**
	 * An attempt by process <code>p</code> to lock Any <code>a</code>.
	 * If the object is not already locked by another process, the
	 * lock will be established for process <code>p</code> and that
	 * process continues execution.  Otherwise the process waits for
	 * the existing lock to be released, when it will retry.
	 * <p>
	 * If there are several processes waiting, it is undefined which
	 * one will obtain the lock when the current lock is released.
	 * <p>
	 * If the same process locks an object more than once the lock count
	 * is incremented.
   * <p>
   * When a lock is released the same <code>DomainAgent</code> that
   * was supplied in the lock request will be used to release the lock.
   * Note that if the lock count is incremented, the same lock
   * agent must be used in all lock requests or an exception is thrown.
   * @param p the process attempting the lock
   * @param a the object to be locked
   * @param timeout the amount of time the caller is prepared to wait
   * for the lock to become available in milliseconds. Zero
   * indicates return immediately, less than zero means wait
   * indefinitely.
   * @param domainAgent the agent defining the domain in which the
   * lock will take place.  <code>DomainAgent.null__</code> defines
   * the local domain only.
	 */
	public boolean lock(Process     p,
                      Any         a,
                      long        timeout,
                      DomainAgent domainAgent) throws AnyException
	{
		boolean haveLock  = false;
		boolean enterWait = false;

	  synchronized (mutex_)
	  {
			if (locked_.contains(a) && (locked_.get(a).equals(p)))
			{
				// If we already have the lock just bump the count

        if (!domainAgent_.get(a).equals(domainAgent))
          throw new AnyException("Lock attempt is incompatible with the existing lock");

        incrementLockCount(a);
        return true;
      }

			//if (!locked_.contains(a))
			if (!locked_.contains(a) && !lockWaitObjects_.contains(a))
			{
        // Use the appropriate DomainAgent to authorise permission
        // to proceed.  In the remote lock case, this would involve
        // communicating with the hosting server to acquire the lock
        // there also.
        Process mandateProcess = domainAgent.getLockMandate(p, a, timeout);
        if (mandateProcess != p)
        {
          // We can't get the lock.  We know that the object is not
          // already locked in this environment but we consider it
          // so once we know it is somewhere else.  In this way we
          // make use of the local LockManager and remote resources
          // more efficiently.  Wherever the object is hosted, in
          // that lock manager we will be waiting for the lock to be
          // released.
          locked_.add(a, mandateProcess);
          lockCount_.add(a, new AnyInt(1));
        }
        else
          haveLock = true;
			}
      if (haveLock)
      {
        locked_.add(a, p);
        lockCount_.add(a, new AnyInt(1));
        domainAgent_.add(a, domainAgent);
      }
      else if (timeout != 0)
      {
        // Set ourselves up in the wait structures
        inLockWait(p, a);
        enterWait = true;
			}
		}   // End synchronized(mutex_)

    // Check if we need to enter a wait.  If we do then we need a sync loop
    // and condition that is respected by the current locker so we can't
    // miss the notify.  We can't wait on another object inside the
    // synchronized(mutex_) block or we will deadlock other threads using the
    // lock manager
    if (enterWait)
    {
	    long timeLeft = timeout;
	    long timeNow  = System.currentTimeMillis();
      while (!haveLock)
      {
        synchronized(p)
        {
          try
          {
            // Check if an unlock operation has already signalled us
            if (p.getLockWaitObject() == null)
            {
              // No signal yet - wait for it.  Set the waiting timeout
              // for the deadlock scanner
              p.setLockWaitObject(a, timeout);
              if (timeout > 0)
              {
                p.wait(timeLeft);
              }
              else
              {
                p.wait();
              }
            }
          }
          catch (InterruptedException e)
          {
            // kill or deadlock victim
            synchronized(mutex_)
            {
              removeLockWait(p, a);
              p.setLockWaitObject(null, 0);
              throw new ContainedException(e);
            }
          }

          // Can't be sure we would get the lock even after notification, as
          // someone else might have snuck in
          synchronized(mutex_)
          {
            if (!locked_.contains(a))
            {
              // Ok to grab lock now
              locked_.add(a, p);
              lockCount_.add(a, new AnyInt(1));
              domainAgent_.add(a, domainAgent);
              removeLockWait(p, a);
              p.setLockWaitObject(null, 0);
              haveLock = true;
            }
            else
            {
              // Cannot acquire lock at this time.  If our timeout expired
              // then give up.  Else re-wait
              p.setLockWaitObject(null, 0);
              if (timeout > 0)
              {
                timeLeft -= System.currentTimeMillis() - timeNow;
                timeNow = System.currentTimeMillis();
                if (timeLeft <= 0)
                {
                  removeLockWait(p, a);
                  break;
                }
              }
            }
          }  // End synchronized(mutex_)
        }  // End synchronized(p)
		  }
	  }
	  return haveLock;
	}

	/**
	 * Unlock object <code>a</code> without force.
	 */
	public int unlock(Process p, Any a) throws PermissionException
	{
		return unlock (p, a, false);
	}

	/**
	 * Unlock object <code>a</code>.  If this is the last unlock operation
	 * matching the corresponding number of locks then the physical lock
	 * will be released and any processes waiting for the object are notified.
	 * Otherwise the lock count is decremented.  If <code>force</code> is
	 * <code>true</code> then the object is unlocked regardless of the
	 * lock count.
	 * @param p The unlocking process
	 * @param a The object to unlock
	 * @param force If <code>true</code> unlock the object regardless of the
	 * lock count.
	 * @throws PermissionException if <code>p</code> is not the
	 * process holding the lock or does not have supervisor rights
	 */
	public int  unlock(Process p, Any a, boolean force) throws PermissionException
	{
		return unlock(p, a, force, true);
	}

	// The unlock engine
	public int unlock(Process p,
										 Any     a,
										 boolean force,
										 boolean notify) throws PermissionException
	{
		Process waitingForLock = null;
		int     i = 0;

		synchronized (mutex_)
		{
			if (locked_.contains(a))
			{
			  Process locker = (Process)locked_.get(a);
			  if ((!locker.equals(p)) && (!p.isSupervisor()))
    			throw new PermissionException("Lock of " + a + "\n" +
                                        "is held by " + locker + "\nLock remove by " + p
                                        + dumpTables());

	      IntI ai = (IntI)lockCount_.get(a);
	      ai.setValue(i = (ai.getValue() - 1));
	      if (i == 0 || force)
	      {
          i = 0;
          
          // Use the appropriate DomainAgent to notify lock release.
          // In the remote lock case, this would involve
          // communicating with the hosting server to release the lock
          // there also.
  				locked_.remove(a);
  				lockCount_.remove(a);

          // Note - there doesn't have to be a lock agent present
          // if we are removing a remote lock 'imported' from another
          // environment.
  				DomainAgent domainAgent = (DomainAgent)domainAgent_.remove(a);
          if (domainAgent != null)
            domainAgent.release(p, a);

          // Check for a pending notify in the waitFor stuff
          if (pendingNotify_.contains(a) &&
              pendingNotify_.get(a).equals(p))
          {
            Array waiters = (Array)wait_.get(a);
            Process wp = (Process)waiters.get(0);
            // Just in case of value-based equality semantics.
            Any na = wp.getWaitingObject();
        //System.out.println("Pended notify on " + a);
        //System.out.println("a is now " + na + " from process " + wp);
            synchronized(na)
            {
              //System.out.println(Thread.currentThread().getName() + " pended notify on " + a);
              na.notify();
            }
            pendingNotify_.remove(a);
          }

          // Check for any process waiting to acquire the lock.  If there are
          // any then prepare to notify the first one that the object is
          // becoming available
          if (lockWaitObjects_.contains(a))
          {
				    Array pwait = (Array)lockWaitObjects_.get(a);
				    waitingForLock = (Process)pwait.get(0);
          }
          unlockNotify(a);
  			}
			}
		} // End synchronized(mutex_)

    // If there is a waiting process then send it a signal that it can
    // try for the lock again
		if (waitingForLock != null)
		{
			synchronized(waitingForLock)
			{
        // Check again that the process is still waiting - it would be
        // possible for two threads to notify the same process. The
        // second one need not bother.
        synchronized(mutex_)
        {
          boolean stillWaiting = false;
          if (lockWaitObjects_.contains(a))
          {
            Array pwait = (Array)lockWaitObjects_.get(a);
            stillWaiting = pwait.contains(waitingForLock);
          }
          if (stillWaiting)
          {
            // If the waiter is not already there then tell it it can go
            // ahead.  Otherwise notify it
            if (waitingForLock.getLockWaitObject() == null)
              waitingForLock.setLockWaitObject(a, 0);
            else
              waitingForLock.notify();
          }
        }
			}
		}
    
    return i;
	}

	/**
	 * Unlock all the objects contained in the
	 * composite <code>c</code>.  If this is the last unlock operation
	 * matching the corresponding number of locks then the physical lock
	 * will be released and any processes waiting for the object are notified.
	 * Otherwise the lock count is decremented.  If <code>force</code> is
	 * <code>true</code> then the object is unlocked regardless of the
	 * lock count.
	 * <p>
	 * This method
	 * only notifies waiting threads when all the objects
	 * have been unlocked and so is more efficient than
	 * unlocking a large set of objects individually.
	 */
	public void unlockList(Process p, Composite c, boolean force)
																					throws PermissionException
	{
		//System.out.println ("LockManager.unlock composite");
		Iter i = c.createIterator();
		while (i.hasNext())
		{
			Any a = i.next();
			unlock (p, a, force, false);
		}
	}

	/**
	 * Return the process holding an object lock.  Returns the process holding
	 * the lock on the given object, or <code>null</code> if the object is
	 * not locked.
	 */
	public Process locker(Any a)
	{
		synchronized (mutex_)
		{
			if (locked_.contains(a))
				return (Process)locked_.get(a);
			else
				return null;
		}
	}

  /**
   * Return the lock count of object <code>a</code> held by
   * process <code>p</code>.
   */
  public int lockCount(Process p, Any a)
  {
    int ret = 0;
    synchronized(mutex_)
    {
      if (locked_.contains(a) && (locked_.get(a).equals(p)))
      {
        IntI lockCount = (IntI)lockCount_.get(a);
        ret = lockCount.getValue();
      }
    }
    return ret;
  }

	/**
	 * Scan current object locks and processes in lock-wait to check
	 * for deadlock.  This method enforces exclusive access to the internal
	 * structures used to track object locks.
	 * @param p the root process for deadlock detection scan.  The given
	 * process must already be in lock-wait.
	 * @param list if not null, will contain all the processes involved
	 * in the deadlock, starting at <code>p</code>
	 * @return <code>true</code> if the given process is deadlocked.
	 */
	public boolean isDeadlocked (Process p, Array list)
	{
	  Process root    = p;
	  Process current = p;

	  if (list != null)
	    list.empty();

	  synchronized (mutex_)
	  {
	    Any a   = lockWait_.get(current);
	    do
	    {
	      if (a != null && locked_.contains(a))
	      {
	        current = (Process)locked_.get(a);
	        if (list != null)
						list.add (current);
				  a = (lockWait_.contains(current)) ? lockWait_.get(current)
				                                    : null;
	      }
	      else
	      {
	        current = null;
	      }
	    }
	    while ((root != current) && (current != null));
	  }
	  if (current == null)
			list.empty();

    return current != null ? true : false;
  }

  /**
	 * Scan the processes in lock-wait and return the first list found,
	 * if any
	 */
  public Array scanForDeadlock()
  {
		synchronized (mutex_)
		{
			Iter i = lockWait_.createKeysIterator();
			while (i.hasNext())
			{
				Process p = (Process)i.next();
				list_.empty();
				if (isDeadlocked(p, list_))
					return list_;
			}
		}
		return null;
	}

  /**
   * Process <code>p</code> will wait on object <code>a</code>
   * until <code>timeout</code> milliseconds have passed or
   * another process calls <code>notify()</code> on the same
   * object or an object <code>a1</code>where
   * <code>a.equals(a1)</code> is true.
   * @return <code>true</code> if <code>p</code> was
   * notified, <code>false</code> if a timeout was specified
   * and no notification was received.
   */
  public Any waitFor(Process p, Any a, long timeout) throws AnyException
  {
    Array waiters   = null;
    Any   ret       = AnyBoolean.TRUE;
    int   lockCount = 0;

    // We synchronize on mutex_ even though wait() is a different
    // piece of functionality. [  When a process locks an object
    // we check if there would be deadlock because of any wait()
    // operations in progress. bollocks ]
    synchronized (mutex_)
    {
      // Check if the specified process holds any lock(s) on the
      // object.  It is not compulsory that a process holds a lock
      // on the object it is about to wait on but this would normally
      // be the case when two processes are synchronizing.  Otherwise
      // the wait is just a sleep since, without a lock, the receiver
      // may miss the notify.
      lockCount = lockCount(p, a);

      // Check the wait_ table to see if there's already an object
      // we can wait on.
      if (wait_.contains(a))
      {
        waiters = (Array)wait_.get(a);

        // Get the wait object from the first waiting process
        // so all waiters have the same reference.  (Otherwise
        // we can't notify them!)
        Process waiter = (Process)waiters.get(0);
        a = waiter.getWaitingObject();
        p.setWaitingObject(a, timeout);
        waiters.add(p);
      }
      else
      {
        waiters = AbstractComposite.array();
        waiters.add(p);
        p.setWaitingObject(a, timeout);
        wait_.add(a, waiters);
      }
    }
    try
    {
      synchronized(a)
      {
        // If the requesting process holds a lock on the object then
        // release it with notification.
        if (lockCount != 0)
          unlock(p, a, true, true);

        //System.out.println(Thread.currentThread().getName() + " going into wait on " + a);
        // Wait on the object
        if (timeout > 0)
        {
          long t = System.currentTimeMillis();
          a.wait(timeout);
          t = System.currentTimeMillis() - t; 
          if (t >= timeout)
            ret = AnyBoolean.FALSE;
        }
        else
          a.wait();

      }
      
      //p.setWaitingObject(null, 0);
      // If we had a lock then reacquire it after waking up.
      if (lockCount != 0)
      {
        //System.out.println(Thread.currentThread().getName() + " relocking " + a);
        for (int i = 0; i < lockCount; i++)
          lock(p, a, -1);
      }
    }
    catch (InterruptedException ie)
    {
      // Deadlock victim - another process about to perform
      // a lock() operation sees that we hold the lock and
      // are waiting indefinitely for notificaiton.
      throw new ContainedException(ie);
    }
    finally
    {
      // We have come out of wait one way or another.  Remove
      // ourselves from the wait_ structures.
      synchronized(mutex_)
      {
        int indx = waiters.indexOf(p);
        waiters.remove(indx);
        if (waiters.entries() == 0)
          wait_.remove(a);
        p.setWaitingObject(null, 0);
      }
    }
    return ret;
  }

  /**
   * Notify one of any process that may be waiting on the
   * given object <code>a</code>.
   * <p>
   * Process <code>p</code> will notify one of zero or more
   * other processes waiting on object <code>a</code>.
   * If process <code>p</code> holds a lock on
   * object <code>a</code> then the notification is sent
   * only once the lock is released.
   * @return <code>true</<code> if a process was notified,
   * <code>false</code> if there were no processes waiting
   * on the specified object.
   */
  public boolean notifyVia(Any a, Process p)
  {
    boolean ret    = false;
    boolean pended = false;

    synchronized(mutex_)
    {
      if (wait_.contains(a))
      {
        ret = true;
        Array waiters = (Array)wait_.get(a);
        Process wp = (Process)waiters.get(0);
        // Just in case of value-based equality semantics.
        //System.out.println("Notifying on " + a);
        a = wp.getWaitingObject();
        //System.out.println("a is now " + a + " from process " + wp);
        // If there is a lock on the object then we must hold it.
        // If so pend the notify until we release the lock.  As
        // with waitFor above, it is not compulsory for a notifying
        // process to hold the lock in order to do a notify
        // but if it does the notify is not sent until the
        // process releases the lock.  This mimicks Java native
        // locking, since the lock is presumably held to protect
        // a script region.
        if (locked_.contains(a))
        {
          if (!locked_.get(a).equals(p))
            throw new AnyRuntimeException("Process " + p +
                                          " cannot notify on object " + a +
                                          " whose lock is held by " + locked_.get(a));
          if (!pendingNotify_.contains(a))
          {
            // consider removing test as should never be violated
            pendingNotify_.add(a, p);
            //System.out.println(Thread.currentThread().getName() + " pending notify on " + a);
            pended = true;
          }
          else
          {
            //System.out.println(Thread.currentThread().getName() + " already a pending notify on " + a);
          }
        }
      }
      else
      {
        //System.out.println(Thread.currentThread().getName() + " no waiter for notify on " + a);
      }
    }
    if (ret && !pended)
    {
      synchronized(a)
      {
        //System.out.println(Thread.currentThread().getName() + " unpended notify on " + a);
        a.notify();
      }
    }
    return ret;
  }

  // Always called from sync block on mutex_
  private void inLockWait(Process p, Any a)
  {
    if (lockWait_.contains(p))
      throw new AnyRuntimeException("Thread " + Thread.currentThread().getName() + 
                                    ", Process " + p +
                                    " going into lockWait on object " + a +
                                    " is already waiting on object " +
                                    lockWait_.get(p),
                                    dumpTables());

    lockWait_.add(p, a);
    Array pwait = null;
    if (lockWaitObjects_.contains(a))
    {
      pwait = (Array)lockWaitObjects_.get(a);
    }
    else
    {
      pwait = AbstractComposite.array();
      lockWaitObjects_.add(a, pwait);
    }
    pwait.add(p);
  }

  private void removeLockWait(Process p, Any a)
  {
    if (!lockWait_.contains(p))
    {
      throw new AnyRuntimeException("Thread " + Thread.currentThread().getName() + 
                                    ", Process " + p +
                                    " leaving lockWait on object " + a +
                                    " is not waiting on any object",
                                    dumpTables());
    }

    if (a == null)
      a = lockWait_.get(p);

    lockWait_.remove(p);

    Array pwait = (Array)lockWaitObjects_.get(a);
    int index = pwait.indexOf(p);
    pwait.remove(index);
    if (pwait.entries() == 0)
      lockWaitObjects_.remove(a);
  }

  // Choose the first process of any that are waiting for the
  // given object and notify it of the unlock.  Required for
  // processes that are not implemented with their own thread,
  // notably those representing remote lock requests.
  private void unlockNotify(Any a)
  {
    if (lockWaitObjects_.contains(a))
    {
      Array pwait = (Array)lockWaitObjects_.get(a);
      Process p = (Process)pwait.get(0);
      p.notifyUnlock(a);
    }
  }

	private void incrementLockCount(Any a)
	{
		IntI i = (IntI)lockCount_.get(a);
		i.setValue(i.getValue() + 1);
	}
  
  private StringI dumpTables()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("\nlocked_ : ");
    sb.append(locked_.toString());
    sb.append("\n");
    sb.append("lockWait_ : ");
    sb.append(lockWait_.toString());
    sb.append("\n");
    sb.append("lockCount_ : ");
    sb.append(lockCount_.toString());
    sb.append("\n");
    sb.append("lockWaitObjects_ : ");
    sb.append(lockWaitObjects_.toString());
    sb.append("\n");
    sb.append("wait_ : ");
    sb.append(wait_.toString());
    sb.append("\n");
    return new ConstString(sb.toString());
  }
  
}
