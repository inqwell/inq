/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/SpeakinqProtocolHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import java.net.Socket;

public class SpeakinqProtocolHandler extends    NativeProtocolHandler
                                     implements ProtocolHandler
{
	private static final String protocolName__  = "speakinq";
	
	public String getProtocolName()
	{
		return protocolName__;
	}
}
