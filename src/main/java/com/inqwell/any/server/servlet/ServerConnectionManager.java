/**
 * Copyright (C) 2012 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.server.servlet;

import com.inqwell.any.AbstractResourceAllocator;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.ExceptionContainer;
import com.inqwell.any.IntI;
import com.inqwell.any.Map;
import com.inqwell.any.StringI;
import com.inqwell.any.connect.ServerConnection;

public class ServerConnectionManager extends AbstractResourceAllocator
{
	private static ServerConnectionManager theInstance__ = null;
	
	public static ServerConnectionManager instance()
	{
		if (theInstance__ == null)
		{
			synchronized (ServerConnectionManager.class)
			{
				if (theInstance__ == null)
					theInstance__ = new ServerConnectionManager();
			}
		}
		return theInstance__;
	}
	

	@Override
	protected Any makeNewResource(Any id, Any spec, int made) throws AnyException
	{
		Map m = (Map)spec;
		
		StringI user = (StringI)m.get(InqServlet.INIT_LOGIN_USER_A);
		StringI pwd  = (StringI)m.get(InqServlet.INIT_LOGIN_PWD_A);
		StringI pkg  = (StringI)m.get(InqServlet.INIT_LOGIN_PKG_A);
		StringI url  = (StringI)m.get(InqServlet.INIT_INQ_URL_A);
		
		ServerConnection ret = new ServerConnection(url, user, pwd, pkg, true);
		
		IntI pingTimeout = (IntI)m.get(InqServlet.INIT_PING_TIME_A);
		
		ret.setTransformer(new Transformer.JsonTransformer());
		ret.setPingTimeout(pingTimeout.getValue());
		
		return ret;
	}

	@Override
	protected boolean beforeRelease(Any resource, Any arg, ExceptionContainer e)
	{
		// Just ping it to see if its still ok. Consider not bothering
		// to do this.
		ServerConnection sc = (ServerConnection)resource;
		try
		{
			return sc.ping();
		}
		catch(AnyException ex)
		{
			e.setThrowable(ex);
			return false;
		}
	}

	@Override
	protected boolean beforeAcquire(Any resource)
	{
		// Just ping it to see if its still ok.
		ServerConnection sc = (ServerConnection)resource;
		try
		{
			return sc.ping();
		}
		catch(AnyException ex)
		{
			return false;
		}
	}

	@Override
	protected void afterAcquire(Any resource)
	{
		// no-op
	}

	@Override
	protected void disposeResource(Any resource)
	{
		ServerConnection sc = (ServerConnection)resource;
		sc.close();
	}

}
