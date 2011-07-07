/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/SystemLogout.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.Service;
import com.inqwell.any.Transaction;
import com.inqwell.any.AnyException;
import com.inqwell.any.Catalog;
import com.inqwell.any.Any;
import com.inqwell.any.StringI;
import com.inqwell.any.ConstString;
import com.inqwell.any.Map;

/**
 * A The system level login service on the INQ server.
 * <p>
 * This service is executed on the server when any user logs in
 * as the <code>system</code> package.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SystemLogout extends Service
{
  
  private static StringI servicePath__ = new ConstString("system.services.Logout");

  public SystemLogout() throws AnyException
  {
    init();
  }

  public Any exec(Any a, Transaction t, Map callArgs) throws AnyException
  {
		// At the moment we don't do anything.
  	return a;
  }
  
  private void init() throws AnyException
  {
		Catalog.catalog(this, servicePath__.toString(), Transaction.NULL_TRANSACTION);
	}
}


