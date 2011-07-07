/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Wait.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.identity.Identity;

/**
 * Wait on the specified object, optionally for timeout milliseconds.
 * <p>
 * A process may wait on any object for an indefinite period,
 * or an optional timeout in milliseconds:
 * <code><pre>
 *   wait("hello", 3000);
 * </pre></code>
 * waits on the string "hello" for 3 seconds.  Or
 * <code><pre>
 *   wait("goodbye");
 * </pre></code>
 * waits on the string "goodbye" indefinitely.
 * <p>
 * Any object can be waited on, provided the corresponding
 * <code>notify()</code> function uses an object that equals() that
 * in wait the notify will succeed.
 * <p>
 * Care should be taken not to wait on a typedef instance that
 * has been previously joined into the current transaction.
 * In this case the private instance will be waited on, and
 * it would be unlikely to equals() the public one seen
 * by other threads.
 * <p>
 * The statement
 * <code><pre>
 *   notify("goodbye");
 * </pre></code>
 * executed by another process will wake up the earlier wait.
 * <p>
 * <code>wait()</code> returns <code>true</code> if the object being waited
 * on was notified by another process and <code>false</code> if the wait
 * timed out. <code>notify()</code> returns <code>true</code> if there were
 * other process(es) waiting on the object being notified, <code>false</code>
 * if there were not.
 * <p>
 * More than one process can wait on the same object.  A
 * corresponding <code>notify()</code> will waken one of the waiting
 * processes.
 * <p>
 * A simple use of <code>wait()</code> is to replace the use of
 * <code>syscmd("sleep 5")</code>, which is system dependent.  Instead
 * the statement
 * <code><pre>
 *   wait("anything", 5000);
 * </pre></code>
 * performs the same function.
 * <p>
 * <b>Locking Issues and Thread Synchronisation</b>
 * <p>
 * If a process wants to lock an object, either explicitly
 * or implicitly, and that object is locked by a another
 * process that is also in an indefinite wait then that
 * process will be notified as a deadlock victim.
 * <p>
 * Unlike Java, a process does not have to hold the lock on
 * the object it will wait on.  However, like java, when it
 * does hold the lock then a <code>wait</code> will release the
 * lock.  When the wait times out or the process is notified the
 * lock will be re-acquired. This allows processes to cooperate
 * safely while testing for a mutual condition.  For example, if
 * process A is waiting for a condition to prevail that
 * will be set by (at least) process B then the following
 * may be scripted:
 * <p>
 * Process A:
 * <code><pre>
 * 
 *   transaction
 *   {
 *     lock("mutexAB");
 *     while (!call testForTermination())  // the call is thread-safe w.r.t maybeSetTermination()
 *     {
 *       wait("mutexAB");
 *     }
 *   }
 * 
 * </pre></code>
 * Process B:
 * <code><pre>
 *
 *     .
 *     .
 *     // perform processing
 *     .
 *     .
 *   transaction
 *   {
 *     lock("mutexAB");
 *     call maybeSetTermination();  // call is thread-safe w.r.t testForTermination()
 *     notify("mutexAB");
 *   }
 * 
 * </pre></code>
 * The use of the <code>transaction{}</code> block is a simply a
 * syntactic clarity, the use of which implicitly releases the lock on
 * the string value "mutexAB".  We could have unlocked the
 * mutex object explicitly both in process A and B.
 * <p>
 * Locking the mutex object is not required if one process merely
 * wishes to notify another (i.e. there is no need to protect
 * any state).
 * <p>
 * Object wait/notify is determined by object equality, rather than
 * specific object reference.  Inq objects are generally mutable.
 * If the object (and most do) implements value (as opposed to identity)
 * equality then it is cloned before locking. This
 * helps protect Inq from internal inconsistencies but does not
 * prevent a user from attempting to wait on one value and
 * notify on another.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class Wait extends    AbstractFunc
									implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any any_;
	private Any timeout_;
	
	public Wait(Any any, Any timeout)
	{
    any_     = any;
    timeout_ = timeout;
	}

  public Any exec(Any a) throws AnyException
	{
		Any any     = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    any_);

    if (any == null ||
        AnyNull.isNullInstance(any) ||
        AnyAlwaysEquals.isAlwaysEquals(any))
      throw new IllegalArgumentException("null or equals");
    
		Any timeout = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    timeout_);
    
    if (timeout == null && timeout_ != null)
      nullOperand(timeout_);
    
    long lt = 0;

		// Objects are generally mutable. Protect against
		// waiting on an object which may subsequently be mutated
		// by the caller, thus compromising the integrity
		// of the lock manager.  Should the caller do this
		// with a value-equality object and later attempt to
		// notify with the modified object the noify will have
		// no effect, but Inq will be OK.
		if (!Identity.hasIdentity(any))
      any = any.cloneAny();

    if (timeout != null)
    {
      LongI l = new AnyLong(0);
      l.copyFrom(timeout);
      lt = l.getValue();
    }
    
    Any ret = Globals.lockManager__.waitFor(getTransaction().getProcess(),
                                  any,
                                  lt);
    
		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Wait w     = (Wait)super.clone();
		w.any_     = AbstractAny.cloneOrNull(any_);
		w.timeout_ = AbstractAny.cloneOrNull(timeout_);
		return w;
  }
}
