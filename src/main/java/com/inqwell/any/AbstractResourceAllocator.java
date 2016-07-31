/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/AbstractResourceAllocator.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

/*
 * Defines a common allocation policy with a protected interface to create and
 * otherwise housekeep the resources themselves.  Ensures synchronization in
 * multi-threaded conditions for allocation of resources.
 * <p>
 * $Archive: /src/com/inqwell/any/AbstractResourceAllocator.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
public abstract class AbstractResourceAllocator extends    AbstractAny
                                                implements ResourceAllocator
{
	// Maps keys comprising resource specifications to a channel
  // from which a corresponding resource instance can be retrieved
	private Map containers_;
	
	// Maps keys as above to individual allocation limits for resources
	private Map resourceLimit_;
	
	// Maps keys as above to counts of individual resources made so far
	private Map resourcesMade_;
	
  // If an entry is present in this map for a given specification
  // then the resource is currently unavailable and cannot be
  // acquired.
  private Map resourcesUnavailable_;
  
	private Map specs_;
	
	public AbstractResourceAllocator()
	{
		init();
	}
	
	public void addSpec(Any id, Any spec, IntI limit)
	{
		synchronized(resourcesMade_)
		{
			if (!specs_.contains(id))
			{
				specs_.add(id, spec);
				resourcesMade_.replaceItem(id, new AnyInt(0));
				resourceLimit_.replaceItem(id, limit);
				containers_.replaceItem(id, new ResourceContainer());
			}
		}
	}
	
  public Any getSpec(Any id)
  {
    if (!specs_.contains(id))
      throw new AnyRuntimeException("Resource " + id + " not found");
      
    return specs_.get(id);
  }
  
  public Any acquire(Any spec) throws AnyException
  {
  	return acquire(spec, -1);
  }

  public Any acquire(Any id, long timeout) throws AnyException
  {
    Process acquirer = Globals.getProcessForCurrentThread();
    return acquire(id, acquirer, timeout);

  }

	public Any acquire(Any id, Process acquirer, long timeout) throws AnyException
	{
		Any rSpec = checkResSpec(id);
		
	  ResourceContainer container = (ResourceContainer)containers_.get(id);

    synchronized(resourcesUnavailable_)
    {
      if (resourcesUnavailable_.contains(id))
        throw new AnyException("Resource " + id + " currently unavailable");
    }
  
    boolean got = false;
    Any      resource      = null;
    do
    {
      resource = container.getFromQueue(acquirer, 0);
      
      if (resource == null)
      {
        synchronized(resourcesMade_)
        {
          IntI resourcesMade = (IntI)resourcesMade_.get(id);
          int resMade          = resourcesMade.getValue();
          int resourceLimit    = ((IntI)resourceLimit_.get(id)).getValue();
  
          if (resMade < resourceLimit)
          {
            // there's no resources available  but we're below the resource
            // limit so make a new one
            resource = makeNewResource(id, rSpec, resMade);
            resMade++;
            resourcesMade.setValue(resMade);
            container.newInQueue(acquirer, resource);
          }
        }
        if (resource == null)
        {
          // Wait for a resource to become available
          resource = container.getFromQueue(acquirer, timeout);
        }
      }
      
      if (resource == null)
      	throw new AnyException ("Unable to acquire resource " + id);
      
      got = beforeAcquire(resource);
      
      if (!got)
      {
        // This one is no good so discard it
        synchronized (resourcesMade_)
        {
          IntI resourcesMade = (IntI)resourcesMade_.get(id);
          int resMade          = resourcesMade.getValue();
          resMade--;
          resourcesMade.setValue(resMade);
          container.discardFromQueue(acquirer, resource);
        }
      }
    } while (!got);
    
    afterAcquire(resource);
    return resource;
	}

	public void release(Any id,
                      Any resource,
                      Any arg,
                      ExceptionContainer e) throws AnyException
  {
    release(id, Globals.getProcessForCurrentThread(), resource, arg, e);
  }
	
	public void release(Any id,
			                Process releaser,
                      Any resource,
                      Any arg,
                      ExceptionContainer e) throws AnyException
	{
	  ResourceContainer container  = (ResourceContainer)containers_.get(id);
    
		if (beforeRelease(resource, arg, e))
		{
			// its OK to put it back
      container.putToQueue(releaser, resource);
		}
		else
		{
			synchronized (resourcesMade_)
			{
				IntI resourcesMade = (IntI)resourcesMade_.get(id);
				int resMade          = resourcesMade.getValue();
				resMade--;
				resourcesMade.setValue(resMade);
				container.discardFromQueue(releaser, resource);
			}
		}
    
    // Check if the release operation generated an exception
    // and if so throw it.  Update - defer to caller
//    if (e.getThrowable() != null)
//      throw new ContainedException(e.getThrowable());
	}

  public void deleteAll(Any spec) throws AnyException
  {
    synchronized(resourcesUnavailable_)
    {
      resourcesUnavailable_.add(spec, AnyBoolean.TRUE);
    }

    try
    {
      IntI made = (IntI)resourcesMade_.get(spec);
      int resMade = made.getValue();
      while (resMade-- > 0)
      {
        Any r = acquire(spec);
        disposeResource(r);
      }
    }
    finally
    {
      synchronized(resourcesUnavailable_)
      {
        resourcesUnavailable_.remove(spec);
      }
    }
  }
  
  public int getResourcesMade(Any spec)
  {
    int ret = 0;
    synchronized (resourcesMade_)
    {
      if (resourcesMade_.contains(spec))
      {
        IntI i = (IntI)resourcesMade_.get(spec);
        ret = i.getValue();
      }
    }    
    return ret;
  }
  
	/**
	 * Make a new resource according to the supplied specification for
   * immediate or later allocation.
   * @param id the ID of this resource pool
   * @param spec some information to configure the new resource
   * @made the number of this resource made so far
   * @return the new resource ready to use
	 */	
	protected abstract Any makeNewResource(Any id, Any spec, int made) throws AnyException;
	
	/**
	 * Check the resource is still OK for re-use.  If not
	 * then <code>AbstractResourceAllocator</code> will not put it back
	 * in the pool for future allocation and will decrement the count
	 * of resources available so that the resource will be allocated
	 * again sometime in the future.
	 */
	protected abstract boolean beforeRelease(Any resource,
                                           Any arg,
                                           ExceptionContainer e);
	
  protected abstract boolean beforeAcquire(Any resource);
  
  protected abstract void afterAcquire(Any resource);
  
	/**
	 * Called if the resource is to be disposed of.  This can be due to
	 * <code>beforeRelease</code> returning <code>false</code>
	 * or <code>ResourceAllocator</code> finalization
	 */
	protected abstract void disposeResource(Any resource);
	
	private Any checkResSpec (Any id) throws ResourceUnavailableException
	{
		synchronized(resourcesMade_)
		{
			if (!resourcesMade_.contains(id))
			{
				throw new ResourceUnavailableException("Unknown resource " + id);
			}
		}
		return specs_.get(id);
	}
	
	private void init()
	{
		resourcesMade_        = AbstractComposite.simpleMap();
		resourceLimit_        = AbstractComposite.simpleMap();
    containers_           = AbstractComposite.simpleMap();
		specs_                = AbstractComposite.simpleMap();
		resourcesUnavailable_ = AbstractComposite.simpleMap();
	}
  
  /*
  protected class ResourceItem extends AbstractAny
  {
    // Housekeeping
    private Process   inUseBy_;
    private Process   lastUsedBy_;
    private long      lastAllocated_;
    private long      lastReturned_;
    private AnyFormat f_;
   
    // The resource iteslf
    private Any       resource_;
    
    protected ResourceItem(Any resource)
    {
      // When a resource is being made we assume it is being allocated
      inUseBy_ = Globals.getProcessForCurrentThread();
      lastAllocated_ = System.currentTimeMillis();
      resource_ = resource;
      f_ = AnyFormat.makeFormat(new ConstDate(), "dd MMM yyyy HH:mm:ss:sss");
    }
    
    protected void releasing()
    {
      Process releasedBy = Globals.getProcessForCurrentThread();
      
      if (inUseBy_ == null)
        throw new AnyRuntimeException("Resource " + resource_ +
                                      "last allocated to " + lastUsedBy_ +
                                      " at " + f_.format(new Date(lastAllocated_)) +
                                      " is not currently in use while being released by " +
                                      releasedBy);
      
      if (inUseBy_ != releasedBy)
        throw new AnyRuntimeException("Resource " + resource_ +
            "is currently allocated to " + inUseBy_ +
            " on " + f_.format(new Date(lastAllocated_)) +
            " while being released by " +
            releasedBy);
      
      
      lastUsedBy_ = inUseBy_;
      inUseBy_    = null;
      lastReturned_ = System.currentTimeMillis(); 
    }
    
    protected void acquiring()
    {
      Process acquirer = Globals.getProcessForCurrentThread();
      
      if (inUseBy_ == acquirer)
        throw new AnyRuntimeException("Resource " + resource_ +
            "is already allocated to " + inUseBy_ +
            " on " + f_.format(new Date(lastAllocated_)));
      
      if (inUseBy_ != null)
        throw new AnyRuntimeException("Acquisition of resource " + resource_ +
            " by " + acquirer +
            " when currently allocated to " + inUseBy_ +
            " on " + f_.format(new Date(lastAllocated_)));
       
      inUseBy_ = acquirer; 
      lastAllocated_ = System.currentTimeMillis();
    }
    
    public boolean equals(Any a)
    {
      if (a == null)
        return false;
      
      if (!(a instanceof ResourceItem))
        return false;
      
      ResourceItem other = (ResourceItem)a;
      
      if (resource_ == null && other.resource_ == null)
        return true;
      
      if (resource_ == null && other.resource_ != null)
        return false;

      if (resource_ != null && other.resource_ == null)
        return false;
      
      return resource_.equals(other.resource_);
    }
    
    void setResource(Any resource)
    {
      resource_ = resource;
    }
  }
  */
  
  protected static class ResourceContainer extends AbstractAny
  {
    // The available resources
    private  Queue         q_ = AbstractComposite.queue();
    
    // Those resources out on active service. Maps the resource
    // to the Process it was allocated to
    private  Map           out_ = AbstractComposite.simpleMap();
    
    private Any getFromQueue(Process p, long timeout) throws AnyException
    {
      synchronized (this)
      {
        while (q_.isEmpty())
        {
          if (timeout == 0)
            return null;

          try
          {
            if (timeout > 0)
            {
              wait(timeout);
              timeout = 0;
            }
            else
            {
              wait();
            }
          }
          catch (InterruptedException e)
          {
            // we were interrupted or killed
            if (Globals.getProcessForCurrentThread().killed())
            {
              throw new ProcessKilledException(e);
            }
            else
              throw new ContainedException(e);
          }
        }
        
        Any resource = q_.removeFirst();
        
        out_.add(resource, p);

        return resource;
      } // synchronized
    }

    private void putToQueue(Process p, Any resource)
    {
      synchronized (this)
      {
        Any op = out_.getIfContains(resource);
        
        if (op == null)
          throw new AnyRuntimeException("Resource " + resource +
          " is currently unallocated");
        
        if (op != p)
          throw new AnyRuntimeException("Resource " + resource +
              " is already allocated to " + op);
        
        q_.addLast(resource);
        out_.remove(resource);
        
        notifyAll();  // Notify waiting acquirer thread
      }
    }
    
    private void newInQueue(Process p, Any resource)
    {
      synchronized (this)
      {
        out_.add(resource, p);
      }
    }
    
    private void discardFromQueue(Process p, Any resource)
    {
      synchronized(this)
      {
        Any op = out_.getIfContains(resource);
        
        if (op == null)
          throw new AnyRuntimeException("Resource " + resource +
          " is currently unallocated");
        
        if (op != p)
          throw new AnyRuntimeException("Resource " + resource +
              " is already allocated to " + op);
        
        out_.remove(resource);
      }
    }
  }
}
