/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/LocalTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-07 16:53:31 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * Base class for a transaction with a two-phase commit policy
 * in a local Inq environment.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 * @see com.inqwell.any.Any
 */ 
public abstract class LocalTransaction extends    TwoPhaseTransaction
                                       implements Transaction,
                                                  Cloneable
{
	// Stores the following:
	//    1) copy-on-write private instances by mapping public instance
	//       to private instance.  These objects will be *updated* by their
	//       commit function
	//    2) those objects that will be *created* by the commit function in
	//       this transaction.  Maps unique key of object to object itself.
	//    3) those objects that will be *deleted* by this transaction.
	//       Maps unique key of object to object itself.
	//    4) those unique keys that will be *resynced* by this transaction.
	//       Maps the cached object to the new managed object or the new
	//       managed object to itself, if there was no old one in the cache,
	//       or the cached managed object to null, if an external deletion
	//       was performed.
	private Map participants_;
  // This map is the same as participants_ but may be initialised
  // and used if further objects are entered into the transaction
  // during phase 1 of the commit stage by object mutator functions.
  private Map p1Alt_;
	
	// Map copy-on-write instances to their fields changing
	private Map fields_;
	
	// Map copy-on-write instances to the set of unique keys that have changed
	protected Map keysChanged_;
  
  // The list of events raised by this transaction during the commit phase
  private Array eventList_;
  
  // The list of resources used to commit this transaction. Key is the
  // resource specification and value is the resource itself.
  private Map resources_;
  // The allocators used to acquire the resources used in this
  // transaction. Key is the resource, value is the associated
  // allocator.
  private Map allocators_;
	
	// A Transaction is, by default, optimistic - that is objects
	// are only joined when copyOnWrite() is called.  If the
	// pessimistic flag is true then all objects will be
	// joined with the start() method.  Otherwise this method
	// does nothing.  If start() is never called by the client
	// then the transaction behaves like an optimistic one.
	private boolean isPessimistic_ = false;
  
  private ExceptionContainer e_ = new RuntimeContainedException(null);
  
  private boolean raisesCreateEvents_ = true;
  	
	public LocalTransaction(Process p)
	{
    super(p);
		init();
	}
	
	public LocalTransaction()
	{
		this(null);
	}

  public void fieldChanging(Map m, Any f, Any info)
  {
    Transaction t = getParent();
    
		if (!isJoined(m))
      if (t == null)
  	    throw new IllegalArgumentException("Object not within transaction " + m);
      else
      {
        t.fieldChanging(m, f, null);
        return;
      }
  	
  	Set fields = null;
  	
  	if (fields_.contains(m))
  	  fields = (Set)fields_.get(m);
  	else
  	{
  	  fields = AbstractComposite.fieldSet();
  	  fields_.add(m, fields);
  	}
  	
  	// I think this must always work
  	if (f instanceof Locate)
  	{
      Locate l = (Locate)f;

      if (!fields.contains(l.getPath()))
      {
        // Check if its really changing to optimise subsequent
        // transaction processing.
        Map pvt = getTransInstance(m);
        Any p   = l.getPath();
        if (!m.get(p).equals(pvt.get(p)))
        {
          fields.add(l.getPath());
          // Check foreign dependencies
          checkForeignDependencies(m, p);
        }
      }
    }
    else if (f instanceof Map)
    {
      Map sm = (Map)f;
      Iter i = sm.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        if (m.contains(k) && !fields.contains(k))
        {
          Map pvt = getTransInstance(m);
          if (!m.get(k).equals(pvt.get(k)))
          {
            fields.add(k);
            checkForeignDependencies(m, k);
          }
        }
      }
    }
    else if (f instanceof Set)
    {
      // Importing an existing set of field changes from an
      // ancestor transaction
      fields_.replaceItem(m, f);
    }
    else  // just assume Any is the field name
      if (!fields.contains(f))
      {
        fields.add(f);
        checkForeignDependencies(m, f);
      }
  }
  
  public void purgeKey(KeyDef kd)
  {
    addAction(new PurgeKey(kd), Transaction.BEFORE_EVENTS);
  }
  
  public void setGatherEvents(boolean gather)
  {
    if (gather)
      eventList_ = AbstractComposite.array();
    else
      eventList_ = null;
  }
  
  public void setRaisesCreateEvents(boolean raisesCreateEvents)
  {
    raisesCreateEvents_ = raisesCreateEvents;
  }
  
  public boolean isGatheringEvents()
  {
    return eventList_ != null;
  }
  
  public void addEvent(Event e)
  {
    if (eventList_ != null)
      eventList_.add(e);
  }
  
  public Any acquireResource(Any               spec,
                             ResourceAllocator allocator,
                             long              timeout) throws AnyException
  {
    // Resource specifications cannot be ambiguous across allocators
    Any resource = resources_.getIfContains(spec);
    
    if (resource == null)
    {
      // Not yet allocated in this transaction - try to acquire
      resource = allocator.acquire(spec, timeout);
      
      // Lodge in the transaction tables
      resources_.add(spec, resource);
      allocators_.add(resource, allocator);
    }
    return resource;
  }

	protected void doStart(Any root) throws AnyException
	{
		if (isPessimistic_)
		{
      JoinAllTMaps joinAllTMaps = new JoinAllTMaps();
	
			BreadthFirstIter i = new BreadthFirstIter(root);

			while (i.hasNext())
			{
				Any a = i.next();
				if (a != null)
					a.accept(joinAllTMaps);
			}
		}
	}
	
	/**
	 * Checks if the given object is contained within this
   * transaction and joins it if not.  On joining, creates
   * a private instance that will be returned on subsequent
   * calls to <code>getTransInstance()</code>
   * @return <code>true</code> if the object was joined,
   * <code>false</code> if it is already joined.
	 */
	protected boolean doCopyOnWrite(Map m) throws AnyException
	{
		// Check we haven't already got a transaction instance
//		if (isJoined(m))
//			return false;
    
    // Instead of the above, use this alternative which takes into
    // account any parent txn and whether the candidate instance is
    // joined in that.
//    if (getTransInstance(m) != m)
//      return false;

		join(m);
		
		Map tInstance = (Map)m.getDescriptor().newInstance();
		tInstance.copyFrom(m);
    
    // Bit hacky but we put the public instance inside the
    // private one using the unique key slot.  (I don't think
    // these can escape or if they do that it matters anyway)
    // This is used in Declare and Call to ensure that the pub
    // instance is aliased/passed to give correct semantics in
    // these cases.
    // tInstance.setUniqueKey(m);

		// Add the transaction copy to the participants_ collection.
		// We map the original (public) instance to the transaction's
		// (private) instance.
//		System.out.println ("copyOnWrite participants_: " + participants_);
    addActiveObject(m, tInstance);
    
		return true;
	}
	
  public void exportEvents(Array eventBundle, Transaction t)
  {
    if (eventList_ != null)
      eventList_.addAll(eventBundle);
    else
      eventList_ = eventBundle;
  }
  
	public Map getTransInstance(Map m)
	{
		// Note:  getTransInstance is intended for copyOnWrite objects only.
		// We could check that the commit func is Update for bullet-proofing.
		// Also, participants_.get(m) == m when resync and no existing cache
		// entry.
		if (isJoined(m))
    {
      if (p1Alt_ != null && p1Alt_.contains(m))
        return (Map)p1Alt_.get(m);
        
			return (Map)participants_.get(m);
    }
		else
    {
      // We don't have it. If there is a parent then try that
      Transaction t = getParent();
      if (t != null)
        return t.getTransInstance(m);
      else
        return m;
    }
	}

	protected void doPhase1(Map m, Map commitFuncPhase1) throws AnyException
	{
    // Phase 1 is multi-pass and uses those objects that are active
    // in the current pass.
		runFuncs (m, commitFuncPhase1, false);
	}

  protected boolean phase1Complete()
  {
    return p1Alt_ == null;
  }

	protected void doPhase2(Map commitFuncPhase2) throws AnyException
	{
    // Phase 2 is always all the objects in the txn
		runFuncs (participants_, commitFuncPhase2, false);
	}

	protected void doEvents(Map createEventData) throws AnyException
	{
    Map fieldsChanging = getFields();
    
		Iter i = participants_.createKeysIterator();
		while (i.hasNext())
		{
			Any k = i.next();
			Any a = participants_.get(k);
      boolean createEvent = false;
      
			if (a instanceof EventGenerator || (a == null && (k instanceof EventGenerator)))
			{
				EventGenerator eg = (EventGenerator)a;
				Any f = getPhase1Action(k);

				Event e = null;
								
				if (f == getUpdatePhase1())
				{
					e = eg.makeEvent(EventConstants.BOT_UPDATE);
					Map m = (Map)e.getId();
					m.add(EventConstants.EVENT_FIELDS, fieldsChanging.get(a));
					Any old = getRememberedOld(k);
					if (old != null)
					{
					  e.add(NodeSpecification.atOld__, old);
					}
				}
				else if (f == getDeletePhase1())
				{
					e = eg.makeEvent(EventConstants.BOT_DELETE);
				}
				else if (f == getCreatePhase1())
				{
          createEvent = true;
					e = eg.makeEvent(EventConstants.BOT_CREATE);
					Map m = (Map)a;
					Descriptor d = m.getDescriptor();
					if (d instanceof EventGenerator)
						eg = (EventGenerator)d;
            
          if (createEventData.contains(k))
          {
            // Add the create event data to the event type
            Map et = (Map)e.getId();
            et.replaceItem(EventConstants.EVENT_CREATE, createEventData.get(k));
          }
				}
				else if (f == getResyncPhase1())
				{
          if (k == a)
          {
            // resync - no object was originally in the cache
            // Treat as create
            createEvent = true;
            e = eg.makeEvent(EventConstants.BOT_CREATE);
            Map m = (Map)a;
            Descriptor d = m.getDescriptor();
            if (d instanceof EventGenerator)
              eg = (EventGenerator)d;
          }
          else if (a == null)
          {
            // resync - no object read - treat as delete
            eg = (EventGenerator)k;
            e  = eg.makeEvent(EventConstants.BOT_DELETE);
          }
          else
          {
            eg = (EventGenerator)k;
            e  = eg.makeEvent(EventConstants.BOT_UPDATE);
            Map m = (Map)e.getId();
            m.add(EventConstants.EVENT_FIELDS, fieldsChanging.get(k));
            System.out.println("Resync event "  + e);
          }
				}
        if (eventList_ != null)
          eventList_.add(e);
        
        // If the event is a create event and we are not raising
        // create events then don't fire it. All other event types
        // are always fired.
        if (raisesCreateEvents_ || !createEvent)
				  eg.fireEvent(e);
			}
		}
	}
  
  protected Array getEventBundle()
  {
    return eventList_;
  }

	/**
	 * In fact a factory method the result of which will be a new empty
	 * transaction
	 */
	public Object clone() throws CloneNotSupportedException
	{
		Transaction t = (Transaction)super.clone();
		init();
		return t;
	}
	
	protected boolean isJoined(Map m)
	{
    boolean ret = false;
    
    if (p1Alt_ != null)
      ret = p1Alt_.contains(m);
      
    return ret || participants_.contains(m);
	}
	
  protected void addActiveObject(Any key, Any val)
  {
    if (isCommitting())
    {
      if (p1Alt_ == null)
        p1Alt_ = AbstractComposite.orderedMap();
      p1Alt_.add(key, val);
    }
    else
      participants_.add(key, val);   // see commit()
  }
  
  protected Any getActiveObject(Any k)
  {
    // must be present or will throw
    return participants_.get(k);
  }
  
  protected Map getPhase1Objects()
  {
    // Check if we are in a subsequent phase 1 pass
    Map ret = p1Alt_;
    
    if (p1Alt_ != null)
      p1Alt_ = null;

    return ret != null ? ret : participants_;
  }
  
  protected Map getActiveObjects()
  {
    // Check if we are in a subsequent phase 1 pass
    //Map ret = p1Alt_;
    
    //return ret != null ? ret : participants_;
    return participants_;
  }
  
  protected void removeActiveObject(Any key)
  {
    if (p1Alt_ != null && p1Alt_.contains(key))
      p1Alt_.remove(key);

    participants_.remove(key);
  }
  
  protected Map getFields() { return fields_; }
  
	protected void doReset()
	{
		// Empty all our tables in case this transaction will be reused
		fields_.empty();
		participants_.empty();
		keysChanged_.empty();
    eventList_ = null;;
    //p1Alt_ = null;
    if (p1Alt_ != null)
      p1Alt_.empty();
    p1Alt_ = null;
    super.doReset();
	}
	
	protected void updatingPhase2(Map pubInstance,
                                Map pvtInstance,
                                Set keysChanged) throws AnyException
	{
	}
	
	protected void creatingPhase2(Any uniqueKey,
                                Map pubInstance) throws AnyException
	{
	}
	
	protected void deletingPhase2(Any uniqueKey,
                                Map unmgInstance) throws AnyException
	{
	}
  
  private void checkForeignDependencies(Map m, Any field)
  {
    /*
    Descriptor d = m.getDescriptor()
    if ((foreignChecked_.entries() == 0) ||
        (!foreignChecked_.contains(d)))
    {
      Set s = (Set)foreignChecked_.get(d);
      if (!s.contains(field))
      {
        // We haven't seen this descriptor/field yet, check its
        // descriptor for cached foreign key dependencies
        d.joinForeign(field, this);
      }
    }
    */
  }
	
  protected void releaseResources(boolean aborting)
  {
    Throwable t = null;
    try
    {
      // Release any resources this transaction has acquired
      Iter i = resources_.createKeysIterator();
      while (i.hasNext())
      {
        Any spec = i.next();
        Any resource = resources_.get(spec);
        ResourceAllocator allocator = (ResourceAllocator)allocators_.get(resource);
        
        e_.setThrowable(null);
        allocator.release(spec, resource, (aborting ? AnyBoolean.TRUE : null), e_);
        
        // Remember first error we got. There may have been others but well...
        if (t == null)
          t = e_.getThrowable();
        
        i.remove();
        allocators_.remove(resource);
      }
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    if (t != null)
    {
      e_.setThrowable(null);
      throw new RuntimeContainedException(t);
    }
  }
  
	private void init()
	{
		fields_       = AbstractComposite.simpleMap();
		participants_ = AbstractComposite.orderedMap();
    keysChanged_  = AbstractComposite.simpleMap();
    resources_    = AbstractComposite.simpleMap();
    allocators_   = AbstractComposite.simpleMap();
		
		// also tbd - put in the phase1 (null)
		// and phase 2 (from the base class)funcs
	}
	
	protected class UpdatePhase2 extends    CommitBase
                               implements Func
	{
    private static final long serialVersionUID = 1L;

    public Any exec(Any a) throws AnyException
		{
			Map tm = (Map)a;
			Map  m  = (Map)processing_.get(tm);

			// If there are any modified key values then remove old...
			Set k = null;
			if (keysChanged_.contains(tm))
			{
        k = (Set)keysChanged_.get(tm);
        Iter i = k.createIterator();
        while (i.hasNext())
        {
          KeyDef kd = (KeyDef)i.next();
          kd.unmanage(tm);
        }
      }

			// Copy back the private instance to the public one...
			tm.copyFrom(m);
			
			
			// ...and dump the private one
			processing_.replaceItem(tm, tm);
			
			// ...finally put back any new key values
			if (k != null)
			{
        Iter i = k.createIterator();
        while (i.hasNext())
        {
          KeyDef kd = (KeyDef)i.next();
          kd.manage(tm);
        }
      }

      LocalTransaction.this.updatingPhase2(tm, m, k);
      
			return a;
		}
	}

	protected class CreatePhase2 extends    CommitBase
                               implements Func
	{
    private static final long serialVersionUID = 1L;

    public Any exec(Any a) throws AnyException
		{
			Map o  = (Map)processing_.get(a);

			Descriptor d = o.getDescriptor();

			Map m = d.manage(process_, o);
			
			// Put the managed object back in the list we are processing.
			// This is important because:
			//   1.  Managing an object may include giving it identity
			//       semantics by putting it inside an identity wrapper.  In
			//       this case we must discard all references to the original
			//       object
			//   2.  If the object is sent to other threads via an event then
			//       references proliferate (same as 1 above) and we want
			//       any decorated semantics from now on.
			// Further to this, clients must be extremely cautious not to
			// put unmanaged objects into composite structures they intend
			// hold on to after a transaction is complete as this compromises
			// the server's handling of such objects.
			
			processing_.replaceItem(a, m);

      LocalTransaction.this.creatingPhase2(a, o);
      
			return a;
		}
	}

	protected class DeletePhase2 extends    CommitBase
                               implements Func
	{
    private static final long serialVersionUID = 1L;

    public Any exec(Any a) throws AnyException
		{
			Map k  = (Map)a;
			Map o = (Map)processing_.get(k);

			Descriptor d = o.getDescriptor();

			d.unmanage(process_, o);

      LocalTransaction.this.deletingPhase2(a, o);
      
			return a;
		}
	}
  
	protected class ResyncPhase2 extends CommitBase
                               implements Func
  {
    private static final long serialVersionUID = 1L;

    public Any exec(Any a) throws AnyException
		{
		  // We are passed the existing instance, if there was one
		  // in the cache or the newly read one otherwise...
			Map cm = (Map)a;
			
			// ...this maps to the newly read public instance, which
			// may be the same as above if there was nothing in the cache,
			// or null if an external delete was performed.
			Map rm  = (Map)processing_.get(cm);

      // If there was an old instance, copy the new one to it
      // Otherwise the new one is already in the cache.
      if (rm != null && cm != rm)
      {
        cm.copyFrom(rm);
        // leave the new/old mapping so we know what event to raise.
        // processing_.replaceItem(cm, cm);
      }
      
      if (cm == rm)
      {
        // resync read in a new object - effectively a create operation.
        // Manage in all keys.  Don't use Descriptor.manage as the
        // object already has identity etc
        Iter i = cm.getDescriptor().getAllKeys().createIterator();
        while (i.hasNext())
        {
          KeyDef kd = (KeyDef)i.next();
          kd.manage(cm);
        }
      }
      else if (rm == null)
      {
        // resync read null - effectively a delete operation.
        // Unmanage in all keys.
        cm.getDescriptor().unmanage(process_, cm);
      }
      return a;
    }
  }

  private class JoinAllTMaps extends    AbstractVisitor
														 implements Visitor
  {
    private static final long serialVersionUID = 1L;

    public void visitMap (Map m)
    {
			if (m.isTransactional())
			{
				try
				{
					LocalTransaction.this.join(m);
				}
				catch (AnyException e)
				{
					throw (new RuntimeContainedException(e));
				}
			}
    }

    public void visitArray (Array a)
    {
    }
    
		public void visitUnknown(Any u)
		{
		}
		
		public void visitAnyBoolean (BooleanI b)
		{
		}

		public void visitAnyByte (ByteI b)
		{
		}

		public void visitAnyChar (CharI c)
		{
		}

		public void visitAnyInt (IntI i)
		{
		}

		public void visitAnyShort (ShortI s)
		{
		}

		public void visitAnyLong (LongI l)
		{
		}

		public void visitAnyFloat (FloatI f)
		{
		}

		public void visitAnyDouble (DoubleI d)
		{
		}

		public void visitDecimal(Decimal d)
		{
		}

		public void visitAnyString (StringI s)
		{
		}

		public void visitAnyDate (DateI d)
		{
		}

		public void visitFunc (Func f)
		{
		}

		public void visitAnyObject (ObjectI o)
		{
		}
  }
  
  static private class PurgeKey extends AbstractFunc
  {
    private static final long serialVersionUID = 1L;

    KeyDef kd_;
    
    PurgeKey(KeyDef kd)
    {
      this.kd_ = kd;
    }
    
    public Any exec(Any a) throws AnyException
    {
      return null;
    }
  }
}

