/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/SessionDefunct.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.client;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Array;
import com.inqwell.any.AnyException;
import com.inqwell.any.Event;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.EventListener;
import com.inqwell.any.EventConstants;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.ChannelConstants;

/**
 * Process the <code>SESSION_RECONNECT</code> event, which is
 * sent when unreliable connection breaks down, so that the
 * process thread can perform the reopen function.
 */
public class SessionDefunct extends    AbstractAny
							                implements EventListener
{
  static private Array eventTypes__;
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.SESSION_DEFUNCT);
	}
	
  private AnyChannel    networkInput_;
  
  public SessionDefunct(AnyChannel   networkInput)
  {
  	networkInput_ = networkInput;
  }

  public boolean processEvent(Event e) throws AnyException
  {
   	System.out.println("SessionDefunct.processEvent " + e);
  	synchronized(networkInput_)
  	{
			networkInput_.shutdownInput(new FIFO(0, ChannelConstants.REFERENCE));
    	networkInput_.write(e);
  	}
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
}
