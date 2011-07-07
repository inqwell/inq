/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/InqIoManager.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.channel.*;
import com.inqwell.any.Process;
import com.inqwell.any.io.IoConstants;

/*
 * Manages instances of InqIo so that they can be allocated to consumer
 * threads who sometime later put them back.  InqIo instances are not
 * reentrant yet represent the limited resource of connections to a
 * remote server providing I/O services.
 * <p>
 *
 * $Archive: /src/com/inqwell/any/io/inq/InqIoManager.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
public class InqIoManager extends    AbstractResourceAllocator
                          implements ResourceAllocator
{
	private static InqIoManager theInstance__ = null;

  private Map    monitorProcesses_;
  private Map    domainAgents_;

	public static InqIoManager instance()
	{
		if (theInstance__ == null)
		{
			synchronized (InqIoManager.class)
			{
				if (theInstance__ == null)
					theInstance__ = new InqIoManager();
			}
		}
		return theInstance__;
	}

	private InqIoManager()
	{
    init();
  }

	public static void addInqServer(Any id, Any spec, IntI limit)
	{
		System.out.println ("inside addInqServer " + limit + " " + id + " " + spec);
		InqIoManager.instance().addSpec(id, spec, limit);
		System.out.println ("leaving addInqServer");
	}
	
	public DomainAgent getDomainAgent(Any id)
	{
    return (DomainAgent)domainAgents_.get(id);
  }

	protected Any makeNewResource(Any id, Any spec, int made) throws AnyException
	{
		Map m = (Map)spec;
		Any user    = m.get(IoConstants.user__);
		Any passwd  = m.get(IoConstants.passwd__);
		Any url     = m.get(IoConstants.url__);
    Any pkg     = m.get(UserProcess.package__);

    synchronized(monitorProcesses_)
    {
      if (!monitorProcesses_.contains(id))
      {
        // Making the first connection to the given specification.
        // Start the client (and thus server) monitor processes
        monitorProcesses_.add(id, startMonitor(id, url));
        domainAgents_.add(id, new RemoteDomainAgent(id));
      }
    }

		InqIo inqIo = new InqIo (url,
														 user,
														 passwd,
                             pkg);

		return inqIo;
	}

	protected void afterAcquire(Any resource)
	{
	}

  protected boolean beforeAcquire(Any resource)
  {
    return true;
  }
  
  // Return true if ok to put back in the pool, false if not.
	protected boolean beforeRelease(Any resource,
                                  Any arg,
                                  ExceptionContainer e)
	{
    InqIo inqIo = (InqIo)resource;

		return !inqIo.isClosed();
	}

	protected void disposeResource(Any resource)
	{
    InqIo inqIo = (InqIo)resource;
    inqIo.close();
	}

  private Process startMonitor(Any id, Any url) throws AnyException
  {
    AnyChannel initResponse = new AnyChannel(new FIFO(0,
                                             ChannelConstants.REFERENCE));

    // Create the process.  It responds by sending a message to
    // the given channel as to OK, denied, connect fail.
    //Process p = new ClientMonitor();

    Any resp = initResponse.read();
    return null;
  }

	private void init()
	{
    monitorProcesses_ = AbstractComposite.simpleMap();
    domainAgents_     = AbstractComposite.simpleMap();
	}
}
