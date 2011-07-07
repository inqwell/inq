/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/InvokeEventProcessor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * An <code>EventListener</code> whose purpose is to apply
 * a received BOT_UPDATE node event to the local instance hierarchy.
 * See also 
 */
public class InvokeEventProcessor extends    AbstractAny
                                  implements EventListener
{
	static private Array eventTypes__;
	
	static
	{
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.EVENT_INVOKER);
	}
	
	public InvokeEventProcessor()
	{
	}
	
  public boolean processEvent(Event e) throws AnyException
  {
		EventListener el = (EventListener)e.getContext();
		el.processEvent(e);
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
}
