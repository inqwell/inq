/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/inq/LockerProxy.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.io.inq;

import com.inqwell.any.*;

/**
 * An implementation of the <code>Process</code> interface
 * which acts as a representation of a lock holder from
 * another Inq environment.
 * <p>
 * Instances of <code>LockerProxy</code> are created when a
 * <code>Process</code> is transmitted to the local environment
 * via an IoProcess connection.
 */
public class LockerProxy extends    SerializedProcess
{
  private ServerMonitor monitor_;
  
	public LockerProxy(ServerMonitor monitor, Any catalogPath)
	{
    super(catalogPath);
		monitor_ = monitor;
	}

  public void notifyUnlock(Any a)
  {
    monitor_.notifyUnlock(this, a);
  }
}
