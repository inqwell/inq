/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


/*
 * $Archive: /src/com/inqwell/any/Transaction.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-05-07 16:53:31 $
 */
 
package com.inqwell.any;

import com.inqwell.any.jms.SessionI;

/**
 * Define interface for representing transactions.
 * The <code>Transaction</code> and <code>Process</code> interfaces
 * are closely linked in that a transaction executes on behalf of
 * a process.
 * @author $Author: sanderst $
 * @version $Revision: 1.7 $
 * @see com.inqwell.any.Any
 */ 
public interface Transaction extends Any
{
	public static final Transaction NULL_TRANSACTION = new NullTransaction();
	public static final int         BEFORE_EVENTS = 0;
	public static final int         AFTER_EVENTS  = 1;
	public static final int         UPDATE        = 1001;
	public static final int         CREATE        = 1002;
	public static final int         DELETE        = 1003;
  
  // The type of transactional object most recently involved
  public static final int         R_NOTHING     = 0;
  public static final int         R_MAP         = 1;
  public static final int         R_FIELD       = 2;
	
  public static final Any         transaction__ = new ConstString("internal://Transaction");
  public static final Any         commit__      = new ConstString("commit");
  public static final Any         recreate__    = new ConstString("recreate__");
  
  /**
	 * Commence transaction semantics on the network of objects
	 * at the given root.  Depending on the transaction implementation
	 * this may be a no-operation (optimistic) or a lock of all
	 * transaction-aware objects contained in the network (pessimistic)
	 * preventing <code>join()</code> or <code>start()</code> operations
	 * by blocking other transactions until the object is released.
	 */
	public void start(Any root) throws AnyException;
	
	/**
	 * Enters the given object into the list of objects managed by this
	 * transaction.  Once a join operation for a given object has returned,
	 * the transaction has exclusive control over that object.  Other
	 * transactions which attempt to join on the same object will be
	 * forced to wait until this transaction has completed or been
	 * aborted.
	 */
	public void join (Map m) throws AnyException;
	 
	 
	/**
	 * Sets up a copy of the given object which may
	 * subsequently be retrieved with <code>getTransInstance()</code>.  By this
	 * means a client can mutate the private instance without affecting other
	 * simultaneously executing transactions.
	 * <p>
	 * Although implementation specific,
	 * the object is generally also joined and therefore prevented from
	 * joining with other transactions.
	 * <P>
	 * If <code>copyOnWrite()</code> has not been called then
	 * <code>getTransInstance()</code> returns the given object.  This is
	 * acceptable for read operations and it is up to the client to ensure
	 * that <code>copyOnWrite()</code> is called prior to mutating the object.
	 * <P>
	 * A pessimistic transaction would join all objects taking part
	 * on <code>start()</code> and therefore can be assured that <i>both</i> read
	 * and write operations take place exclusively.  An optimistic implementation
	 * which does not lock any objects until they are mutated is content to
	 * read data which other transactions may be in the process of updating.
	 * <P>
	 * Whether a particular transaction implementation calls <code>join()</code>
	 * when <code>copyOnWrite()</code> is called will depend on how that
	 * implementation wishes to arbitrate with other transactions operating
	 * on the same object.  If <code>join()</code> is not called then
	 * the implementation must resolve any conflicts at commit time.
	 * <P>
	 * Calling <code>copyOnWrite()</code> more than once is not an error and
	 * must not cause multiple instances to be created.
	 */
	public void copyOnWrite (Map m) throws AnyException;

	/**
	 * Allow the transaction the chance of noting which field(s) are
	 * being altered.  The transaction may support fields information
	 * if it raises events on transactional objects
	 */
  public void fieldChanging(Map m, Any f, Any info);
	
	/**
	 * Determines whether the transaction can be committed or not.  If
	 * all necessary <code>join()</code>s have executed then this method
	 * can return <code>true</code>.  Otherwise the implementation has the
	 * opportunity to perform any arbitration of conflicts with other
	 * transactions.
	 * <P>
	 * The <code>canCommit()</code> method need not be called by clients
	 * since the implementation of <code>commit()</code> should do so.
	 * <code>canCommit()</code> must complete before <code>commit()</code>
	 * semantics continue.
	 */
	public boolean canCommit() throws AnyException;
	
	/**
	 * Indicates that this transaction will delete the given object
	 * when it is committed.  Depending on implementation, the transaction
	 * may <code>join()</code> the given object.
	 */
	public void deleteIntent (Map m) throws AnyException;
	
