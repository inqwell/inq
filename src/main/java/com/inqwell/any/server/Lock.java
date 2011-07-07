/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Lock.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.identity.Identity;

/**
 * An explicit attempt by the current process to lock the given object.
 * <p>
 * As well as the Inq run time locking objects for transaction
 * safety, explicit locks and unlocks can be requested. These
 * have the following semantics:
 * <bl>
 * <li>
 * A lock can be requested with a timeout representing the number of
 * milliseconds the process is willing to wait, should another
 * process already hold the lock on the specified object. A value of zero
 * means that we are not willing to wait at all; less than zero means
 * we will wait indefinitely.
 * <li>
 * It is possible to lock an object that has already been locked
 * by the system on behalf of the current process for the purposes
 * of transaction integrity.  However, if the object is explicitly
 * unlocked before the transaction is committed the system lock
 * will remain in place.
 * <li>
 * It is not necessary for an explicit lock to be explicitly
 * unlocked.  When the process's transaction is committed or
 * aborted, all that process's locks will be removed.
 * <li>
 * An explicit unlock on an object for which the current process
 * does not hold the lock causes an exception.
 * <li>
 * Locks are counted, so if a process locks an object more than
 * once it must unlock it the same number of times, apart from
 * transaction termination, which removes all locks regardless.
 * </bl>
 * <p>
 * The <code>Lock</code> function returns a <code>boolean</code>
 * value - <code>true</code> if the lock has been attained,
 * <code>false</code> if we timed out or were not willing to wait.
 * If the lock attempt causes a deadlock and the current process is
 * chosen as the dealock victim then an exception is thrown. This
 * will result in transaction abort, releasing all the locks this
 * process holds.
 * <p>
 * Object locking is determined by object equality, rather than
 * specific object reference.  Thus, the determining factor is how
 * the object implements equality. If this is by <b>value</b> then
 * processes will be excluded from locking the 'same' object even
 * if the objects themselves are distinct instances. Further, Inq
 * objects are generally mutable. If the object (and most do)
 * implements value equality then it is cloned before locking. This
 * helps protect Inq from internal inconsistencies but does not
 * prevent a user from incurring an exception if an attempt is made
 * to unlock an object with the (now mutated) value.
 */
public class Lock extends    AbstractFunc
                  implements Cloneable
{
  private static final long serialVersionUID = 1L;

  static private Any indefinite__ = new ConstLong(-1);
  
  private Any toLock_;
  private Any timeout_;
  private Any condition_;

  /**
   * 
   */
  public Lock(Any toLock, Any timeout, Any condition)
  {
    toLock_   = toLock;
    
    if (timeout == null)
      timeout = indefinite__;
    
    timeout_    = timeout;
    condition_  = condition;
  }

  public Lock(Any toLock)
  {
    toLock_    = toLock;
    timeout_   = indefinite__;
    condition_ = null;
  }

  public Any exec(Any a) throws AnyException
  {
		Any toLock  = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    toLock_);
		
    Any timeout = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    timeout_);

    AnyFuncHolder.FuncHolder condition = (AnyFuncHolder.FuncHolder)
                           EvalExpr.evalFunc(getTransaction(),
                                             a,
                                             condition_);

    if (toLock == null)
      nullOperand(toLock_);
    
    // timeout_ is always set by parser/ctor
    if (timeout == null)
      nullOperand(timeout_);
    
    if (condition == null && condition_ != null)
      nullOperand(condition_);
    
    LongI lITimeout = new ConstLong(timeout);
		
		AnyBoolean ret = new AnyBoolean(false);
		
		// Objects are generally mutable. Protect against
		// locking an object which may subsequently be mutated
		// by the caller, thus compromising the integrity
		// of the lock manager.  Should the caller do this
		// with a value-equality object and later attempt to
		// unlock with the modified object he will incur
		// an exception, but Inq will be OK.
		if (!Identity.hasIdentity(toLock))
      toLock = toLock.cloneAny();

		long systime  = System.currentTimeMillis();
    long remwait  = lITimeout.getValue();
    long ltimeout = lITimeout.getValue();
    
    if (getTransaction().lock(toLock, remwait))
    {
      // Once we have the lock, if there is a condition then ensure it
      // is true
      if (condition != null)
      {
        do
        {
          // Execute the condition while we are holding the lock
          ret.copyFrom(condition.doFunc(getTransaction(), null, a));
          
          if (!ret.getValue())
          {
            // Condition is false. If we have a finite wait then take off
            // whatever amount of time we've waited so far, including any
            // time spent acquiring the lock
            if (ltimeout > 0)
            {
              // finite wait
              remwait -= System.currentTimeMillis() - systime;
              systime = System.currentTimeMillis();
              if (remwait <= 0)
                break; // no time left
            }
          }
          
          if (ltimeout == 0) // no wait but we have done the condition once
            break;
          
          if (!ret.getValue())
          {
            // Condition is false, wait for a notification. We will either
            // be waiting for a finite amount of time or forever by this point
            Globals.lockManager__.waitFor(getTransaction().getProcess(),
                                          toLock,
                                          remwait);
            
            // We will come out of the waitFor either because another
            // process has notified us or because of the timeout. Either
            // way, do the condition again
          }
        }
        while(!ret.getValue());
        
        // If we broke out without the condition then unlock the object and
        // return false.
        if (!ret.getValue())
          getTransaction().unlock(toLock);

      }
      else
      {
        // There was no condition but we do have the lock.
        ret.setValue(true);
      }
    }
    
		return ret;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(toLock_);
		a.add(timeout_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Lock l = (Lock)super.clone();
    
    l.toLock_     = toLock_.cloneAny();

    if (timeout_ != indefinite__)
      l.timeout_     = timeout_.cloneAny();
    
    l.condition_ = AbstractAny.cloneOrNull(condition_);
    
    return l;
  }
}
