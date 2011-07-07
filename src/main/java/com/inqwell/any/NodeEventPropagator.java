/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NodeEventPropagator.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.lang.ref.WeakReference;

/**
 * An event generator which is used for propagating events to potentially
 * multiple listeners each with their own desired event parameter.
 * An example use is multiple references to a BOT instance, to each of which
 * events would be propagated to notify of things like modification.
 * <p>
 * The semantics of the event generation are
 * <ul>
 * <li> the event is cloned (and so must support cloning) so that a new
 * event is sent to each target.
 * <li> if a listener supplies an event parameter it is parameterised
 * with it.
 * <li> all event listeners have <code>processEvent</code> called in the
 * same thread.
 * </ul>
 * <p>
 * This class is thread safe - the list of listeners can be safely maintained
 * from multiple threads.
 * <p>
 * References to event listeners are held within
 * java.lang.ref.WeakReference objects so that listeners can be reclaimed
 * even if they are still held within the event generator.  This also permits
 * listeners to be badly behaved in that they are not required to call
 * <code>removeEventListener</code> when they are reclaimed.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 * @see com.inqwell.any.Any
 */ 
public class NodeEventPropagator extends    AbstractAny
																 implements EventGenerator,
																            Cloneable
{
  private Set listeners_;  // has synchronization issues...
  
  public NodeEventPropagator()
  {
    init();
  }
  
  /**
   * Send the given event to all registered listeners.
   */
  public void fireEvent (Event e) throws AnyException
  {
    // Liveness issues:  We shallow copy the array of event listeners so that
    // we don't have to retain a synchronization lock on it.  Since we are
    // propagating the event to an unknown number of listeners, upwards
    // through a composite structure of unknown depth, this seems to be the
    // best policy.
    // The down-side is that we cannot clean the listeners list of reclaimed
    // listeners as we go through.  This has to be done at the end (if we
    // want to do it here at all).
    Set listeners;
    synchronized (listeners_)
    {
      if (listeners_.entries() == 0)
        return;
      
      listeners = listeners_.shallowCopy();
    }
    
    boolean toRemove = false;
    Iter targets = listeners.createIterator();
    while (targets.hasNext())
    {
      NodeLink n = (NodeLink)targets.next();

      EventListener l;
      if ((l = n.getListener()) == null)
      {
				toRemove = true;
        continue;
      }

      Event thisEvent = e;
			thisEvent = thisEvent.cloneEvent();

      Any param = n.getNameInContainer();
      //System.out.println ("NodeEventPropagator.fireEvent() param " + param);
      if (param != null)
      {
				thisEvent.setParameter(param);
			}
      l.processEvent (thisEvent);
    }
    // Check if any listeners have been garbage collected and if so remove them
    // from our listener list.
    if (toRemove)
    {
			synchronized (listeners_)
			{
				Iter  i = listeners_.createIterator();
				while (i.hasNext())
				{
					NodeLink n = (NodeLink)i.next();

					if (n.getListener() == null)
						i.remove();
				}
			}
		}
  }
  
  /**
   * Add an event listener.  When fireEvent is called the event will be
   * cloned, the parameter associated with the listener set into the
   * new event and that event passed to com.inqwell.any.EventListener.processEvent(),
   * for all registered listeners.
   */
  public void addEventListener (EventListener l, Any eventParam)
  {
    NodeLink nodeLink = new NodeLink(l, eventParam);
    synchronized(listeners_)
    {
			int idx;
      if (!listeners_.contains (nodeLink))
        listeners_.add (nodeLink);
			else
			{
				listeners_.remove(nodeLink);
				listeners_.add(nodeLink);
			}
    }
    // silently ignore if given listener is already present
  }

  /**
   * Not supported.  An event parameter must be supplied.
   * @exception UnsupportedOperationException
   */
  public void addEventListener (EventListener l)
  {
		addEventListener(l, null);
  }
  
  /**
   * Remove an event listener.  Note that because EventListeners are
   * held as weak references there is no actual need to call this
   * method.  If an EventListener has been reclaimed the listener list
   * is updated on next event fire.
   */
  public void removeEventListener (EventListener l)
  {
    NodeLink nodeLink = new NodeLink(l);
    int i = -1;
    synchronized(listeners_)
    {
      if (listeners_.contains (nodeLink))
        listeners_.remove (nodeLink);
    }
  }
  
  public void removeAllListeners ()
  {
    synchronized(listeners_)
    {
			listeners_.empty();
    }
  }
  
  public Array getGeneratedEventTypes()
  {
		return EventConstants.ALL_TYPES;
  }

  public Event makeEvent(Any eventType)
  {
		throw new UnsupportedOperationException("NodeEventPropagator.makeEvent()");
  }

  public boolean raiseAgainstChildren(Event e)
  {
    return true;
  }

  public Object clone() throws CloneNotSupportedException
  {
  	NodeEventPropagator n = (NodeEventPropagator)super.clone();
  	n.listeners_          = AbstractComposite.set();
    return n;
  }

  private void init()
  {
    listeners_ = AbstractComposite.set();
  }
  
  /*
   * A simple class which holds an EventListener and a parameter which is
   * the identity of the event generator, as held within the event listener,
   * the event listener being a container of some sort.
   */
  static class NodeLink extends AbstractAny
  {
    Any              nameInContainer_;
    WeakReference    containerTarget_;
    
    NodeLink (EventListener l, Any name)
    {
      containerTarget_ = new WeakReference(l);
      nameInContainer_ = name;
    }
    
    // Only used for temporaries...
    NodeLink (EventListener l)
    {
      this (l, null);
    }
    
    // must be a NodeLink and have exact same container.
    public boolean equals(Object o)
    {
      NodeLink n = (NodeLink)o;
      
      return n.containerTarget_.get() == this.containerTarget_.get();
    }
    
    public int hashCode()
    {
      return System.identityHashCode(containerTarget_.get());
    }
    
    EventListener getListener()
    {
      return (EventListener)containerTarget_.get();
    }
    
    Any getNameInContainer()
    {
      return nameInContainer_;
    }
  }
}
