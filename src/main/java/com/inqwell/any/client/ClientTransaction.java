/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/ClientTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-05-13 10:15:39 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * Provides the little functionality that is required of a 'transaction'
 * in the client.  In fact, this is just to raise events on data we are
 * modifying in order that views can pick up the modifications for
 * the purposes of MVC-type processing.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.8 $
 */ 
public class ClientTransaction extends    AbstractTransaction
															 implements Transaction
{
	private Process process_;
	
	private Map     fields_;
	private Map     source_;

	private boolean active_;
  
  // The list of events raised by this transaction during the commit phase
  private Array   eventList_;
	
	public ClientTransaction(Process p)
	{
		process_ = p;
		init();
	}
	
	public ClientTransaction()
	{
		this(null);
	}
	
	public void start(Any root) throws AnyException {}

	public void join (Map m) throws AnyException {}
	
  public void fieldChanging(Map m, Any f, Any info)
  {
    active_ = true;
    
  	Set fields = null;
  	Map addTo  = fields_;
  	
  	if (addTo.contains(m))
  	  fields = (Set)addTo.get(m);
  	else
  	{
  	  fields = AbstractComposite.fieldSet();
  	  addTo.add(m, fields);
  	}
  	
  	// I think this must always work
  	if (f instanceof Locate)
  	{
      Locate l = (Locate)f;

      if (!fields.contains(l.getPath()))
        fields.add(l.getPath());
    }
    else if (f instanceof Map)
    {
      Map sm = (Map)f;
      Iter i = sm.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        if (m.contains(k) && !fields.contains(k))
          fields.add(k);
      }
    }
    else if (!fields.contains(f))   // just assume Any is the field name
    {
      fields.add(f);
    }
  	
  	if (info != null && !source_.contains(m))
  	  source_.add(m, info);
  }
  
	public void deleteIntent (Map m) throws AnyException {}
	
	public void resync(Map keyVal, Map cachedObject, Map readObject) throws AnyException
	{
	}

  public void copyOnWrite (Map m) throws AnyException
  {
    if (m != null)
    {
      active_ = true;
      
      rememberOld(m);
    }
  }
  
	public void commit() throws AnyException
	{
		execBeforeActions();
		
    doEventSet(fields_);
		
		execAfterActions();
    eventList_ = null;
    reset();
	}
	
  public void setGatherEvents(boolean gather)
  {
    if (gather)
      eventList_ = AbstractComposite.array();
    else
      eventList_ = null;
  }
  
  public boolean isGatheringEvents()
  {
    return eventList_ != null;
  }
  
  public void addEvent(Event e)
  {
    
    if (eventList_ != null && e != null)
    {
      active_ = true;
      eventList_.add(e);
    }
  }
  
	public void abort()
	{
	  fields_.empty();

		getProcess().setContext(null);
		getProcess().setContextPath(null);
    reset();
	}
	
	public void interrupt() {}

  public boolean isActive()
  {
    return active_;
  }
  
  public Transaction getParent()
  {
    // In the client there are no child transactions
    return null;
  }
  
	public Map getTransInstance (Map m) { return m; }
	
	public Process getProcess()
	{
		return process_;
	}
	
	public void setProcess(Process p) { process_ = p; }
	
	public Map getContext() { return process_.getContext(); }

	public Any getContextPath() { return process_.getContextPath(); }

  public Map getCurrentStackFrame() { return process_.getCurrentStackFrame(); }

	public Map pushStackFrame() { return process_.pushStackFrame(); }
	
	public Map popStackFrame() { return process_.popStackFrame(); }
	
	public boolean isDegenerate() { return false; }
	
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
  
  protected Array getEventBundle()
  {
    return eventList_;
  }
	
  protected void doReset()
  {
    active_ = false;
    source_.empty();
    super.doReset();
  }
  
  protected void doResetRememberOld()
  {
    resetRememberOld(false);
  }

  private void doEventSet(Map eventSet) throws AnyException
  {
    while (eventSet.entries() != 0)
    {
      Map fields = eventSet.shallowCopy();
      eventSet.empty();
      
      Iter i = fields.createKeysIterator();
      while (i.hasNext())
      {
        Any a = i.next();
        if (a instanceof EventGenerator)
        {
          EventGenerator eg = (EventGenerator)a;
  
          Event e = eg.makeEvent(EventConstants.BOT_UPDATE);
          e.setContext(eg);
          Map m = (Map)e.getId();
          m.add(EventConstants.EVENT_FIELDS, fields.get(a));
          Any old = getRememberedOld(a);
          if (old != null)
          {
            e.add(NodeSpecification.atOld__, old);
          }
          
          Any source = source_.getIfContains(a);
          if (source != null)
            e.add(NodeSpecification.atModel__, source);
          
          addEvent(e);
          eg.fireEvent(e);
        }
      }
    }
  }
  
  private void init()
	{
		fields_ = AbstractComposite.orderedMap();
		source_ = AbstractComposite.orderedMap();
	}
}

