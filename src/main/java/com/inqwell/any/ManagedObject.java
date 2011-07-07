/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ManagedObject.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

import com.inqwell.any.identity.AnyMapEgDecor;

/**
 * The Map implementation which represents instances of managed objects within
 * the Any server.
 * <p>
 * Serialization:
 * This class replaces itself in a serialization stream with com.inqwell.any.ClientObject
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class ManagedObject extends    AnyMap
                           implements Map,
                                      EventGenerator,
                                      Cloneable
{
  private static final long serialVersionUID = 1L;

  // Our event generation strategy is delegated to a contained generator
  // object.  We can then change this strategy, if required.
  private transient EventGenerator eventPropagator_;
  
	// This remains null until instances are created by the descriptor
	// itself
  private Descriptor descriptor_;
  
  public ManagedObject()
  {
  	init();
  }
  
  public ManagedObject(EventGenerator eg)
  {
  	eventPropagator_ = eg;
    init();
  }
  
  public Map shallowCopy()
  {
		// shallowCopy does a buildNew so this should not be necessary
		Map m = super.shallowCopy();
		return m;
  }
  
  public Map bestowIdentity()
	{
		return new AnyMapEgDecor (this);
	}

	/**
	 * Override.  Default implementation recreates the contents of this
	 * because it does not assume that this is similar to the argument.
	 * In this case we assume that we are taking on transaction private
	 * values whereby we contain the same type of children as the argument
	 */
  public Any copyFrom (Any a)
  {
    if (!(a instanceof Map))
      throw new IllegalArgumentException (a.getClass().toString() + " is not a map");

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
    return this;
  }

  public void fireEvent (Event e) throws AnyException
  {
    eventPropagator_.fireEvent(e);
  }

  public void addEventListener (EventListener l, Any eventParam)
  {
    eventPropagator_.addEventListener(l, eventParam);
  }
  
  public void addEventListener (EventListener l)
  {
    throw new UnsupportedOperationException ("Must supply an event parameter");
  }
  
  public void removeEventListener (EventListener l)
  {
    eventPropagator_.removeEventListener(l);
  }

  public void removeAllListeners ()
  {
    eventPropagator_.removeAllListeners();
  }
  
  public Array getGeneratedEventTypes()
  {
		Array ret = AbstractComposite.array();
		
		ret.add(makeEventType(EventConstants.BOT_CREATE));
		ret.add(makeEventType(EventConstants.BOT_DELETE));
		ret.add(makeEventType(EventConstants.BOT_UPDATE));
		
		return ret;
  }
  
  public Event makeEvent(Any eventType)
  {
		Event ret = null;
		
		if (eventType.equals(EventConstants.BOT_CREATE))
		{
			ret = new SimpleEvent(makeEventType(EventConstants.BOT_CREATE));
			ret.setContext(this);
		}
    else if (eventType.equals(EventConstants.BOT_DELETE))
		{
			ret = new NodeEvent(makeEventType(EventConstants.BOT_DELETE));
			ret.setContext(this);
		}
		else if (eventType.equals(EventConstants.BOT_UPDATE))
		{
			ret = new NodeEvent(makeEventType(EventConstants.BOT_UPDATE));
			ret.setContext(this);
		}
		
//		if (eventType.equals(EventConstants.NODE_REPLACED) ||
//				eventType.equals(EventConstants.NODE_REPLACED_CHILD))
//		{
//			ret = new NodeEvent(makeEventType(eventType));
//		}
		
//		if (ret == null)
//		{
//			throw new IllegalArgumentException
//				("ManagedObject.makeEvent() invalid type " + eventType);
//		}
		
		return ret;
	}
	
  public boolean raiseAgainstChildren(Event e)
  {
    return false;
  }

  public Descriptor getDescriptor()
  {
		return descriptor_;
  }

  public void setDescriptor(Descriptor d)
  {
		if (descriptor_ != null && d != null)
			throw new IllegalArgumentException("Descriptor is already set!");
			
		descriptor_ = d;
    init();
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    // Note that a managed object only contains leaf nodes.
    // If we are transactional then we are happy to propagate
    // tha same object from within clone().  Otherwise make new

    if (isTransactional())
      return this;
    
    ManagedObject m = (ManagedObject)super.clone();
    
    if (eventPropagator_ != null)
      m.eventPropagator_ = (EventGenerator)eventPropagator_.cloneAny();
    else
      m.init();
      
    return m;
  }
  
	protected boolean beforeAdd(Any key, Any value)
  {
//    if (isTransactional())
//      throw new AnyRuntimeException("Mutating the transactional map child " + key);
    
    return true;
  }
  
	protected void afterAdd(Any key, Any value) {}
  
	protected void beforeRemove(Any key)
  {
//    if (isTransactional())
//      throw new AnyRuntimeException("Mutating the transactional map child " + key);
//    
//    // Only allow removal of AnyObject instances. May be a bit cheesy
//    // but let's see
//    Any a = this.get(key);
//    if (!(a instanceof AnyObject))
//      throw new AnyRuntimeException("Not a place-holder field: " + key);
  }
  
	protected void afterRemove(Any key, Any value) {}
	
  protected void emptying() {}

//	protected Object writeReplace() throws ObjectStreamException
//	{
//		Map m = new ClientObject();
//		
//		Iter i = createKeysIterator();
//		while (i.hasNext())
//		{
//			Any k = i.next();
//			m.add(k, get(k));
//		}
//		
//		// We can put our descriptor in as this will also be replaced
//		// by the serialization process
//		m.setDescriptor(descriptor_);
//		
//		// and we are transctional
//		m.setTransactional(true);
//		return m;
//	}
	
	private Map makeEventType(Any type)
	{
		Map ret = AbstractComposite.eventIdMap();
		
		ret.add (Descriptor.descriptor__, getDescriptor());
		ret.add (EventConstants.EVENT_TYPE, type);
    if (type.equals(EventConstants.BOT_CREATE))
      ret.add(EventConstants.EVENT_CREATE, AnyAlwaysEquals.instance());
		
		return ret;
	}
	
  private void init()
  {
  	if (eventPropagator_ == null)
      eventPropagator_ = new NodeEventPropagator();
  }
}
