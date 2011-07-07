/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/rmi/AnyByRMI.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;

/**
 * Transport of Anys by RMI.  A simple use of RMI to enable their sending
 * between JVMs.
 */
public interface AnyByRMI extends Remote
{
	public Any send (Any a) throws RemoteException, AnyException;
}
