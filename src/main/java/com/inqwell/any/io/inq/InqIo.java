/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/InqIo.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.channel.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Connects to a remote Inq server and starts an IO process in that
 * server.  Supports I/O
 */
public class InqIo extends AbstractAny
{
  // Our connection to the remote host
  private OutputChannel   oc_;
  private InputChannel    ic_;
  
  private EventDispatcher ed_;
  
  // Just keep for debug purposes
  private Any             host_;
  
  InqIo(Any host, Any user, Any passwd, Any pkg) throws AnyException
  {
    host_ = host;
    ed_   = new EventDispatcher();
    ed_.addEventListener(new LoginOK());
    ed_.addEventListener(new LoginDenied());
    open(host, user, passwd, pkg);      
  }
  
  public Descriptor fetchDescriptor(Any fQName) throws AnyException
  {
    Event e = new SimpleEvent(EventConstants.IO_FETCHDESCRIPTOR,
                              fQName);
    oc_.write(e);
    oc_.flushOutput();
    Descriptor ret = (Descriptor)processResponse();
    return ret;
  }
  
  /**
   * Invoke a remote read request.  We don't mandate what Map
   * prototype will be used here, we leave that to the caller on this,
   * the client side.
   * <p>
   * By convention the descriptor is part of the ioKey mappings, for the
   * purposes of object locking, but we don't assume this and pass it
   * explicitly.
   * <p>
   * @return The read result from the remote server.  May be a single
   * instance or an Array, if read operation returned multiple instances.
   */
  public Any read(Descriptor d, Map ioKey) throws AnyException
  {
    Map m = AbstractComposite.simpleMap();
    m.add(Descriptor.descriptor__, d);
    m.add(KeyDef.key__, ioKey);
    
    Event e = new SimpleEvent(EventConstants.IO_READREQUEST,
                              m);
    oc_.write(e);
    oc_.flushOutput();
    
    return processResponse();
  }
  
  public boolean write (Descriptor d,
                        //Map        ioKey,
                        Map        outputItem) throws AnyException
  {
    Map m = AbstractComposite.simpleMap();
    m.add(Descriptor.descriptor__, d);
    //m.add(KeyDef.key__, ioKey);
    m.add(ServerConstants.IO_ITEM, outputItem);
    
    Event e = new SimpleEvent(EventConstants.IO_WRITEREQUEST,
                              m);
    oc_.write(e);
    oc_.flushOutput();
    
    BooleanI ret = (BooleanI)processResponse();
    
    return ret.getValue();
    
  }
  
  public boolean delete (Descriptor d,
                         //Map        ioKey,
                         Map        outputItem) throws AnyException
  {
    Map m = AbstractComposite.simpleMap();
    m.add(Descriptor.descriptor__, d);
    //m.add(KeyDef.key__, ioKey);
    m.add(ServerConstants.IO_ITEM, outputItem);
    
    Event e = new SimpleEvent(EventConstants.IO_DELETEREQUEST,
                              m);
    oc_.write(e);
    oc_.flushOutput();
    
    BooleanI ret = (BooleanI)processResponse();
    
    return ret.getValue();
    
  }
  
  public Process lockRequest (Process p,
                              Any     a,
                              LongI   timeout) throws AnyException
  {
    Map m = AbstractComposite.simpleMap();
    m.add(EventConstants.LCK_PROCESS, p);
    m.add(EventConstants.LCK_OBJECT,  a);
    m.add(EventConstants.LCK_TIMEOUT, timeout);
    
    Event e = new SimpleEvent(EventConstants.LCK_LOCKREQUEST,
                              m);
    oc_.write(e);
    oc_.flushOutput();
    
    Process ret = (Process)processResponse();
    
    return ret;
    
  }
  
  public void commitRequest (Transaction t) throws AnyException
  {
    Event e = new SimpleEvent(EventConstants.IO_COMMIT,
                              t);
    oc_.write(e);
    oc_.flushOutput();
  }
  
  public Process unlockRequest (Process p,
                                Any     a) throws AnyException
  {
    Map m = AbstractComposite.simpleMap();
    m.add(EventConstants.LCK_PROCESS, p);
    m.add(EventConstants.LCK_OBJECT,  a);
    
    Event e = new SimpleEvent(EventConstants.LCK_UNLOCKREQUEST,
                              m);
    oc_.write(e);
    oc_.flushOutput();
    
    Process ret = (Process)processResponse();
    
    return ret;
    
  }
  
  public boolean isClosed()
  {
    return ic_.isClosed() || oc_.isClosed();
  }
  
  public void close()
  {
    try
    {
      oc_.close();
      ic_.close();
    }
    catch (Exception e) {}
  }
  
  private void open(Any host, Any user, Any passwd, Any pkg) throws AnyException
  {
    try
    {
      AnyChannel    ich = new AnyChannel(new FIFO());
      OutputChannel och = null;
      BooleanI      requestSession = null;
      Socket        cd;
      cd = new Socket(new URL(host.toString()));
      och = new AnyChannel(cd);
      
      // Note that we do not support the where-with-all to make
      // remote Inq i/o drivers work over http tunnels.  This seems
      // unnecessary when their purpose is to support server farms.
      
/*  
      if (cd.isKeepOpen())
      {
        requestSession = new AnyBoolean(true);
      }
*/  
      och.write(new StartProcessEvent(EventConstants.START_IOPROCESS,
                                      requestSession));
  
  
      Map login = AbstractComposite.simpleMap();
      login.add(NodeSpecification.user__, user);
      login.add(UserProcess.passwd__, passwd);
      login.add(UserProcess.package__, pkg);
      och.write(new SimpleEvent(EventConstants.LOGIN_REQUEST, login));
      och.flushOutput();
      AnyChannel readInput = new AnyChannel(cd, ich);
      
      ic_ = ich;
      oc_ = och;
      
      processResponse();
    }
    catch (MalformedURLException e)
    {
      throw new ContainedException(e);
    }
  }
  
  private Any processResponse() throws AnyException
  {
    return processResponse(-1);
  }
  
  private Any processResponse(int timeout) throws AnyException
  {
    Event e = (Event)ic_.read(timeout);
    ed_.processEvent(e);
    
    // Actually a bit sad that processEvent doesn't return
    // anything.  May be that was a mistake.
    
    // We only need to actually dispatch the event somewhere if there
    // is some processing specific to the event that is required.
    return e.getContext();
  }

  private class LoginOK extends    AbstractAny
                        implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("InqIo connected to " + host_);
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.loginOKEventTypes__;
    }
  }

  private class LoginDenied extends    AbstractAny
                            implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      throw new AnyException("Login Denied");
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.loginDeniedEventTypes__;
    }
  }
  
  private class DescriptorAck extends    AbstractAny
                              implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("InqIo$DescriptorAck.processEvent");

      // Make a BOTDescriptor out of the received ClientDescriptor
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return ServerMonitor.ioDescriptorAckEventTypes__;
    }
  }
}
