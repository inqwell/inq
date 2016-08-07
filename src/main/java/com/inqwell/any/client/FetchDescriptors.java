/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/FetchDescriptors.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.ServerConstants;

/**
 * Invokes the <code>LoadDescriptors</code> service in the server.  The
 * invocation includes a response specification to return the
 * results to this JVM.  No service invocation is required here as
 * the serialization process takes care of the
 * incoming <code>Descriptor</code>s, lodging them in
 * our <code>Catalog</code>
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class FetchDescriptors extends    AbstractFunc
												      implements Cloneable
{
	private SendRequest sr_;

	public FetchDescriptors ()
	{
		init();
	}
	
	/**
	 * 
	 */
	public Any exec(Any a) throws AnyException
	{
		// The contents of the SendRequest instance is already resolved
		// with the exception of the output channel.  This must therefore
		// be reachable from the exec argument a.
		sr_.setTransaction(getTransaction());
		sr_.exec(a);
		
	  return a;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	FetchDescriptors fd = (FetchDescriptors)super.clone();
  	
  	fd.sr_         = (SendRequest)sr_.cloneAny();
  	
    return fd;
  }
  
	private void init()
	{		
		sr_ = new SendRequest (new AnyString("system.server.services.LoadDescriptors"),
													 null,  // no context
													 null,  // no args
													 new LocateNode (ServerConstants.ROCHANNEL));
		sr_.setPropagateContext(false);
	}
}
