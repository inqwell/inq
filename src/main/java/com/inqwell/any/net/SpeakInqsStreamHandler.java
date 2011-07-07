/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/SpeakInqsStreamHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 

package com.inqwell.any.net;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.io.IOException;
import java.net.Socket;

/**
 * Implement the <code>URLStreamHandler</code> sub-class
 * for <code>speakinqs://</code> style URLs.
 * <p>
 * URLs of the style <code>speakinqs://host:port</code> are handled by
 * this derivation of <code>java.net.URLStreamHandler</code>.
 */
public class SpeakInqsStreamHandler extends URLStreamHandler
{
	public URLConnection openConnection(URL u) throws IOException
	{
		int    port = u.getPort();
		
		if (port < 0)
		  port = getDefaultPort();
		
		return new SpeakInqsURLConnection(u, port);
	}

	/**
	 * The default port for the <code>speakinq</code> protocol is 6556
	 */
	public int getDefaultPort()
	{
		return 6557;
	}
}
