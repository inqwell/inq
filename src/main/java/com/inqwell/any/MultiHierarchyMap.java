/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MultiHierarchyMap.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

import com.inqwell.any.identity.AnyMapEgDecor;
import java.io.ObjectStreamException;
import java.io.InvalidObjectException;

/**
 * A Map implementation that places itself as a listener on
 * any event generating children and propagates any events
 * received from them to its listeners.
 * <p>
 * This class is not subject to single parent restrictions and
 * so can be used for globally established lists that individual
 * processes may want to put in their own node space.
 * <p>
 * No ordering is supported, so this map type cannot be sorted
 * or maintain insertion order. However, it de-serializes to
 * an <code>InstanceHierarchyMap</code> so once transferred to
 * a client it can be sorted.
 * <p>
 * This map does call <code>Composite.setParent(this)</code> on
 * composite children, so those that require a strict hierarchy
 * cannot also be added to other such map implementations.
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */ 
public class MultiHierarchyMap extends    AnyMap
                               implements Map,
                                          EventGenerator,
                                          EventListener,
                                          Cloneable
{
  // Our event generation strategy is delegated to a contained generator
  // object.  We can then change this strategy, if required.
  private transient EventGenerator eventPropagator_;
  
  public MultiHierarchyMap()
  {
  	init();
  }
  
  public MultiHierarchyMap(EventGenerator eg)
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

  public void fireEvent (Event e) throws AnyException
  {
    eventPropagator_.fireEvent(e);
  }

  public boolean processEvent(Event e) throws AnyException
  {
    // pass the event on to our listeners
    fireEvent (e);
    return true;
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
		return EventConstants.ALL_TYPES;
  }
  
  public Array getDesiredEventTypes()
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
    return false;
  }

  public Object clone() throws CloneNotSupportedException
  {
    MultiHierarchyMap m = (MultiHierarchyMap)super.clone();
    
    m.eventPropagator_ = (EventGenerator)eventPropagator_.cloneAny();
    return m;
  }
  
  public boolean equals(Object o)
  {
    return (o == this);
  }

  public boolean equals(Any a)
  {
    return (a == this);
  }

  public int hashCode()
  {
		return identity();
	}

  /**
	 * Adding an entry to this <code>Map</code>.  If the item being added
	 * is an event generator then we add ourselves as an event listener
	 * on that object with the event parameter of the given key
	 */
  protected void afterAdd (Any key, Any value)
  {
		establishParent(value);
		establishListener(key, value);
  }

	protected void afterRemove(Any key, Any value) {}

  protected void beforeRemove (Any key)
  {
		undoListener(get(key));
		undoParent(get(key));
  }
  
	protected boolean beforeAdd(Any key, Any value) { return true; }

  protected void emptying()
  {
		Iter i = createIterator();
		while (i.hasNext())
		{
			Any v = i.next();
			undoListener(v);
		}
  }
	
	protected Object readResolve() throws ObjectStreamException
	{
    Map m = new InstanceHierarchyMap();
    Iter i = this.createKeysIterator();
    System.out.println("MultiHierachyMap.readResolve() " + this);
    while (i.hasNext())
    {
      Any k = i.next();
      Any v = this.get(k);
      m.add(k, v);
    }
    return m;
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
	
	private void establishParent(Any value)
	{
		if (value instanceof Composite)
		{
			Composite c = (Composite)value;
			c.setParent(this);
		}
	}
	
	private void undoParent(Any value)
	{
		if (value instanceof Composite)
		{
			Composite c = (Composite)value;
			c.setParent(null);
		}
	}
	
	private Map makeEventType(Any type)
	{
		Map ret = AbstractComposite.eventIdMap();
		
		ret.add (Descriptor.descriptor__, getDescriptor());
		ret.add (EventConstants.EVENT_TYPE, type);
		
		return ret;
	}
	
  private void init()
  {
  	if (eventPropagator_ == null)
      eventPropagator_ = new NodeEventPropagator();
  }
}
