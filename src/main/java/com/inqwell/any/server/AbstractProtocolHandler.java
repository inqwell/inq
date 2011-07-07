/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/AbstractProtocolHandler.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import com.inqwell.any.AnyException;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.StartProcessEvent;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.ContentCipher;
import com.inqwell.any.channel.Socket;

import java.net.ServerSocket;
import java.io.IOException;

public abstract class AbstractProtocolHandler implements ProtocolHandler
{
	private static EventDispatcher  ed_;
	
	static
	{
		ed_ = new EventDispatcher();
		ed_.addEventListener(new StartUserProcess());
		ed_.addEventListener(new StartRouterProcess());
		//ed_.addEventListener(SessionManager.instance());
	}	

  /**
   * Returns a plain server socket by default.
   */
  public ServerSocket getServerSocket(int port) throws IOException
  {
    return new ServerSocket(port);
  }

  protected static void startUserProcess(Socket cd) throws AnyException
  {
  	startUserProcess(cd, null);
  }

  protected static void startUserProcess(Socket cd,
                                         ContentCipher cc) throws AnyException
  {
	  // Read the event from the input socket which tells us
	  // what kind of process we will start or whether to
	  // resume an unreliable, broken speakinq connection. (TODO: code for this to go)
	  // Its OK to read the socket directly as there are no
	  // syncronization issues here - we are still in the thread of
    // the server listener. We are, for that reason, blocking further
    // connect requests coming in. A badly behaved client could not
    // send anything and we'll wait here forever. Set the SoTimeout
    // to guard against this. Hard-coded at 5s
    cd.setSoTimeout(5000);
	  StartProcessEvent spe = (StartProcessEvent)cd.read();
    cd.setSoTimeout(0);
	  
	  // Set any keep-alive message interval that has been passed in the
	  // event
	  cd.setProbeTimeout(spe.getKeepAliveTimeout());
	  
	  // This nonsense relates to http tunnelling - not really viable
	  spe.setNetworkDriver(cd);
	  spe.setCipher(cc);
	  
	  // Dispatch the event
	  ed_.processEvent(spe);
  }
}
