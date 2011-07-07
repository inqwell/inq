/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/rmi/Login.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.rmi;

import com.inqwell.any.StringI;
import com.inqwell.any.AnyException;
import com.inqwell.any.channel.ChannelDriver;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Define the remote interface for RTF server login
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */ 
public interface Login extends Remote
{
	/**
	 * Login to the RTF server to which this remote reference is bound.
	 * @param user user name
	 * @param passwd valid password for given user name
	 * @param d a suitable channel driver through which the server
	 * will communicate with the invoking client.
	 */
	public Connection login (StringI     user,
													 StringI     passwd,
													 ChannelDriver d) throws RemoteException,
																									 AnyException;

}
