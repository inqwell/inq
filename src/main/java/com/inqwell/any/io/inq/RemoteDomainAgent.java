/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/inq/RemoteDomainAgent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/*
 * The <code>DomainAgent</code> for objects implemented on remote
 * Inq hosts.
 * <p>
 * $Archive: /src/com/inqwell/any/io/inq/RemoteDomainAgent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
public class RemoteDomainAgent extends    AbstractAny
                               implements DomainAgent
{
  private Any resourceId_;

  RemoteDomainAgent(Any resourceId)
  {
    resourceId_ = resourceId;
  }
  
  public Any getHostId()
  {
    return resourceId_;
  }
  
  public void commit(Transaction t) throws AnyException
  {
		InqIo inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
		inqIo.commitRequest(t);
  }

	public Process getLockMandate(Process p, Any a, long timeout) throws AnyException
	{
		InqIo inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
		return inqIo.lockRequest(p, a, new ConstLong(timeout));
	}
	
	public void release(Process p, Any a)
	{
    try
    {
      InqIo inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
      inqIo.unlockRequest(p, a);
    }
    catch (Exception e) {}
	}
}
