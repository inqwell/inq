/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EventDispatcher.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * An event listener and generator which propagates events to a
 * specific listener based on event id.  The same listener may
 * register as the recipient of more than one event id but only
 * one listener at a time can be registered against a given id.
 * If more than one recipient of an event is required then the
 * immediate listener registered here can be
 * an <code>EventMultiplexer</code>.
 * <p>
 * Propagated events can be parameterised if the listener registered
 * a parameter at the same time, although this is not required.
 * Events arriving with ids for which there is no registered listener
 * are ignored.  Obviously, only listener registration including an
 * event type is supported.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 */
public class EventDispatcher extends    AbstractAny
														 implements EventGenerator,
                                        EventListener
{
	// Map event ids to listeners and to event parameters(optional)
	private Map     listeners_;
	private Map     params_;
	private Array   eventTypes_;
	private boolean concurrentSafe_ = false;
	
	public EventDispatcher()
	{
		listeners_  = AbstractComposite.simpleMap();
		params_     = AbstractComposite.simpleMap();
		eventTypes_ = AbstractComposite.array();
	}
	
  public boolean processEvent(Event e) throws AnyException
  {
    boolean ret = true;
    if (concurrentSafe_)
    {
      synchronized(listeners_)
      {
        doFire(e);
        ret = listeners_.entries() != 0;
      }
    }
    else
    {
      doFire(e);
      ret = listeners_.entries() != 0;
    }
    return ret;
	}

  public void fireEvent (Event e) throws AnyException
  {
    if (concurrentSafe_)
    {
      synchronized(listeners_)
      {
        doFire(e);
      }
    }
    else
    {
      doFire(e);
    }
  }
  
  public void addEventListener(EventListener l, Any eventParam)
  {
    if (concurrentSafe_)
    {
      synchronized(listeners_)
      {
        Iter i = l.getDesiredEventTypes().createIterator();
        while (i.hasNext())
        {
          Any eventType = i.next();
          registerEventType(eventType, l);
          if (eventParam != null)
            registerEventParam(eventType, eventParam);
        }
      }
    }
    else
    {
      Iter i = l.getDesiredEventTypes().createIterator();
      while (i.hasNext())
      {
        Any eventType = i.next();
        registerEventType(eventType, l);
        if (eventParam != null)
          registerEventParam(eventType, eventParam);
      }
    }
  }

  public void addEventListener(EventListener l)
  {
    this.addEventListener(l, null);
  }
  
  public void removeEventListener (EventListener l)
  {
    if (concurrentSafe_)
    {
      synchronized(listeners_)
      {
        doRemove(l);
      }
    }
    else
    {
      doRemove(l);
    }
  }
  
  public void removeAllListeners ()
  {
    if (concurrentSafe_)
    {
      synchronized(listeners_)
      {
        listeners_.empty();
        params_.empty();
        eventTypes_.empty();
      }
    }
    else
    {
      listeners_.empty();
      params_.empty();
      eventTypes_.empty();
    }
  }
  
  public Array getGeneratedEventTypes()
  {
		// the types of event we can generate depends on the types of
		// the generators we are listening to
		return eventTypes_;
  }

  public Array getDesiredEventTypes()
  {
		return EventConstants.ALL_TYPES;
	}
	
  public Event makeEvent(Any eventType)
  {
    // We could encounter one of these when descending and
    // raising ..._CHILD events so just return null here.
    // Callers check for this anyway as not all event types
    // are raised by all generators.
    return null;
  }

  public boolean raiseAgainstChildren(Event e)
  {
    return true;
  }

	public EventListener getEventListener(Any eventType)
	{
    if (concurrentSafe_)
    {
      synchronized(listeners_)
      {
        return (EventListener)listeners_.get(eventType);
      }
    }
    else
    {
      return (EventListener)listeners_.get(eventType);
    }
	}
	
	public boolean isDispatching(Any eventType)
	{
    if (concurrentSafe_)
    {
      synchronized(listeners_)
      {
        return listeners_.contains(eventType);
      }
    }
    else
    {
      return listeners_.contains(eventType);
    }
	}
	
	public void setConcurrentSafe(boolean concurrentSafe)
	{
    concurrentSafe_ = concurrentSafe;
	}
	
  private void registerEventType(Any eventType, EventListener l)
  {
  	listeners_.add(eventType, l);
  	// we can't register for the same type more than once.  Attempts
  	// to do so result in a run-time exception courtesy of Map.add()
  	eventTypes_.add(eventType);
  }
  
  private void registerEventParam(Any eventType, Any eventParam)
  {
  	params_.add(eventType, eventParam);
  }
  
  private void doRemove(EventListener l)
  {
		Iter i = l.getDesiredEventTypes().createIterator();
		while (i.hasNext())
		{
			Any eventType = i.next();
			listeners_.remove(eventType);
			
			if (params_.contains(eventType))
				params_.remove(eventType);
				
      int index = eventTypes_.indexOf(eventType);
      // should not be -1 anyway
      if (index >= 0)
        eventTypes_.remove(index);
		}
  }
  
  private void doFire(Event e) throws AnyException
  {
    EventListener eventListener = null;
		if ((eventListener = (EventListener)listeners_.getIfContains(e.getId())) != null)
		{
			if (params_.contains(e.getId()))
			  e.setParameter(params_.get(e.getId()));
			  
			if (!eventListener.processEvent(e))
      {
        // Make the assumption that the listener is not viable for any of the types
        // it is processing. Then we don't have to wait for them all to have occurred
        // before jettisoning
        this.removeEventListener(eventListener);
      }
		}
  }
}

