/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/HttpInqURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 

package com.inqwell.any.net;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.HttpTunnel;
/*
import java.net.Socket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.NoRouteToHostException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.io.ByteArrayOutputStream;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AnyString;
import com.inqwell.any.Map;
import com.inqwell.any.util.Util;
import com.inqwell.any.util.Base64;
*/

/**
 * Implement the <code>URLConnection</code> sub-class
 * for handling Inq connections over http.
 * <p>Inq tunneled connections are differentiated from
 * standard http connections by specifying the protocol
 * <code>httpinq://</code> in the URL.
 * <p>
 * This class also handles the creation of encrypted
 * connections, specified by the use of the
 * protocol <code>httpinqs://</code>.
 */
public class HttpInqURLConnection extends InqURLConnection
{
	private int    port_;    // only used if connecting directly
	
	private ChannelDriver readChannelDriver_;
	private ChannelDriver writeChannelDriver_;
	
	private boolean       encrypted_;
	
	private static int defaultPort__ = -1;
	
	/**
   * Create a new <code>HttpInqURLConnection</code> that is not
   * encrypted
   */
	public HttpInqURLConnection(URL url, int port)
	{
    this(url, port, false);
	}
	
	public HttpInqURLConnection(URL url, int port, boolean encrypted)
	{
		super(url);
		port_      = port;
		encrypted_ = encrypted;
		HttpTunnel.setDefaultPort(defaultPort__);
	}

	public ChannelDriver getReadChannelDriver() throws AnyException
	{
		ChannelDriver ret = getWriteChannelDriver();
		if (ret.getSessionId() == null)
		  return ret;
		
		if (readChannelDriver_ == null)
		{
		  readChannelDriver_ = new HttpTunnel(HttpTunnel.GET,
		                                      getURL(),
		                                      port_,
		                                      encrypted_);
		  
		  readChannelDriver_.setSessionId(ret.getSessionId());
		}
		
		return readChannelDriver_;
	}

	public ChannelDriver getWriteChannelDriver() throws AnyException
	{
		if (writeChannelDriver_ == null)
		  writeChannelDriver_ = new HttpTunnel(HttpTunnel.POST,
		                                       getURL(),
		                                       port_,
                                           encrypted_); 
		
		return writeChannelDriver_;
	}

	public void connect() throws IOException
	{
		try
		{
			getWriteChannelDriver();
			getReadChannelDriver();
		}
		catch (AnyException e)
		{
			throw new RuntimeContainedException(e);
		}
		this.connected = true;
	}
	
	public void disconnect() throws AnyException
	{
		this.connected = false;

    // Just close the read side as 1) this method can only
    // be called from the client side and 2) only the read
    // side has any length of life.
    getReadChannelDriver().close();
	}
	
	public boolean isUnreliable()
	{
		return true;
	}

	public boolean isPermanent()
	{
		return false;
	}
	
  static void setDefaultPort(int defaultPort)
  {
    defaultPort__ = defaultPort;
  }
}
