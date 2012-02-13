/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/IoConstants.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.io;

import com.inqwell.any.*;

/**
 * Constants for the SQL service.  Used by client and server
 * components.
 */

public interface IoConstants
{
  // These are used by the SQL resource allocator to extract map keys
  // for creating SQL connections
	static Any user__        = AbstractValue.flyweightString("user");
	static Any passwd__      = AbstractValue.flyweightString("password");
	static Any pkg__         = AbstractValue.flyweightString("pkg");
	static Any url__         = AbstractValue.flyweightString("url");
	static Any null__        = AbstractValue.flyweightString("null");
  static Any delim__       = AbstractValue.flyweightString("delim");
  static Any dateAsTime__  = AbstractValue.flyweightString("dateastimestamp");
	static Any cardinality__ = AbstractValue.flyweightString("cardinality");
  static Any initStmts__   = AbstractValue.flyweightString("init");
  static Any onException__ = AbstractValue.flyweightString("onexception");
}
