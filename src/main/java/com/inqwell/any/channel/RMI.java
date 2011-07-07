/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/RMI.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.Composite;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.rmi.AnyByRMI;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.ExceptionContainer;
import java.rmi.RemoteException;
import java.io.Serializable;


/**
 * A channel driver that uses a com.inqwell.any.rmi.AnyByRMI transport object
 * to send Any structures to processes in remote JVMs.
 * <p>
 * Only the <code>write()</code> method is supported.  The
 * <code>read()</code> throws an UnsupportedOperationException.
 * If the sending process wishes to receive responses from
 * the remote JVM it must construct an <code>RMI</code> object
 * with a <code>AnyByRMII</code> and export the driver to
 * that JVM.
 * <p>
 * Classes within the structure to be written may, of course,
 * impose serialization semantics of their own design but this
 * class ensures that any <code>parent</code> references from
 * <code>Composite</code> implementations are cleared from the
 * root of the structure being transported, so that parts of
 * structures can safely be written.  This handling is thread-safe.
 */
public class RMI extends    AbstractChannelDriver
								 implements ChannelDriver,
														Serializable
{
	private AnyByRMI rmi_;
	
	public RMI(AnyByRMI rmi)
	{
	  rmi_ = rmi;
  }
	
  public Any read() throws AnyException
  {
    throw (new UnsupportedOperationException());
	}
    
  public void write(Any a) throws AnyException
  {
    try
    {
			if (a instanceof ExceptionContainer)
			{
				rmi_.send(((ExceptionContainer)a).collapseException());
			}
			else
			{
				rmi_.send(a);
			}
    }
    catch (RemoteException e)
    {
      throw (new ContainedException(e));
    }
  }
  
  public void purgeReceived()
  {
  }
  
	public boolean isEmpty()
	{
	  // read is not supported anyway.
	  return true;
	}
	
  public boolean isFull()
  {
    // A remote channel is never deemed to be full
    return false;
  }

  protected void doClose() throws AnyException
  {
	}

	/**
	 * Define cloning.  In fact, this class is immutable and only contains
	 * a remote object.  Therefore cloning is not relevant and we just return
	 * <i>this</i>.  Cloning must be defined, however, as these objects may be
	 * transmitted to remote processes through channels, which are at liberty
	 * to clone what they transport, if they so wish.
	 */  
  public Object clone()
  {
		return this;
  }
}
