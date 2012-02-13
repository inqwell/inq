/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/InvokeService.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.client.swing.SwingInvoker;

/**
 * Invokes the specified service.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 */
public class InvokeService extends    AbstractAny
                           implements ChannelListener,
																			Cloneable
{
	private Map root_;
  private Transaction transaction_;

  // General purpose locate node etc
  private LocateNode        ln_   = new LocateNode();
  private NodeSpecification ns_   = new NodeSpecification();
  private BuildNodeMap      bn_   = new BuildNodeMap(AbstractComposite.managedMap());

  private BooleanI          threadFlag_ = new AnyBoolean();

  // Only set if we have a non-standard event type
  public Array eventTypes_;

  static public Array eventTypes__;

  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.INVOKE_SVC);
	}

	public static InvokeService makeInvokeService(Transaction t, Map root)
	{
		return makeInvokeService(EventConstants.INVOKE_SVC, t, root);
	}

	public static InvokeService makeInvokeService(Any         eventType,
	                                              Transaction t,
	                                              Map         root)
	{
		InvokeService is = new InvokeService();
		is.setEventType(eventType);
		is.setTransaction(t);
		is.setRoot(root);
		return is;
	}

  public boolean processEvent(Event e) throws AnyException
  {
		//exec(e.getContext());
    Map m = (Map)e.getContext();

    Any       serviceName    = m.get(ServerConstants.SVCEXEC);

    Any context = null;
    context = m.getIfContains(ServerConstants.SVCCTXT);
    {
      if (AnyNull.isNull(context))
        context = ServerConstants.NSROOT;
    }

    Map args = null;
    if (m.contains(ServerConstants.SVCINAR))
      args = (Map)m.get(ServerConstants.SVCINAR);
    
    // For the client - whether to dispatch on the graphics thread can
    // be specified in the request as an argument
    Any syncGui = null;
    threadFlag_.setValue(false);
    if (!Globals.isServer() && args != null && (syncGui = args.getIfContains(NodeSpecification.atSyncGUI__)) != null)
      threadFlag_.copyFrom(syncGui);
    boolean bSyncGui = threadFlag_.getValue();

    if (bSyncGui)
    {
      final Any         lServiceName = serviceName;
      final Any         lContext     = context;
      final Map         lArgs        = args;
      final boolean     lBSyncGui    = bSyncGui;
  
      SwingInvoker ss = new SwingInvoker()
      {
        protected void doSwing()
        {

          // Hmmm, must drains-up the use of checked exceptions sometime.
          try
          {
            invokeService(lServiceName,
                          lContext,
                          lArgs,
                          lBSyncGui);
          }
          catch(AnyException e)
          {
            throw new RuntimeContainedException(e);
          }
        }
      };
  
      ss.serviceAsync(getTransaction());
    }
    else
      invokeService(serviceName,
                    context,
                    args,
                    bSyncGui);
    
    return true;
  }

  public Array getDesiredEventTypes()
  {
    if (eventTypes_ != null)
      return eventTypes_;
    else
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

	/**
   * Override the default event type of <code>INVOKE_SVC</code>
   */
	public void setEventType(Any eventType)
	{
		if (!eventType.equals(EventConstants.INVOKE_SVC))
		{
			eventTypes_ = AbstractComposite.array();
			eventTypes_.add(eventType);
		}
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
														Any       context,
														Map       args,
                            boolean   onGuiThread) throws AnyException
  {
		getTransaction().getCallStack().empty();

    // Check for veto
    veto(serviceName);

		ln_.setTransaction(getTransaction());
		Service s = (Service)ln_.locate
										(new NodeSpecification(serviceName.toString()),
										 Catalog.instance().getCatalog());
		if (s == null)
		{
			throw new UnknownServiceException
												("Service " + serviceName + " not found");
		}

		// If we have been given a context to execute in, locate it with
		// respect to our universe.
		Map contextNode = null;
		if (context != null)
		{
			ln_.setTransaction(getTransaction());
			bn_.setTransaction(getTransaction());
      
			NodeSpecification cns;
      if (context instanceof NodeSpecification)
        cns = (NodeSpecification)context;
      else
        cns = new NodeSpecification(context.toString());
      
			contextNode = (Map)ln_.locate(cns,
															      root_);

			if (contextNode == null)
			{
			  bn_.build(cns, (contextNode = AbstractComposite.managedMap()), root_);
			}
		}
		else
		{
			contextNode = root_;
		}

    boolean syncGui = s.isSyncGraphics() && !onGuiThread;

		Map callArgs = setupServiceArgs(s, args);
		Any ret = runService(s, contextNode, context, callArgs, syncGui);

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
												 Map     contextNode,
                         Any     contextPath,
												 Map     callArgs,
                         boolean syncGui) throws AnyException
	{
    if (callArgs != null && callArgs.contains(NodeSpecification.atException__))
    {
      // Place the process id arg into our exception handler.
      // Croaks if the exception handler does not support this
      // function.
      // Remove the argument - otherwise any throw() statement at this
      // level will assume there is a nested exception
      Any procId = callArgs.remove(NodeSpecification.atException__);
      if (procId != null)
      {
        getTransaction().getProcess().getExceptionHandler().setHandlerProcess(procId);
      }
    }
    
    // If we are a client then there is an option to run the
    // service on the swing thread.  This can be important when
    // the service raises events in the AWT system - if the service
    // is running on the Process thread then these are deferred
    // until service execution is complete.  This can mean that
    // things don't happen in the expected order, which can be
    // important.
    if (syncGui && Globals.process__ != null)
    {
      final Map         lContextNode = contextNode;
      final Any         lContextPath = contextPath;
      final Map         lCallArgs    = callArgs;
      final Service     lService     = s;
      final Transaction lT           = getTransaction();

      SwingInvoker ss = new SwingInvoker()
      {
        protected void doSwing()
        {
          lT.getProcess().setContext(lContextNode);
          lT.getProcess().setContextPath(lContextPath);

          // Hmmm, must drains-up the use of checked exceptions sometime.
          try
          {
            lService.exec(lContextNode, lT, lCallArgs);
          }
          catch(AnyException e)
          {
            throw new RuntimeContainedException(e);
          }
        }
      };

      ss.serviceAsync(getTransaction());
      return null;
    }
    else
    {
      // Run the service function relative to the context node.
      getTransaction().getProcess().setContext(contextNode);
      getTransaction().getProcess().setContextPath(contextPath);

      return s.exec(contextNode, getTransaction(), callArgs);
    }
	}
}
