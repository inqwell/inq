/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ClientObject.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

import java.io.ObjectStreamException;
import com.inqwell.any.identity.AnyMapEgDecor;

/**
 * The Map implementation which represents instances of managed objects within
 * the Any client.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 * @see com.inqwell.any.Any
 */ 
public class ClientObject extends    AnyMap
													implements Map,
																		 EventGenerator,
																		 Cloneable
{
  // Our event generation strategy is delegated to a contained generator
  // object.  We can then change this strategy, if required.
  private transient EventGenerator eventPropagator_;
  
	// This remains null until instances are created by the descriptor
	// itself
  private Descriptor descriptor_;
  
  public ClientObject()
  {
  	//init();
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

  public void accept (Visitor v)
  {
    v.visitMap(this);
  }

	/**
	 * Override.  Default implementation recreates the contents of this
	 * because it does not assume that this is similar to the argument.
	 * In this case we assume that we are taking on transaction private
	 * values whereby we contain the same type of children as the argument
	 */
  public Any copyFrom (Any a)
  {
    if (a == this)
      return this;
      
    if (!(a instanceof Map))
      throw new IllegalArgumentException (a.getClass().toString() + " is not a map");

    Map from = (Map)a;
    
    Iter i = from.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      if (this.contains(k))
        this.get(k).copyFrom(from.get(k));
    }
    return this;
  }

  public void fireEvent (Event e) throws AnyException
  {
    // There may be no one listening yet
    if (eventPropagator_ != null)
      eventPropagator_.fireEvent(e);
  }

  public void addEventListener (EventListener l, Any eventParam)
  {
    if (eventPropagator_ == null)
      init();

    eventPropagator_.addEventListener(l, eventParam);
  }
  
  public void addEventListener (EventListener l)
  {
    throw new UnsupportedOperationException ("Must supply an event parameter");
  }
  
  public void removeEventListener (EventListener l)
  {
    if (eventPropagator_ != null)
      eventPropagator_.removeEventListener(l);
  }

  public void removeAllListeners ()
  {
    if (eventPropagator_ != null)
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
		else if (eventType.equals(EventConstants.NODE_REPLACED) ||
				     eventType.equals(EventConstants.NODE_REPLACED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
			ret.setContext(this);
		}
		else if (eventType.equals(EventConstants.NODE_ADDED) ||
				     eventType.equals(EventConstants.NODE_ADDED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
			ret.setContext(this);
		}
		else if (eventType.equals(EventConstants.NODE_REMOVED) ||
				     eventType.equals(EventConstants.NODE_REMOVED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
		}
		
		if (ret == null)
		{
			throw new IllegalArgumentException
				("ClientObject.makeEvent() invalid type " + eventType);
		}
		
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
		if (descriptor_ != null)
			throw new IllegalArgumentException("Descriptor is already set!");
			
		descriptor_ = d;
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    // Note that a managed object only contains leaf nodes.
    
    if (isTransactional())
      return this;

    ClientObject m = (ClientObject)super.clone();
    
    m.eventPropagator_ = (EventGenerator)AbstractAny.cloneOrNull(eventPropagator_);
    return m;
  }
  
	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	
  protected void emptying() {}

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
		eventPropagator_ = new EventMultiplexer();
  }
}
