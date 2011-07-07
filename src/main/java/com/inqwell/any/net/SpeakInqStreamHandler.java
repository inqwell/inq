/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/SpeakInqStreamHandler.java $
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
 * for <code>speakinq://</code> style URLs.
 * <p>
 * URLs of the style <code>speakinq://host:port</code> are handled by
 * this derivation of <code>java.net.URLStreamHandler</code>.  Such
 * a URL can be used to support interfacing between
 * an <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup> environment
 * and external systems.
 */
public class SpeakInqStreamHandler extends URLStreamHandler
{
	public URLConnection openConnection(URL u) throws IOException
	{
		int    port = u.getPort();
		
		if (port < 0)
		  port = getDefaultPort();
		
		return new SpeakInqURLConnection(u, port);
	}

	/**
	 * The default port for the <code>speakinq</code> protocol is 6556
	 */
	public int getDefaultPort()
	{
		return 6556;
	}
}
