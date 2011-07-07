/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/RemoteTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.server.LocalTransaction;

/**
 * A transaction whose state during the processing phase is built in the
 * Inq environment executing the service but is committed in the Inq
 * environment where its participant objects are hosted.
 * <p>
 * A given instance of <code>RemoteTransaction</code> can only include
 * objects hosted in a single Inq environment.  The management of this
 * is not handled 
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
//public abstract class RemoteTransaction extends LocalTransaction
public class RemoteTransaction extends LocalTransaction
{
	private Any                   resourceId_;
	
	// The transaction that created us as remotely defined objects
	// were joined into it.
	private transient Transaction parent_;
	
	public RemoteTransaction(Process     p,
                           Transaction parentPrimary,
                           Any         resourceId)
  {
    super(p);
    parent_     = parentPrimary;
    resourceId_ = resourceId;
  }
  
//  public void addParticipant(Any a, Map m)
//  {
//    participants_.add(a, m);
//  }
  
	/**
   * Set the key changes for an object undergoing update in this txn.
   */
	public void setKeyChanges(Map m, Set keys)
	{
    keysChanged_.add(m, keys);
	}

	public void join (Map m) throws AnyException
	{
    // Should not be called
    throw new UnsupportedOperationException();
  }

	protected boolean doCopyOnWrite(Map m) throws AnyException
	{
    Map p = parent_.getTransInstance(m);
    if (p == m)
      throw new AnyException ("No transaction-private instance available for " + m);
      
    addActiveObject(m, p);
    
    return true;
	}
  
	protected Map doCreateIntent (Map m) throws AnyException
	{
	  Descriptor d = m.getDescriptor();
	  Map uniqueKey = d.getUniqueKey(m);  // review whether uniqueKey is already in map?
    addActiveObject(uniqueKey, m);
    return uniqueKey;
	}
	
  protected Map doIsCreateMarked(Map m) throws AnyException
  {
    return CREATE_NO;
    /**
     TBD
    Descriptor d = m.getDescriptor();
    if (!identityFuncs_.contains(d))
      return CREATE_NOID;
      
    Func f = (Func)identityFuncs_.get(d);
    Any id = execIdentityFunc(d, f, m);
    if (identityVals_.contains(id))
      return CREATE_YES;
    else
      return CREATE_NO;
    */
  }
  
	protected Map doDeleteIntent(Map m) throws AnyException
	{
	  Descriptor d = m.getDescriptor();
	  Map uniqueKey = d.getUniqueKey(m);  // review whether uniqueKey is already in map?
	  if (isJoined(uniqueKey))
			throw new TransactionException("Double-delete within transaction " +
																		 d +
																		 " of object " +
																		 m);

	  if (isJoined(m))
	  {
		  // Object has already been joined in this transaction
		  // for update.  Change its status to delete.
		  removeActiveObject(m);
		}

    addActiveObject(uniqueKey, m);
    
    return uniqueKey;
	}
	
  protected boolean doLock(Any a, long timeout) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

	protected void unlockAll() throws AnyException
	{
    // called after phase 2, noop in this case
  }

	protected void validateKeyChanges(Map objs,
                                    Map commitFuncPhase1) throws AnyException
	{
    // called before phase 1, noop in this case
	}
	
	protected void doAbort() throws AnyException
  {
    throw new UnsupportedOperationException();
  }

	protected Any doResync(Map    keyVal,
                         Map    cachedObject,
                         Map    readObject) throws AnyException
	{
    throw new UnsupportedOperationException();
  }
}
