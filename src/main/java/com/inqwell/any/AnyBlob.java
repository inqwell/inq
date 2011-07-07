/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyBlob.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-17 14:43:54 $
 */

package com.inqwell.any;

import com.inqwell.any.io.AbstractStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;

/**
 * A representation of a binary large object.  The Blob itself
 * is an Any implementation and is stored as the underlying
 * object.
 * <p>
 *
 */
public class AnyBlob extends    AnyObject
                     implements BlobI,
                                Cloneable
{
	private static final long serialVersionUID = 1L;

	// A map of read handler functions held against the stream class.
  // Used to process the filling of the blob taking into account
  // the semantics of the underlying Inq stream.
  static HashMap readHandler__ = new HashMap();

  // Defines, according to the exact stream type, how this
  // Blob is represented in a data stream.  Used as the basis
  // for reading and writing the blob to persistent media etc.
  private AbstractStream stream_;

  static
  {
    readHandler__.put(com.inqwell.any.io.PrintStream.class,
                      new ReadAllLines());
  }

  public AnyBlob(AbstractStream stream)
  {
    stream_ = stream;
  }

  /*
	public void close() throws AnyException
  {
    stream_.close();
  }
  
  public Any getAny()
  {
    return (Any)getValue();
  }
  */

	public Object clone() throws CloneNotSupportedException
	{
		AnyBlob b = (AnyBlob)super.clone();
		
		b.stream_ = (AbstractStream)stream_.clone();
		
		return b;
	}
	
  public Any bestowConstness()
  {
    return new ConstBlobDecor(this);
  }

  public void fillBlob(InputStream is, long length)
  {
    try
    {
      setStreams(is, null);

      //Any ret = stream_.read();
      Any ret;

      if (readHandler__.containsKey(stream_.getClass()))
      {
        // There's a handler, use it
        Func f = (Func)readHandler__.get(stream_.getClass());
        try
        {
          ret = f.exec(stream_);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
      }
      else
      {
        // There isn't, just perform a single read on the stream.
        ret = stream_.read();
      }

      stream_.close();
      this.setValue(ret);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  /**
   * Place the data represented by this blob into a stream from
   * which it can be read back
   */
  public InputStream sinkBlobStream()
  {
    try
    {
      ByteArrayOutputStream bos = new BlobByteArrayOutputStream(1024);
      setStreams(null, bos);
      stream_.write((Any)getValue(), null);
      stream_.close();
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray(),
                                                          0,
                                                          bos.size());
      return bis;
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public byte[] toByteArray()
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
      setStreams(null, bos);
      stream_.write((Any)getValue(), null);
      stream_.close();
      return bos.toByteArray();
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
//      if (!(a instanceof ObjectI))
//        throw new IllegalArgumentException("AnyBlob.copyFrom(): " + a.getClass());
//
//      // Apart from a friendlier exception above, we are happy to use the
//      // super class copy semantics
      super.copyFrom(a);
    }
    return this;
  }

  private void setStreams(InputStream is, OutputStream os) throws AnyException
  {
    stream_.setStreams(is, os);
  }
  
  // Just expose the underlying buffer so we don't allocate it twice
  private class BlobByteArrayOutputStream extends ByteArrayOutputStream
  {
    public BlobByteArrayOutputStream()
    {
      super();
    }

    public BlobByteArrayOutputStream(int size)
    {
      super(size);
    }

    public byte toByteArray()[]
    {
      return buf;
    }
  }

  // Read all lines and concatenate them into a single string
  // including the line terminators
  private static class ReadAllLines extends AbstractFunc
  {
		private static final long serialVersionUID = 1L;

		static String lineTerminator__ = SystemProperties.instance().getSystemProperties().get(new ConstString("line_separator")).toString();

    public Any exec(Any a) throws AnyException
    {
      com.inqwell.any.io.PrintStream s = (com.inqwell.any.io.PrintStream)a;
      StringBuffer sb = new StringBuffer();
      Any l;
      while ((l = s.read()) != AnyNull.instance())
      {
        sb.append(l.toString());
        sb.append(lineTerminator__);
      }
      return new AnyString(new String(sb));
    }
  }
}
