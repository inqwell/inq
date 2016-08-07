/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/ResourceAllocator.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/*
 * Defines an interface for a scheme to allocate resources to consumer
 * threads who sometime later put them back.  Such resources might be SQL
 * connections.
 * <p>
 * $Archive: /src/com/inqwell/any/ResourceAllocator.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
public interface ResourceAllocator extends Any
{
	/**
	 * Acquire, or wait indefinitely until available for, the resource
	 * identified by the given identifier.
	 * @param id the identifier of the resource being acquired
	 */
	public Any acquire(Any specification) throws AnyException;
	
	/**
	 * Acquire for the current process, waiting at most the specified time for,
   * the resource identified by the given identifier.
	 * @param id the identifier of the resource being acquired
	 * @param timeout the amount of time, in ms, to wait for
	 * the resource to become available. -1 is an indefinite
	 * wait.
	 * @return the acquired resource 
   * @throws AnyException if no resource
	 * became available in the finite wait time or none could be
	 * made.
	 */
	public Any acquire(Any id, long timeout) throws AnyException;

	/**
	 * Acquire, for the specified Process, waiting at most the specified
   * time for, the resource identified by the given identifier.
	 * @param id the identifier of the resource being acquired
	 * @param acquirer the Process acquiring the resource
	 * @param timeout the amount of time, in ms, to wait for
	 * the resource to become available. -1 is an indefinite
	 * wait.
	 * @return the acquired resource 
   * @throws AnyException if no resource
	 * became available in the finite wait time or none could be made.
	 */
	public Any acquire(Any id, Process acquirer, long timeout) throws AnyException;
	
	/**
	 * Release the given resource.  It is undefined what happens if the
	 * id is not the same as the one used to acquire the
	 * resource. The current process is assumed to be the present
	 * owner of the resource. 
	 */
	public void release(Any id,
                      Any resource,
                      Any arg,
                      ExceptionContainer e) throws AnyException;
  
	/**
	 * Release the given resource.  It is undefined what happens if the
	 * id is not the same as the one used to acquire the
	 * resource.
	 * <p/>
	 * Releasing a resource may involve exception-prone operations,
	 * for example an SQL connection committing a transaction.
	 * Implementations may choose to throw or return any
	 * such exception via the ExceptionContainer argument.
	 * @param id the identifier of the resource being released
	 * @param releaser current owner of the resource.
	 * @param arg an additional argument that can be used by
	 * implementations as required
	 * @param e an ExceptionContainer through which an exception
	 * occurring during release may be returned.
	 */	
	public void release(Any     id,
                      Process releaser,
                      Any     resource,
                      Any     arg,
                      ExceptionContainer e) throws AnyException;

      /**
   * Returns the number of resources currently available for allocation,
   * whether or not they are currently allocated to a user.
   * @return the number of resources available, or zero if none have
   * been created or the specification is unknown.
   */
  public int getResourcesMade(Any spec);
  
  /**
   * Fetch the configuration information for this resource id.
   * The information returned depends on the implementation.
   * For a pool of SQL connections there may be login details
   * together with any aspects of how the JDBC driver behaves. 
   * @param id
   * @return The configuration information, typically a Map
   */
  public Any getSpec(Any id);
}
