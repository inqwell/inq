/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/IoProcess.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.client.ClientDescriptor;
import com.inqwell.any.server.BOTDescriptor;
import com.inqwell.any.channel.*;

/**
 * Perform I/O on behalf of a connected client server. 
 */
public class IoProcess extends UserProcess
{
  static Array ioReadRequestEventTypes__;
  
  static private Array ioFetchDescriptorEventTypes__;
  static private Array ioWriteRequestEventTypes__;
  static private Array ioDeleteRequestEventTypes__;
  static private Array ioLockRequestEventTypes__;
  
  private LocateNode ln_ = new LocateNode();
  
  // The process with which all retrieved instances are lodged
  // in order that events may be returned to the client host.
  // One monitor process may be used by several i/o processes.
  private UserProcess monitorProcess_;
  
  private Any         processName_;
  
  static
  {
    ioReadRequestEventTypes__ = AbstractComposite.array();
    ioReadRequestEventTypes__.add(EventConstants.IO_READREQUEST);

    ioFetchDescriptorEventTypes__ = AbstractComposite.array();
    ioFetchDescriptorEventTypes__.add(EventConstants.IO_FETCHDESCRIPTOR);

    ioWriteRequestEventTypes__ = AbstractComposite.array();
    ioWriteRequestEventTypes__.add(EventConstants.IO_WRITEREQUEST);

    ioDeleteRequestEventTypes__ = AbstractComposite.array();
    ioDeleteRequestEventTypes__.add(EventConstants.IO_DELETEREQUEST);

    ioLockRequestEventTypes__ = AbstractComposite.array();
    ioLockRequestEventTypes__.add(EventConstants.LCK_LOCKREQUEST);
  }

	/**
	 * Starts a server-side i/o process that is connected via
	 * channels to a server client.
	 */
	public IoProcess(InputChannel     ic,
                   OutputChannel    oc,
                   ExceptionHandler exceptionHandler,
                   Transaction      transaction,
                   Map              root,
                   EventDispatcher  ed,
                   UserProcess      monitorProcess) throws AnyException
	{
    super(ic,
          oc,
          exceptionHandler,
          transaction,
          root,
          ed,
          null);
    
    monitorProcess_ = monitorProcess;
    ln_.setTransaction(transaction);
	}

  protected void initServer(EventListener   rootListener,
                            EventDispatcher ed)
  {
    super.initServer(rootListener, ed);
    // Set up dispatcher for io types
    ed.addEventListener(new ReadRequest());
    ed.addEventListener(new WriteRequest());
    ed.addEventListener(new FetchDescriptor());
    ed.addEventListener(new LockRequest());
  }
  
  private void readRequest(Map m) throws AnyException
  {
    Descriptor d = (Descriptor)m.get(Descriptor.descriptor__);
    Map        v = (Map)m.get(KeyDef.key__);
    
    Any ret = d.read(this, v, 0);
    
    SimpleEvent e = new SimpleEvent(EventConstants.IO_READRESULTS,
                                    ret);

    if (processName_ == null)
      processName_ = this.get(processName__);
    
    boolean got = false;
    
    // Check if we read anything.  If not, no need to liaise
    // with the monitor process.
    Array a = null;
    if (ret != null)
    {
      if (ret instanceof Array)
      {
        a = (Array)ret;
        if (a.entries() != 0)
          got = true;
      }
      else
        got = true;
    }
    
    // Send any read results to the monitor process
    if (got)
    {
      // Using the inq lock manager, wait for a signal from the
      // monitor process that it has lodged the read results into
      // its local node space for event propagation to the client
      // server before sending back the results themselves.  Note
      // that the local read will have given us managed (identity
      // semantics) objects, which is what we want for this
      // purpose.  However, when sending the results back to the
      // client host we only need simple maps with no descriptors
      // in them, as the desired map type and local descriptor is
      // under the control of the client itself.
      Globals.lockManager__.lock(this, processName_);
      Map map = AbstractComposite.simpleMap();
      map.add(processName__, processName_);
      map.add(EventConstants.IO_READRESULTS, ret);
      monitorProcess_.send(new SimpleEvent(EventConstants.IO_READRESULTS,
                                           map));
      
      Globals.lockManager__.waitFor(this, processName_, 0);
      Globals.lockManager__.unlock(this, processName_);
    }
    
    writeOutput(e);
  }
  
