/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/StartRouterProcess.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.client.StackTransaction;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.ChannelConstants;

/**
 * An <code>EventListener</code> that starts a router process.  The event
 * is passed from the initiator of a connection to a remote
 * <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup>
 * environment.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class StartRouterProcess extends    AbstractAny
																implements EventListener,
																					 Cloneable
{
  static private Array eventTypes__;
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.START_ROUTERPROCESS);
	}
	
	/**
	 * 
	 */
  public StartRouterProcess()
  {
  }

  public boolean processEvent(Event e) throws AnyException
  {
		StartProcessEvent spe = (StartProcessEvent)e;
	  
	  AnyChannel processInput = new AnyChannel(new FIFO(0,
																											ChannelConstants.REFERENCE));

	  AnyChannel readInput = new AnyChannel(null);
	  Socket s = spe.getSocket();
	  AnyChannel processOutput = new AnyChannel(s);
	  
	  try
	  {
			// If there is a session request then request the ID
			if (spe.getContext() != null)
	    {
				SessionManager.instance().establishSessionId(processInput,
				                                             readInput,
	                                                   processOutput,
	                                                   spe.getSocket(),
	                                                   spe.getCipher());
	    }

		  Transaction t = new StackTransaction();

			EventDispatcher      ed   = new EventDispatcher();
      BasicProcess.RootMap root = new BasicProcess.RootMap();
			ed.addEventListener(new RouteMessage());

			// Set up the event dispatcher to cope with resume writes
			// in case we are using a stateless protocol
			ed.addEventListener (new ResumeRead(readInput,
			                                    processInput));
			ed.addEventListener (new ResumeWrite(processInput,
                                           processOutput));
	
			UserProcess p = new UserProcess (processInput,
                                       processOutput,
                                       new ExceptionToClient(processOutput),
                                       t,
                                       root,
                                       ed,
                                       null); // no events arriving at process root

      root.setProcess(p);
      // Safe to start the reader thread now we've set any session id
			// and sent the session event to the client.
			readInput.startReader(s, processInput, true, p);
			
      p.startThread();
      return true;
    }
    catch (AnyException ex)
    {
      ex.printStackTrace();
    	processOutput.close();
		  throw (ex);
    }
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
		AbstractAny.cloneNotSupported(this);
		return null;
  }
}