	/**
	 * Indicates that this transaction will create the given object
	 * when it is committed.
	 * <P>
	 * The caller may re-use the same Map object to create another object
	 * some time in the future (with a different unique key, of course)
	 */
	public void createIntent (Map m, Any eventData) throws AnyException;
	
	/**
   * Resync any cached object with its externally stored (and
   * therefore possibly externally changed) counterpart, whose
   * unique key is given by <code>keyVal</code>.  The cached
   * object can be null (if the object is not in the cache)
   * but the read object should not be.
   */
	public void resync(Map keyVal, Map cachedObject, Map readObject) throws AnyException;
	
	/**
	 * Commits this transaction.  The responsibilities of the <code>commit()</code>
	 * method are:
	 * <BL>
	 * <LI>Update the public (<code>Map</code>) instances with any private
	 * copies created by <code>copyOnWrite()</code>.  If the <code>Map</code>
	 * implementation in question implements the <code>EventGenerator</code>
	 * interface then an event can be generated to signal this occurrence to
	 * other observers of the object;
	 * <LI>Manage any objects whose creation was prepared
	 * with <code>createIntent()</code>.  If the <code>Descriptor</code>
	 * for the object implements the <code>EventGenerator</code> interface
	 * then an event can be generated to signal this occurrence to observers
	 * of the descriptor;
	 * <LI>Remove any objects whose deletion was prepared
	 * with <code>deleteIntent()</code>.  If the <code>Map</code>
	 * implementation in question implements the <code>EventGenerator</code>
	 * interface then an event can be generated to signal this occurrence to
	 * other observers of the object.
	 * </BL>
	 */
	public void commit() throws AnyException;
	
  public boolean isAutoCommit();

	/**
	 * Interrupt this transaction.  This method may be called by threads
	 * other than the one executing the transaction context proper to
	 * signal that the transaction should be interrupted.  Implementations
	 * will commonly set a flag which is periodically checked by the
	 * transaction's "owner" thread.  This thread must throw a
	 * TransactionException when it detects interrupt.
	 */
	public void interrupt() throws AnyException;
  
	/**
	 * Returns <code>true</code> if this transaction is in any way active.
	 * In general, a transaction is active if it has any instances
	 * pending creation, modification or deletion, or is managing
	 * any locks, but it can mean any state that the implementation
	 * deems other than idle.
	 * @return <code>true</code> if this transaction is active,
	 * <code>false</code> otherwise.
	 */
  public boolean isActive();
  
	/**
	 * Discards any actions not already committed and reverts any actions
	 * which have.
	 */
	public void abort() throws AnyException;
	
	/**
	 * Place an action to be executed after the transaction
	 * has successfully committed
	 */
	public void addAction(Func f, int when);
  
  /**
   * Set whether this transaction gathers events it will raise (and
   * those raised by automatic node-set pruning as a result) so that
   * they can be retrieved within the context of an after-events
   * action by calling getEventBundle()
   */
  public void setGatherEvents(boolean gather);
	
  /**
   * Whether this transaction is gathering its events
   */
  public boolean isGatheringEvents();
  
  /**
   * Export the event bundle from the specified transaction
   * to <code>this</code>.
   * @param eventBudle
   * @param t
   */
  public void exportEvents(Array eventBudle, Transaction t);
  
  public void addEvent(Event e);
  
  /**
   * Attempt to place a user lock the specified object.
   */
  public boolean lock(Any a, long timeout) throws AnyException;
  
  /**
   * Unlock an object previously locked with <code>lock</code>.
   */
  public void unlock(Any a) throws AnyException;
	
	/**
	 * See <code>copyOnWrite()</code> above
	 */
	public Map getTransInstance (Map m);
  
  /**
   * Is this object joined in the transaction for delete?
   */
  public boolean isDeleteMarked(Map m);
  
  /**
   * Is this object joined in the transaction for create?
   */
  public Map isCreateMarked(Map m) throws AnyException;

  /**
   * Is this object joined in the transaction for modify?
   */
  public boolean isModifying(Map m) throws AnyException;
  
  /**
   * Returns a list of instances that are currently within the transaction
   * pending formal creation. If there are no such
   * instances <code>null</code> is returned.
   * @return A Map of unique key --> instance for those instances
   * pending creation.
   */
  public Map getCreateList(Descriptor d);
  
