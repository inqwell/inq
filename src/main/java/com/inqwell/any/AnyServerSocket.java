/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyServerSocket.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.io.NullIO;
import java.net.ServerSocket;
import java.io.IOException;

/**
 * Encapsulate a server socket as an Any type. Instances of
 * this type are acceptable as the first operand to Accept.
 * The port to bind to is specified by a once-only assignment
 * from an integer.
 * <p>
 * This class extends <code>NullIO</code> just so
 * that it is acceptable to <code>close</code>.
 */
public class AnyServerSocket extends NullIO
{
  private ServerSocket ss_;
  
	public AnyServerSocket()
	{
	}
  
	/**
	 */
	public AnyServerSocket(int port)
	{
    initialise(port);    
	}

	public ServerSocket getServerSocket()
	{
    if (ss_ == null)
      throw new IllegalStateException("ServerSocket not bound");
      
		return ss_;
	}

	protected void setPort(int port)
	{
    socketAlreadyInitialised();
    initialise(port);
	}

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (a instanceof IntI)
      {
        IntI i = (IntI)a;
        setPort(i.getValue());
      }
      else
      {
        throw new IllegalArgumentException("Must copy from IntI (to specify port)");
      }
		}
    return this;
  }

	public void close()
	{
    try
    {
      ss_.close();
    }
    catch (IOException e)
    {
      throw new RuntimeContainedException(e);
    }
	}

  public Object clone() throws CloneNotSupportedException
  {
    AnyServerSocket ss = (AnyServerSocket)super.clone();
    ss.ss_ = null;
    return ss;
  }
  
  private void initialise(int port)
  {
    try
    {
      ss_ = new ServerSocket(port);
    }
    catch (Exception e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  private void socketAlreadyInitialised()
  {
    if (ss_ != null)
      throw new IllegalArgumentException("Socket is already bound to port " + ss_.getInetAddress());
  }
}
