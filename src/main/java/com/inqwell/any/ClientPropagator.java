/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ClientPropagator.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.channel.OutputChannel;

/**
 * An event listener which is used for propagating events from a
 * server-side <code>UserProcess</code> to its client.
 * This class waits on a specified listener (usually some node
 * in an instance hierarchy) and sends events it receives to
 * a given <code>OutputChannel</code>
 */
public class ClientPropagator extends    AbstractAny
															implements EventListener
{
	private Array eventTypes_ = AbstractComposite.array();
	
	private OutputChannel oc_;
	
	public static ClientPropagator makeClientPropagator(OutputChannel oc)
	{
    ClientPropagator cp = new ClientPropagator(oc);
    cp.addEventType(EventIdMap.makeNodeEventType(EventConstants.BOT_UPDATE,
                                                 AnyAlwaysEquals.instance(),
                                                 AnyAlwaysEquals.instance(),
                                                 AnyAlwaysEquals.instance()));
    cp.addEventType(EventIdMap.makeNodeEventType(EventConstants.BOT_DELETE,
                                                 AnyAlwaysEquals.instance(),
                                                 AnyAlwaysEquals.instance()));
                       
    cp.addEventType(EventIdMap.makeNodeEventType(EventConstants.NODE_REPLACED,
                                                 AnyAlwaysEquals.instance(),
                                                 AnyAlwaysEquals.instance()));
                       
    cp.addEventType(EventIdMap.makeNodeEventType(EventConstants.NODE_REMOVED,
                                                 AnyAlwaysEquals.instance(),
                                                 AnyAlwaysEquals.instance()));
                       
    cp.addEventType(EventIdMap.makeNodeEventType(EventConstants.NODE_ADDED,
                                                 AnyAlwaysEquals.instance(),
                                                 AnyAlwaysEquals.instance()));
    return cp;                       
	}
	
	public ClientPropagator(OutputChannel oc)
	{
		this(oc, null);
	}
	
	public ClientPropagator(OutputChannel oc, Any eventType)
	{
		oc_ = oc;
		if (eventType != null)
			addEventType(eventType);
	}
	
  public boolean processEvent(Event e) throws AnyException
  {
  	//System.out.println ("ClientPropagator.processEvent() " + e);
    // Ignore exceptions here. Brutal and a little temporary but if another
    // process is propagating events in the server then it's no fault of
    // his that an exception occurs.
    try
    {
  		oc_.write(e);
  		oc_.flushOutput();
    }
    catch(Throwable t) { }
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes_;
  }

  public void addEventType(Any eventType)
  {
		eventTypes_.add(eventType);
  }
}


