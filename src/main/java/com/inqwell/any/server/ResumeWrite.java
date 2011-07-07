/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/ResumeWrite.java $
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
import com.inqwell.any.Globals;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.HttpTunnel;
import com.inqwell.any.channel.ContentCipher;

/**
 * An <code>EventListener</code> that reconnects a server
 * process's output channel to a new network-based channel
 * driver. Useful when dealing with unreliable or
 * stateless network protocols like HTTP.
 * <p>
 * This listener is dispatched to by a process's event
 * dispatcher and causes any messages waiting to be sent
 * to be flushed to the new channel driver.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class ResumeWrite extends    AbstractAny
							           implements EventListener
{
  static private Array  eventTypes__;
  
  private AnyChannel    networkOutput_;
  private InputChannel  processInput_;
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.RESUME_WRITE);
  }
	
	/**
	 * 
	 */
  public ResumeWrite(InputChannel  processInput,
                     OutputChannel networkOutput)
  {
  	processInput_  = processInput;
  	networkOutput_ = (AnyChannel)networkOutput;
  }

  public boolean processEvent(Event e) throws AnyException
  {
  	HttpTunnel newDriver = (HttpTunnel)e.getContext();
		// Check if there'a a Cipher
		Any sessionId = newDriver.getSessionId();
		ContentCipher cc = SessionManager.instance().getCipher(sessionId);
		newDriver.setCipher(cc);
  	newDriver.doRequestHeaders();
  	
  	System.out.println("ResumeWrite 1");
		networkOutput_.resetOutput(newDriver);
		networkOutput_.startKeepOpenProbe();
  	System.out.println("ResumeWrite 2");
    if (Globals.sessionList__.isZombieSession(sessionId))
    {
      if (!networkOutput_.hasPendingOutput())
      {
        Globals.sessionList__.deleteSession(sessionId);
        processInput_.close();
      }
    }
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
}
