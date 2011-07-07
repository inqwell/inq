/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/StringURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */


package com.inqwell.any.net;

import com.inqwell.any.Any;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyString;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Globals;
import com.inqwell.any.Process;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.LocateNode;
import com.inqwell.any.NodeSpecification;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>string://</code> style URLs.
 * <p>
 * URLs of the style <code>string://[path]</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL can be used to support stream access to strings.
 */
public class StringURLConnection extends URLConnection
{
  private boolean append_;

  static private byte[] empty__ = new byte[0];

	public StringURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
	  Any a = getStringFromURL();

	  ByteArrayInputStream ret;

	  if (AnyNull.isNullInstance(a))
	    ret = new ByteArrayInputStream(empty__);
    else
      ret = new ByteArrayInputStream(a.toString().getBytes());
    
//	  else if (a instanceof StringI)
//	  {
//	    ret = new ByteArrayInputStream(a.toString().getBytes());
//	  }
//	  else
//	    throw new IOException(a.getClass().toString() + " not a string");

	  return ret;
	}

	public OutputStream getOutputStream() throws IOException
	{
	  Any a = getStringFromURL();

	  if (!(a instanceof AnyString))
	    throw new IOException(a.getClass().toString() + " not a mutable string");

    AnyString str = (AnyString)a;

		return new ByteArrayToStringOutputStream(str, append_);
	}
	
  /**
   * Get an output stream that will read from the
   * given {@link StringI} <code>s</code>.
   * @param s The {@link StringI} to read from.
   * @return
   */
	public InputStream getInputStream(StringI s)
	{
    if (AnyNull.isNullInstance(s))
      return new ByteArrayInputStream(empty__);
    else
      return new ByteArrayInputStream(s.toString().getBytes());
	}
	
	/**
	 * Get an output stream that will write to the
   * given {@link AnyString} <code>s</code>. Note
   * that <code>s</code> will not be filled until the
   * stream is closed.
	 * @param s The {@link AnyString} to write to.
	 * @return
	 */
	public OutputStream getOutputStream(AnyString s)
	{
    return new ByteArrayToStringOutputStream(s, append_);
	}

	public void connect() throws IOException
	{
	}

  /**
   * Sets the append mode of subsequent output streams returned by this.
   * If true then writing to the stream will append to the specified string.
   * Otherwise output overwrites any current contents.
   */
	public void setAppend(boolean append)
	{
	  append_ = append;
	}

	public boolean isAppend()
	{
	  return append_;
	}

  // Will return the string or throw
  private Any getStringFromURL() throws IOException
  {
    URL u = getURL();

    // The URL host is interpreted as a node reference. It must
    // resolve or we throw an exception.
    String path = u.getHost();
    //System.out.println("getHost " + path);

    // Use the magical way to get the process
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();

    // Resolve the string
    NodeSpecification n = new NodeSpecification(path);
    n = NodeSpecification.setPrefices(n);

    LocateNode ln = new LocateNode(n);
    ln.setLineNumber(t.getLineNumber());
    try
    {
      Any a = EvalExpr.evalFunc(t,
                                p.getContext(),
                                ln);

      /*
      // Explicit check for AnyNull (which can come out of a database, for
      // example. If so, return
      if (AnyNull.isNullInstance(a))
        return

      AnyString str = (AnyString)EvalExpr.evalFunc(t,
                                                   p.getContext(),
                                                   ln,
                                                   AnyString.class);

      if (str == null)
        throw new IOException("Could not resolve " + path);
      */

      if (a == null)
        throw new IOException("Could not resolve " + path);

      return a;
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  // Expose the underlying buffer so we don't allocate it twice
  // and initialise member AnyString with the content on close.
  static private class ByteArrayToStringOutputStream extends ByteArrayOutputStream
  {
    private AnyString str_;
    private boolean append_;

    public ByteArrayToStringOutputStream(AnyString str, boolean append)
    {
      super();
      str_    = str;
      append_ = append;
    }

    public ByteArrayToStringOutputStream(AnyString str, boolean append, int size)
    {
      super(size);
      str_    = str;
      append_ = append;
    }

    public void close() throws IOException
    {
      if (str_ != null)
      {
        byte[] b = toByteArray();

        if (append_)
          str_.setValue(str_.getValue() + new String(b, 0, size()));
        else
          str_.setValue(new String(b, 0, size()));

        super.close();
      }
      str_ = null;
    }

    public byte[] toByteArray()
    {
      return buf;
    }
  }
}
