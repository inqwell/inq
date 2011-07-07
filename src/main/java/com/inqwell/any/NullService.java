/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NullService.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * A pre-canned service on the INQ server.
 * <p>
 * This service does nothing.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class NullService extends Service
{
  
  private static Any servicePath__ = new ConstString("system.services.NullService");

  public NullService() throws AnyException
  {
    init();
  }

  public Any exec(Any a, Transaction t, Map callArgs) throws AnyException
  {
  	return a;
  }
  
  private void init() throws AnyException
  {
		Catalog.catalog(this, servicePath__.toString(), Transaction.NULL_TRANSACTION);
		setBaseURL(new ConstString("internal://NullService"));
		setFQName(new ConstString("system:NullService"));
	}
}


