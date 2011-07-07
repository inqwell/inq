/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/ProtocolHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import com.inqwell.any.AnyException;

public interface ProtocolHandler
{
  /**
   * Handle a connection that has been accepted on the given
   * <code>java.net.Socket</code>.
   * If a connection represents a new client, a server process will
   * be started.  The connection may represent a recovery attempt
   * after network failure or stateless protocol disconnect (eg
   * HTTP tunneling) in which case the existing process is reconnected.
   */
	public void handleConnect(Socket s) throws AnyException;
	
  /**
   * Returns a suitable socket for the protocol implementation
   */
  public ServerSocket getServerSocket(int port) throws IOException;
  
  /**
   * Returns the name of this protocol
   */
	public String getProtocolName();
}
