/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/NativeProtocolHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.AnyException;
import com.inqwell.any.Globals;
import com.inqwell.any.io.ResolvingInputStream;
import com.inqwell.any.io.ReplacingOutputStream;

public abstract class NativeProtocolHandler extends    AbstractProtocolHandler
                                            implements ProtocolHandler
{
	/**
   * Handle a new connection on the <code>speakinq://</code> protocol.
   * The <code>speakinq://</code> protocol assumes connections are
   * never broken or that, if they are, they will never be recovered and
   * the server process will terminate. Thus, this method always starts
   * a new process.
   */
	public void handleConnect(java.net.Socket s) throws AnyException
	{
    InputStream           is  = null;
    OutputStream          os  = null;
    ResolvingInputStream  ois = null;
    ReplacingOutputStream oos = null;
  	  
    try
    {
      s.setTcpNoDelay(true);
      s.setKeepAlive(true);
      s.setTrafficClass(0x1c);

      os = s.getOutputStream();
      //oos = new ReplacingOutputStream(os,
      //oos = new ReplacingOutputStream(new GZIPOutputStream(os),
      int bufsiz = 2 * s.getSendBufferSize();
      if (bufsiz < 0)
        bufsiz = 8192;
      
      oos = new ReplacingOutputStream(new ReplacingOutputStream.SupportingOutputStream(os, bufsiz),
      //oos = new ReplacingOutputStream(new BufferedOutputStream(os, bufsiz),
                                      Globals.channelOutputReplacements__);
      oos.flush();

      is = s.getInputStream();
      //ois  = new ResolvingInputStream(is,
      //ois  = new ResolvingInputStream(new GZIPInputStream(is),
      bufsiz = 2 * s.getReceiveBufferSize();
      ois  = new ResolvingInputStream(new ResolvingInputStream.SupportingInputStream(is, bufsiz),
                                      oos,
      //ois  = new ResolvingInputStream(new BufferedInputStream(is, bufsiz),
                                      Globals.channelInputReplacements__);	
                                      
      //oos.setCompressed(true);
      //ois.readAny();  // consume compress ack
      
      // If we get here then we have open i/o streams on the accepted
      // socket.
      Socket cd = new Socket(s, ois, oos);

      startUserProcess(cd);
    }
    catch (Exception e)
    {
      AnyException.throwExternalException(e);
    }
	}
}
