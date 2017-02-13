/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.Map;

/**
 * An adapter to interface some external system to the Inq
 * environment.
 *
 * @author Tom
 */
public interface Plugin
{
  /**
   * Performs any initialisation required by the external environment.
   * Any exceptions that occur must be caught and if required rethrown
   * by wrapping in a {@link com.inqwell.any.RuntimeContainedException}
   * @param argsMap An Inq Map that the implementation can query for,
   * say, configuration parameters. Often the command line arguments
   * specified when the Inq environment was launched.
   * @throws RuntimeContainedException if any kind of error occurs in
   * the external environment.
   * environment
   */
  public void start(com.inqwell.any.Map argsMap);
  
  /**
   * Performs any closedown required by the external environment.
   * Any exceptions that occur must be caught and if required rethrown
   * by wrapping in a {@link com.inqwell.any.RuntimeContainedException}
   * @throws RuntimeContainedException if any kind of error occurs in
   * the external environment.
   * environment
   */
  public void stop();
  
  /**
   * Starts a transaction. If called by the external environment,
   * all calls to {@link #create(Map)}, {@link #modify(Map)}
   * and {@link #delete(Map)}
   * have their effects deferred until the transaction is committed
   * by calling {@link #end(boolean)}
   * <p>
   * If these methods throw an
   * exception while a transaction is open then all actions
   * hitherto are discarded. The external environment may continue
   * with further actions and if no exceptions are encountered
   * these will be committed when {@link #end(boolean)} is called.
   * <p>
   * If <code>begin()</code> is not called then actions submitted are
   * committed after each invocation of those methods. <code>begin()</code>
   * cannot be called more than once before {@link #end(boolean)} is called
   * to close the transaction.
   * @throws AnyRuntimeException if a transaction is already open.
   */
  public void begin();
  
  /**
   * Called by the external system to indicate that the entity
   * represented by this plugin should be created.
   * @param m a Map containing an agreed key/value set representing
   * the entity to be created.
   * @return a unique identifier for the created entity
   * @throws AnyRuntimeException if any kind of error occurs in the Inq
   * environment
   */
  public String create(Map<String, String> m);
  
  /**
   * Called by the external system to indicate that the entity
   * represented by this plugin should be updated.
   * @param m a Map containing an agreed key/value set representing
   * the entity to be updated.
   * @throws AnyRuntimeException if any kind of error occurs in the Inq
   * environment
   */
  public void modify(Map<String, String> m);
  
  /**
   * Called by the external system to indicate that the entity
   * represented by this plugin should be deleted.
   * @param m a Map containing an agreed key/value set representing
   * the entity to be deleted.
   * @return <code>true</code> if the entity existed and was scheduled for
   * deletion, <code>false</code> if it did not exist in the first place.
   * @throws AnyRuntimeException if any kind of error occurs in the Inq
   * environment
   */
  public boolean delete(Map<String, String> m);
  
  /**
   * Commits the transaction opened by <code>begin()</code>. If
   * no transaction was opened this method has no effect.
   * <p>
   * <bold>Note:</bold> If <code>begin()</code> is called and later
   * an exception is thrown while submitting actions
   * to <code>create()</code> or <code>modify()</code> then
   * the external environment must still call <code>end(boolean)</code>
   * to tidy the transaction state.
   * @param commit if true then the transaction (if open) is committed.
   * Otherwise it is rolled back.
   * @throws AnyRuntimeException if some error occurs during
   * the commit.
   */
  public void end(boolean commit);
}