  private void writeRequest(Map m) throws AnyException
  {
    Descriptor d = (Descriptor)m.get(Descriptor.descriptor__);
    Map        i = (Map)m.get(ServerConstants.IO_ITEM);
    
    d.write(this, i);
    
    // Descriptor.write() is not defined as returning anything, so
    // just assume OK here or that an exception occurred.
    writeOutput(new SimpleEvent(EventConstants.IO_WRITEACK,
                                AnyBoolean.TRUE));
  }
  
  private void deleteRequest(Map m) throws AnyException
  {
    Descriptor d = (Descriptor)m.get(Descriptor.descriptor__);
    Map        i = (Map)m.get(ServerConstants.IO_ITEM);
    
    d.delete(this, i);
    
    // Descriptor.write() is not defined as returning anything, so
    // just assume OK here or that an exception occurred.
    writeOutput(new SimpleEvent(EventConstants.IO_DELETEACK,
                                AnyBoolean.TRUE));
  }
  
  private void lockRequest(Map m) throws AnyException
  {
    Process p = (Process)m.get(EventConstants.LCK_PROCESS);
    Any     a = m.get(EventConstants.LCK_OBJECT);
    LongI   t = (LongI)m.get(EventConstants.LCK_TIMEOUT);
    
    p = Globals.lockManager__.lockOrEnterWait(p, a, t.getValue());
    
    // Send response.  We return the locking process - it may be
    // the same as the supplied process (if we got the lock) or
    // it will be the process already holding the lock.  If a
    // timeout of < 0 or > 0 was specified then, if the lock
    // could not be obtained, a lock-wait entry will be made
    // for the given process.
    writeOutput(new SimpleEvent(EventConstants.LCK_LOCKACK,
                                p));
  }
  
  private void unlockRequest(Map m) throws AnyException
  {
    Process p = (Process)m.get(EventConstants.LCK_PROCESS);
    Any     a = m.get(EventConstants.LCK_OBJECT);
    
    Globals.lockManager__.unlock(p, a);
    
    // Send response.
    writeOutput(new SimpleEvent(EventConstants.LCK_UNLOCKACK,
                                p));
  }
  
  private void fetchDescriptor(Any fQName) throws AnyException
  {
    ln_.setNodePath(fQName);
    BOTDescriptor ret = (BOTDescriptor)ln_.exec(Catalog.instance().getCatalog());
    
    // Don't return if not fully resolved.  If OK then we send back
    // the descriptor.
    if (ret != null && !ret.isResolved(null))
      ret = null;

    Event e = new SimpleEvent(EventConstants.IO_DESCRIPTORACK,
                              new AsIsDescriptorDecor(ret));

    if (processName_ == null)
      processName_ = this.get(processName__);
    
    // We tell the monitor process about the descriptors
    // that have been returned to the client so that it
    // can subscribe to creation events and notify the
    // client when new instances are created.
    if (ret != null && (ret instanceof EventGenerator))
    {
      Globals.lockManager__.lock(this, processName_);
      Map m = AbstractComposite.simpleMap();
      m.add(processName__, processName_);
      m.add(Descriptor.descriptor__, ret);
      monitorProcess_.send(new SimpleEvent(EventConstants.IO_DESCRIPTORACK,
                                           m));
      Globals.lockManager__.waitFor(this, processName_, 0);
      Globals.lockManager__.unlock(this, processName_);
    }
      
    writeOutput(e);
  }
  
  // Inner class event processors
  
  private class ReadRequest extends    AbstractAny
                            implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$ReadRequest.processEvent");

      readRequest((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioReadRequestEventTypes__;
    }
  }

  private class WriteRequest extends    AbstractAny
                             implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$WriteRequest.processEvent");

      writeRequest((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioWriteRequestEventTypes__;
    }
  }

  private class DeleteRequest extends    AbstractAny
                              implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$DeleteRequest.processEvent");

      deleteRequest((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioWriteRequestEventTypes__;
    }
  }

  private class LockRequest extends    AbstractAny
                            implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$LockRequest.processEvent");

      lockRequest((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioLockRequestEventTypes__;
    }
  }

  private class UnlockRequest extends    AbstractAny
                              implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$UnlockRequest.processEvent");

      unlockRequest((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioLockRequestEventTypes__;
    }
  }

  private class FetchDescriptor extends    AbstractAny
                                implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("IoProcess$FetchDescriptor.processEvent");

      fetchDescriptor(e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ioWriteRequestEventTypes__;
    }
  }
}
