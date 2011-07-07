/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/PlainSocketURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-02 20:37:03 $
 */
 

package com.inqwell.any.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

import com.inqwell.any.RuntimeContainedException;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>socket://</code> style URLs.
 * <p>
 * URLs of the style <code>socket://host:port</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL can be used to support interfacing between
 * an <code>inq</code> environment
 * and external systems.
 */
public class PlainSocketURLConnection extends URLConnection
{
	private Socket        socket_;
	
	private int           port_;
	
	public PlainSocketURLConnection(URL url, int port)
	{
		super(url);
		port_ = port;
	}

	public InputStream getInputStream() throws IOException
	{
		connect();
		return socket_.getInputStream();
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		connect();
		return socket_.getOutputStream();
	}
	
	public void connect() throws IOException
	{
    if (!this.connected)
    {
      socket_ = new Socket(getURL().getHost(), port_);
      this.connected = true;
    }
	}
	
	public Socket getSocket()
	{
	  return socket_;
	}
	
	public void setSoTimeout(int timeout)
	{
	  try
	  {
	    socket_.setSoTimeout(timeout);
	  }
	  catch(SocketException s)
	  {
	    throw new RuntimeContainedException(s);
	  }
	}
}
