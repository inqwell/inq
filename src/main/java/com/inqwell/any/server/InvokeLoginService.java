/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/InvokeLoginService.java $
 * $Archive: /src/com/inqwell/any/server/InvokeLoginService.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;

/**
 * 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class InvokeLoginService extends InvokeService
{
  public InvokeLoginService()
  {
    setEventType(EventConstants.INVOKE_SVRLOGIN);
  }
  
  protected boolean veto(Any serviceName) throws AnyException
  {
		//if (Globals.isServer()) // && !getTransaction().getProcess.isRealSet())
		if (getTransaction().getProcess().isRealSet())
		{
		  throw new AnyException("User already logged in running service " + serviceName);
		}

    return false;
  }
}
