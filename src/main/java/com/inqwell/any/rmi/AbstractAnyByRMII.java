/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/rmi/AbstractAnyByRMII.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.rmi;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.rmi.RemoteException;
import com.inqwell.any.Func;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;

/**
 * Transport of Anys by RMI.  A simple use of RMI to enable their sending
 * between JVMs.  In this, the implementation, the received Any is passed
 * to the given function object for whatever processing is appropriate.
 */
public abstract class AbstractAnyByRMII extends    UnicastRemoteObject
											                 implements AnyByRMI,
																	                Unreferenced
{
	transient private Func func_;

	public AbstractAnyByRMII(Func f) throws RemoteException
	{
		super();
		func_ = f;
	}
	
	public Any send (Any a) throws RemoteException, AnyException
	{
		//System.out.println ("Got some data: " + a.toString());
		return doSend(a);
	}
	
	public void unreferenced()
  {
		System.out.println ("Unreferenced");
  	doUnreferenced();
  }

  protected abstract void doUnreferenced();
  
  protected Any doSend(Any a) throws AnyException
  {
		Any ret = null;
		if (func_ != null)
		{
			ret = func_.exec (a);
		}
		return ret;
  }
}
