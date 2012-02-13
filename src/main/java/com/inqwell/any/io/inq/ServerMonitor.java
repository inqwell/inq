/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/ServerMonitor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.channel.*;

/**
 * A <code>ServerMonitor</code> executes in a server host that is
 * acting as an I/O provider for another Inq server in a n-tier
 * environment. It processes various event types in order to keep
 * its corresponding client host up to date.
 * <p>
 * <code>IO_READRESULTS</code> events received locally from
 * an <code>IoProcess</code> are processed by placing
 * the results in the local node space.  These objects represent
 * the contents of the i/o cache in the client and any modifications
 * or deletions carried out on them will be propagated in the
 * normal way.
 * <p>
 * When the corresponding <code>ClientMonitor</code> receives
 * notification from its local cache that the object is no
 * longer referenced it sends a <code>IO_CACHEREMOVAL</code> event
 * to the corresponding peer <code>ServerMonitor</code>.
 * <p>
 * <code>IO_DESCRIPTORACK</code> events indicate that a
 * Descriptor has been imported by the client host and future
 * BOT_CREATE, BOT_EXPIRE and BOT_CATALOGED events should be
 * propagated.
 * <p>
 * There is only a single Client/Server monitor pair between
 * two hosts operating in this way.
 */
public class ServerMonitor extends UserProcess
{
  static Array ioReadResultsEventTypes__;
  static Array ioCacheRemovalEventTypes__;
  static Array ioDescriptorAckEventTypes__;
  
  private static Map   monitorProcesses__;
  
  private EventDispatcher ed_;
  
  // The name of the server monitor process as held in monitorProcesses__
  // This is the server name in $catalog.serverName in the client host.
  private Any             name_;
  
  static
  {
    ioReadResultsEventTypes__ = AbstractComposite.array();
    ioReadResultsEventTypes__.add(EventConstants.IO_READRESULTS);

    ioCacheRemovalEventTypes__ = AbstractComposite.array();
    ioCacheRemovalEventTypes__.add(EventConstants.IO_CACHEREMOVAL);

    ioDescriptorAckEventTypes__ = AbstractComposite.array();
    ioDescriptorAckEventTypes__.add(EventConstants.IO_DESCRIPTORACK);
    
    monitorProcesses__ = AbstractComposite.simpleMap();
  }
  
  public static synchronized ServerMonitor getMonitorProcess(Any name)
  {
    return (ServerMonitor)monitorProcesses__.get(name);
  }
  
  public static synchronized void addMonitorProcess(ServerMonitor p,
                                                    Any           name)
  {
    monitorProcesses__.add(name, p);
  }
  
  public static synchronized ServerMonitor removeMonitorProcess(Any name)
  {
    return (ServerMonitor)monitorProcesses__.remove(name);
  }
  
	/**
	 * Starts a server-side monitor process that exchanges events
	 * with its client-side peer to manage cache and object status
	 * in a remote inq i/o environment.
	 */
	public ServerMonitor(Any              name,
                       InputChannel     ic,
                       OutputChannel    oc,
                       ExceptionHandler exceptionHandler,
                       Transaction      transaction,
                       Map              root,
                       EventDispatcher  ed,
                       EventListener    rootListener) throws AnyException
	{
    super(ic,
          oc,
          exceptionHandler,
          transaction,
          root,
          ed,
          rootListener);
          
    // Try adding us to the list of active server monitor processes.
    // This is before the thread is started, so could cause an exception
    // in the ServerListener if there is a duplicate name.
    name_ = name;
    ServerMonitor.addMonitorProcess(this, name);
    
    ed_ = new EventDispatcher();
    Map catalog = Catalog.instance().getCatalog();
    EventGenerator catalogEg = (EventGenerator)catalog;
    catalogEg.addEventListener(ed_);
	}

  public void notifyUnlock(Any a)
  {
    throw new UnsupportedOperationException("notifyUnlock");
  }

  // dedicated method
  void notifyUnlock(Process p, Any a)
  {
    // If we can't tell the client the lock is going then
    // too bad - just proceed anyway.
    Map m = AbstractComposite.simpleMap();
    m.add(EventConstants.LCK_PROCESS, p);
    m.add(EventConstants.LCK_OBJECT,  a);
    
    try
    {
      writeOutput(new SimpleEvent(EventConstants.LCK_UNLOCKNOTIFY,
                                  m));
    }
    catch(Exception e) {}
  }

  protected void initServer(EventListener   rootListener,
                            EventDispatcher ed)
  {
    super.initServer(rootListener, ed);
    // Set up dispatcher for io types
    ed.addEventListener(new ReadResults());
    ed.addEventListener(new CacheRemoval());
    ed.addEventListener(new DescriptorAck());
  }
  
