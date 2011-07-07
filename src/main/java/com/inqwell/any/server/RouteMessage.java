/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/RouteMessage.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.channel.OutputChannel;

/**
 * Routes the received event to at least the next <code>inq</code>
 * environment in the path, or possibly its ultimate recipient.
 * <p>
 * If the event has arrived at the destination <code>inq</code>
 * environment it is routed to a user process and has its event
 * id changed from INVOKE_SVC to ROUTE_MSG.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class RouteMessage extends    AbstractAny
													implements ChannelListener,
																		 Cloneable
{
  static private Array eventTypes__;
  static private Map   users__;
  static private Map   domains__;
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.ROUTE_MSG);
		
		BuildNodeMap bn = new BuildNodeMap();
		
		// Set up the user and domain tables in the catalog
		try
		{
			bn.build(NodeSpecification.user__.toString(),
							 users__ = AbstractComposite.simpleMap(),
							 Catalog.instance().getCatalog());

			bn.build(NodeSpecification.domain__.toString(),
							 domains__ = AbstractComposite.simpleMap(),
							 Catalog.instance().getCatalog());
		}
		catch (AnyException e)
		{
			e.printStackTrace();
		}
	}
	
	static public Map getUsers()
	{
		return users__;
	}
	
	static public Map getDomains()
	{
		return domains__;
	}
	
	/**
	 * 
	 */
  public RouteMessage()
  {
  }

  public boolean processEvent(Event e) throws AnyException
  {
		Map m = (Map)e.getContext();
		
		InqAddress to = (InqAddress)m.get(ServerConstants.MSGTO);
		
		if (to.hasDomain(Server.instance().getDomain()))
		{
			// Message is destined for our server.  Check user table
			toUserProcess(to, e);
		}
		else
		{
			// Message has to be routed on.  Check router table
			toNextServer(to, e);
		}
    return true;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
		AbstractAny.cloneNotSupported(this);
		return null;
  }
  
  public void setRoot(Map root)
  {
	}
	
	private void toUserProcess(InqAddress to, Event e) throws AnyException
	{
		if (users__.contains(to.getUser()))
		{
			OutputChannel oc = (OutputChannel)users__.get(to.getUser());
			
			// Turn the event into a service invocation
			oc.write(new SimpleEvent(EventConstants.INVOKE_SVC, e.getContext()));
			oc.flushOutput();
		}
		else
		{
			// path is invalid - return an error to sender?
		}
	}
  
	private void toNextServer(InqAddress to, Event e) throws AnyException
	{
		if (domains__.contains(to.getDomain()))
		{
			OutputChannel oc = (OutputChannel)domains__.get(to.getDomain());
			
			oc.write(e);
			oc.flushOutput();
		}
		else
		{
			// path is invalid - return an error to sender?
		}
	}
}
