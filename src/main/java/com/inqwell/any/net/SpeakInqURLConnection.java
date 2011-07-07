/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/SpeakInqURLConnection.java $
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
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import com.inqwell.any.Globals;
import com.inqwell.any.AnyException;
import com.inqwell.any.StartProcessEvent;
import com.inqwell.any.EventConstants;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.io.ResolvingInputStream;
import com.inqwell.any.io.ReplacingOutputStream;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.Serialize;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>speakinq://</code> style URLs.
 * <p>
 * URLs of the style <code>speakinq://host:port</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL can be used to support interfacing between
 * an <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup> environment
 * and external systems.
 */
public class SpeakInqURLConnection extends InqURLConnection
{
	private Socket        socket_;
	
	private ChannelDriver channelDriver_;
	
	private int           port_;
	
	public SpeakInqURLConnection(URL url, int port)
	{
		super(url);
		port_ = port;
	}

	public InputStream getInputStream() throws IOException
	{
		return socket_.getInputStream();
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		return socket_.getOutputStream();
	}
	
	public void connect() throws IOException
	{
    if (!this.connected)
    {
      socket_ = new Socket(getURL().getHost(), port_);
      socket_.setTcpNoDelay(true);
      socket_.setKeepAlive(true);
      socket_.setTrafficClass(0x1c);
      this.connected = true;
      
      // if there is a session id then we are resuming a broken
      // connection, so send the session resume event.
      if (getSessionId() != null)
      {
	      try
	      {
			    getWriteChannelDriver().write
			        (new StartProcessEvent(EventConstants.SESSION_RESUME,
			                               getSessionId()));
	      }
	      catch(AnyException e)
	      {
	      	throw new RuntimeContainedException(e);
	      }
      }
    }
	}
	
	public void disconnect() throws AnyException
	{
    if (!this.connected)
      return;

		this.connected = false;

    try
    {
      InputStream  is = socket_.getInputStream();
      OutputStream os = socket_.getOutputStream();
      is.close();
      os.close();
      socket_.close();
    }
    catch (Exception e)
    {
      AnyException.throwExternalException(e);
    }
	}
	
	public ChannelDriver getReadChannelDriver() throws AnyException
	{
    if (channelDriver_ != null)
      return channelDriver_;

    try
    {
      connect();

      ResolvingInputStream  ois = null;
      ReplacingOutputStream oos = null;

      // Create the o/p stream first to avoid deadlock
      // across the socket while the stream header is
      // written/read.
      try
      {
        //oos = new ReplacingOutputStream(getOutputStream(),
        //oos = new ReplacingOutputStream(new GZIPOutputStream(getOutputStream()),
        int bufsiz = 2 * socket_.getSendBufferSize();
        if (bufsiz < 0)
          bufsiz = 8192;
        oos = new ReplacingOutputStream(new ReplacingOutputStream.SupportingOutputStream(getOutputStream(), bufsiz),
        //oos = new ReplacingOutputStream(new BufferedOutputStream(getOutputStream(), bufsiz),
                                        Globals.channelOutputReplacements__);
        
        // Make sure the serialization header goes in spite of buffering
        oos.flush();
      }
      catch (Exception ooe)
      {
        disconnect();
        AnyException.throwExternalException (ooe);
      }

      try
      {
        //ois = new ResolvingInputStream(getInputStream(),
        //ois = new ResolvingInputStream(new GZIPInputStream(getInputStream()),
        int bufsiz = 2 * socket_.getReceiveBufferSize();
        ois = new ResolvingInputStream(new ResolvingInputStream.SupportingInputStream(getInputStream(), bufsiz),
                                       oos,
        //ois = new ResolvingInputStream(new BufferedInputStream(getInputStream(), bufsiz),
                                       Globals.channelInputReplacements__);
        //oos.setCompressed(true);
        //ois.readAny();  // consume compress response
      }
      catch (Exception oie)
      {
        disconnect();
        oos.close();
        AnyException.throwExternalException (oie);
      }

      channelDriver_ = new Serialize(ois, oos);

      return channelDriver_;
    }
    catch (Exception e)
    {
      AnyException.throwExternalException (e);
      return null;
    }
  }

	public ChannelDriver getWriteChannelDriver() throws AnyException
	{
    return getReadChannelDriver();
  }

  public Socket getSocket()
  {
    return socket_;
  }
  
	public boolean isUnreliable()
	{
		return false;
	}

	public boolean isPermanent()
	{
		return true;
	}
}
