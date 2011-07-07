/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/StdinURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 

package com.inqwell.any.net;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.IOException;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>stdin:///</code> style URLs.
 * <p>
 * URLs of the style <code>stdin:///</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL can be used to refer to the stdin input stream.
 */
public class StdinURLConnection extends URLConnection
{
	public StdinURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
		return System.in;
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}
	
	public void connect() throws IOException
	{
	}
}
