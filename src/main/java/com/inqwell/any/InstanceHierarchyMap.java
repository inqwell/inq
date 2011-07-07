/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/InstanceHierarchyMap.java $
 * $Author: sanderst $
 * $Revision: 1.9 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.NoSuchElementException;

/**
 * The Map implementation used to build structures of managed objects within
 * the Any server through which events can propagate.  Children implementing
 * the <code>EventGenerator</code>
 * interface have this map establishing itself as an event listener on that
 * child.  By composing structures of <code>InstanceHierarchyMap</code>s 
 * a client can listen for events at some node which eminated from somewhere
 * within the structure rooted at that node.  The
 * event parameter at any point is established on the event.  Typically, instances
 * of class <code>NodeEvent</code> are propagated and as this class prepends
 * each argument passed to <code>Event.setParameter()</code>, the event
 * carries a node specification which could be applied at
 * that point to locate the source of the event.
 * <p>
 * The anticipated thread model for the use of this class is that the owner
 * thread will inspect and mutate the it, use <code>LocateNode</code> and
 * so forth, while possibly other threads will call <code>processEvent</code>
 * (and therefore <code>fireEvent</code>).  The synchronization issues
 * surrounding these aspects are handled by this class.  Otherwise there are
 * no special synchronization considerations over other implementations.
 * <p>
 * The <code>hashCode()</code> method operates identity semantics without
 * being wrapped inside an identity decorator.  This is because this
 * implementation is complicated with the EventGenerator/Listener interfaces
 * and we are not interested in comparisons with value semantics which would
 * propagate the evaluation of the hash code to whatever depth the structure
 * was.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.9 $
 * @see com.inqwell.any.Any
 */ 
