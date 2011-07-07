/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/SpeakinqsProtocolHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.inqwell.any.RuntimeContainedException;

public class SpeakinqsProtocolHandler extends    NativeProtocolHandler
                                      implements ProtocolHandler
{
	private static final String protocolName__  = "speakinqs";
	
  /**
   * Returns a ssl socket
   */
  public ServerSocket getServerSocket(int port) throws IOException
  {
    try
    {
      SSLServerSocket s;
      
      SSLServerSocketFactory sslSrvFact =
        (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        
      s = (SSLServerSocket)sslSrvFact.createServerSocket(port);
      
      return s;
    }
    catch (SocketException scktEx)
    {
      throw new RuntimeContainedException(scktEx,
                                          "SSL not initialised - check properties");
    }
  }

	public String getProtocolName()
	{
		return protocolName__;
	}
}
