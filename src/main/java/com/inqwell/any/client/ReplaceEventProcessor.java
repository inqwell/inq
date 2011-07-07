/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/ReplaceEventProcessor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * An <code>EventListener</code> whose purpose is to apply
 * a received NODE_REPLACED node event to the local instance hierarchy
 */
public class ReplaceEventProcessor extends    AbstractFunc
																	 implements EventListener
{
	private Array      eventTypes_ = AbstractComposite.array();
	private Any        root_;

	private AddTo      adder_     = new AddTo();

	public ReplaceEventProcessor()
	{
		this(EventIdMap.makeNodeEventType(EventConstants.NODE_REPLACED,
																			AnyAlwaysEquals.instance(),
																			AnyAlwaysEquals.instance()));
	}
	
	public ReplaceEventProcessor(Any eventType)
	{
		this(eventType, null);
	}
	
	public ReplaceEventProcessor(Any eventType, Any root)
	{
		if (eventType != null)
			addEventType(eventType);
		
		root_ = root;
		
		adder_.setRaiseEvent(AnyBoolean.TRUE);
	}
	
  public Any exec(Any a) throws AnyException
  {
		//System.out.println ("ReplaceEventProcessor.exec " + a);
		Event e = (Event)a;
		
		Map m = (Map)e.getId();
		NodeSpecification ns = (NodeSpecification)m.get(EventConstants.EVENT_PATH);
		
		Any replaceWith = e.getContext();
		
		adder_.setPath(ns);
		adder_.setNode(replaceWith);
		
//		System.out.println ("ReplaceEventProcessor " + ns + " " + replaceWith);
		EvalExpr.evalFunc(getTransaction(),
											root_,
											adder_);

		
		return null;
  }

  public boolean processEvent(Event e) throws AnyException
  {
		exec(e);
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes_;
  }

	public void setRoot(Any root)
	{
		root_ = root;
	}

  public void addEventType(Any eventType)
  {
		eventTypes_.add(eventType);
  }
}
