/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/StreamURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */


package com.inqwell.any.net;

import com.inqwell.any.Any;
import com.inqwell.any.AnyNull;
import com.inqwell.any.StringI;
import com.inqwell.any.AnyString;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Globals;
import com.inqwell.any.Process;
import com.inqwell.any.Transaction;
import com.inqwell.any.LocateNode;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.io.AbstractStream;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;
import java.io.IOException;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>pipe://</code> style URLs.
 * <p>
 * URLs of the style <code>pipe://</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL creates connected piped streams, typically used to capture
 * output when running system commands.
 */
public class PipedURLConnection extends URLConnection
{
	private PipedInputStream   is_;
	private PipedOutputStream  os_;
	
	public PipedURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
		if (is_ == null)
		{
	    is_ = new PipedInputStream(os_ = new PipedOutputStream());
	    this.connected = true;
	  }

	  return is_;
	}

	public OutputStream getOutputStream() throws IOException
	{
		if (os_ == null)
		{
	    os_ = new PipedOutputStream(is_ = new PipedInputStream());
	    this.connected = true;
	  }

	  return os_;
	}

	public void connect() throws IOException
	{
		if (!this.connected)
		  getInputStream();
	}
}
