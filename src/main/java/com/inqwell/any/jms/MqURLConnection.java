/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */


package com.inqwell.any.jms;

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
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>mq://</code> style URLs.
 * <p>
 * URLs of the style <code>mq://[path]</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL can be used to support stream access to bytes and stream messages.
 */
public class MqURLConnection extends URLConnection
{
  public MqURLConnection(URL url)
  {
    super(url);
  }

  public InputStream getInputStream() throws IOException
  {
    BytesMessageI b = getMessageFromURL();

    return new MessageInputStream(b);
  }

  public OutputStream getOutputStream() throws IOException
  {
    BytesMessageI b = getMessageFromURL();

    return new MessageOutputStream(b);
  }
  
  /**
   * Get an output stream that will read from the
   * given {@link StringI} <code>s</code>.
   * @param s The {@link StringI} to read from.
   * @return
   */
  public void connect() throws IOException
  {
  }

  // Will return the string or throw
  private BytesMessageI getMessageFromURL() throws IOException
  {
    URL u = getURL();

    // The URL host is interpreted as a node reference. It must
    // resolve or we throw an exception.
    String path = u.getHost();
    //System.out.println("getHost " + path);

    // Use the magical way to get the process
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();

    // Resolve the message
    NodeSpecification n = new NodeSpecification(path);
    n = NodeSpecification.setPrefices(n);

    LocateNode ln = new LocateNode(n);
    ln.setLineNumber(t.getLineNumber());
    try
    {
      BytesMessageI a = (BytesMessageI)EvalExpr.evalFunc(t,
                                                         p.getContext(),
                                                         ln,
                                                         BytesMessageI.class);

      if (a == null)
        throw new IOException("Could not resolve " + path);

      return a;
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  static public class MessageOutputStream extends OutputStream
  {
    private BytesMessageI m_;
    
    public MessageOutputStream(BytesMessageI m)
    {
      super();
      m_ = m;
    }
    
    public BytesMessageI getMessage()
    {
      return m_;
    }

    public void write(int b) throws IOException
    {
      // no-op (not used)
    }
  }

  static public class MessageInputStream extends InputStream
  {
    private BytesMessageI m_;
    
    public MessageInputStream(BytesMessageI m)
    {
      super();
      m_ = m;
    }

    public BytesMessageI getMessage()
    {
      return m_;
    }

    public int read() throws IOException
    {
      // no-op (not used)
      return 0;
    }
  }
}
