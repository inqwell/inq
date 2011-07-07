/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/ByteArrayURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
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
 * for <code>bytearray://</code> style URLs.
 * <p>
 * URLs of the style <code>bytearray://path.to.bytearray</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL returns connections
 * and external systems.
 */
public class ByteArrayURLConnection extends URLConnection
{
	private ByteArrayInputStream   is_;
	private ByteArrayOutputStream  os_;
	
	public ByteArrayURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
		return null;
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		return new ByteArrayOutputStream();
	}
	
	public void connect() throws IOException
	{
	}
}
