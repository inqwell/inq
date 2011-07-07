/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/cache/Cache.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.server.cache;

import com.inqwell.any.*;

public interface Cache extends Map
{
	public void remove (Any key, Any value);
	
  public Any uniqueReference(Any key, Any value);
  
  public void expire(Transaction t) throws AnyException;
  
	public void destroy();

  public void purge();

  public void setStaticticsVariables(IntI loaded);
}