public class InstanceHierarchyMap extends    AnyPMap
																  implements Map,
																						 EventGenerator,
																						 EventListener,
																						 Vectored,
																						 Orderable
{
  // This is the event parameter - but its not compulsory because
  // when the event reaches the root it has all the data it needs.
  // We may still have a listener, however, since the event has to
  // be actually processed by someone.
  protected transient Any             nameInContainer_ = null;
  private   transient EventListener   parentListener_  = null;
  private   transient OrderComparator comparator_;

  private transient EventMultiplexer  em_           = null;
  private transient Array             order_        = null;
  private transient AnyInt            index_        = null;
  private transient Set               deleteMarked_ = null;
  
  private transient Array             eventMask_;
  
  public InstanceHierarchyMap()
  {
    this(false);
  }
  
  public InstanceHierarchyMap(boolean maintainOrder)
  {
    init();
    
    if (maintainOrder)
      order_ = initOrderBacking();
  }
  
  /**
	 * Adding an entry to this <code>Map</code>.  If the item being added
	 * is an event generator then we add ourselves as an event listener
	 * on that object with the event parameter of the given key
	 */
  protected void afterAdd (Any key, Any value)
  {
		super.afterAdd(key, value);
		establishListener(key, value);
		if (order_ != null)
	    order_.add(key);
  }

  protected void beforeRemove (Any key)
  {
		if (order_ != null)
		{
      // If the key was removed by iteration then this test will always
      // fail, so no ConcurrentModificationException will be thrown
			int idx = order_.indexOf(key);
			if (idx >= 0)
				order_.remove (idx);
		}

		undoListener(get(key));

    synchronized(this)
    {
  		if (deleteMarked_ != null)
  		{
        if (deleteMarked_.contains(key))
        {
	        deleteMarked_.remove(key);
          //deleteMarked_ = null;
	      }
	    }
	  }
		super.beforeRemove (key);
  }
  
  protected void emptying()
  {
		Iter i = createIterator();
		while (i.hasNext())
		{
			Any v = i.next();
			undoListener(v);
		}

		super.emptying();

		if (order_ != null)
      order_.empty();

    synchronized(this)
    {
  		if (deleteMarked_ != null)
  		{
	      deleteMarked_.empty();
        deleteMarked_ = null;
			}
		}
  }
  
  public int entries()
  {
	  int ret = super.entries();
	
    synchronized(this)
    {
  		if (deleteMarked_ != null)
  		{
				ret -= deleteMarked_.entries();
	    }
	  }
    return ret;
  }

  public boolean contains(Any key)
  {
	  //System.out.println("InstanceHierarchyMap.contains("+key+") - delete marked " + deleteMarked_);
	  boolean ret = true;
	
    synchronized(this)
    {
  		if (deleteMarked_ != null)
  		{
        if (deleteMarked_.contains(key))
        {
	        deleteMarked_.remove(key);
	        this.remove(key);
		      ret = false;
	      }
	    }
	    if (ret)
	      ret = super.contains(key);
  	  else
  	  {
  		  ret = super.contains(key);
  		}
    }
		return ret;
	}

  public void replaceItem (Any key, Any item)
  {
    synchronized(this)
    {
      initOrderBacking();
      removeAllDeleted();
      Any v = null;
      //if ((v = getIfContains(key)) != null)
      if (contains(key))
      {
        v = get(key);
        if (v != null)
        {
          // beforeRemove functionality for the value. The key stays where
          // it is in the order backing
          undoListener(v);
          undoParent(v);
        }
        getMap().put(key, item);

        establishParent(item);
        establishListener(key, item);
      }
      else
        add(key, item);
    }
  }

  public Any getIfContains(Any key)
  {
    Any ret = null;

    synchronized(this)
    {
      if (deleteMarked_ != null)
      {
        if (deleteMarked_.contains(key))
        {
          deleteMarked_.remove(key);
          this.remove(key);
          ret = null;
        }
        else
          ret = super.getIfContains(key);
      }
      else
      {
        ret = super.getIfContains(key);
      }
    }
    return ret;
  }

//  public String toString()
//  {
//    //removeAllDeleted();
//    if (deleteMarked_ != null)
//    {
//      synchronized (deleteMarked_)
//      {
//        return super.toString() + "\ndeleted: " + deleteMarked_.toString() + " order " + order_;
//      }
//    }
//    return super.toString() + "\ndeleted: []" + " order " + order_;
//  }

  /**
   * Check if the key has been marked for deletion and if
   * so remove it from the map and throw an exception.
   */
	protected void handleNotExist(Any key)
	{
		// not strictly thread-safe but we tolerate this for the
		// sake of efficiency and its not serious
    synchronized(this)
    {
  		if (deleteMarked_ != null)
  		{
        if (deleteMarked_.contains(key))
        {
	        deleteMarked_.remove(key);
	        this.remove(key);
		      throw new FieldNotFoundException ("Key: " + key);
	      }
	    }
  	  else
  	  {
  		  super.handleNotExist(key);
  		}
    }
	}

  public void replaceValue (Any key, Any value)
  {
		throw new UnsupportedOperationException ("replaceValue() " + getClass());
	}
	
  public Map shallowCopy()
  {
		throw new UnsupportedOperationException ("shallowCopy() " + getClass());
  }
  
  public void fireEvent (Event e) throws AnyException
  {
		if (e.isConsumed())
			return;

    // If we have a list of delete-marked objects and the
    // event is from one of them then ignore it
    if (e instanceof NodeEvent)
    {
      synchronized(this)
      {
        if (deleteMarked_ != null)
        {
          NodeEvent ne = (NodeEvent)e;
          if (ne.getNodeSpec().entries() > 1 &&
              deleteMarked_.contains(ne.getSourceName()))
            return;
        }
      }
    }
    
		// For true BOTs we would really like to remove them
		// from our structure but this is not easy to make
		// thread-safe in the server context so settle for
		// removing transactional status from the object.
//		Any a = e.getContext();
//		if (a != null && a.isTransactional())
//		{
//			// in fact, this won't happen where the delete
//			// semantics of the transaction have already reset the
//			// transactional status of the object
//			if (AbstractEvent.getBasicType(e.getId()).equals(EventConstants.BOT_DELETE))
//			{
//				Map m = (Map)a;
//				m.setTransactional(false);
//			}
//		}
		
		// Check if our parent is a node set collection. If it is
		// and 1) the originator of the event has the same name as the
		//        node set marker
		//     2) the event is a BOT delete event
		// then we take this to mean that we represent the aggregation
		// collection whose primary reason to exist is no longer valid.
		// Mark ourselves for removal in our parent and raise a
		// NODE_REMOVED event as well.
		// If we are the container of the BOT which is being deleted then
		// just mark the instance for removal here.
		Event nodeRemoved = null;
		if (AbstractEvent.getBasicType(e.getId()).equals(EventConstants.BOT_DELETE))
		{
      NodeEvent n = (NodeEvent)e;
      // Check the event has been raised by our immediate child by
      // looking at the current state of the path in the event type.
      // The value of 2 is because NodeEvent.setParam adds "strict" also!
      // If we didn't do this first then in recursively defined
      // structures the vector number (and mark for delete more importantly)
      // would be performed at each node-set level.
      if (n.getNodeSpec().entries() == 2)
      {
        Any orig = n.getOriginatorName();
  		  if (getParentAny() != null && !isDeleteMarked(orig))
        {
          // We know its a BOT_DELETE so this should not croak...
          Any origFQName = ((Map)e.getContext()).getDescriptor().getFQName();
          
  		    Any nodeSet = (getParentAny() != null) ? getParentAny().getNodeSet() : null;
          // We know its a NodeEvent by now
          if (nodeSet != null &&
              (nodeSet.equals(origFQName) || // allow two possibilities for
               nodeSet.equals(orig)) &&      // node set master - fqname and map key
              !getParentAny().isDeleteMarked(getNameInParent()))
          {
  			    nodeRemoved = makeEvent(EventConstants.NODE_REMOVED);
  			    Map eventType = (Map)nodeRemoved.getId();
  			    // Check if we are a able to put the vector number
  			    // in the node removed event
  			    if (getParentAny() instanceof Vectored)
  			    {
  			      Vectored v = (Vectored)getParentAny();
  			      int indx = v.indexOf(getNameInParent());
  			      if (indx >= 0)
  			      {
  			        eventType.replaceItem(EventConstants.EVENT_VECTOR,
  			                              new AnyInt(indx));
  			      }
  			    }
            
            // Put the originating event's descriptor in the removal
            // event. This allows better filtering in scripted "listen"
            // statements
            eventType.replaceItem(Descriptor.descriptor__,
                                  ((Map)n.getId()).get(Descriptor.descriptor__));
          }
        }
  		  this.markForDelete(orig);
      }
		}
		
		// Send the event(s).  If we have a NODE_REMOVED event
		// because we have been marked for deletion then send
		// that first.
		if (nodeRemoved != null)
    {
      Process     p = Globals.getProcessForCurrentThread();
      Transaction t = p.getTransaction();
      if (t.isGatheringEvents())
      {
        // If this node is in my node space then pop the event in
        if (getOwnerProcess() == p)
          p.getTransaction().addEvent(nodeRemoved);
      }
      
		  sendEvent(nodeRemoved);
    }
		
		sendEvent(e);
    
    // Do this last. Otherwise the check at the top of this function, when
    // run in the parent, will stop the event in its tracks.
    if (nodeRemoved != null)
      getParentAny().markForDelete(getNameInParent());
      
  }

  public synchronized void addEventListener (EventListener l, Any eventParam)
  {
		// Note: our parent MUST be established before it adds
		// itself as our parentListener!!
		// Note also that when called by our parent the parameter is
		// our name in the parent.
		// For other listeners the parameter is passed on to the multiplexer
		//if ((eventParam != null) && l != getParentAny())
		//{
			//throw new IllegalArgumentException
								// ("event parameter only allowed for parent listener");
		//}
		
		if (l == getParentAny())
		{
			//System.out.println ("InstanceHierarchyMap.addEventListener() establishing parent listener");
			parentListener_  = l;
			nameInContainer_ = eventParam;
		}
		else
		{
			if (em_ == null)
				em_ = new EventMultiplexer();
				
			em_.addEventListener(l, eventParam);
		}
			
  }
  
  public synchronized void addEventListener (EventListener l)
  {
		addEventListener(l, null);
  }
  
  public synchronized void removeEventListener (EventListener l)
  {
		// If we are removing the parent listener remove the
		// other as well
		
		// Note that generally, since we do not traverse the
		// structure from the node being removed downwards,
		// we cannot be sure that an otherListener at a lower
		// level won't get fired in a shared data, multithreaded
		// environment if an event goes off before the now
		// unreferenced object network is GC'ed.  Hence we should
		// avoid doing this, or de-construct structures appropriately,
		// which means always removing a node which has an otherListener
		// from its immediate parent.
		if (l == getParentAny())
		{
			parentListener_ = null;
			em_             = null;
			nameInContainer_       = null;
		}
		else
		{
			if (em_ != null)
				em_.removeEventListener(l);
		}
  }
  
	public void removeAllListeners ()
	{
	}

  public boolean processEvent(Event e) throws AnyException
  {
    // pass the event on to our listeners. If there is a mask
    // in place then respect any event types it should be
    // preventing. Optimise acquisition of monitor as mask is
    // most often not used.
    if (eventMask_ != null)
    {
      synchronized(this)
      {
        if (eventMask_ != null && eventMask_.contains(e.getId()))
          return true;
      }
    }
    fireEvent (e);
    return true;
  }

  public void removeInParent()
  {
  	Composite parent = null;
  	if ((parent = getParentAny()) != null)
  	  parent.remove(nameInContainer_);
  }
  
  /**
   *  Mark the given key as deleted. This is the only thread-safe method
   *  offered and supports the removal of key-value mapping by threads
   *  other than the "owner process" of this map. 
   */
  public void markForDelete(Any key)
  {
    synchronized(this)
    {
  	  if (deleteMarked_ == null)
  	  {
			  deleteMarked_ = AbstractComposite.set();
      }
      
	    if (!deleteMarked_.contains(key))
	      deleteMarked_.add(key);
	  }
	}
	
  public boolean isDeleteMarked(Any key)
  {
    synchronized(this)
    {
      if (deleteMarked_ != null)
      {
        return(deleteMarked_.contains(key));
      }
    }
    return false;
  }
  
  public Array getGeneratedEventTypes()
  {
		return EventConstants.ALL_TYPES;
  }

  public Event makeEvent(Any eventType)
  {
		Event ret = null;
		
		// We can make node replaced events only
		if (eventType.equals(EventConstants.NODE_REPLACED) ||
				eventType.equals(EventConstants.NODE_REPLACED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
		}
		else if (eventType.equals(EventConstants.NODE_REMOVED) ||
				     eventType.equals(EventConstants.NODE_REMOVED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
      if (eventType.equals(EventConstants.NODE_REMOVED))
          ret.setContext(this);
		}
		else if (eventType.equals(EventConstants.NODE_ADDED) ||
				     eventType.equals(EventConstants.NODE_ADDED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
		}
		else if (eventType.equals(EventConstants.BOT_UPDATE))
		{
			ret = new NodeEvent(makeEventType(EventConstants.BOT_UPDATE));
			ret.setContext(this);
		}
		
		if (ret == null)
		{
			throw new IllegalArgumentException
				("InstanceHierarchyMap.makeEvent() invalid type " + eventType);
		}
		
		return ret;
  }

  public boolean raiseAgainstChildren(Event e)
  {
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return EventConstants.ALL_TYPES;
	}

  public void sort (Array orderBy)
  {
	  comparator_ = null;
  	order_ = initOrderBacking();
		AbstractComposite.sortOrderable(this, orderBy);
	}
	
  public void sort (OrderComparator c)
  {
	  comparator_ = c;
  	order_ = initOrderBacking();
		AbstractComposite.sortOrderable(this, c);
		if (comparator_ != null)
      comparator_.setTransaction(Transaction.NULL_TRANSACTION);
  }

  public void sort (Array orderBy, OrderComparator c)
  {
	  // Remember the comparator if some ordering expressions
	  // were supplied.
	  if (orderBy != null)
	    comparator_ = c;  // The orderBy gets put into the comparator later
	  else
	  	comparator_ = null; 
	
  	order_ = initOrderBacking();
		AbstractComposite.sortOrderable(this, orderBy, comparator_);
		if (comparator_ != null)
		  comparator_.setTransaction(Transaction.NULL_TRANSACTION);
  }

  public OrderComparator getOrderComparator()
  {
    return comparator_;
	}
	
  public int reorder(Any a)
  {
    removeAllDeleted();
    
	  // If we don't have any order backing there's nothing to do
	  if (order_ == null)
	    return -1;
	
	  if (comparator_ == null)
	    return -1;
	
	  // It should be the last item in the order backing! Do this
	  // as an optimisation, rather than using indexOf.  Bit ooer,
	  // missus...
	  int i = order_.entries() - 1;
	  a = order_.get(i);
	  Any v = this.get(a);
	  this.removeByVector(i);

		//System.out.println ("InstanceHierarchyMap.reorder " + a + " " + v);

    comparator_.setOrderMode(Map.I_VALUES);
		int insertionPosition = AbstractComposite.findInsertionPosition(this,
			                                                              comparator_,
			                                                              v);
			
		//System.out.println ("InstanceHierarchyMap.reorder adding at" + insertionPosition);

		if (insertionPosition < 0)
		{
	    this.add(a, v);
		}
		else
		{
	    this.addByVector(insertionPosition, a, v);
	  }
    comparator_.setOrderMode(Map.I_KEYS);
    return insertionPosition;
	}
	
  public java.util.List getList ()
  {
  	order_ = initOrderBacking();
		return order_.getList();
  }

  public Array getArray()
  {
  	order_ = initOrderBacking();
    return order_;
  }
  
  public void removeByVector (int at)
  {
  	order_ = initOrderBacking();
  	Any key = order_.remove(at);
    // don't want to do indexOf in beforeRemove, above, in this case
    Array order = order_;
    order_      = null;
    try
    {
    	this.remove(key);
    }
    finally
    {
      order_ = order;
    }
  }
  
  public void removeByVector (Any at)
  {
  	order_ = initOrderBacking();
  	index_.copyFrom(at);
  	this.removeByVector(index_.getValue());
  }
  
  
  public int indexOf(Any a)
  {
  	//initOrderBacking();
  	if (order_ != null)
    {
      synchronized(this)
      {
        if (deleteMarked_ != null)
        {
          if (deleteMarked_.contains(a))
          {
            deleteMarked_.remove(a);
            this.remove(a);
            return -1;
          }
        }
      }
  	  return order_.indexOf(a);
    }  	
  	return -1;
  }
  
  public Any getNameInParent()
  {
		return nameInContainer_;
  }
  
  public Any getPath()
  {
    Any ret = null;
    
    if (getOwnerProcess() != null)
    {
      Composite parent = getParentAny();
      Any       name   = getNameInParent();
      
      if (parent == null)
        return ServerConstants.ROOT;
      
      ret = new NodeSpecification(parent.getPath().toString() +
                                  NodeSpecification.strict__.toString() +
                                  name.toString());
    }
    return ret;
  }
  
  public Any getByVector (int at)
  {
    order_ = initOrderBacking();
    removeAllDeleted();
  	Any key = order_.get(at);
    
  	return this.get(key);
  }
  
  public Any getByVector (Any at)
  {
  	index_.copyFrom(at);
  	return this.getByVector(index_.getValue());
  }
  
  public Any getKeyOfVector(int at)
  {
    order_ = initOrderBacking();
    removeAllDeleted();
    Any key = order_.get(at);
    return key;
  }

  public Any getKeyOfVector(Any at)
  {
    index_.copyFrom(at);
    return getKeyOfVector(index_.getValue());
  }
  
  public void addByVector(Any value)
  {
  	order_ = initOrderBacking();
  	Any key = IdentityOf.identityOf(value);
  	add(key, value);
  }
  
  public void addByVector(int at, Any value)
  {
    Any key = null;
    if (value instanceof Map)
      key = ((Map)value).getUniqueKey();
    
		addByVector(at, key, value);
  }
  
  public void addByVector(int at, Any key, Any value)
  {
		if (at < 0)
			throw new ArrayIndexOutOfBoundsException(at);
		
		if (at > entries())
			throw new ArrayIndexOutOfBoundsException(at);
			
  	order_ = initOrderBacking();
  	
  	if (at < entries())
  	{
			add((key == null ? IdentityOf.identityOf(value) : key), value);
			// Because of afterAdd() the key is in the ordering vector at the
			// end.  Move it to the desired place
			Any keyOf = order_.remove(entries() - 1);
			order_.add(at, keyOf);
  	}
  	else if (at == entries())
  	{
			// just adding at the end
			add((key == null ? IdentityOf.identityOf(value) : key), value);
		}
		else
		{
			// Put place-holders in to get the desired result
			int toAdd = at - entries();
			for (int i = 0; i < toAdd; i++)
			{
				Any anyNull = new AnyNull();
				add(IdentityOf.identityOf(anyNull), anyNull);
			}
			add((key == null ? IdentityOf.identityOf(value) : key), value);
		}
  }
  
  public void reverse()
  {
    order_ = initOrderBacking();
    order_.reverse();
  }
  
  public void setSparse(boolean isSparse)
  {
  }
  
  synchronized public void setEventMask(Array eventMask)
  {
    eventMask_ = eventMask;
  }
  
	public Iter createKeysIterator()
	{
		removeAllDeleted();
    if (order_ == null)
      return super.createKeysIterator();
    else
      return new InstanceHierarchyMapKeysIter();
	}

	public Iter createIterator()
	{
		removeAllDeleted();
    if (order_ == null)
      return super.createIterator();
    else
      return new InstanceHierarchyMapIter();
	}

  public Iter createConcurrentSafeIterator()
  {
    removeAllDeleted();
    return new InstanceHierarchyMapIter(true);
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    Array order = order_;
    order_ = null;
    
    InstanceHierarchyMap m;
   
    try
    {
      m = (InstanceHierarchyMap)super.clone();
      m.init();

      if (order != null)
      {
        m.order_ = order.shallowCopy();
        m.index_ = new AnyInt();
      }

      synchronized(this)
      {
        if (deleteMarked_ != null)
        {
          // Or should we just remove the delete-marked entries?
          m.deleteMarked_ = (Set)deleteMarked_.shallowCopy();
        }
      }
    }
    finally
    {
      order_ = order;
    }
    return m;
  }
  
	/**
	 * Override.  Default implementation recreates the contents of this
	 * because it does not assume that this is similar to the argument.
	 * In this case, if we are transactional,  we assume that we are
   * taking on transaction private values whereby we contain the same
   * type of children as the argument
	 */
  public Any copyFrom (Any a)
  {
    if (this.isTransactional())
    {
      if (!(a instanceof Map))
        throw new IllegalArgumentException ();
  
      Map from = (Map)a;
      
      Iter i = from.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        if (this.contains(k))
        {
          Any v = this.get(k);
          if (!v.isConst())
            v.copyFrom(from.get(k));
        }
      }
    }
    else
      super.copyFrom(a);
      
    return this;
  }

  public int hashCode()
  {
		return identity();
	}

  public boolean equals(Object o)
  {
    return (o == this);
  }

  public boolean equals(Any a)
  {
    return (a == this);
  }

  public Object[] toArray()
  {
  	if (order_ == null)
  	  throw new IllegalArgumentException("Not in ordered state");
  	  
    Object[] o = new Object[entries()];

    int i;

    for (i = 0; i < entries(); i++)
    {
      o[i] = this.get(order_.get(i));
    }

    return o;
  }

	public Array initOrderBacking()
	{
		if (order_ != null)
		  return order_;
		
		Iter i = createKeysIterator();

		index_ = new AnyInt();
		
		Array order = AbstractComposite.array(this.entries() == 0 ? 10 : this.entries());
		
		while (i.hasNext())
		{
			Any key = i.next();
			order.add(key);
		}
		
		return order;
	}
	
	protected void finalize() throws Throwable
	{
		em_ = null;
    super.finalize();
	}
	
  public Process getOwnerProcess()
  {
    Composite child  = this;
    Composite parent = null;
    while((parent = child.getParentAny()) != null)
      child = parent;
    
    // child is now as far as we can go. It will be the root of the
    // process's node space if this was somewhere in it, otherwise it
    // won't be. The root node has the process of the space it is
    return child.getProcess();
  }
  
  private void init()
  {
		nameInContainer_ = null;
		parentListener_  = null;

		em_              = null;
		order_           = null;
		index_           = null;
    comparator_      = null;

    deleteMarked_    = null;
  }

  private void removeAllDeleted()
  {
    synchronized(this)
    {
  		if (deleteMarked_ != null)
  		{
			  Iter i = deleteMarked_.createIterator();
			  while (i.hasNext())
			  {
			  	Any a = i.next();
			  	// remove from deleteMarked_ here so remove(a)
			  	// doesn't cause a concurrent modification exception
			  	// and handleNotExist(key) a FieldNotFound exception
			  	i.remove();
			  	this.remove(a);
			  }
  		}
    }
  }
		
  protected void sendEvent(Event e) throws AnyException
  {  
		EventListener pl = null;
		EventListener ol = null;
		Any           p  = null;
		
		synchronized (this)
		{
		  pl = parentListener_;
		  ol = em_;
		  p  = nameInContainer_;
		}
		
		Event toOther = null;
		
		// first send to the other listener and check for consumption
		if (ol != null)
		{
			toOther = e.cloneEvent();
			ol.processEvent(toOther);
		}
		
		// Only pass on the event to our parent listener if it
		// hasn't been consumed by our other listener.
		if (toOther != null && toOther.isConsumed())
			return;
		
		if (pl != null)
		{
			if (p != null)
				e.setParameter(p);
			
			//System.out.println("InstanceHierarchyMap.fireEvent() " + p);
			//System.out.println("InstanceHierarchyMap.fireEvent() " + e);
			//System.out.println("InstanceHierarchyMap.fireEvent() " + pl);
			pl.processEvent(e);
		}
  }

	private Map makeEventType(Any type)
	{
		Map ret = AbstractComposite.eventIdMap();
		
		ret.add (Descriptor.descriptor__, getDescriptor());
		ret.add (EventConstants.EVENT_TYPE, type);
		
		return ret;
	}
	
  private void establishListener(Any key, Any value)
  {
		if (value instanceof EventGenerator)
		{
			EventGenerator e = (EventGenerator)value;
			e.addEventListener(this, key);
		}
	}
	
  private void undoListener(Any a)
  {
		if (a instanceof EventGenerator)
		{
			EventGenerator e = (EventGenerator)a;
			e.removeEventListener(this);
		}
	}
	
	// Use the last comparator to find the ordering position
	// for the node we are inserting.  Employs binary-chop
	// algorithm for speedyish processing
	private void readObject(ObjectInputStream instr)
																							throws IOException,
																										 ClassNotFoundException
	{
		instr.defaultReadObject();
		// once this method has returned the object graph
		// underneath us is complete, so we can traverse our
		// immediate children and fix up the parental links
		Iter i = createKeysIterator();
		while (i.hasNext())
		{
			Any k = i.next();
			afterAdd(k, get(k));
		}
	}


  private class InstanceHierarchyMapIter extends AbstractIter implements Iter
  {
    private Iter i_;
    private Any  current_;
    
    public InstanceHierarchyMapIter()
    {
      this(false);
    }
    
    public InstanceHierarchyMapIter(boolean concurrentSafe)
    {
      setIterRoot(InstanceHierarchyMap.this);
      
      if (order_ != null)
      {
        if (concurrentSafe)
          i_ = InstanceHierarchyMap.this.order_.shallowCopy().createIterator();
        else
          i_ = InstanceHierarchyMap.this.order_.createIterator();
      }
      else
      {
        if (concurrentSafe)
        {
          Any keys = InstanceHierarchyMap.this.keys();
          i_ = keys.createIterator();
        }
        else
          throw new IllegalStateException();
      }
    }

    public boolean hasNext()
    {
      return i_.hasNext();
    }

    public Any next()
    {
      // Use underlying Map so as to avoid problems with anything
      // delete-marked
      return (Any)InstanceHierarchyMap.this.getMap().get(current_ = i_.next());
    }

    public void remove()
    {
      i_.remove();
      InstanceHierarchyMap.this.remove(current_);
    }
  }

  private class InstanceHierarchyMapKeysIter extends AbstractIter implements Iter
  {
    private Iter i_;
    private Any current_;
    
    public InstanceHierarchyMapKeysIter()
    {
      setIterRoot(InstanceHierarchyMap.this);
      i_ = InstanceHierarchyMap.this.order_.createIterator();
    }

    public boolean hasNext()
    {
      return i_.hasNext();
    }

    public Any next()
    {
      return current_ = i_.next();
    }

    public void remove()
    {
      i_.remove();
      InstanceHierarchyMap.this.remove(current_);
    }
  }
}
