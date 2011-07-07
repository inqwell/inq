/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/StreamURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-02 20:39:13 $
 */


package com.inqwell.any.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Globals;
import com.inqwell.any.LocateNode;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;
import com.inqwell.any.io.AbstractStream;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>stream://</code> style URLs.
 * <p>
 * URLs of the style <code>stream://[path]</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL can be used to support stream access to other streams.
 */
public class StreamURLConnection extends URLConnection
{
  private AbstractStream tiedStream_;
  
	public StreamURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
	  AbstractStream a = getStreamFromURL();

	  return a.getUnderlyingInputStream();
	}

	public OutputStream getOutputStream() throws IOException
	{
	  AbstractStream a = getStreamFromURL();

		return a.getUnderlyingOutputStream();
	}

	public void connect() throws IOException
	{
	}

	public AbstractStream getTiedStream()
	{
	  return tiedStream_;
	}
	
  // Will return the stream or throw
  private AbstractStream getStreamFromURL() throws IOException
  {
    URL u = getURL();

    // The URL host is interpreted as a node reference. It must
    // resolve or we throw an exception.
    String path = u.getHost();

    // Use the magical way to get the process
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();

    // Resolve the stream
    NodeSpecification n = new NodeSpecification(path);
    n = NodeSpecification.setPrefices(n);

    LocateNode ln = new LocateNode(n);
    ln.setLineNumber(t.getLineNumber());
    try
    {
      AbstractStream a = (AbstractStream)EvalExpr.evalFunc
                               (t,
                                p.getContext(),
                                ln,
                                AbstractStream.class);

      if (a == null)
        throw new IOException("Could not resolve " + path);
      
      tiedStream_ = a;

      return a;
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
}
