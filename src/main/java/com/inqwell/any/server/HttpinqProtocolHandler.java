/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/HttpinqProtocolHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import com.inqwell.any.Any;
import com.inqwell.any.Map;
import com.inqwell.any.AnyString;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.HttpTunnel;
import com.inqwell.any.channel.ContentCipher;
import com.inqwell.any.AnyException;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.EventConstants;
import com.inqwell.any.net.NetUtil;
import com.inqwell.any.util.Util;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class HttpinqProtocolHandler extends    AbstractProtocolHandler
                                    implements ProtocolHandler
{
	private static final String protocolName__  = "httpinq";
	
	private static final String openHtml__   = "<HTML>\r\n";
	private static final String closeHtml__  = "</HTML>\r\n";

	/**
   * Handle a new connection on the <code>httpinq://</code> protocol.
   * The <code>httpinq://</code> protocol implements http tunneling
   * and thus assumes connections are frequently broken and
   * re-established.  If a connection is broken the process that
   * was started is kept alive subject to a timeout.  The client
   * is responsible for requesting that a session id is established
   * when initially connecting.
   */
	public void handleConnect(java.net.Socket s) throws AnyException
	{
    try
    {
	    System.out.println ("Connected from Host " + s.getInetAddress().getHostName());

    	// Read the first line of the http request and process
    	// it to see if its Inq or otherwise
    	InputStream  is = s.getInputStream();
			String commandLine = Util.readLine(is);
	    System.out.println(commandLine);
			
      AnyString httpCommand   = new AnyString();
      AnyString path          = new AnyString();
      AnyString protocolLevel = new AnyString();
			URL u = HttpTunnel.processHttpCommand(commandLine,
			                                      httpCommand,
			                                      s.getInetAddress().getHostName(),
			                                      path,
                                            protocolLevel);
		  
    	// First make a HttpTunnel around the accepted socket.
    	// The HttpTunnel class will read the HTTP command and
    	// determine the session id, if any, from the URL.
	    //System.out.println ("PATH " + path);
	    if (path.equals(HttpTunnel.inq__))
    	{
    	  handleInqConnection(s, u, httpCommand, protocolLevel, false);
    	}
      else if (path.equals(HttpTunnel.inqs__))
    	{
    	  handleInqConnection(s, u, httpCommand, protocolLevel, true);
    	}
    	else
    	{
    		handleDebugConnection(s, httpCommand);
    	}
    }
    catch (Exception e)
    {
      AnyException.throwExternalException(e);
    }
	}

	public String getProtocolName()
	{
		return protocolName__;
	}
	
	private void handleInqConnection(java.net.Socket s,
	                                 URL             u,
	                                 AnyString       httpCommand,
                                   AnyString       protocolLevel,
	                                 boolean         encrypted) throws AnyException
	{
    HttpTunnel httpTunnel = new HttpTunnel(s);
    httpTunnel.setupTunnel(u, httpCommand, protocolLevel);
  	Any sessionId = httpTunnel.getSessionId();
  	
    if (sessionId == null)
    {
      // If there was no session id then the connection must
      // be a new client and we start the user process. In this
      // case the Socket driver uses the tunnel for both input
      // and output at this stage
      
      ContentCipher cc = null;
      if (encrypted)
        httpTunnel.setCipher((cc = ContentCipher.makeCipher()));
        
      httpTunnel.doRequestHeaders();
      Socket cd = new Socket(httpTunnel, httpTunnel);

      startUserProcess(cd, cc);
    }
    else
    {
      // If there is a session id then this is (with both terms
      // applied from the server perspective) a read or a
      // write resumption. These things happen separately as,
      // once the session id is established, two separate
      // connections are used for bi-directional communication.
      // We ask the tunnel whether it is an input channel or
      // otherwise.
      
      if (httpTunnel.isServerInput())
      {
      	// Its for reading - send a RESUME_READ event to
      	// the process input
      	AnyChannel pi = SessionManager.instance().getProcessInputChannel(sessionId);
      	if (pi != null)
      	{
        	pi.write(new SimpleEvent(EventConstants.RESUME_READ,
        	                         httpTunnel));
      	}
      	else
      	{
      		// Session is defunct - timed out or server restart.
      		// We can write directly to the tunnel to tell the
      		// client that all is not well
          if (encrypted)
            httpTunnel.setCipher((ContentCipher.makeCipher()));

      		defunctSession(httpTunnel, HttpTunnel.NS410);
      	}
      }
      else
      {
      	// Its for writing - send a RESUME_WRITE event to
      	// the process input
      	AnyChannel pi = SessionManager.instance().getProcessInputChannel(sessionId);
      	if (pi != null)
      	{
        	pi.write(new SimpleEvent(EventConstants.RESUME_WRITE,
        	                         httpTunnel));
      	}
      	else
      	{
      		// Session is defunct - timed out or server restart.
      		// We can write directly to the tunnel to tell the
      		// client that all is not well
          if (encrypted)
            httpTunnel.setCipher((ContentCipher.makeCipher()));

      		defunctSession(httpTunnel, HttpTunnel.OK200);
      	}
      }
    }
	}
	
	private void handleDebugConnection(java.net.Socket s,
	                                   AnyString       httpCommand) throws AnyException
	{
		try
		{
			InputStream  is = s.getInputStream();
			OutputStream os = s.getOutputStream();
			ByteArrayOutputStream response = new ByteArrayOutputStream();
			
			os.write(HttpTunnel.OK200.getBytes());
			os.write(HttpTunnel.SERVER.getBytes());
			
			if (httpCommand.equals(HttpTunnel.GET1))
			  handleDebugGet(is, os, response);
			else if (httpCommand.equals(HttpTunnel.POST1))
			  handleDebugPost(is, os, response);
			
			if (httpCommand.equals(HttpTunnel.GET1) ||
			    httpCommand.equals(HttpTunnel.POST1))
			{
				response.write(closeHtml__.getBytes());
		    os.write(("Content-Length: " +
			                  response.size() +
			                  "\r\n\r\n").getBytes());
				
				response.writeTo(os);
			}
			s.shutdownOutput();
		}
		catch (IOException iox)
		{
			AnyException.throwExternalException(iox);
		}
	}
	
	private void handleDebugPost(InputStream           is,
	                             OutputStream          os,
	                             ByteArrayOutputStream response) throws IOException
	{
		niceMessage(response);
		int contentLength = handleDebugHeaders(is, os, response);
    response.write("<BR>Your post data is<BR><CODE><PRE>".getBytes());
    int byteIn;
    while (contentLength-- > 0)
    {
    	response.write(is.read());
    }
    response.write("</PRE></CODE><BR>".getBytes());
	}
	
	private void handleDebugGet(InputStream           is,
	                            OutputStream          os,
	                            ByteArrayOutputStream response) throws IOException
	{
		niceMessage(response);
		int contentLength = handleDebugHeaders(is, os, response);
	}
	
	private int handleDebugHeaders(InputStream           is,
	                               OutputStream          os,
	                               ByteArrayOutputStream response) throws IOException
	{
		String lineIn = null;

    response.write("<BR>Your headers are<BR><CODE><PRE>".getBytes());
    
    Map headers = AbstractComposite.simpleMap();
    
		while ((lineIn = Util.readLine(is)).length() != 0)
		{
			response.write((lineIn + "\r\n").getBytes());
			NetUtil.parseHttpHeader(lineIn, headers);
		}
		
    response.write("</PRE></CODE><BR>".getBytes());
    return NetUtil.getContentLength(headers);
	}
	
	private void niceMessage(ByteArrayOutputStream response) throws IOException
	{
		response.write(openHtml__.getBytes());
		response.write("<H1>Inq over HTTP</H1>\r\n".getBytes());
	}
	
	private void defunctSession(HttpTunnel t,
	                            String     responseCode) throws AnyException
	{
    t.doRequestHeaders(responseCode);
    if (responseCode == HttpTunnel.OK200)
		  t.write(new SimpleEvent(EventConstants.SESSION_DEFUNCT));
		t.flushOutput();
		t.close();
	}
}
