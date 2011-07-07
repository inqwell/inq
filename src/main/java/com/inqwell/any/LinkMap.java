/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LinkMap.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

import com.inqwell.any.identity.AnyMapEgDecor;
import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;

/**
 * A Map implementation whose purpose is to link a node in some
 * distant (usually higher up) part of the node space into another
 * (usually the current context).
 * <p>
 * Inq code executing at a given context, particularly GUI clients
 * rendering data, often require that events emanate from a point
 * below that context. The <code>NodeSpecification</code> matching
 * process can't handle paths like <code>$root...</code> and events
 * not from below a context node will not, of course, pass through that
 * context node.
 * <p>
 * In many cases it is desirable to have a single instance of a
 * dataset, for example a tree of permission/privilege data, that
 * is accessed by many parts of an application and thus from within
 * many different contexts. When events are raised within this data
 * these should be picked up in whatever context the data is being
 * viewed.  A <code>LinkMap</code> accepts a node specification
 * referencing the desired node and adds it as a direct child to
 * itself. The name of the node in the <code>LinkMap</code> does
 * not have to be the same as its name at its original path.
 * <p>
 * The <code>LinkMap</code> listens to the process's root node
 * to solicit events relating to the target nodes and maintains
 * its children accordingly. For this reason, <code>LinkMap</code>s
 * can only contain children that implement
 * the <code>EventGenerator</code> interface. It is pointless
 * to do otherwise and prevents <code>LinkMap</code> from
 * keeping track of its children, which leads to unwanted reference
 * proliferation and thus gc prevention.
 * <p>
 * This class <b>is</b> itself subject to single parent restrictions
 * but does not place itself as the parent of the nodes it is linking
 * to, so does not violate any parental restrictions they may have.
 * It cannot deliver events to anything other than its parent node. It
 * is an error to attach any other kind of listener to a
 * <code>LinkMap</code>.
 * <p>
 * As an <code>EventGenerator</code> this class propagates the events
 * it receives to its <code>EventListener</code> parent. <code>LinkMap</code>s
 * do not support any listeners other than their parent and so must
 * have a parent when <code>addEventListener()</code> is called.
 * <p>
 * No ordering is supported, so this map type cannot be sorted
 * or maintain insertion order.
 * <p>
 * When serialized this implementation will transfer as an empty map
 * of the same class, thus preventing the unwanted arrival of the
 * linked node(s) as real nodes in the target VM. This implementation
 * is thus not intended to be serialized but it is not an error to
 * do so.
 * <p>
 * In order to prevent the retention of a potentially ever-increasing
 * number of node sets referenced through the linked-to path,
 * <code>LinkMap</code> maintains itself as a weak reference on its
 * EventGenerator children and removes all registrations on them
 * when it is finalized.
 * <p> 
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 */ 
public class LinkMap extends    AnyPMap
                     implements Map,
                                EventGenerator,
                                EventListener,
                                Cloneable
{
  protected transient Any             nameInContainer_ = null;
  private   transient EventListener   parentListener_  = null;
  private   transient ChildListener   weakLink_;
  private   transient Map             targets_;
  private   transient EventGenerator  root_;

  public LinkMap(EventGenerator root)
  {
  	init(root);
  }
  
  private LinkMap()
  {
    // see writeReplace
  }
  
  public Map shallowCopy()
  {
    throw new UnsupportedOperationException("LinkMap.shallowCopy()");
  }
  
  public Map bestowIdentity()
	{
		return new AnyMapEgDecor (this);
	}
	
	public void addTarget(NodeSpecification target) throws AnyException
	{
    this.addTarget(target, null);
	}
	
	public void addTarget(NodeSpecification target, Any childKey)
	{
    target = normaliseTarget(target);
    
    if (targets_.contains(target))
      throw new DuplicateChildException("Already linked to target " + target);
      
    if (childKey == null)
      childKey = target.getLast();
    
    targets_.add(target, childKey);
    
    // Attach a listener to the root that will dispatch event relating
    // to our target.
		new RootListener(target,
                     this,
                     childKey,
                     root_);
                     
    // See if we can find the target node now.  If we can
    // then add it here and raise an event here.  Caters
    // for the case where the target node is already present.
    // LocateNode ln = new LocateNode(target);
	}
	

  public void fireEvent (Event e) throws AnyException
  {
    sendEvent(e);
  }

  public boolean processEvent(Event e) throws AnyException
  {
		if (e.isConsumed())
			return true;
			
    // pass the event on to our listeners
    fireEvent (e);

    return true;
  }

  /**
   * This method can only be called by our parent and we must have the
   * parent established.
   */
  public void addEventListener (EventListener l, Any eventParam)
  {
    if (eventParam == null)
      throw new UnsupportedOperationException ("Must supply an event parameter");

    if (getParentAny() == null || l != getParentAny())
      throw new IllegalArgumentException ("parent undefined or listener not parent");
    
    nameInContainer_ = eventParam;
    parentListener_  = l;
  }
  
  public void addEventListener (EventListener l)
  {
    addEventListener(l, null);
  }
  
  public void removeEventListener (EventListener l)
  {
    if (getParentAny() == null || l != getParentAny())
      throw new IllegalArgumentException ("parent undefined or listener not parent");
      
    parentListener_ = null;
  }

  public void removeAllListeners ()
  {
    throw new UnsupportedOperationException();
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
		
		if (eventType.equals(EventConstants.NODE_REPLACED) ||
				eventType.equals(EventConstants.NODE_REPLACED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
		}
		else if (eventType.equals(EventConstants.NODE_REMOVED))
		{
      // In case LinkMap is deconstructed from a higher level
      // don't support NODE_REMOVED_CHILD.  Otherwise all
      // this LinkMap's children will propagate the event
      // up any other linkages.
			ret = new NodeEvent(makeEventType(eventType));
		}
		else if (eventType.equals(EventConstants.NODE_ADDED) ||
				     eventType.equals(EventConstants.NODE_ADDED_CHILD))
		{
			ret = new NodeEvent(makeEventType(eventType));
		}

    /*
		if (ret == null)
		{
			throw new IllegalArgumentException
				("LinkMap.makeEvent() invalid type " + eventType);
		}
		*/
		return ret;
  }
	
  public boolean raiseAgainstChildren(Event e)
  {
    return true;
  }

  /**
   * Not a true clone in that resulting map is empty.
   */
  public Object clone() throws CloneNotSupportedException
  {
    LinkMap m = (LinkMap)super.clone();
    
    m.weakLink_ = new ChildListener(m);
    
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
    super.afterAdd(key, value);
		establishListener(key, value);
  }

  protected void beforeRemove (Any key)
  {
		undoListener(get(key));
		super.beforeRemove(key);
  }
  
	protected boolean beforeAdd(Any key, Any value)
  {
    super.beforeAdd(key, value);
    
    if (!(value instanceof EventGenerator))
      throw new IllegalArgumentException("Child is not an EventGenerator");
    
    return true;
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
  }
	
	protected Object writeReplace() throws ObjectStreamException
	{
    Map m = new LinkMap();
    return m;
	}
	
	protected void finalize() throws Throwable
	{
    // Remove all children and thus our weak link as
    // an event listener on them.
		empty();
	}
	
	// Get rid of $root if its there.
	private NodeSpecification normaliseTarget(NodeSpecification target)
	{
    if (target.getFirst().equals(ServerConstants.ROOT))
      target.removeFirst();
    
    return target;
	}
	
  private void establishListener(Any key, Any value)
  {
    EventGenerator e = (EventGenerator)value;
    e.addEventListener(weakLink_, key);
  }
	
  private void undoListener(Any a)
  {
    EventGenerator e = (EventGenerator)a;
    e.removeEventListener(weakLink_);
	}
	
	private Map makeEventType(Any type)
	{
		Map ret = AbstractComposite.eventIdMap();
		
		ret.add (Descriptor.descriptor__, getDescriptor());
		ret.add (EventConstants.EVENT_TYPE, type);
		
		return ret;
	}
	
  private void sendEvent(Event e) throws AnyException
  {  
		EventListener pl = null;
		Any           p  = null;
		
		synchronized (this)
		{
		  pl = parentListener_;
		  p  = nameInContainer_;
		}
		
		if (pl != null)
		{
			if (p != null)
				e.setParameter(p);
			
			//System.out.println("LinkMap.sendEvent() " + p);
			//System.out.println("LinkMap.sendEvent() " + e);
			//System.out.println("LinkMap.sendEvent() " + pl);
			pl.processEvent(e);
		}
  }

  private void init(EventGenerator root)
  {
    weakLink_ = new ChildListener(this);
    root_     = root;
  }
  
  // In order to prevent LinkMaps creating a strong path
  // from the linked-to node space into the local node space
  // the EventListener reference from the linked-to node
  // is held weakly.  This is true also for the root node
  // listener.
  // Do not make this class a member inner class or the
  // implicit reference to 'this' will obviate this step.
  // NB AbstractAny has object identity as its equality
  // semantics.  This is OK.
  static private class ChildListener extends    AbstractAny
                                     implements EventListener
  {
    private WeakReference owner_;
    
    ChildListener(LinkMap owner)
    {
      owner_ = new WeakReference(owner);
    }

    public boolean processEvent(Event e) throws AnyException
    {
      // If the reference is still established then pass
      // on the event to the LinkMap.  Otherwise ignore.
      LinkMap lm = (LinkMap)owner_.get();
      if (lm != null)
        lm.processEvent(e);
        
      // We can't remove ourselves from the EventGenerator
      // that sent the event because it is not passed to this
      // method, but we do that in the finalizer of the LinkMap.
      // In any case, the originator may not have been our
      // immediate child.

      return true;
    }
    
    public void addEventListener (EventListener l, Any eventParam) {}

    public Array getDesiredEventTypes()
    {
      return EventConstants.ALL_TYPES;
    }
  }

  // Process events arriving at the root node that relate to events
  // occurring at a linked-to node stored as a child in the LinkMap.
  static private class RootListener extends    AbstractAny
                                    implements EventListener
  {
		private Array           eventTypes_ = AbstractComposite.array();
    private WeakReference   owner_;
    private EventDispatcher rootEd_ = new EventDispatcher();
    private EventGenerator  root_;
    private Any             childKey_;
    private boolean         doingEvent_ = false;

		RootListener(NodeSpecification target,
                 LinkMap           owner,
                 Any               childKey,
                 EventGenerator    root)
		{
      makeReplaceEventType(target);
      makeAddEventType(target);
      makeRemoveEventType(target);
      owner_    = new WeakReference(owner);
      rootEd_.addEventListener(this);
      root.addEventListener(rootEd_);
      root_     = root;
      childKey_ = childKey;
		}

    // In here we get the events that relate to the linked-to target
    // and enable us to maintain the node as a child in the LinkMap.
		public boolean processEvent(Event e) throws AnyException
		{
      try
      {
        // Note that if we raise an event on the child for the
        // purpose of dispatching listeners on the LinkMap
        // path then that event may arrive at the root via the
        // target path and we end up in here again.  Prevent
        // infinite loop!
        if (doingEvent_)
          return true;
        
        doingEvent_ = true;
        
        Event linkPathEvent = null;
        
        // If the reference is still established then pass
        // on the event to the LinkMap.  Otherwise ignore.
        LinkMap lm = (LinkMap)owner_.get();
        if (lm != null)
        {
          Any            id        = e.getId();
          Any            basicType = AbstractEvent.getBasicType(id);
          EventGenerator child     = (EventGenerator)e.getContext();

          if (basicType.equals(EventConstants.NODE_REPLACED) ||
              basicType.equals(EventConstants.NODE_REPLACED_CHILD))
          {
            // Replace the new child in the LinkMap.  Currently the
            // LinkMap has the old child in it.  An event needs to be
            // raised up the path from the link map.
            // It is only necessary to do this on the new child node
            // once, i.e. we don't need to descend like AddTo/RemoveFrom
            // do.  Since ther are already doing this and the child is
            // now in the link map as well any subsequent child events will
            // pass through both paths.
            lm.replaceItem(childKey_, child);
            linkPathEvent = child.makeEvent(basicType);
            child.fireEvent(linkPathEvent);
          }
          else if (basicType.equals(EventConstants.NODE_ADDED) ||
                   basicType.equals(EventConstants.NODE_ADDED_CHILD))
          {
            lm.add(childKey_, child);
            linkPathEvent = child.makeEvent(basicType);
            child.fireEvent(linkPathEvent);
          }
          else if (basicType.equals(EventConstants.NODE_REMOVED) ||
                   basicType.equals(EventConstants.NODE_REMOVED_CHILD))
          {
            // In the remove case, the processing RemoveFrom first
            // removes the node and then places itself as a listener
            // on the removed node.  This will generate an event up
            // the primary path but not via the LinkMap.  To make sure
            // we get an event here we must raise it on the LinkMap
            // itself and pre-can the path spec of the child.  The only
            // loophole in this implementation is we only dispatch exact
            // matching path into this class and generate a single event
            // on the LinkMap, i.e. no further descent of the structure
            // will be performed. This means that LinkMap usage is
            // slightly (though not seriously) limited.
            lm.remove(childKey_);
            linkPathEvent = lm.makeEvent(basicType);
            linkPathEvent.setParameter(childKey_);
            lm.fireEvent(linkPathEvent);
          }
        }
        else
        {
          // If our associated LinkMap is garbage-collected remove
          // ourselves from the root.
          root_.removeEventListener(rootEd_);
        }
      }
      finally
      {
        doingEvent_ = false;
      }
      
      return true;
		}
		
		public Array getDesiredEventTypes()
		{
			//System.out.println ("DataListener.getDesiredEventTypes " + eventTypes_);
			return eventTypes_;
		}

		private void makeReplaceEventType(Any nodeSpec)
		{
			Map eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.NODE_REPLACED);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
      eventType.add(EventConstants.EVENT_VECTOR, AnyAlwaysEquals.instance());
			eventTypes_.add(eventType);

			eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.NODE_REPLACED_CHILD);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
			eventTypes_.add(eventType);
		}

		private void makeRemoveEventType(Any nodeSpec)
		{
			Map eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.NODE_REMOVED);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
      //eventType.add(EventConstants.EVENT_PARENT, AnyAlwaysEquals.instance());
      eventType.add(EventConstants.EVENT_VECTOR, AnyAlwaysEquals.instance());
			eventTypes_.add(eventType);

			eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.NODE_REMOVED_CHILD);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
			eventTypes_.add(eventType);
		}

		private void makeAddEventType(Any nodeSpec)
		{
			Map eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.NODE_ADDED);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
      eventType.add(EventConstants.EVENT_VECTOR, AnyAlwaysEquals.instance());
			eventTypes_.add(eventType);

			eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.NODE_ADDED_CHILD);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
			eventTypes_.add(eventType);
		}
  }
}
