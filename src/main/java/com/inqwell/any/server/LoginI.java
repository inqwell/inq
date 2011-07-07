/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/LoginI.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import com.inqwell.any.StringI;
import com.inqwell.any.AnyException;
import com.inqwell.any.rmi.Connection;
import com.inqwell.any.rmi.ConnectionI;
import com.inqwell.any.rmi.Login;
import com.inqwell.any.channel.ChannelDriver;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.rmi.RemoteException;

/**
 * Implementation of the remote interface for RTF server login
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.rmi.Login
 */ 
public class LoginI extends    UnicastRemoteObject
										implements Login //,
															 //Unreferenced
{
	public LoginI () throws RemoteException
	{
		super();
	}
	
	public Connection login (StringI       user,
													 StringI       passwd,
													 ChannelDriver d) throws RemoteException,
																									 AnyException
	{
		System.out.println ("User: " + user);
		System.out.println (" Pwd: " + passwd);
		
		return new ConnectionI(user, passwd, d);
	}
	
//	public void unreferenced ()
//	{
//		System.out.println ("Unreferenced: " + toString());
//	}
}
