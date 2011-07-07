/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/ClassPathStreamHandler.java $
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
 * for <code>classpath://</code> style URLs.
 * <p>
 * URLs of the style <code>classpath://</code> are handled by
 * this derivation of <code>java.net.URLStreamHandler</code>.  Such
 * a URL can be used to open streams to resources found on the
 * CLASSPATH.
 */
public class ClassPathStreamHandler extends URLStreamHandler
{
	public URLConnection openConnection(URL u) throws IOException
	{
		return new ClassPathURLConnection(u);
	}

	/**
	 * Not relevant.
	 */
	public int getDefaultPort()
	{
		return -1;
	}
}
