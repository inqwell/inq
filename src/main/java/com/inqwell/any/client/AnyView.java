/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyView.java $
 * $Author: sanderst $
 * $Revision: 1.17 $
 * $Date: 2011-05-13 10:15:39 $
 */

package com.inqwell.any.client;

import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyAlwaysEquals;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Composite;
import com.inqwell.any.ContainedException;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.EventGenerator;
import com.inqwell.any.EventListener;
import com.inqwell.any.EventMultiplexer;
import com.inqwell.any.ExceptionHandler;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.InstanceHierarchyMap;
import com.inqwell.any.Iter;
import com.inqwell.any.JoinTransaction;
import com.inqwell.any.Locate;
import com.inqwell.any.Map;
import com.inqwell.any.PropertyBinding;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.beans.DialogF;
import com.inqwell.any.beans.EventSet;
import com.inqwell.any.beans.Facade;
import com.inqwell.any.beans.Listener;
import com.inqwell.any.beans.ListenerAdaptee;
import com.inqwell.any.beans.ListenerAdapter;
import com.inqwell.any.beans.PropertySet;
import com.inqwell.any.beans.UIFacade;
import com.inqwell.any.beans.WindowF;
import com.inqwell.any.client.swing.SwingInvoker;

/**
 * A view of data in the node space.  This class provides base
 * functionality for picking up events from the process node
 * space and dispatching them for processing
 * defined in sub-classes.
 * Typically, sub-classes will wrap a delegate object, for
 * example a GUI component, and act as an interface between
 * the Inq event system and that object.
 * <p/>
 * This class also provides this functionality on a general
 * basis to bind node events to a Java Beans property.
 * A relevant node event will cause the property to be set with the
 * new data value. Unlike specific view functionality, Java Beans
 * is generic and no sub-class implementation is required.
 * <p/>
 * In a similar way, any Java Beans events that the delegate
 * object supports can be listened for and dispatched to
 * Inq {@link EventListener} instances, for example button
 * action events.
 */
