/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/InqAddress.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;


/**
 * The address of an <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup>
 * server and user process within it.
 * <p>
 * An <code>InqAddress</code> represents a destination process in
 * an <code>inq</code> server amongst a network of such servers.  Using
 * an address an originating <code>inq</code> process can make service
 * requests in processes elsewhere in an inter-connected <code>inq</code>
 * network without knowing exactly where that service will run.  Addresses
 * take the form
 * <pre>
 *
 *   user@domain
 *
 * </pre>
 * where <code>domain</code> represents the <code>inq</code> server and
 * <code>user</code> the process within that server.
 * <p>
 * Each server node has an address table it uses to determine whether the
 * current server is the desired one or that the message must be forwarded.
 * If the destination server has been reached then that server's user
 * process list is consulted to determine the destination process.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class InqAddress extends AbstractAny
{
	// Using a node spec for the server address allows us to make
	// use of its equality semantics to identify routes through the
	// inq network.
	private NodeSpecification server_;

	private Any               userProcess_;
	
	public InqAddress(String address)
	{
		init(address);
	}
	
	public InqAddress(StringI address)
	{
		this(address.toString());
	}
	
	public boolean equals(Any a)
	{
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

		if (!(a instanceof InqAddress))
			return false;
		
		InqAddress i = (InqAddress)a;
		
		if (!userProcess_.equals(i.getUser()))
			return false;
			
		if (!server_.equals(i.getDomain()))
			return false;
		
		return true;
	}
	
	public int hashCode()
	{
		return userProcess_.hashCode() * server_.hashCode();
	}
	
	public Any getUser()
	{
		return userProcess_;
	}
	
	public Any getDomain()
	{
		return server_;
	}
	
	/**
	 * Return <code>true</code> if this has a domain part which is equal
	 * to the given argument
	 */
	public boolean hasDomain(Any a)
	{
		return getDomain().equals(a);
	}
	
	private void init(String address)
	{
		int i;

		if (((i = address.indexOf('@')) < 0) || (i+1 == address.length()))
			throw new AnyRuntimeException("Badly formatted address " + address);
		
		userProcess_ = new ConstString(address.substring(0, i));
		server_      = new NodeSpecification(address.substring(i+1));
	}
}

