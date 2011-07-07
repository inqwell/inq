/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/InqURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */


package com.inqwell.any.net;

import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.channel.ChannelDriver;

/**
 * Base class for Inq environment URL connections
 */
public abstract class InqURLConnection extends URLConnection
{
	private Any              sessionId_;

	public InqURLConnection(URL url)
	{
		super(url);

		// Set up base class protected fields
		setDoInput(true);
		setDoOutput(true);
		setIfModifiedSince(0);
		setUseCaches(false);
		setAllowUserInteraction(false);
		this.connected = false;
	}

  /**
   * Disconnect from the resource this InqURLConnection represents.
   * The connection may be re-established using <code>connect()</code>
   */
	public abstract void disconnect() throws AnyException;

  /**
   * Get a <code>ChannelDriver</code> that is suitable for
   * channel-based communication with the resource represented
   * by this InqURLConnection.
   */
	public abstract ChannelDriver getReadChannelDriver() throws AnyException;

	public abstract ChannelDriver getWriteChannelDriver() throws AnyException;

	public abstract boolean isUnreliable();

	public abstract boolean isPermanent();

	public String getContentType()
	{
		return "application/octet-stream";
	}

  public void setTrusted(Any toTrust)
  {
  }

  public Any getTrusted()
  {
    return null;
  }

	public boolean getDefaultUseCaches()
	{
		return false;
	}

  public void setSessionId(Any sessionId)
  {
    sessionId_ = sessionId;
  }

  public Any getSessionId()
  {
    return sessionId_;
  }
  
  public Socket getSocket()
  {
    throw new UnsupportedOperationException();
  }
}
