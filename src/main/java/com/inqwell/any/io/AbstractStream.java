/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/AbstractStream.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-03 14:35:51 $
 */


package com.inqwell.any.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyURL;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.PropertyAccessMap;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.net.PipedURLConnection;
import com.inqwell.any.net.PlainSocketURLConnection;
import com.inqwell.any.net.StreamURLConnection;
import com.inqwell.any.net.StringURLConnection;

/**
 * Provides utility functionality for sub-classes which implement
 * the <code>PhysicalIO</code> interface for I/O to streams.
 */

public abstract class AbstractStream extends    PropertyAccessMap
																		 implements PhysicalIO,
																								Cloneable
{
	protected transient InputStream  istream_;
	protected transient OutputStream ostream_;

  private   AnyURL        openedURL_;
  
  private   URLConnection urlc_;

  private transient Map  propertyMap_;

  private static    Any  tiedStream__ = new ConstString("tiedStream__");

	public void setAuxInfo (Any a, Any subs) {}

  public Object clone() throws CloneNotSupportedException
  {
		AbstractStream s = (AbstractStream)super.clone();
		s.istream_     = null;
		s.ostream_     = null;
    s.openedURL_   = null;
    s.propertyMap_ = null;
		return s;
  }

	public void close()
	{
		// If open for both input and output then we are a pipe://
    // In this case just close the write side, on the assumption that
    // the writer closes the pipe and the reader will do so some time
    // later when it sees eof.
    boolean isPipe = ostream_ != null && istream_ != null;
    
		if (ostream_ != null)
    {
      boolean closed = true;
		  try
		  {
        closed = doCloseWrite();
      }
      finally
      {
        propertyMap_ = null;
        if (closed)
        {
          try
          {
    			  ostream_.close();
          }
          catch(Exception e)
          {
            throw new RuntimeContainedException(e);
          }
          finally
          {
            ostream_ = null;
          }
        }
      }
    }

		if (istream_ != null && !isPipe)
    {
      boolean closed = true;
      try
      {
        closed = doCloseRead();
      }
      finally
      {
        if (closed)
        {
          try
          {
            istream_.close();
          }
          catch(Exception e)
          {
            throw new RuntimeContainedException(e);
          }
          finally
          {
            istream_ = null;
          }
        }
      }
    }

		if (propertyMap_ != null)
    {
      AbstractStream s = (AbstractStream)propertyMap_.get(tiedStream__);
      if (s != null)
        s.close();
    }
	}
	
	public void flush()
	{
    if (propertyMap_ != null)
    {
      AbstractStream s = (AbstractStream)propertyMap_.get(tiedStream__);
      if (s != null)
        s.flush();
    }
    doFlush();
	}

  public void setStreams(InputStream is, OutputStream os) throws AnyException
  {
    istream_ = is;
    ostream_ = os;
    acceptStreams();
  }

  public void setTiedStream(Any s)
  {
    // We don't want the overhead of storing a stream in
    // a class as commonly used as this one, so store it
    // in the property map instead.
    if (propertyMap_ == null)
      propertyMap_ = makePropertyMap();
    
    if (AnyNull.isNull(s))
    {
      propertyMap_.replaceItem(tiedStream__, null);
    }
    else
    {
      propertyMap_.replaceItem(tiedStream__, s);
    }
  }
  
  public AnyURL getURL()
  {
    return openedURL_;
  }

  public InputStream getUnderlyingInputStream()
  {
    return istream_;
  }

  public OutputStream getUnderlyingOutputStream()
  {
    return ostream_;
  }
  
  public boolean isOpenWrite()
  {
    return ostream_ != null;
  }
  
  public boolean isOpenRead()
  {
    return istream_ != null;
  }
  
  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;
      
    return false;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }
        
      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  public Any copyFrom (Any a)
  {
    // Do any derived processing first - if that fails there's no
    // point in us doing anything and we know cast must succeed.
    doCopyFrom(a);

    //AbstractStream as = (AbstractStream)a;
    this.istream_   = null;
    this.ostream_   = null;
    this.openedURL_ = null;

    return this;
  }
  
  // Properties
  
  public Any getAvailable()
  {
    if (isOpenRead())
    {
      try
      {
        return new AnyInt(istream_.available());
      }
      catch (IOException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    else
    {
      notOpenedRead();
      return null;
    }
  }
  
  public void setSoTimeout(int timeout)
  {
    if (isOpenRead())
    {
      if (urlc_ instanceof PlainSocketURLConnection)
      {
        ((PlainSocketURLConnection)urlc_).setSoTimeout(timeout);
      }
      else
        throw new AnyRuntimeException("Not a socket");
    }
    else
      notOpenedRead();
  }

  protected void doCopyFrom(Any a)
  {
  	// Can we ever copy a stream? What does it mean?
  	throw new IllegalArgumentException("Cannot copy streams");
  }

	protected void finalize() throws Throwable
	{
		// If we get finalized then do our best close to release resources
		close();
	}
  
  protected void attachStreams(AbstractStream stream) throws AnyException
  {
    setStreams(stream.istream_, stream.ostream_);
  }

  protected abstract void acceptStreams() throws AnyException;
  
  protected abstract boolean doCloseRead();

  protected abstract boolean doCloseWrite();
  
  protected abstract void doFlush();

  protected void notOpenedWrite()
  {
    throw new UnsupportedOperationException("Not opened for writing");
  }

  protected void notOpenedRead()
  {
    throw new UnsupportedOperationException("Not opened for reading");
  }

  /**
   * Make streams to the underlying source represented by the URL.
   * Where supported, streams will be created according to the
   * supplied mode.
   * <p>
   * The streams created only make the connection to the underlying
   * source/sink and are not characterised with any particular
   * format, such as native serialised form or text line-based.
   * That function is performed by derived implementations
   * of <code>acceptStreams</code>, which can be called after
   * this method has executed successfully.
   * <p>
   * If the supplied URL, <code>toOpen</code>, is relative then
   * it is based on the file:// protocol, assuming the user's
   * current working directory.
   */
	protected final boolean makeStreams(Process p, Any toOpen, IntI mode) throws AnyException
	{
    String cwd = System.getProperties().getProperty("user.dir");
    String fs  = System.getProperties().getProperty("file.separator");

    AnyURL    baseUrl = new AnyURL("file:///" + cwd + fs + "dummy");
    AnyURL    openUrl = new AnyURL(toOpen);

		boolean ret = true;

		try
		{
  		URL url = openUrl.getURL(baseUrl);

		  // URL looks OK - try to process it

			URLConnection uc = url.openConnection();

      openedURL_ = openUrl;
      urlc_      = uc;

			if ((mode.equals(PhysicalIO.write__)) ||
					(mode.equals(PhysicalIO.append__)))
			{
				// Not every type of URL can support append (or is it meaningful to
				// do so). Currently, only files support append and because we
				// cannot get at the underlying stream handler class we undertake
				// to create the output stream in this case
				String protocol = url.getProtocol();
				if (protocol.equals("file:") && mode.equals(PhysicalIO.append__))
				{
          String filePath = url.getPath();
          ostream_ = new FileOutputStream(filePath, true);
				}
				else
				{
          OutputStream ostream = getOutputStream(uc, mode);
          close();
          ostream_ = ostream;
          
          // If its a piped stream then we must get the input one at
          // the same time.
          if ((uc instanceof PipedURLConnection) ||
              (uc instanceof PlainSocketURLConnection) ||
              (uc instanceof StreamURLConnection))
          {
            istream_ = getInputStream(uc);
            
            if (uc instanceof StreamURLConnection)
            {
              this.setTiedStream(((StreamURLConnection)uc).getTiedStream());
            }
          }
        }
			}
			else
			{
				InputStream istream = getInputStream(uc);
        close();
        istream_ = istream;

        // If its a piped or socket stream then get the input one at
        // the same time.
        if ((uc instanceof PipedURLConnection) ||
            (uc instanceof PlainSocketURLConnection) ||
            (uc instanceof StreamURLConnection))
        {
          ostream_ = getOutputStream(uc, mode);
          
          if (uc instanceof StreamURLConnection)
          {
            this.setTiedStream(((StreamURLConnection)uc).getTiedStream());
          }
        }
			}
		}
		catch (MalformedURLException mue)
		{
			throw new ContainedException(mue);
		}
		catch (IOException ioex)
		{
		  openedURL_ = null;
		  urlc_      = null;
		  
			ret = false;
		}
		return ret;
	}

	protected boolean isPipe()
	{
    if ((urlc_ instanceof PipedURLConnection) ||
        (urlc_ instanceof PlainSocketURLConnection))
      return true;
    
    if (propertyMap_ != null)
    {
      AbstractStream s = (AbstractStream)propertyMap_.get(tiedStream__);
      if (s != null)
        return s.isPipe();
    }
    
    return false;
	}
	
  public Iter createIterator () { return DegenerateIter.i__; }

  public boolean isEmpty() { return false; }

  /**
   *  Make sure there is a filter__ child, because of PropertyMap.contains()
   *  always returning true.
   */
  protected Map makePropertyMap()
  {
    Map m = super.makePropertyMap();
    m.add(tiedStream__, null);
    return m;
  }
  
  protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

	private OutputStream getOutputStream(URLConnection uc,
																			 IntI          mode) throws AnyException
	{
		OutputStream ret = null;

		try
		{
		  if (uc instanceof StringURLConnection)
		  {
		    StringURLConnection struc = (StringURLConnection)uc;
		    struc.setAppend(mode.equals(PhysicalIO.append__));
		    ret = struc.getOutputStream();
	    }
	    else
			  ret = uc.getOutputStream();
		}
		catch (UnknownServiceException usex)
		{
			// In fact, the file: protocol doesn't support output streams
			// so we have to handle this case ourselves.
			ret = getFileOutputStream(uc, mode);
		}
		catch (Exception e)
		{
			throw new ContainedException(e);
		}

		return ret;
	}

	private OutputStream getFileOutputStream(URLConnection uc,
																					 IntI          mode) throws AnyException
	{
		OutputStream ret = null;

		String protocol = uc.getURL().getProtocol();

		//System.out.println ("AbstractStream.getFileOutputStream() protocol: " + protocol);

		if (!protocol.equals("file"))
		{
			throw new AnyIOException("Unsupported protocol for output");
		}

		String path = uc.getURL().getPath();

		try
		{
			ret = new FileOutputStream(path, mode.equals(PhysicalIO.append__));
		}
		catch(Exception e)
		{
			throw new ContainedException(e);
		}

		return ret;
	}

	private InputStream getInputStream(URLConnection uc) throws IOException
	{
    return uc.getInputStream();
	}
}
