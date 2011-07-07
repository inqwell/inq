/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.server;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.Array;
import com.inqwell.any.Catalog;
import com.inqwell.any.ChannelListener;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.ServerConstants;
import com.inqwell.any.Service;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.Transaction;
import com.inqwell.any.UnknownServiceException;
import com.inqwell.any.Value;
import com.inqwell.any.channel.OutputChannel;

public class InvokeWebService extends    AbstractAny
                              implements ChannelListener,
                                         Cloneable
{
  private Map                root_;
  private Transaction        transaction_;
  private OutputChannel      toClient_;
  private LocateNode         ln_ = new LocateNode();
  private NodeSpecification  s_  = new NodeSpecification();
  private IntI               sequence_ = new AnyInt();
  
 static public Array eventTypes__;

  static
  {
    eventTypes__ = AbstractComposite.array();
    eventTypes__.add(EventConstants.INVOKE_WEBSVC);
  }

  public static InvokeWebService makeInvokeService(Transaction   t,
                                                   Map           root,
                                                   OutputChannel toClient)
  {
    return makeInvokeService(EventConstants.INVOKE_WEBSVC, t, root, toClient);
  }

  public static InvokeWebService makeInvokeService(Any           eventType,
                                                   Transaction   t,
                                                   Map           root,
                                                   OutputChannel toClient)
  {
    InvokeWebService is = new InvokeWebService(root, t, toClient);
    //is.setEventType(eventType);
    is.setTransaction(t);
    is.setRoot(root);
    return is;
  }

  private InvokeWebService(Map root, Transaction t, OutputChannel toClient)
  {
    root_        = root;
    transaction_ = t;
    toClient_    = toClient;
  }

  public boolean processEvent(Event e) throws AnyException
  {
    // The service to invoke and any arguments
    Map m = (Map)e.getContext();
    sequence_.setValue(e.getSequence());

    // Service name - must have
    Any serviceName = m.get(ServerConstants.SVCEXEC);

    // Arguments - may be
    Map args = null;
    if (m.contains(ServerConstants.SVCINAR))
      args = (Map)m.get(ServerConstants.SVCINAR);
    
    Any resp = invokeService(serviceName,
                             args);
    
    sendResponse(resp);
    
    return true;
  }

  public Array getDesiredEventTypes()
  {
    return eventTypes__;
  }

  public Transaction getTransaction()
  {
    return transaction_;
  }

  public void setTransaction(Transaction t)
  {
    transaction_ = t;
  }

  public Object clone () throws CloneNotSupportedException
  {
    AbstractAny.cloneNotSupported(this);
    return null;
  }

  public void setRoot(Map root)
  {
    root_ = root;
  }

  protected boolean veto(Any serviceName) throws AnyException
  {
    //if (Globals.isServer()) // && !getTransaction().getProcess.isRealSet())
    if (Globals.isServer() && !getTransaction().getProcess().isRealSet())
    {
      throw new AnyException("User is not logged in running service " + serviceName);
    }

    return false;
  }

  private Any invokeService(Any       serviceName,
                            Map       args) throws AnyException
  {
    getTransaction().getCallStack().empty();

    // Check for veto
    veto(serviceName);

    // Locate the service
    ln_.setTransaction(getTransaction());
    Service s = (Service)ln_.locate
                    (s_.setPathSpec(serviceName.toString()),
                     Catalog.instance().getCatalog());
    if (s == null)
    {
      throw new UnknownServiceException
                        ("Service " + serviceName + " not found");
    }

    Map callArgs = setupServiceArgs(s, args);
    Any ret = runService(s, callArgs);

    return ret;
  }

  private Map setupServiceArgs(Service s,
                               Map     args) throws AnyException
  {
    // We could check here if the input arguments fully satisfy the
    // service arguments.  Maybe we should do that...
    // In any case, we use the names of the args map as node
    // specifications through the param locations to assign
    // the service arguments.  Again, we could check for
    // ambiguity (would need to expand LocateNode to do that!)

    Map ret = null;

    if (args != null)
    {
      Map targetArgs = s.getParams();
      Iter i = args.createKeysIterator();
      while (i.hasNext())
      {
        Any argName = i.next();

        Any targetArg = targetArgs.contains(argName) ? targetArgs.get(argName)
                                                     : null;

        Any sourceArg = args.get(argName);
        if (targetArg != null)
        {

          if (targetArg instanceof Value)
          {
            //System.out.println("COPYING " + argName + " " + sourceArg + " " + sourceArg.getClass());
            targetArg.copyFrom(sourceArg);
          }
          else
          {
            //System.out.println("REPLACING " + argName + " " + sourceArg + " " + sourceArg.getClass());
            targetArgs.replaceItem(argName, sourceArg);
          }
        }
        else
        {
          // Allow arguments through even if they are not any of those that
          // are formally declared
          targetArgs.replaceItem(argName, sourceArg);
        }
      }
      ret = targetArgs;
    }
    return ret;
  }

  private Any runService(Service s,
                         Map     callArgs) throws AnyException
  {
    // Web container services always run at the root context
    getTransaction().getProcess().setContext(root_);
    getTransaction().getProcess().setContextPath(ServerConstants.NSROOT);

    return s.exec(root_, getTransaction(), callArgs);
  }

  private void sendResponse(Any response) throws AnyException
  {
    // We're alright reusing sequence_ if the client is always remote (i.e.
    // serialized to !!)
    Event e = new SimpleEvent(EventConstants.WEBSVC_RESP, response, sequence_);
    
    toClient_.write(e);
    toClient_.flushOutput();
  }
}
