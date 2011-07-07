/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/NullDomainAgent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/*
 * 
 * <p>
 * $Archive: /src/com/inqwell/any/NullDomainAgent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
public class NullDomainAgent extends    AbstractAny
                             implements DomainAgent
{
  public Any getHostId()
  {
    return null;
  }
  
  public void commit(Transaction t) throws AnyException {}
  
	public Process getLockMandate(Process p, Any a, long timeout) throws AnyException
	{
    return p;
	}
	
	public void release(Process p, Any a)	{}
}
