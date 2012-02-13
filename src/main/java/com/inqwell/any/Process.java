/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/Process.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * Executables in the Any environment.  All known implementations start
 * their own thread.
 */
public interface Process extends Map
{
	public static final Any DETACHED  = new ConstInt(0);
	public static final Any CHILD     = new ConstInt(1);
	public static final Any NAME      = new ConstString("name");
	public static final Any STARTED   = new ConstString("started");
	public static final Any PROCESSES = new ConstString("processes");
	public static final Any STATUS    = new ConstString("status");
	
  public static final short MINIMUM_PRIVILEGE = 255;
  public static final short MAXIMUM_PRIVILEGE = 0;
  public static final short DEFAULT_PRIVILEGE = 128;
  
  public static final ShortI A_MAXIMUM_PRIVILEGE = (ShortI)AbstractValue.flyweightConst(new ConstShort(MAXIMUM_PRIVILEGE));
  
  public static Any sync__ = AbstractValue.flyweightString("sync__");
  
  public static final Any processName__ = new ConstString("processName");


  /**
	 * Send data to this process.  The data will be read by the
	 * process's thread and actioned.  Of course, this method is intended
	 * to be called by other process threads so is thread-safe
	 */
	public void send (Any a) throws AnyException;
	
	/**
	 * Set the transaction object for this process
	 */
	public void setTransaction(Transaction t);
	
	public Transaction getTransaction();
	
	/**
	 * Called by another process (say, the LockManager) to bump this
	 * process out of a <code>wait()</code> in which it is deadlocked
	 * with one or more competing processes.
	 */
	public void deadlockVictim();

	/**
	 * Called by another process <code>p</code> (say, the LockManager) to kill it.
	 */
	public void kill(Process p) throws AnyException;
  
  /**
   * Whether this process has been killed.
   */
  public boolean killed();
  
  /**
   * Interrupt the current transaction or lock-wait.
   */
  public void interrupt();

  /**
   * Start the thread for this process
   */
  public void startThread();
	
	/**
	 * Check if this process has supervisor rights.
	 */
	public boolean isSupervisor ();
	
  public boolean isAlive();

  public void setSupervisor (boolean b);

	public void addChildProcess(Process p, Transaction t);
	
	public void removeChildProcess(Process p, Transaction t);

	public boolean isAncestor(Process p);
	
	/**
	 * Get the root of the data managed by this process
	 */
	public Map getRoot();
	
	/**
   * Return this process's timer scheduler
   */
	public AnyTimer getTimer();
	
	/**
	 * Get the context node that this process is executing its current
	 * operation at
	 */
	public Map getContext();
	
	/**
	 * Set the context node that this process is executing its current
	 * operation at
	 */
	public void setContext(Map context);
	
	public Any getContextPath();
	public void setContextPath(Any contextPath);
	
	public Any getCatalogPath();
	
	/**
	 * Return the current stack level, from where call parameters
	 * may be accessed.
	 */
	public Map getCurrentStackFrame() throws StackUnderflowException;

	/**
	 * Make a new stack frame and return the Map representing it.  This
	 * same Map will be returned by subsequent calls
	 * to <code>getCurrentStackFrame()</code>
	 */
	public Map pushStackFrame();
	
	/**
	 * Discard the current stack frame and return the Map representing
	 * the previous one.  This same Map will be returned by subsequent
	 * calls to <code>getCurrentStackFrame()</code>
	 */
	public Map popStackFrame() throws StackUnderflowException;
	
	
	public void emptyStack();
  
  /**
   * Set the object this Process is waiting for notification
   * on and the timeout it is willing to wait for.
   * @param a the object or <code>null</code> if this process
   * is no longer waiting.
   * @param timeout the length of time the process is willing
   * to wait or zero if not waiting.
   */
  public void setWaitingObject(Any a, long timeout);
  
  /**
   * Set the object this Process is waiting to acquire the lock
   * for and the timeout it is willing to wait for the lock to
   * become available.
   * @param a the object or <code>null</code> if this process
   * is no longer waiting for the lock.
   * @param timeout the length of time the process is willing
   * to wait or zero if not waiting.
   */
  public void setLockWaitObject(Any a, long timeout);
  
  /**
   * Return the object this process is waiting for notification on
   * or null if the process is not waiting for notification.
   */
  public Any  getWaitingObject();
  
  /**
   * Return the timeout that that is running against the object
   * this process is waiting for notification on.
   * @return -1 if waiting indefinitely, non-zero if waiting for
   * a finite time or zero if not currently waiting
   */
  public long getWaitingTimeout();
  
  /**
   * Return the object this process is waiting to acquire the lock
   * on or null if this process is not currently waiting for a
   * lock.
   * A lock may be implicit (taken out as required by the system)
   * or explicit (taken out by the user).  Explicit locks may be
   * temporarily relinquished if the locking process chooses to
   * wait on a locked object for notification from another
   * process.
   */
  public Any  getLockWaitObject();

  /**
   * Return the timeout that that is running against the object
   * this process is waiting to acquire the lock for.
   * @return -1 if waiting indefinitely, non-zero if waiting for
   * a finite time or zero if not currently waiting
   */
  public long getLockWaitTimeout();
  
  
  public void setLineNumber(int line);
  public void setColumn(int col);
  public int  getLineNumber();
  public int  getColumn();
  public Any  getExecURL();
  public void setExecURL(Any execUrl);
  public Stack getCallStack();
  
  public Any getSync();
  
  /**
   * Wait indefinitely for this process to terminate.
   */
  public void join();
  
  /**
   * Wait <code>waitTime</code> milliseconds for this process
   * to terminate.
   */
  public void join(long waitTime);

  /**
   * Notify this process that the object it is in lock-wait for
   * is being unlocked.
   */
  public void notifyUnlock(Any a);
  
  public short getRealPrivilegeLevel();
  public short getEffectivePrivilegeLevel();
  public void setRealPrivilegeLevel(short level);
  public void setEffectivePrivilegeLevel(short level);
  
  public ExceptionHandler getExceptionHandler();
  
  public boolean isRealSet();
}
