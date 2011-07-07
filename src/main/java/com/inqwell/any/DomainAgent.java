/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/DomainAgent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/*
 * A utility interface for those operations required to support
 * remotely hosted objects.
 * <p>
 * $Archive: /src/com/inqwell/any/DomainAgent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
public interface DomainAgent extends Any
{
  public DomainAgent null__ = new NullDomainAgent();
  
  /**
   * Returns the host that this <code>DomainAgent</code> will reach.
   */
  public Any getHostId();
  
  /**
   * Commit the given transaction on the host represented by
   * this <code>DomainAgent</code>.
   */
  public void commit(Transaction t) throws AnyException;
   
	/**
	 * Performs any processing that must be completed prior to a
	 * lock request progressing. Note that implementations should
	 * not run the timeout, which is supplied for use by the
	 * lock engine(s) in the domain represented by
	 * this <code>DomainAgent</code>.
	 * @param p the process performing the lock
	 * @param a the object to be locked
	 * @param timeout the period we are willing to wait for the lock
   * the mandate.
	 * @return <code>p</code> if the lock request can proceed
	 * for process <code>p</code>, a representation of the locking process
	 * if the request cannot proceed at this time.
	 * @throws ContainedException wrapping InterruptedException
	 */
	public Process getLockMandate(Process p, Any a, long timeout) throws AnyException;
	
	/**
	 * Inform this <code>DomainAgent</code> that the existing lock
	 * on <code>a</code> held by process <code>p</code> is being released.
	 */
	public void release(Process p, Any a);
}
