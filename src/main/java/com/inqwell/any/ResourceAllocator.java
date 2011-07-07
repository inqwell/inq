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
	 * identified by the given specification
	 */
	public Any acquire(Any specification) throws AnyException;
	
	/**
	 * Acquire, or waiting at mos the specified time for, the resource
	 * identified by the given specification
	 */
	public Any acquire(Any specification, long timeout)
																			 throws AnyException;

	/**
	 * Release the given resource.  It is undefined what happens if the
	 * specification is not the same as the one used to acquire the
	 * resource!
	 */	
	public void release(Any specification,
                      Any resource,
                      Any arg,
                      ExceptionContainer e) throws AnyException;
  
  /**
   * Returns the number of resources currently available for allocation,
   * whether or not they are currently allocated to a user.
   * @return the number of resources available, or zero if none have
   * been created or the specification is unknown.
   */
  public int getResourcesMade(Any spec);
  
  public Any getSpec(Any id);
}