  private void readResults(Map m) throws AnyException
  {
    Map root = getRoot();
    
    Any results = m.get(EventConstants.IO_READRESULTS);
    Any processName = m.get(UserProcess.processName__);
    
    // We only send through non-null read results
    if (results instanceof Array)
    {
      Array array = (Array)results;
      int entries = array.entries();
      for (int i = 0; i < entries; i++)
        inNodeSpace(root, array.get(i)); 
    }
    else
    {
      inNodeSpace(root, results);
    }
    Globals.lockManager__.notifyVia(processName, this, false);
  }

  private void cacheRemoval(Any item) throws AnyException
  {
    Map root = getRoot();
    removeNodeSpace(root, item);
  }
  
  private void descriptorAck(Map m)
  {
    // We only get fully resolved descriptors in here.
    // If its not already dispatching the event type
    // for this descriptor, add it on so that events
    // coming from the catalog are picked up.  Note that
    // we are only passed descriptors that are
    // EventGenerators so this cast should be safe (see
    // IoProcess.fetchDescriptor()
    Descriptor d = (Descriptor)m.get(Descriptor.descriptor__);
    Any processName = m.get(UserProcess.processName__);
    
    EventGenerator dEg = (EventGenerator)d;
    Event e = dEg.makeEvent(EventConstants.BOT_CREATE);
    Map eventType = (Map)e.getId();
    if (!ed_.isDispatching(eventType))
    {
      // Replace the real descriptor with a proxy one.  Ensures
      // no stray references if the real descriptor is recompiled.
      d = new ProxyDescriptor(d.getFQName());
      eventType.replaceItem(Descriptor.descriptor__, d);
      Array createEventTypes = AbstractComposite.array();
      createEventTypes.add(eventType);
      
      // Set up the cataloged and expiry events also
      Array botEventTypes = AbstractComposite.array();
      eventType = (Map)eventType.cloneAny();
      eventType.replaceItem(EventConstants.EVENT_TYPE,
                            EventConstants.BOT_CATALOGED);
      botEventTypes.add(eventType);
      eventType = (Map)eventType.cloneAny();
      
      eventType.replaceItem(EventConstants.EVENT_TYPE,
                            EventConstants.BOT_EXPIRE);
      botEventTypes.add(eventType);
      
      ed_.addEventListener(new BotListener(botEventTypes));
      ed_.addEventListener(new CreateListener(createEventTypes));
    }
    Globals.lockManager__.notifyVia(processName, this, false);
  }

  private void inNodeSpace(Map root, Any a)
  {
    Map m = (Map)a;
    Any u = m.getUniqueKey();
    if (!root.contains(u))
      root.add(u, m);
  }
  
  private void removeNodeSpace(Map root, Any u)
  {
    if (root.contains(u))
      root.remove(u);
  }
  
  // Inner class event processors
  
  private class ReadResults extends    AbstractAny
                            implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$ReadResults.processEvent");

      readResults((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioReadResultsEventTypes__;
    }
  }
  
  private class CacheRemoval extends    AbstractAny
                             implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$CacheRemoval.processEvent");

      // The unique key of the item removed from the client
      // cache is sent through to us, the server
      cacheRemoval(e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioCacheRemovalEventTypes__;
    }
  }
  
  private class DescriptorAck extends    AbstractAny
                              implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$DescriptorAck.processEvent");

      descriptorAck((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioDescriptorAckEventTypes__;
    }
  }
  
  // Picks up BOT_EXPIRE and BOT_CATALOG events and
  // forwards them to the client monitor peer.  One instance is
  // created per BOT of interest.
  private class BotListener extends    AbstractAny
                            implements EventListener
  {
    private Array eventTypes_;
    
    BotListener(Array eventTypes)
    {
      eventTypes_ = eventTypes;
    }
    
    public boolean processEvent(Event e) throws AnyException
    {
      ServerMonitor.this.writeOutput(e);
      return true;
    }
    
    public Array getDesiredEventTypes()
    {
      return eventTypes_;
    }
  }
  
  // Picks up BOT_CREATE event, enters them into local
  // node space and forwards them to the client monitor peer.
  // One instance is created per BOT of interest.
  private class CreateListener extends    AbstractAny
                               implements EventListener
  {
    private Array eventTypes_;
    
    CreateListener(Array eventTypes)
    {
      eventTypes_ = eventTypes;
    }
    
    public boolean processEvent(Event e) throws AnyException
    {
      inNodeSpace(getRoot(), e.getContext());
      ServerMonitor.this.writeOutput(e);
      return true;
    }
    
    public Array getDesiredEventTypes()
    {
      return eventTypes_;
    }
  }
}
