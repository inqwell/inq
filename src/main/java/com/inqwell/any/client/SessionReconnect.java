/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/SessionReconnect.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.client;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Array;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Event;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.EventListener;
import com.inqwell.any.EventConstants;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.AnyChannel;

/**
 * Process the <code>SESSION_RECONNECT</code> event, which is
 * sent when unreliable connection breaks down, so that the
 * process thread can perform the reopen function.
 */
public class SessionReconnect extends    AbstractAny
							                implements EventListener
{
  static private Array eventTypes__;
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.SESSION_RECONNECT);
	}
	
  private Socket        s_;
  private AnyChannel    networkInput_;
  
  public SessionReconnect(Socket       s,
                          AnyChannel   networkInput)
  {
  	s_            = s;
  	networkInput_ = networkInput;
  }

  public boolean processEvent(Event e) throws AnyException
  {
  	// We know the socket is shutdown because that was done
  	// prior to sending this event through the process's
  	// input channel.  We just do the reopen and send a
  	// SESSION_RESUME event back to the network input
  	// read thread to let it know to resume normal operation.
  	System.out.println("SessionReconnect.processEvent " + e);
    s_.reOpen();
  	networkInput_.write(new SimpleEvent(EventConstants.SESSION_RESUME,
  	                                    s_));
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
}