  /**
   * Returns a list of instances that are currently within the transaction
   * pending modification. If there are no such
   * instances <code>null</code> is returned.
   * @return A Map of unique key --> instance for those instances
   * pending modification.
   */
  public Map getModifyList(Descriptor d);
  
  /**
   * Returns a list of instances that are currently within the transaction
   * pending deletion. If there are no such
   * instances <code>null</code> is returned.
   * @return A Map of unique key --> instance for those instances
   * pending deletion.
   */
  public Map getDeleteList(Descriptor d);
  
  /**
   * Attempt to export the given object from the specified transaction.
   * to <code>this</code>.
   * The exact operation of this method depends on the implementation
   * of the two <code>Transaction</code> objects involved.  Generally,
   * if the given object is not taking part in <code>this</code>
   * then this method does nothing. Otherwise the possibilities
   * include remove from <code>this</code> and place
   * in <code>t</code> or throw an exception if the object is
   * already participating in <code>t</code>.
   */
	public void export(Map m, Transaction t) throws AnyException;

	/**
	 * Return the process this transaction is executing within.
	 */
	public Process getProcess();
	
	/**
	 * Establish the process this transaction is executing within.
	 */
	public void setProcess(Process p);
	
	/**
   * Check if the required privilege permits the requested access.
   * The privilege information for the specified node is checked
   * to see if the specified access is allowed for the process
   * represented by this transaction.
   * @throws AnyRuntimeException if the requested access is not
   * allowed.
   */
	public void checkPrivilege(Any access, Map node, Any key);
	
  public void setParent(Transaction t);
  
  public Transaction getParent();
  
  public void setChild(Transaction t);
  
  public void setIdentity(Descriptor d, Func f);

	/**
	 * Return the context node this transaction is executing at
	 */
	public Map getContext();
	
	public Any getContextPath();
	
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
  
  /**
   * Acquire the specified resource from the given allocator.
   * The implementation may return the same resource more than
   * once for a given specification and allocator during the
   * lifetime of the transaction.
   * <p>
   * If there are no resources available then the allocator
   * determines the policy of either waiting or throwing.
   * The <code>timeout</code> argument may be used by the
   * allocator.
   * <p>
   * The transaction implementation is responsible for releasing
   * any resources this method returns on commit or abort.
   * @param spec
   * @param allocator
   * @return the resource
   */
  public Any acquireResource(Any               spec,
                             ResourceAllocator allocator,
                             long              timeout) throws AnyException;
	
	/**
	 * Set the given iterator so that it
	 * can be used during the course of a transaction. This method
	 * allows an iterator to be passed within the transaction
	 * object so that things like <code>Iter.remove()</code> can be
	 * called at appropriate locations within a function network.
	 * <note>The methods <code>Transaction.commit()</code>
	 * and <code>Transaction.abort()</code> may reset the current
	 * iterator to null.
	 * @return the existing iterator or null if none.
	 */
	public Iter setIter(Iter i);
	
	/**
	 * Get the current iterator.
	 * @return the existing iterator or null if none.
	 */
	public Iter getIter();
	
  public Any setLoop(Any loop);

  public Any getLoop();
  
  public void setMqSession(SessionI session);
  
  public SessionI getMqSession();
  public void setMqCommit(boolean commit);
  public void mqDirty(boolean dirty);
  public boolean isMqDirty();

  /**
   * Get a temporary variable of the type specified by the argument.
   * The value may be the same object reference each time this method
   * is called with the same argument type.
   *  
   * @param a
   * @return
   */
  public Any getTemporary(Any a);
  
  /**
   * If the argument is a {@link PropertyBinding} then read it and return the
   * property value. Otherwise return the argument.
   * @param a
   * @return
   */
  public Any readProperty(Any a);
  
	/**
	 * Provides for various implementations to be flagged as
	 * null transaction handling.
	 */
	public boolean isDegenerate();

  public void setLineNumber(int line);
  public void setColumn(int col);
  public int  getLineNumber();
  public int  getColumn();

  public Any  getExecURL();
  public void setExecURL(Any execUrl);

  public Stack getCallStack();

  public void setNotifyOld(Descriptor d, boolean notify);
  
  public void setResolving(int resolving);
  public void resetResolving();
  public int getResolving();
  
  public void setLastTMap(Map m);
  public void setLastTField(Any a);
  public Map getLastTMap();
  public Any getLastTField();
}
