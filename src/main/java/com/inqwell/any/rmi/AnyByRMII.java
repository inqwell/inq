/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/rmi/AnyByRMII.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.rmi;

import java.rmi.RemoteException;
import com.inqwell.any.channel.ChannelClosedException;
import com.inqwell.any.Func;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;

public class AnyByRMII extends AbstractAnyByRMII
{
	public AnyByRMII(Func f) throws RemoteException
	{
		super(f);
	}
	
	public void doUnreferenced()
  {
  	try
  	{
  	  doSend(new ChannelClosedException("Channel is closing"));
  	}
  	catch (AnyException e)
  	{
  		throw (new RuntimeContainedException(e));
  	}
  }


}
