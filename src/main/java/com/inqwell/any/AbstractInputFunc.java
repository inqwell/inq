/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractInputFunc.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Acts as a base class for those <code>Func</code> objects that support
 * inputs from client requests.
 * <p>
 * A <code>Func</code> implementation of some sort usually operates within
 * some context of a larger structure with that context being defined by the
 * node passed to its <code>exec()</code> method.  However, this context
 * represents the working set of data at hand.  There are often other operands
 * provided externally.  Consider a filter which tests a value within
 * the working data (say Quantity) against some externally supplied value
 * which can vary.  In this case one of the operands is defined relative
 * to the context node (and must be satisfied by the context unambiguously if
 * the filter is to work properly) and may be specified as <code>*Qty</code>.
 * The other operand would be the client's variant input and must be supplied
 * outside of the working data.
 * <p>
 * When such a component is used within a service the locations for these inputs
 * must be created.  This class simply offers support to allow the specification
 * of paths for these input locations.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public abstract class AbstractInputFunc extends    AbstractNamedFunc
										                    implements NamedFunc,
                                                   EventGenerator,
                                                   Cloneable
{
  private Map params_;   // Maps argument names to prototype values
  // See cloning behaviour for this member below
  
  //private URL baseURL_;
  private Any baseURL_;
  
  private WaitReady waitReady_;
  
  private short effectivePrivilegeLevel_ = Process.DEFAULT_PRIVILEGE;
  private boolean privDone_              = false;

  // Our direct listeners
  private NodeEventPropagator ourListeners_;
  
  // What events stored in the transaction are raised by this function,
  // if any.
  private Array eventTypes_;

  public static Any exec__ = new ConstString("exec__");
  
  public static void catalog(AbstractInputFunc f,
                             NodeSpecification path,
                             Transaction       t) throws AnyException
  {
    LocateNode l       = new LocateNode(path);
    Catalog    c       = Catalog.instance();
    Map        catalog = c.getCatalog();
    
    // Look for any existing descriptor by this name and expire/destroy
    // if found.
		AbstractInputFunc of = (AbstractInputFunc)EvalExpr.evalFunc
                                                 (t,
                                                  catalog,
                                                  l,
                                                  AbstractInputFunc.class,
                                                  Locate.class);
    try
    {
      if (of != null)
      {
        // If there's an old function by the same name then
        // wait for anyone who may be using it to finish and
        // clear its ready status (see WaitIdle.waitIdle())
        of.waitIdle();
        
        // Once the old function is locked, make it defunct
        // so that any references cached by call statements are
        // unusable.
        of.setDefunct();
      }

      // catalog the new function
      c.catalog(f, l.getNodePath(), t);
    }
    finally
    {
      // Signal the old function so that anyone waiting will
      // be woken up.  They then check the defunct status
      // and if so relocate the new function.
      if (of != null)
        of.signalReady();
    }
  }
  
  public AbstractInputFunc()
  {
  	params_  = AbstractComposite.orderedMap();
    init();
  }

  public void addParam(Any name, Any value)
  {
    if (params_.contains(name))
      throw new AnyRuntimeException("Duplicate parameter definition: " + name);
  	params_.add (name, value);
  }
  
  public void addParam(String name, Any value)
  {
  	addParam (AbstractValue.flyweightString(name), value);
  }
  
	public void setBaseURL(Any url)
	{
    //if (url.length() != 0)
      //baseURL_ = new URL(url);
    baseURL_ = url;
	}

  public Any getBaseURL()
  {
	  return baseURL_;
	}

  public Map buildArgs()
  {
		return buildArgs(AbstractComposite.simpleMap());
	}
  
  public Map buildArgs(Map m)
  {
  	if (m == null)
  	  m = AbstractComposite.simpleMap();

  	Iter i = params_.createKeysIterator();
  	
  	while (i.hasNext())
  	{
  		Any a = i.next();
			//System.out.println ("buildLocations() : key: " + a + " " + a.getClass());
			m.add(a, AbstractAny.cloneOrNull(params_.get(a)));
  	}
		return m;
  }
  
  public void setFQName(Any fqName)
  {
    super.setFQName(fqName);
    waitReady_ = new WaitReady(getFQName());
  }
  
  public short getEffectivePrivilegeLevel()
  {
    return effectivePrivilegeLevel_;
  }
  
  public void setEffectivePrivilegeLevel(short level)
  {
    if (privDone_)
      throw new AnyRuntimeException("Cannot reset effective privilege!");
      
    setPrivilegeOnce(level);
  }
  
	public abstract void setExpr(Any a);

  public Object clone () throws CloneNotSupportedException
  {
    AbstractInputFunc f = (AbstractInputFunc)super.clone();

    // Its OK to share params_ as they are read-only
    return f;
  }
  
  public void fireEvent (Event e) throws AnyException
  {
    ourListeners_.fireEvent(e);
  }
  
  public void addEventListener (EventListener l, Any eventParam)
  {
    ourListeners_.addEventListener(l, eventParam);
  }

  public void addEventListener (EventListener l)
  {
    ourListeners_.addEventListener(l);
  }
  
  public void removeEventListener (EventListener l)
  {
    ourListeners_.removeEventListener(l);
  }

  public void removeAllListeners ()
  {
    ourListeners_.removeAllListeners();
  }

  public Array getGeneratedEventTypes()
  {
    Array eventTypes = AbstractComposite.array();

    eventTypes.add(makeEventType(EventConstants.EXEC_START));
    eventTypes.add(makeEventType(EventConstants.EXEC_COMPLETE));

    return eventTypes;
  }
  
  public Event makeEvent(Any eventType)
  {
    Event ret = null;
    
    if (eventType.equals(EventConstants.EXEC_START))
    {
      ret = new SimpleEvent(makeEventType(EventConstants.EXEC_START));
    }

    if (eventType.equals(EventConstants.EXEC_COMPLETE))
    {
      ret = new SimpleEvent(makeEventType(EventConstants.EXEC_COMPLETE));
    }
    
    return ret;
  }
  
  public boolean raiseAgainstChildren(Event e)
  {
    return true;
  }

  public void setRaisesEvents(Array eventTypes)
  {
    eventTypes_ = eventTypes;
  }
  
  protected boolean raisesEvents()
  {
    return eventTypes_ != null;
  }
  
  protected Array getEventTypes()
  {
    return eventTypes_;
  }

  private Map makeEventType(Any type)
  {
    Map ret = AbstractComposite.eventIdMap();
    
    ret.add (exec__, this.getFQName());
    ret.add (EventConstants.EVENT_TYPE, type);
    
    return ret;
  }
  
  private void setPrivilegeOnce(short level)
  {
    privDone_ = true;
    effectivePrivilegeLevel_ = level;
  }
  
  protected void setDefunct()
  {
    waitReady_.setDefunct();
  }
	
  public boolean isDefunct()
  {
    return waitReady_.isDefunct();
  }
	
  public void startUse()
  {
    // We do defunct checking ourselves at a higher level.
    waitReady_.startUse(false);
  }
	
  public void endUse()
  {
    waitReady_.endUse();
  }
	
  private void waitIdle()
  {
    waitReady_.waitIdle();
  }
	
  private void signalReady()
  {
    waitReady_.signalReady();
  }
  
  private void init()
  {
    ourListeners_  = new NodeEventPropagator();
  }
}
