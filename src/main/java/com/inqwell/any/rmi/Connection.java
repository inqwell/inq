/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/rmi/Connection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.rmi;

import com.inqwell.any.Any;
import com.inqwell.any.channel.ChannelDriver;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The remote interface for a client connection to the RTF server using RMI.
 * Instances are created and exported via the com.inqwell.any.rmi.Login instance,
 * representing the RTF server bootstrap.
 * <p>
 * One instance is created per invocation of <code>Login.login()</code>.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */ 
public interface Connection extends Remote
{
	/**
	 * Discards any resources allocated to the connected client and
	 * terminates the client session.
	 */
	public void disconnect() throws RemoteException;
	
	/**
	 * Obtain the channel driver object to support <code>write()</code>
	 * operations to the process set up in the server for this connection.
	 * Note that the <code>read()</code> method is not supported on
	 * the channel driver returned by this method.  It is an output only
	 * driver and should be propagated to users in the client as an
	 * com.inqwell.any.OutputChannel interface.
	 */
	public ChannelDriver getChannelDriver() throws RemoteException;
}
