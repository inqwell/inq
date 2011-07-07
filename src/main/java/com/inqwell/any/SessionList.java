/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SessionList.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.ContentCipher;

public interface SessionList extends Any
{
	public void establishSessionId(InputChannel  pi,
	                               InputChannel  ni,
                                 OutputChannel oc,
                                 Socket        channelDriver,
                                 ContentCipher cc) throws AnyException;

  public void deleteSession(Any sessionId) throws AnyException;
  public void zombieSession(Any sessionId);
  
  public boolean isSessionActive(Any sessionId);
  public boolean isZombieSession(Any sessionId);
}
