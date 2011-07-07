/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Accept.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

import com.inqwell.any.io.PhysicalIO;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Accept incoming connections on the given AnyServerSocket and
 * put streams from the accepted socket on to the given
 * PhysicalIO.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Accept extends    AbstractFunc
                    implements Cloneable
{
	private Any  serverSocket_;
  private Any  io_;

	public Accept(Any serverSocket, Any io)
	{
    serverSocket_ = serverSocket;
    io_           = io;
	}

	public Any exec(Any a) throws AnyException
	{
		AnyServerSocket serverSocket    = (AnyServerSocket)EvalExpr.evalFunc
                                           (getTransaction(),
                                            a,
                                            serverSocket_,
                                            AnyServerSocket.class);

		PhysicalIO io                   = (PhysicalIO)EvalExpr.evalFunc
                                           (getTransaction(),
                                            a,
                                            io_,
                                            PhysicalIO.class);

    if (serverSocket == null)
      throw new AnyException("Could not resolve server socket " + serverSocket_);

    if (io == null)
      throw new AnyException("Could not resolve io " + io_);
    
    accept(serverSocket, io);
    
    return io;
	}

  private void accept(AnyServerSocket serverSocket, PhysicalIO io) throws AnyException
  {
    ServerSocket ss = serverSocket.getServerSocket();
    Socket       s  = null;
    
    try
    {
      s = ss.accept();
      InputStream  is = s.getInputStream();
      OutputStream os = s.getOutputStream();
      io.setStreams(is, os);
    }
    catch (Exception e)
    {
      throw new ContainedException(e);
    }
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Accept a = (Accept)super.clone();

    a.serverSocket_ = serverSocket_.cloneAny();
    a.io_           = io_.cloneAny();

    return a;
  }
}
