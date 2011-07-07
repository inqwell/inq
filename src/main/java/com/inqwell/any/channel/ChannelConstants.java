/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/ChannelConstants.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.ConstString;

public interface ChannelConstants
{
  public static int COPY      = 1;
  public static int REFERENCE = 0;

  public static final Any SESSION_ID     = new ConstString("sessionId__");
  public static final Any SESSION_SOCKET = new ConstString("socket__");
}
