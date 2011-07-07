/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/ResumeRead.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.Any;
import com.inqwell.any.Array;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AnyException;
import com.inqwell.any.Event;
import com.inqwell.any.EventListener;
import com.inqwell.any.EventConstants;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.HttpTunnel;
import com.inqwell.any.channel.ChannelConstants;
import com.inqwell.any.channel.ContentCipher;

/**
 * An <code>EventListener</code> that reconnects a server
 * process's input channel to a new network-based channel
 * driver. Useful when dealing with unreliable or
 * stateless network protocols like HTTP.
 * <p>
 * This listener is dispatched to by a process's event
 * dispatcher
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class ResumeRead extends    AbstractAny
							          implements EventListener
{
  static private Array  eventTypes__;
  
  private AnyChannel    networkInput_;
  private AnyChannel    processInput_;
  private ChannelDriver newCd_ = new FIFO(0, ChannelConstants.REFERENCE);
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.RESUME_READ);
  }
	
	/**
	 * 
	 */
  public ResumeRead(AnyChannel networkInput,
                    AnyChannel processInput)
  {
  	networkInput_ = networkInput;
  	processInput_ = processInput;
  }

  public boolean processEvent(Event e) throws AnyException
  {
    System.out.println("ResumeRead.processEvent 1 " + e);
    
		networkInput_.shutdownInput(newCd_);
		
		HttpTunnel t = (HttpTunnel)e.getContext();
		// Check if there'a a Cipher
		Any sessionId = t.getSessionId();
		ContentCipher cc = SessionManager.instance().getCipher(sessionId);
		t.setCipher(cc);
		t.doRequestHeaders();
		
		Any a;
		a = t.read();
    System.out.println ("ResumeRead.processEvent 2.1 " + a);
    while (a != InputChannel.shutdown__)
    {
    System.out.println ("ResumeRead.processEvent 2.2");
		  processInput_.write(a);
    System.out.println ("ResumeRead.processEvent 2.3");
      a = t.read();
    }
		
//		while ((a = t.read()) != InputChannel.shutdown__)
//		{
//      System.out.println ("ResumeRead.processEvent 2 " + a);
//		  processInput_.write(a);
//    }
		
		//networkInput_.write(e);
    System.out.println("ResumeRead.processEvent 3 " + e);

    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
}
