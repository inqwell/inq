/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/rmi/ConnectionI.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.rmi;

import com.inqwell.any.*;
import com.inqwell.any.server.StartUserProcess;
import com.inqwell.any.server.StartRouterProcess;
import com.inqwell.any.channel.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.rmi.RemoteException;

/**
 * Implementation of the remote interface for RTF server connection
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.rmi.Connection
 */ 
public class ConnectionI extends    UnicastRemoteObject
												 implements Connection,
																		Unreferenced
{
  // A connection implies a process to service client requests
  private com.inqwell.any.Process process_;
  
  // Our process's input channel...
  private AnyChannel      c_;
  
  // ...and the driver we pass back to the client connected to it
  private RMI             driver_;
  
	static private EventDispatcher  ed_;
	
  static
  {
		ed_ = new EventDispatcher();
		ed_.addEventListener(new StartUserProcess());
		ed_.addEventListener(new StartRouterProcess());
  }

	public ConnectionI (StringI user,
											StringI passwd,
											ChannelDriver d) throws RemoteException,
																							AnyException
	{
		super();
		init (user, passwd, d);
	}
	
	public void disconnect() throws RemoteException
	{
    // kill the process
	}
	
	public ChannelDriver getChannelDriver() throws RemoteException
	{
	  return driver_;
	}
	
  public void unreferenced()
  {
		System.out.println ("Unreferenced");
    // kill the process
  }

	private void init(StringI     user,
										StringI     passwd,
										ChannelDriver d) throws RemoteException,
																						AnyException
	{
	  c_ = new AnyChannel(new FIFO(0, ChannelConstants.REFERENCE));
	  
	  OutputChannel oc = c_;
	  ToChannel toC = new ToChannel(oc);
	  
	  // Make our channel.  Connect an RMI driver
	  driver_ = new RMI(new AnyByRMII(toC));
	  
	  OutputChannel toClient = new AnyChannel(d);

	  // Read the event from the input channel which tells us
	  // what kind of process we will start
	  StartProcessEvent spe = (StartProcessEvent)c_.read();
	  spe.setInputChannel(c_);
	  spe.setOutputChannel(toClient);
	  
	  try
	  {
			ed_.processEvent(spe);
    }
    catch (AnyException e)
    {
    	toClient.close();
    }
	}
}
