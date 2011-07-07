/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/StreamStreamHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 
package com.inqwell.any.net;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.io.IOException;

/**
 * Implement the <code>URLStreamHandler</code> sub-class
 * for <code>stream://</code> style URLs.
 * <p>
 * URLs of the style <code>stream://</code> are handled by
 * this derivation of <code>java.net.URLStreamHandler</code>.  Such
 * a URL can be used to attach one i/o stream to another.
 */
public class StreamStreamHandler extends URLStreamHandler
{
	public URLConnection openConnection(URL u) throws IOException
	{
		return new StreamURLConnection(u);
	}

	/**
	 * Not relevant.
	 */
	public int getDefaultPort()
	{
		return -1;
	}
}
