/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.net;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Implement the <code>URLStreamHandler</code> sub-class
 * for <code>classpath://</code> style URLs.
 * <p>
 * URLs of the style <code>classpath://</code> are handled by
 * this derivation of <code>java.net.URLStreamHandler</code>.  Such
 * a URL can be used to open streams to resources found on the
 * CLASSPATH.
 */
public class GileStreamHandler extends URLStreamHandler
{
	public URLConnection openConnection(URL u) throws IOException
	{
		URL fileURL = new URL(u.toString().replaceFirst("gile:", "file:"));
		return new GileURLConnection(fileURL);
	}

	/**
	 * Not relevant.
	 */
	public int getDefaultPort()
	{
		return -1;
	}
}
