/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/RemoveEventProcessor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * An <code>EventListener</code> whose purpose is to apply
 * a received NODE_REMOVED node event to the local instance hierarchy
 */
public class RemoveEventProcessor extends    AbstractFunc
																	implements EventListener
{
	private Array      eventTypes_ = AbstractComposite.array();
	private Any        root_;

	private RemoveFrom remover_      = new RemoveFrom();

	public RemoveEventProcessor()
	{
		this(EventIdMap.makeNodeEventType(EventConstants.NODE_REMOVED,
																			AnyAlwaysEquals.instance(),
																			AnyAlwaysEquals.instance()));
	}
	
	public RemoveEventProcessor(Any eventType)
	{
		this(eventType, null);
	}
	
	public RemoveEventProcessor(Any eventType, Any root)
	{
		if (eventType != null)
			addEventType(eventType);
		
		root_ = root;
		
		remover_.setRaiseEvent(AnyBoolean.TRUE);
	}
	
  public Any exec(Any a) throws AnyException
  {
		//System.out.println ("RemoveEventProcessor.exec " + a);
		Event e = (Event)a;
		
		Map m = (Map)e.getId();
		NodeSpecification ns = (NodeSpecification)m.get(EventConstants.EVENT_PATH);
		
		remover_.setPath(ns);
		
//		System.out.println ("RemoveEventProcessor " + ns + " " + replaceWith);
		EvalExpr.evalFunc(getTransaction(),
											root_,
											remover_);

		//remover_.reset();
    // This is now done by before/afterExecute
		
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