public abstract class AnyView extends    InstanceHierarchyMap
                              implements EventGenerator,
                                         EventListener,
                                         UIFacade,
                                         ListenerAdaptee
{
  static public    Any component__      = AbstractValue.flyweightString("component");
  static private   Any renderedValue__  = AbstractValue.flyweightString("renderedValue");
  static public    Any  x__             = AbstractValue.flyweightString("x");
  static public    Any  y__             = AbstractValue.flyweightString("y");
  static public    Any  width__         = AbstractValue.flyweightString("width");
  static public    Any  height__        = AbstractValue.flyweightString("height");
  
  static protected BooleanI b__         = new AnyBoolean();
  
  static protected AnyInt     iX__      = new AnyInt();
  static protected AnyInt     iY__      = new AnyInt();
  static int awtEventCount__ = 0;
  
  static protected Map savedProperties__ = AbstractComposite.simpleMap();

  // The root from which Any locates and expressions etc will be
  // evaluated.  In fact a string, given to service requests made
  // as a result of events processed at this component.
  private Any context_;
  private Any contextNode_; //... as above but the actual node which we
                            // use as the root node of expressions like
                            // evaluating the whereabouts of our data node.

  // Only set if we are rendering some data
  private EventDispatcher  updateDispatcher_;

	// Contains a ListenerAdapter implementation for each type
	// of event that the underlying component can generate.
  // Initialised when first event listener is attached.
  private Map adapters_;

  // Key property names (like "foreground") to a
  // RenderInfo object which is used to provide the property
  // value.  Reflection and Java Beans facilities are then used
  // to write the value yielded by the RenderInfo.
  // Initialised when first property binding is performed.
  private Map         eventProperties_;

  // As above but treating properties like children
  // of <this>.properties.<property_name>
  private Map         propertyMap_;

  // Use an EventDispatcher to manage the subscriptions
  // to the various event types this component can generate
  private EventDispatcher adaptedEventDispatcher_;

  // Only set if we have some bound properties
  private EventMultiplexer propertyMultiplexer_;
  // The above EventDispatcher and EventMultiplexer warrant some
  // explanation:  An EventDispatcher maps a set of unique event
  // types to listeners and dispatches any event it receives to
  // the listener for that type, if any.  Further, because the
  // set of event types is unique, the EventDispatcher will only
  // ever fire to one and only one listener.
  // An EventMultiplexer multicasts any event it receives to all
  // its listeners.  As event listeners are added to a EventDispatcher
  // the type(s) of event that that listener requires (as returned
  // by EventListener.getDesiredEventTypes()) are added to the set
  // that the dispatcher will deliver.  If the dispatcher is
  // already delivering the event to another listener, an exception
  // is thrown.
  // The nature of <inq> node update events is that their type is a
  // composite value of the path the event has taken, the descriptor
  // of the originating event's object, that the event represents a
  // data update, and, most crucially, the fields in the object
  // that have changed.  These fields are represented as a FieldSet
  // whose equality semantics are TRUE if the set contents overlap.
  // Thus, while a dispatcher may contain event types that have only
  // one field component, such types will match with any that contain
  // that field plus others.  They will therefore be dispatched only to
  // the first matching type the dispatcher holds.  This is OK for
  // rendering data updates because we want to do it only once for
  // any event that might affect what is rendering.  Its not so good
  // for bound properties when more than one property should be
  // triggered for what is a single event delivery.
  // Gawd, what does all that mean?  Well it means that an
  // EventDispatcher is used for rendering update events while
  // an EventMultiplexer is used for each property, delivering to
  // an EventDispatcher to filter the event.  We need a picture now!
  //

  // The value, args etc we will use to set a property of a Java Bean.
  // We make it a member so the swing invoker inner class object can get
  // at it cleanly.  They should only get used synchronously (I think)!
	static private Object[]      propertyArgs_ = new Object[1];
	static private Object        propertyObj_;
	static private Object        propertyValueObj_;
	static private Method        propertyWrite_;
	static private WriteProperty writeProperty_;
	
	static private Set           fired__ = AbstractComposite.set();

  // If we have been solicited for the ContextEstablished event
  protected NotifyContext notifyContext_;

  //private boolean       modelFiring_;

  // Helper objects providing access to event information yielded
  // by the beans api.
  private static ClassMap eventSetMap__;

  protected static Array actionEventType__    = AbstractComposite.array();

  protected static Any   enabled__        = AbstractValue.flyweightString("enabled");
  protected static Any   checkedValue__   = AbstractValue.flyweightString("checkedValue");
  protected static Any   uncheckedValue__ = AbstractValue.flyweightString("uncheckedValue");


  // Used if the model is to be fired after a component event.
  private   static JoinTransaction  jt_ = new JoinTransaction();

  static
  {
		eventSetMap__      = new ClassMap();

    // Set up the eventSet for the ContextListener, which is
    // supported for all wrappers
		EventSet eventSet = new EventSet(NotifyContext.class);
		eventSetMap__.add(NotifyContext.class, eventSet);

		actionEventType__.add(EventConstants.E_ACTION);
  }

  public Any getContext()
	{
		return context_;
	}

	public Any getContextNode()
	{
		return contextNode_;
	}
  
  public boolean isContextEstablished()
  {
    // The context is only really established when the path is
    // known. This happens when the Inq component subtree is placed
    // in the Process node space, that is the process root can
    // be reached. 
    return context_ != null;
  }

  /**
   * Set the contextNode property. If <code>b</code> converts
   * to <code>true</code> then <code>this</code> is established
   * as the context node for events originating from it and
   * any Inq child components that do not theselves have this
   * property set to <code>true</code>.
   * @param b
   */
	public void setContextNode(Any b)
	{
		// Uses an Any type to represent a boolean so that
		// property reflection works.
		AnyBoolean a = new AnyBoolean();
		a.copyFrom(b);

		// Remember any old context node
		Any contextNode = contextNode_;

		if (a.getValue())
		{
		  contextNode_ = this;

		  // Evaluate the context path if possible
		  context_     = getPath(null);
    }
		else
		{
		  contextNode_ = null;
		  context_     = null;
    }

		if (contextNode != contextNode_)
		{
      if (contextNode != null)
      {
        EventGenerator contextEg = (EventGenerator)contextNode;
        if (updateDispatcher_ != null)
          contextEg.removeEventListener(updateDispatcher_);
        if (propertyMultiplexer_ != null)
          contextEg.removeEventListener(propertyMultiplexer_);

        updateDispatcher_    = null;
        propertyMultiplexer_ = null;
      }
      evaluateChildContext();
		}
	}

  public boolean contextNode()
  {
    // is this a context node?
    return contextNode_ == this;
  }

  public void addEventListener (EventListener l, Any eventParam)
  {
    super.addEventListener(l, eventParam);
    if (l == getParentAny())
      evaluateContext();
    
    // Since there's just been a change in the hierarachy, if we are
    // a context node and the path to our process root is navigable
    // then re-evaluate the context path.
    if (contextNode_ == this && l == getParentAny())
    {
      context_ = null;
      
      context_ = getPath(null);
      
      if (context_ != null && notifyContext_ != null)
      {
        // Context node itself is in the node space
        notifyContext_.fireContextEstablished(new ContextEvent(notifyContext_));
      }

      
    }
    // Re-evaluate the context[path] in our component children
    if (context_ != null && l == getParentAny())
      evaluateChildContext();
  }

  public boolean raiseAgainstChildren(Event e)
  {
    return !contextNode();
  }

	public void evaluateContext()
	{
		// If we are the context then leave it as it is.
		// If we don't have a context, inherit our parent's which we assume
		// is already resolved.  If our parent is null (we are a top-level
		// component of some sort) then our context should already have
		// been set as ourselves from script.  We don't need to recurse
		// up the tree as each node has its context evaluated as it is
		// added to its parent.

		if (contextNode_ != this)
		{
		  //Any oldContextNode = contextNode_;
      context_     = null;
      contextNode_ = null;
			Facade parent = getParentComponent();
			if (parent != null)
			{
				context_     = parent.getContext();
				contextNode_ = parent.getContextNode();
        if (contextNode_   != null &&
            //oldContextNode != contextNode_ &&
            isContextEstablished())
        {
          contextEstablished();
          setupRenderedValue();
          if (context_ != null && notifyContext_ != null && getOwnerProcess() != null)
          {
            notifyContext_.fireContextEstablished(new ContextEvent(notifyContext_));
          }
        }
			}
		}
	}

  public synchronized void removeEventListener (EventListener l)
  {
		// This method gets called when we are being removed from our parent.
		// Under these circumstances we must remove the event listeners we
		// maintain to handle data viewing events.

		if (l == getParentAny())
		{
      context_ = null;

			//if (getContextNode() != this)
			//{
				EventGenerator contextEg = (EventGenerator)getContextNode();

        if (updateDispatcher_ != null)
          contextEg.removeEventListener(updateDispatcher_);

        if (propertyMultiplexer_ != null)
          contextEg.removeEventListener(propertyMultiplexer_);

        if (contextNode_ != this)
          contextNode_ = null;
        context_     = null;
        //evaluateChildContext();
			//}
		}
		super.removeEventListener(l);
  }

	/**
	 * Bind a data node given by the <code>RenderInfo</code> object
	 * to the given named property.
	 * <p>
	 *
	 */
	public void bindProperty(String     propertyName,
													 RenderInfo r,
													 boolean    immediate) throws AnyException
	{
    if (getContextNode() == null)
      throw new AnyException("Context must be established before binding properties");

		Any pName = AbstractValue.flyweightString(propertyName);

    if (eventProperties_ == null)
      eventProperties_ = AbstractComposite.simpleMap();

		if (eventProperties_.contains(pName))
			return;

    r.resolveNodeSpecs(getContextNode());

		// Now remember the RenderInfo object that represents this
		// property value
		eventProperties_.add(pName, r);

		// If there are any references, attach an inq event listener on
		// to the data node(s) representing this property value.
		Map nodeSpecs = r.getNodeSpecs();
		if (nodeSpecs.entries() != 0)
		{
      DataListener d = new PropertyListener(pName, nodeSpecs);
      listenForProperties(d, pName);
    }

		// If binding is immediate  then set this property now.  Means
    // that, from scripts executed earlier, that the source value must
    // have a meaningful value in it.
		if (immediate)
			setProperty(pName, null);
	}

	/**
	 * Fetch the current value of the given property according to
	 * its rendering expression. If the given property has been
	 * bound to a rendering expression then evaluate that it
	 * and return its value, or null if the property is not bound.
	 * <p>
	 * Note that this is not necessarily the same thing as retrieving
	 * the property value from the underlying component itself.
	 * @return The bound property value or null if the named property
	 * has not been bound when the GUI was built.
	 */
	public Any getProperty(Any property) throws AnyException
	{
		if (eventProperties_ == null ||
        !eventProperties_.contains(property))
		  return null;

		RenderInfo propertyRender = (RenderInfo)eventProperties_.get(property);

	  return propertyRender.resolveDataNode(getContextNode(), true);
	}

  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return super.contains(key);
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
        //this.replaceItem(properties__, propertyMap_);
      }

      return propertyMap_;
    }
    else
    {
      return super.get(key);
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
        //this.replaceItem(properties__, propertyMap_);
      }
    
      return propertyMap_;
    }
    else
    {
      return super.getIfContains(key);
    }
  }
  
	public void setProperty(Any property, Event e) throws AnyException
	{
		Object o = getPropertyOwner(property);
		PropertySet propertySet = getPropertySet(o);

		RenderInfo propertyRender = (RenderInfo)eventProperties_.get(property);

		Any eventType = EventConstants.NODE_REPLACED;
		if (e != null)
		{
			Map id = (Map)e.getId();
		  eventType = id.get(EventConstants.EVENT_TYPE);
		}

		Any sourceValue =
			propertyRender.resolveDataNode
				(getContextNode(),
				 !eventType.equals(EventConstants.BOT_UPDATE));

		Any propertyValueAny = propertySet.getTargetAnyValue(property);

    if (propertyValueAny == PropertySet.anyClass__)
    {
      // If the property type is Any then take value as is
      propertyArgs_[0] = sourceValue;
    }
    else
    {
      propertyValueObj_ =    propertySet.getTargetObjValue(property, propertyValueAny, sourceValue);
      propertyArgs_[0] = propertyValueObj_;
    }

		propertyWrite_ = propertySet.getWriteMethod(property);
		propertyObj_ = o;

		if (writeProperty_ == null)
			writeProperty_ = new WriteProperty();

		writeProperty_.maybeSync();
	}

	public Facade getParentComponent()
	{
		Composite parent = this;
		while((parent = parent.getParentAny()) != null)
    {
      if (parent instanceof Facade)
        return (Facade)parent;
    }
    return null;
	}
  
	public void addAdaptedEventListener(EventListener l, Any eventParam)
	{
		Array eventTypes = l.getDesiredEventTypes();

		if (eventTypes == EventConstants.DEFAULT_TYPE)
		{
			doListenerType(getDefaultEventType(), l, eventParam);
		}
		else
		{
			Iter i = eventTypes.createIterator();
			while (i.hasNext())
			{
				doListenerType(i.next(), l, eventParam);
			}
		}
	}

  public void addAdaptedEventListener (EventListener l)
  {
  	addAdaptedEventListener(l, null);
  }

  public EventBinding makeEventBinding(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
  {
  	return new EventBinding(expr, eventTypes, consume, busy, modelFires);
  }

	//public void setDialogNotify(DialogRedirector dr)
//	{
//		dr.setParentComponent(this);
//		addAdaptedEventListener(dr);
//	}
  public EventBinding makeDialogRedirect(Any dialogEventType, Array eventTypes, boolean consume, boolean busy)
  {
    return new DialogRedirector(dialogEventType, eventTypes, consume, busy);
  }

  public EventBinding makeEditorRedirect(Any editorEventType, Array eventTypes, boolean consume, boolean busy)
  {
    return new EditorRedirector(editorEventType, eventTypes, consume, busy);
  }

  /**
	 * Receive the event from the external component. Pass on to any
	 * listeners that were added with <code>addAdaptedEventListener</code>
	 * for the type contained in the event.
	 */
  public void adaptEvent(Event e)
  {
		try
		{
      adaptedEventDispatcher_.fireEvent(e);
		}
		catch (AnyException ex)
		{
      // This shouldn't happen!!
      // We entered the Inq world from some external one.  We must
      // handle exceptions from the Inq world at lower levels in
      // the call stack as we cannot pass them back to the
      // calling world.
			//ex.printStackTrace();
		}
	}

	public void removeAdaptedEventListener(EventListener l)
  {
		Array eventTypes = l.getDesiredEventTypes();

		if (eventTypes == EventConstants.DEFAULT_TYPE)
		{
			undoListenerType(getDefaultEventType(), l);
		}
		else
		{
			Iter i = eventTypes.createIterator();
			while (i.hasNext())
			{
				undoListenerType(i.next(), l);
			}
		}
  }
  
  /**
   * Whether this view has a default external event type and therefore
   * whether adding an adapted listener for the default type will succeed.
   * @return true if this view is not a null object and the object has
   * a default event type.
   */
  public boolean hasDefaultEventType()
  {
    Object o = getObject();
    if (o == null)
      return false;

    Listener l = ListenerFactory.getHandle().getListener(o);

    // get the default listener name
    return l.hasDefaultEventType();
  }



	// Listen to our context node for the data events that
	// should update this component.
	// If called more than once previous dispatcher is discarded
	// so model can be reset during execution rather than only at
	// gui setup.
	protected void setupDataListener(Map nodeSpecs)
	{
	  if (nodeSpecs.entries() != 0)
	  {
    	// All data events will pass through our context node so we
    	// listen to it for those that are of interest to us for
    	// data rendering.
  		DataListener d = new RenderingListener(nodeSpecs);
  
  		EventGenerator contextEg = (EventGenerator)getContextNode();
  
      if (contextEg == null)
        throw new AnyRuntimeException("Context node not yet established");
  
  		if (updateDispatcher_ != null)
  		{
  			contextEg.removeEventListener(updateDispatcher_);
  		}
  
  		updateDispatcher_ = new EventDispatcher();
  
  		listenForUpdates(contextEg, updateDispatcher_, d);
	  }
	}

  /**
   * Tell this class about the object(s) that support Java Beans
   * events that will be adapted into the inq environment
   * <NOTE>First call must be with the object that supports the
   * default event type, if any. [TS not any more!]</NOTE>
   */
	protected void setupEventSet(Object o)
	{
    // Uses reflection and the beans api to discover the native
    // events this component can generate
    EventSet eventSet = (EventSet)eventSetMap__.get(o);
    if (eventSet == null)
    {
			eventSet = new EventSet(o);
			eventSetMap__.add(o.getClass(), eventSet);
		}
	}

  private Any getDefaultEventType()
  {
    Object o = getObject();

    Listener l = ListenerFactory.getHandle().getListener(o);

    // get the default listener name
    return l.getDefaultEventType();
  }

  /*
  protected boolean connectedToRoot()
  {
    Composite a    = this;
    Composite last = null;
    while (a != null)
    {
      last = a;
      a = a.getParentAny();
    }
    return last == Globals.process__.getRoot();
  }
  */

  public Array getDesiredEventTypes()
  {
		return EventConstants.ALL_TYPES;
	}

  public void setTransactional(boolean isTransactional)
  {
  	throw new UnsupportedOperationException
  	  ("Variables in views aren't allowed, use intermediate map");
  }

  public void validate() {}

	/**
	 * Override base implementation.  Since they represent views on
	 * data it is generally not appropriate to assign data to a
	 * component.  Instead the component is updated by receiving
	 * events on the node which represents its data, established
	 * by calling <code>setDataNode()</code>.  Accordingly, this
	 * method throws an <code>UnsupportedOperationException</code>
	 */
  public Any copyFrom (Any a)
  {
		throw new UnsupportedOperationException("AnyView.copyFrom()");
  }

	public Object clone() throws CloneNotSupportedException
	{
		AbstractAny.cloneNotSupported(this);
		return null;
	}

  /**
   * In case we are asked to instantiate ourselves in this way, just
   * return our underlying <code>Map</code> implementation.  The
   * assumption is that this is for the purposes of tree-building
   * and, anyway, AnyComponents are only created for real from script
   */
  public Any buildNew(Any a)
  {
    Any ret = AbstractComposite.managedMap();
    if (a != null)
      ret.copyFrom(a);
    return ret;
  }

  /**
   * Called when a data event occurs that is dispatched to this
   * object.  Derived classes should implement according to
   * their own requirements, for example updating a GUI
   * component.
   */
	protected abstract void componentProcessEvent(Event e) throws AnyException;

  /**
   * Return the object to which a native (that is Java Beans)
   * event listener can be attached to.
   */
	protected abstract Object getAttachee(Any eventType);

  protected AnyPopupMenu getParentPopupMenu()
  {
	  Facade f = getParentComponent();

	  if (f instanceof AnyPopupMenu)
	    return (AnyPopupMenu)f;

	  return null;
  }

  protected Composite getPreferredListenerTypes()
  {
    return null;
  }

  /**
   * Once we know what out context node is, allow derived
   * classes the opportunity to process any model information
   * that they may already have but couldn't use until now.
   */
  protected void contextEstablished()
  {
  }

  public void setParent(Composite parent)
  {
    super.setParent(parent);
    if (parent == null)
    {
      // Have been removed from the hierarchy. We cannot have a context node or
      // path
      context_     = null;
      if (contextNode_ != this)
        contextNode_ = null;
      evaluateChildContext();
    }
  }
  
  /**
   * Return the object to which the given Java Beans property
   * applies.
   */
	protected abstract Object getPropertyOwner(Any property);

	public abstract RenderInfo getRenderInfo();

  protected Any getGUIRendered(Event e) throws AnyException
  {
    RenderInfo r = getRenderInfo();
    Any ret = null;
    
    if (r != null)
    {
      Map id = (Map)e.getId();
    
      Any eventType = id.get(EventConstants.EVENT_TYPE);
      
    
      if (eventType.equals(EventConstants.BOT_UPDATE))
      {
        ret = r.resolveDataNode(getContextNode(), false);
      }
      else
      {
        boolean notDeleting = !(eventType.equals(EventConstants.NODE_REMOVED) ||
                                eventType.equals(EventConstants.NODE_REMOVED_CHILD));
        
        ret = r.resolveDataNode(getContextNode(), true, notDeleting);
      }
    }
    
    return ret;
  }

  protected void listenForUpdates(EventGenerator  listenTo,
                                  EventDispatcher dispachVia,
                                  EventListener   processWith)
	{
		listenTo.addEventListener(dispachVia);

		Iter i = processWith.getDesiredEventTypes().createIterator();

		while (i.hasNext())
		{
			Any eventType = i.next();
			EventMultiplexer em = null;
			if (dispachVia.isDispatching(eventType))
			{
				em = (EventMultiplexer)dispachVia.getEventListener(eventType);
			}
			else
			{
				em = new EventMultiplexer(eventType);
				dispachVia.addEventListener(em);
			}
			em.addEventListener(processWith);
		}
	}

  protected boolean handleBoundEvent(Event e)
  {
    if (e.getUnderlyingEvent() instanceof MouseEvent)
    {
      // Add the component mouse coordinates
      MouseEvent me = (MouseEvent)e.getUnderlyingEvent();

      iX__.setValue(me.getX());
      iY__.setValue(me.getY());
      e.add(x__, iX__);
      e.add(y__, iY__);
    }
    return true;
  }
  
  private void setupRenderedValue()
  {
    // Put an expression that yields the value we are rendering
    // into this GUI component.  Can be used to refer to the
    // rendered value in event callbacks no matter what component
    // generated the event.
    RenderInfo r = getRenderInfo();
    if (r != null)
    {
      this.replaceItem(renderedValue__, r.getValueExpression());
    }
  }

  private Any getValueExpression()
  {
    Any ret = null;

    RenderInfo r = getRenderInfo();
    if (r != null)
      ret = r.getValueExpression();

    return ret;
  }

  private void evaluateChildContext()
  {
    // Traverse children and set us in their context node
    Any child;
    Iter i = createConcurrentSafeIterator();
    while (i.hasNext())
    {
      child = i.next();
      if (child instanceof AnyView)
      {
        AnyView v = (AnyView)child;
        v.evaluateContext();
        v.evaluateChildContext();
      }
    }
  }

	private void listenForProperties(EventListener l, Any pName)
	{
		if (propertyMultiplexer_ == null)
		{
			EventGenerator contextEg = (EventGenerator)getContextNode();
			propertyMultiplexer_ = new EventMultiplexer();
			contextEg.addEventListener(propertyMultiplexer_);
		}

		EventDispatcher ed = new PropertyDispatcher(pName);
		ed.addEventListener(l);
		// junk any old bound property
		propertyMultiplexer_.removeEventListener(ed);
		propertyMultiplexer_.addEventListener(ed);
	}

	private void doListenerType(Any           eventType,
															EventListener l,
															Any           eventParam)
	{
    if (adapters_ == null)
    {
      adapters_               = AbstractComposite.simpleMap();
      adaptedEventDispatcher_ = new EventDispatcher();
    }

		// First check if there is already a multiplexer in the
		// dispatcher for this event type
		if (adaptedEventDispatcher_.isDispatching(eventType))
		{
			EventMultiplexer em =
				(EventMultiplexer)adaptedEventDispatcher_.getEventListener(eventType);
			em.addEventListener(l, eventParam);
		}
		else
		{
			ListenerAdapter sla = null;
			boolean attached = false;
			// otherwise look for any listeners already attached to
			// the component which support the given event type
			Iter i = adapters_.createIterator();
			while (i.hasNext())
			{
				ListenerAdapter la = (ListenerAdapter)i.next();
				if (la.isSupported(eventType))
				{
					sla = la;
					attached = true;
					break;
				}
			}

			// If none found then get a new one
			if (sla == null)
				sla = locateListenerAdapter(eventType);

			// add it to our map - note the same listener might be a value
			// in the map under more than one key.
			if (!adapters_.contains(eventType))
        adapters_.add(eventType, sla);


			// then create a multiplexer, add the given listener
			// to it and add it to the dispatcher
			EventMultiplexer em = new EventMultiplexer(eventType);
			adaptedEventDispatcher_.addEventListener(em, eventParam);
			em.addEventListener(l, eventParam);

			sla.hasInterest(eventType);

			// finally, once all is set up (order is important for thread
			// safety) we must also start to use it by adding the
			// ListenerAdapter to the component.
			if (!attached)
			{
				attach(sla.eventCategory(), sla);
			}
		}
	}

	private void undoListenerType(Any           eventType,
																EventListener l)
	{
		EventMultiplexer em =
			(EventMultiplexer)adaptedEventDispatcher_.getEventListener(eventType);
		em.removeEventListener(l);

		if (em.numListeners() == 0)
		{
			adaptedEventDispatcher_.removeEventListener(em);
			ListenerAdapter la = (ListenerAdapter)adapters_.get(eventType);
			la.hasNoInterest(eventType);
      // TODO: When AnyComboBox is rewritten detach(eventType, la);
		}
	}

  /**
   * A helper function to find the ListenerAdapter for this event type.
   * We also attach the ListenerAdapter, which is also a suitable
   * implementation of the component's listener interface.
   */
  private ListenerAdapter locateListenerAdapter(Any eventType)
  {
		// get a new ListenerAdapter and connect it to the component.
		ListenerAdapter la = null;

		la = ListenerAdapterFactory.getAdapter(eventType,
                                           getPreferredListenerTypes());

		// add ourselves as the adaptee to the external component's
		// adapter.
		la.setAdaptee(this);

		return la;
  }

  // The JDK exceptions are wrapped in RuntimeContainedException
  // to avoid exception propagation problems through the Any listener
  // interface
  private void attach(Any             eventType,
											ListenerAdapter la)
  {
    Method addMethod = null;
    try
    {
			// Ask this or derived class for the appropriate
			// component on which to add the listener.
			Object attachTo = getAttachee(eventType);

			// silently fail if there's no attachee
			if (attachTo == null)
        return;


      // Find the right EventSet
			EventSet eventSet = (EventSet)eventSetMap__.get(attachTo);
      if (eventSet == null)
        throw new AnyRuntimeException("No event set available for " +
                                      attachTo);

      addMethod = eventSet.getAddMethod(eventType);
      if (addMethod == null)
        throw new AnyRuntimeException("Unsupported event type " +
                                      eventType +
                                      " on object " + attachTo);

      // now we have to invoke this method on the attachee
      // and use the passed in EventGenerator (which is
      // in fact the right Listener implementation) as argument
      Object[] o = new Object[1];
      o[0] = la;

      addMethod.invoke(attachTo, o);
    }
    catch (InvocationTargetException itx)
    {
			throw new RuntimeContainedException(itx.getTargetException());
		}
    catch (Exception e)
    {
			throw new RuntimeContainedException(e);
    }
	}

  private void detach(Any             eventType,
                      ListenerAdapter la)
  {
    Method removeMethod = null;
    try
    {
      // Ask this or derived class for the appropriate
      // component on which to remove the listener.
      Object attachedTo = getAttachee(eventType);
      
      // silently fail if there's no attachee
      if (attachedTo == null)
        return;
  
      // Find the right EventSet
      EventSet eventSet = (EventSet)eventSetMap__.get(attachedTo);
      if (eventSet == null)
        throw new AnyRuntimeException("No event set available for " +
                              attachedTo);
  
      removeMethod = eventSet.getAddMethod(eventType);
      if (removeMethod == null)
        throw new AnyRuntimeException("Unsupported event type " +
                              eventType +
                              " on object " + attachedTo);
  
      // now we have to invoke this method on the attachee
      // and use the passed in EventGenerator (which is
      // in fact the right Listener implementation) as argument
      Object[] o = new Object[1];
      o[0] = la;
      
      removeMethod.invoke(attachedTo, o);
    }
    catch (InvocationTargetException itx)
    {
      throw new RuntimeContainedException(itx.getTargetException());
    }
    catch (Exception e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  // Override to make our own extension of PropertySet.PropertyBinding
  protected PropertyBinding makePropertyBinding(PropertySet p, Object o, Any property)
  {
    PropertySet.BeansPropertyBinding b;
    p.makePropertyBinding(o, property, b = new BoundProperty(o));
    return b;
  }

//  protected boolean modelIsFiring()
//  {
//    return modelFiring_;
//  }

  private class RenderingListener extends DataListener
  {
		RenderingListener(Map nodeSpecs)
		{
			super(nodeSpecs);
		}

    protected void dispatchToGraphics(Event e) throws AnyException
		{
//      // Avoid unnecessary model events here if we are firing them
//      if (!modelFiring_)
      Map              context     = Globals.process__.getContext();
      Any              contextPath = Globals.process__.getContextPath();
      try
      {
        Globals.process__.setContext((Map)getContextNode());
        Globals.process__.setContextPath(getContext());
        componentProcessEvent(e);
      }
      finally
      {
        Globals.process__.setContext(context);
        Globals.process__.setContextPath(contextPath);
      }
		}
	}

	private void init()
	{
		context_ = null;

    // Dummy entry for properties to make sure path
    // resolution works before real properties map is
    // installed
    //this.add(properties__, properties__);
	}
  
  // The entry point for node events that will be dispatched to
  // graphical components. Node events can arise either on the
  // awt event dispatch thread or the server handling thread.
  // In the latter case the event must be transferred to the
  // awt thread.
  protected abstract class DataListener extends    AbstractAny
                                        implements EventListener
  {
		Array eventTypes_ = AbstractComposite.array();

		protected DataListener(Map nodeSpecs)
		{
			Iter i = nodeSpecs.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				makeUpdateEventType(k, nodeSpecs.get(k));
				makeDeleteEventType(k);
				makeReplaceEventType(k);
				makeAddEventType(k);
				makeRemoveEventType(k);
			}
		}

		public Array getDesiredEventTypes()
		{
			return eventTypes_;
		}
    
    protected abstract void dispatchToGraphics(Event e) throws AnyException;
    
    public boolean processEvent(final Event e) throws AnyException
    {
      if (SwingUtilities.isEventDispatchThread())
      {
        dispatchToGraphics(e);
      }
      else
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            try
            {
              dispatchToGraphics(e);
            }
            catch(Exception ee)
            {
              throw new RuntimeContainedException(ee);
            }
          }
        });
      }
      return true;
    }

		private void makeUpdateEventType(Any nodeSpec, Any fields)
		{
			Map eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.BOT_UPDATE);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
			eventType.add(EventConstants.EVENT_FIELDS, fields);
			eventTypes_.add(eventType);
		}

		private void makeDeleteEventType(Any nodeSpec)
		{
			Map eventType = AbstractComposite.eventIdMap();
			eventType.add(Descriptor.descriptor__, AnyAlwaysEquals.instance());
			eventType.add(EventConstants.EVENT_TYPE, EventConstants.BOT_DELETE);
			eventType.add(EventConstants.EVENT_PATH, nodeSpec);
			eventTypes_.add(eventType);
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

  // Listens for <inq> events according to the given set of node
  // specifications and
  private class PropertyListener extends DataListener
  {
		Any property_;

		PropertyListener(Any property, Map nodeSpecs)
		{
			super(nodeSpecs);

			property_ = property;
		}

    protected void dispatchToGraphics(Event e) throws AnyException
		{
			AnyView.this.setProperty(property_, e);
		}
  }

  private class PropertyDispatcher extends EventDispatcher
  {
  	Any property_;

  	PropertyDispatcher(Any property)
  	{
  		super();
  		property_ = property;
  	}

		public int hashCode()
		{
			return property_.hashCode();
		}

		public boolean equals(Any a)
		{
			return (a instanceof PropertyDispatcher) &&
		       (((PropertyDispatcher)a).property_.equals(property_));
		}
  }

  private class WriteProperty extends SwingInvoker
  {
		protected void doSwing()
		{
			try
			{
				propertyWrite_.invoke(propertyObj_, propertyArgs_);
				if (propertyObj_ instanceof JComponent)
				{
					JComponent c = (JComponent)propertyObj_;
					c.repaint();
					c.revalidate();
				}
			}
			catch (InvocationTargetException itx)
			{
				throw new RuntimeContainedException(itx.getTargetException());
			}
			catch (Exception e)
			{
				throw new RuntimeContainedException(e);
			}
		}
	}

	static private class BoundProperty extends PropertySet.BeansPropertyBinding
	{
    BoundProperty(Object o)
    {
      super(null, o);
    }

    protected void doWriteProperty(final Object o, final Object[] args, final Method m)
    {
      SwingInvoker sb = new SwingInvoker()
      {
        protected void doSwing()
        {
          BoundProperty.super.doWriteProperty(o, args, m);
        }
      };
      sb.maybeSync();
    }
	}
	
	static private class GuiRedispatcher extends SwingInvoker
	{
	  private Event e_;
	  private EventListener l_;
	  
	  public GuiRedispatcher(Event e, EventListener l)
	  {
	    e_ = e;
	    l_ = l;
	  }

    protected void doSwing()
    {
      try
      {
        l_.processEvent(e_);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
	}

  // A Java Beans-style event generator that component wrappers
  // can use to generate events when their context is established.
  // A useful event to allow Inq scripts to call initialisation
  // services etc when the environment is ready
	protected static class NotifyContext
	{
		ArrayList listeners_ = new ArrayList();

		public void addContextListener(ContextListener l)
		{
			listeners_.add(l);
		}

		public void removeContextListener(ContextListener l)
		{
			int i = -1;
			if ((i = listeners_.indexOf(l)) >= 0)
				listeners_.remove(i);
		}

		public void fireContextEstablished(ContextEvent e)
		{
			Iterator i = listeners_.iterator();
			while (i.hasNext())
			{
				ContextListener c = (ContextListener)i.next();
				c.contextEstablished(e);
			}
		}
	}

  // Provide support to prevent AWT events from committing the
  // transaction when the current execution is a syncgui service.
  // If awtEventCount__ is not zero then AWT events transferred
  // through EventBinding instances will never commit.  This is
  // done in ServiceInvocationEvent instead.
  public static int syncGuiStart()
  {
    return awtEventCount__++;
  }

  public static void syncGuiEnd(int count)
  {
    if (count >= 0)
      awtEventCount__ = count;
    else
      awtEventCount__ = 0;
  }

  // Create one of these for each (set of) awt event(s) we
  // have attached a listener adapter to on the component.
  // This represents the entry point into Inq code from
  // the awt event system.  We have the monitor of Globals.process__
  // at this time so the awt and inq threads never run together
  public class EventBinding extends    AbstractAny
                            implements EventListener
	{
		// Evaluate this when the event fires
		private Func expr_;

		// The inq event types we are accepting
	  private Array         eventTypes_;

	  private boolean       consume_;
	  //private boolean       busy_;

    // Whether, on completion of this event handler, a modification event is
    // raised on the item the associated view is rendering, or any other aspect
	  // the view maintains.
    private boolean       modelFires_;
    
	  // For derived classes whose execExpr does direct Java calls, rather
	  // than Inq scripting calls.
	  protected EventBinding(Array eventTypes, boolean consume)
	  {
      this(null, eventTypes, consume, false, false);
	  }

	  protected EventBinding(Array eventTypes, boolean consume, boolean busy)
	  {
      this(null, eventTypes, consume, busy, false);
	  }

	  protected EventBinding(Any eventType, boolean consume)
	  {
    	eventTypes_ = AbstractComposite.array();
    	eventTypes_.add(eventType);

	  	consume_    = consume;
      modelFires_ = false;
	  }

	  public EventBinding(Func expr, Array eventTypes, boolean consume, boolean busy)
	  {
      this(expr, eventTypes, consume, busy, false);
	  }

	  public EventBinding(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
	  {
	  	expr_       = expr;
	  	eventTypes_ = eventTypes;
	  	consume_    = consume;
//	  	busy_       = busy;
	  	modelFires_ = modelFires;
	  }

	  public boolean processEvent(Event e) throws AnyException
	  {
	  	// If the inq thread is active (and the event hasn't
	  	// already been through here once) then this event has
	  	// arisen because of a swing action undertaken by the
	  	// current service request.  We may or may not be on the
	    // swing thread but we can't enter the global objects (the
	  	// process, transaction and node space) so we enqueue
	  	// the event instead for processing when the current
	  	// service request has completed.  This gives us the
	  	// maximum thread liveness between the gui and
	  	// client/server threads.
	  	Any id = e.getId();

	  	if (Globals.inqActive__ && !id.equals(EventConstants.EVENT_INVOKER))
	  	{
	  	  if (id.equals(EventConstants.CONTEXT_ESTABLISHED))
	  	  {
	  	    // Context established events are better dispatched to
	  	    // the graphics thread in case they do more graphics-related
	  	    // things. Do this always.
	  	    SwingInvoker si = new GuiRedispatcher(e, this);
	  	    si.maybeAsync(true);
	  	  }
	  	  else
	  	  {
  	  		Event qe = new SimpleEvent(EventConstants.EVENT_INVOKER,
  	  		                           this,
  	  		                           e);
  	  		Globals.process__.send(qe);
	  	  }
	  		return true;
	  	}

	  	boolean isAwt     = false;
	  	int awtEventCount = awtEventCount__;

	  	if (Globals.inqActive__)
      {
        e = (Event)e.getParameter(); // Its a redispatch
      }
      else
      {
        awtEventCount__++; // Its an awt event
        isAwt = true;
      }
	  	
	  	if (isAwt && fired__.contains(this))
	  	{
	  	  // A protected re-entry - ignore
	  	  return true;
	  	}

	  	Transaction      t           = Globals.process__.getTransaction();
      Any              contextPath = Globals.process__.getContextPath();
      Map              context     = Globals.process__.getContext();
      ExceptionHandler eh          = Globals.process__.getExceptionHandler();

	  	try
	  	{
		  	// All events are also maps - add this as component.  Take care as we
		  	// could be dispatching the same event more than once via Inq.
		  	Map m = (Map)e;
		  	if (!m.contains(component__))
        {
          m.add(component__, AnyView.this);
        }

        Map newContext = (Map)AnyView.this.getContextNode();
        if (newContext == null && awtEventCount == 0)
        {
          return true;
        }

        if (newContext != null)
        {
          Globals.process__.setContext(newContext);
          Globals.process__.setContextPath(AnyView.this.getContext());
        }

//		  	if (isAwt && busy_ && awtEventCount == 0)
//		  	{
//          AnyWindow.setBusyCursor();
//		  	}

        // Check if we have a popup menu for a parent.  If so, add
        // it to the event.
        AnyPopupMenu pm = AnyView.this.getParentPopupMenu();
        if (pm != null)
        {
          e.add(AnyPopupMenu.popupComponent__, pm.getLastPoppedUpOn());
        }
        
        // Set the transaction to be active. We will be committing it
        // once here.
        t.copyOnWrite(null);

        // Give this a chance to augment the event with component-specific data
        // or veto it altogether
        if (handleBoundEvent(e))
          execExpr(t, getContextNode(), expr_, e);

        if (modelFires())
          doFireModel(t, e);

        if (awtEventCount == 0)
        {
          Globals.process__.getTransaction().commit();
        }
        
				if (consume_)
				  e.consume();
	  	}
			catch (AnyException ex)
			{
				// Normal exceptions from the Any framework
				ex.fillInCallStack(t);
        //ex.printStackTrace();
        eh.handleException(ex, t);
        awtEventCount__ = awtEventCount;
        if (!Globals.inqActive__ && awtEventCount__ == 0)
        {
				  t.getCallStack().empty();
        }
        Globals.process__.setContext(context);
        Globals.process__.setContextPath(contextPath);
			}

			catch (AnyRuntimeException ex)
			{
				// Runtime exceptions from the Any framework
				ex.fillInCallStack(t);
        //ex.printStackTrace();
        eh.handleException(ex, t);
        awtEventCount__ = awtEventCount;
        if (!Globals.inqActive__ && awtEventCount__ == 0)
        {
				  t.getCallStack().empty();
        }
        Globals.process__.setContext(context);
        Globals.process__.setContextPath(contextPath);
			}

			// Handle uncaught JDK exceptions
			catch (Exception ex)
			{
				AnyException ce = new ContainedException(ex);
				ce.fillInCallStack(t);
        //ce.printStackTrace();
        eh.handleException(ce, t);
        awtEventCount__ = awtEventCount;
        if (!Globals.inqActive__ && awtEventCount__ == 0)
        {
				  t.getCallStack().empty();
        }
        Globals.process__.setContext(context);
        Globals.process__.setContextPath(contextPath);
			}

			catch (StackOverflowError ex)
			{
				AnyException ce = new ContainedException(ex);
        ce.topOfStack(t);
        //ce.printStackTrace();
        eh.handleException(ce, t);
        awtEventCount__ = awtEventCount;
        if (!Globals.inqActive__ && awtEventCount__ == 0)
        {
				  t.getCallStack().empty();
        }
        Globals.process__.setContext(context);
        Globals.process__.setContextPath(contextPath);
      }

      finally
      {
//		  	if (isAwt && busy_ && awtEventCount == 0)
//		  	{
//          AnyWindow.unsetBusyCursor();
//		  	}

        awtEventCount__ = awtEventCount;
        if (!Globals.inqActive__ && awtEventCount__ == 0)
        {
				  t.getCallStack().empty();
        }
        Globals.process__.setContext(context);
        Globals.process__.setContextPath(contextPath);
        jt_.setAny(null, null);
        if (awtEventCount__ == 0)
          fired__.empty();
      }
      return true;
	  }

	  public Array getDesiredEventTypes()
	  {
			return eventTypes_;
	  }

    public Any getContextNode()
    {
      return AnyView.this.getContextNode();
    }

    public Func removeBinding()
    {
      AnyView.this.removeAdaptedEventListener(this);
      return expr_;
    }
    
    protected boolean modelFires()
    {
      return modelFires_;
    }

    protected boolean doFireModel(Transaction t, Event e) throws AnyException
    {
      Any valExpr = AnyView.this.getValueExpression();
      if (valExpr instanceof Locate)
      {
        Locate l = (Locate)valExpr;
        jt_.setAny(l, null);
        EvalExpr.evalFunc(t, getContextNode(), jt_);
        jt_.setAny(null, null);
        
        return true;
      }
      return false;
    }

    // Provide a chance for derived classes to pick up the function return
	  // value, set up arguments if its a Call statement etc.  If there is
	  // an expression then derived should call super but if there
	  // isn't (because derived calls Java methods, say) then there's no need.
	  protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
	  {
      // If the expression is null then (presumably) derived binding
      // class didn't override this implementation.
      if (expr != null)
      {
        expr.setParam(e);

        return EvalExpr.evalFunc(t,
                                 context,
                                 expr);
      }
      return null;
	  }
	  
	  protected void modelFireGuard()
	  {
      fired__.add(this);
	  }
	}

  public class DialogRedirector extends EventBinding
  {
    private Any            dialogEventType_;

    public DialogRedirector(Any dialogEventType, Array eventTypes, boolean consume, boolean busy)
    {
      super(null, eventTypes, consume, busy);
      dialogEventType_ = dialogEventType;
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      // Searches up the Inq hierarchy to find our parent dialog on which the
      // event is fired.
      DialogF d = AnyDialog.getParentDialog(AnyView.this);
      if (d != null)
      {
        if (dialogEventType_.equals(MakeComponent.dialogOk__))
        {
          d.fireOk();
        }
        else if (dialogEventType_.equals(MakeComponent.dialogCancel__))
        {
          d.fireCancel();
        }
      }
      return null;
    }
  }

  public class EditorRedirector extends EventBinding
  {
    private Any            cellEditType_;

    public EditorRedirector(Any cellEditType, Array eventTypes, boolean consume, boolean busy)
    {
      super(null, eventTypes, consume, busy);
      cellEditType_ = cellEditType;
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      // Raise an event on this AnyView which will be picked up by the
      // editor currently using it.
      Event ee = new SimpleEvent(cellEditType_);
      
      // Short-circuit all the node event stuff etc in fireEvent
      AnyView.this.sendEvent(ee);
      
      return null;
    }
  }
}
