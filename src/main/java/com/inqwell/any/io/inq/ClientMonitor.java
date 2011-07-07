/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/ClientMonitor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.channel.*;
import java.net.URL;

/**
 * A <code>ClientMonitor</code> executes in a server host that is
 * acting as a client of another Inq server in a n-tier
 * environment.  The following events can be received from the
 * peer process in the i/o server host:
 * <ul>
 * <li><code>BOT_CREATE</code> enters the new instance as a managed
 * object in the local cache. Raises the corresponding event in this
 * host.</li>
 * <li><code>BOT_EXPIRE</code> expires the local BOT.</li>
 * <li><code>BOT_CATALOGED</code> indicates that the Descriptor has
 * been recompiled in the i/o server host. Will always be preceded
 * by a <code>BOT_EXPIRE</code> event on the old Descriptor.  The
 * new Descriptor will become current.</li>
 * <li><code>BOT_UPDATE</code> modified BOT is copied to locally
 * cached instance and the corresponding event is raised in this
 * host.</li>
 * <li><code>BOT_DELETE</code> deleted BOT is removed from the
 * local cache and the corresponding event is raised in this
 * host.</li>
 * </ul>
 * In addition to these events the local cache informs the
 * ClientMonitor when objects are ejected so that the peer
 * ServerMonitor can be notified that we no longer have an interest
 * in receiving future update and delete events on the specified
 * instance.
 */
public class ClientMonitor extends UserProcess
{
  static Array ioReadResultsEventTypes__;
  static Array ioCacheRemovalEventTypes__;

  private OutputChannel initResponse_;
  private Any           spec_;

  static
  {
    ioCacheRemovalEventTypes__ = AbstractComposite.array();
    ioCacheRemovalEventTypes__.add(EventConstants.IO_CACHEREMOVAL);
  }

	/**
	 *
	 */
	public ClientMonitor(Any              spec,
                       ExceptionHandler exceptionHandler,
                       Transaction      transaction,
                       Map              root,
                       EventDispatcher  connectedDispatcher,
                       EventDispatcher  disconnectedDispatcher,
                       OutputChannel    initResponse) throws AnyException
	{
    super(exceptionHandler,
          transaction,
          root,
          connectedDispatcher,
          disconnectedDispatcher,
          null);

    initResponse_ = initResponse;
    spec_         = spec;
	}

  protected void initClient(EventDispatcher connectedDispatcher) throws AnyException
  {
    super.initClient(connectedDispatcher);
  }

  protected Any getStartEventType()
  {
    return EventConstants.START_MONITORPROCESS;
  }

  protected void connectError(AnyException e)
  {
    try
    {
      initResponse_.write(e);
    }
    catch (AnyException ex) {}
  }

  protected void serverLost()
  {
    try
    {
      InqIoManager.instance().deleteAll(spec_);
    }
    catch (AnyException ex) {}
  }

  private void update (Map m)
  {
    // 
  }
  
  private abstract class MonitorEvents extends    AbstractAny
                                       implements EventListener
  {
    private Array eventTypes_ = AbstractComposite.array();

    public Array getDesiredEventTypes()
    {
      return eventTypes_;
    }

    public void addEventType(Any eventType)
    {
      eventTypes_.add(eventType);
    }
  }

  private class Update extends    MonitorEvents
                       implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("ClientMonitor$Update.processEvent");

      //updateEvent((Map)e.getContext());
      return true;
    }
  }

  private class UnlockNotify extends    AbstractAny
                             implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("ClientMonitor$UnlockNotify.processEvent");

      //unlockRequest((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return null; //LCK_UNLOCKNOTIFYioLockRequestEventTypes__;
    }
  }


}
