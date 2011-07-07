/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EventMultiplexer.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.channel.WriteClosedChannelException;

/**
 * An event listener and generator which propagates events to multiple
 * listeners.
 * <P>
 * This class is useful for those cases where there might be
 * multiple listeners for an event but for which we don't want
 * to connect to the actual source until all those listeners are
 * set up.
 * <P>
 * It might also be the case that an event generator only supports
 * one listsner at any one time, for example Map implementations
 * whose one and only listener is usually their parent.
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 */
public class EventMultiplexer extends    AbstractAny
															implements EventGenerator,
                                         EventListener,
                                         Cloneable
{
	private Map listeners_ = new SynchronizedMap(new AnyOrderedMap());

	// The types of event that will pass through this multiplexer.
	// In fact, this is constrained to a single type, as the EventDispatcher
	// class deals with multiple types.
	Array eventTypes_;

	public EventMultiplexer()
	{
	}

	public EventMultiplexer(Any eventType)
	{
		eventTypes_ = AbstractComposite.array();
		eventTypes_.add(eventType);
	}

  public boolean processEvent(Event e) throws AnyException
  {
  	fireEvent(e);
    return true;
	}

  public void fireEvent (Event e) throws AnyException
  {
		Iter i = listeners_.createConcurrentSafeKeysIterator();
		while (i.hasNext())
		{
			EventListener eventListener = (EventListener)i.next();
			// This looks wierd but the listener may have been removed
			// and our iterator is concurrent-safe!
			if (listeners_.contains(eventListener))
			{
        boolean b = true;
        Any param = listeners_.get(eventListener);
        try
        {
          if (param != null)
          {
            Event e1 = e.cloneEvent();
            e1.setParameter(param);
            b = eventListener.processEvent(e1);
          }
          else
            b = eventListener.processEvent(e);
          
          if (!b)
          {
            listeners_.remove(eventListener);
            //System.out.println("***** Listener " + eventListener + " was removed 1 *****");
          }
        }
        catch (WriteClosedChannelException ex)
        {
          // If a service request listener gives us grief then remove it
          //ex.printStackTrace();
          //System.out.println("***** Listener " + eventListener + " was removed 2 *****");
          listeners_.remove(eventListener);
        }
      }
		}
  }

  public void addEventListener (EventListener l, Any eventParam)
  {
    removeEventListener(l);
  	listeners_.add(l, eventParam);
  }

  public void addEventListener (EventListener l)
  {
  	addEventListener(l, null);
  }

  /**
   * Remove an event listener.
   */
  public void removeEventListener (EventListener l)
  {
  	if (listeners_.contains(l))
  	{
  	  listeners_.remove(l);
		}
  }

  public void removeAllListeners ()
  {
		listeners_.empty();
  }

  public Array getGeneratedEventTypes()
  {
		return (eventTypes_ != null) ? eventTypes_ : EventConstants.ALL_TYPES;
  }

  public Event makeEvent(Any eventType)
  {
		throw new UnsupportedOperationException("EventMultiplexer.makeEvent()");
  }

  public boolean raiseAgainstChildren(Event e)
  {
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return (eventTypes_ != null) ? eventTypes_ : EventConstants.ALL_TYPES;
	}

	public int numListeners() { return listeners_.entries(); }

  public Object clone() throws CloneNotSupportedException
  {
  	EventMultiplexer em = (EventMultiplexer)super.clone();
  	em.listeners_   = new AnyOrderedMap();
    em.eventTypes_  = (Array)AbstractAny.cloneOrNull(eventTypes_);
    return em;
  }
}

