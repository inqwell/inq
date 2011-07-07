/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/ResolvingInputStream.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 

package com.inqwell.any.io;

import com.inqwell.any.*;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import com.inqwell.any.beans.ClassMap;

/**
 * Extends JDK serialization to support object resolution on input
 * according to a replacement map.
 */
public class ResolvingInputStream extends    ObjectInputStream
                                  implements ReplacingStream
{
	private Map inputResolveMap_;
	private Any replacementInfo_;
  
  private SupportingInputStream in_;
  private ReplacingOutputStream out_;
	
//	public ResolvingInputStream() throws IOException, SecurityException
//	{
//		inputResolveMap_ = new ClassMap();
//		enableResolveObject(true);
//	}
//	
//	public ResolvingInputStream(OutputStream in) throws IOException,
//																												SecurityException
//	{
//		super(in);
//		inputResolveMap_ = new ClassMap();
//		enableResolveObject(true);
//	}
	
	public ResolvingInputStream(InputStream in,
															Map         replacements)
																					throws IOException,
																								 SecurityException,
																								 StreamCorruptedException
	{
		super(in);
		inputResolveMap_ = replacements;
		enableResolveObject(true);
	}
	
	public ResolvingInputStream(SupportingInputStream in,
                              ReplacingOutputStream out,
															Map         replacements)
																					throws IOException,
																								 SecurityException,
																								 StreamCorruptedException
	{
		super(in);
    in_              = in;
    out_             = out;
		inputResolveMap_ = replacements;
		enableResolveObject(true);
	}
	
  public Object readAny() throws IOException,
                                 ClassNotFoundException
  {
    Object o = null;
    
    if (in_ != null)
    {
      Any compressedTrue   = ReplacingOutputStream.CompressedTrue.instance();
      Any compressedFalse  = ReplacingOutputStream.CompressedFalse.instance();
      Any compressedOnAck  = ReplacingOutputStream.CompressedOnAck.instance();
      Any compressedOffAck = ReplacingOutputStream.CompressedOffAck.instance();
  
      do
      {
        //System.out.println("READ START");
        o = readObject();
        //System.out.println("READ object " + o);

        if (o == compressedTrue)
        {
          in_.setCompressed(true);
          out_.writeAny(compressedOnAck);
          out_.flush();
          out_.reset();
        }
        else if (o == compressedFalse)
        {
          in_.setCompressed(false);
          out_.writeAny(compressedOffAck);
          out_.flush();
          out_.reset();
        }
        else if (o == compressedOnAck || o == compressedOffAck)
          out_.compressAck();  // This will be returned
      }
      while (o == compressedTrue  ||
             o == compressedFalse);
    }
    else
      o = readObject();
      
    return o;
  }
  
	protected Object resolveObject(Object o) throws IOException
	{
//			System.out.println ("Resolving : " + o.getClass());
//			System.out.println ("Resolving : " + inputResolveMap_);
    StreamFunc f = (StreamFunc)inputResolveMap_.get(o.getClass());
    if (f != null)
    {
      Any a = (Any)o;

      o = f.exec(a, this);
//					System.out.println ("Resolved : " + o.getClass());
    }
		return o;
	}
	
	public void setInputResolveMap(Map m)
	{
		inputResolveMap_ = m;
	}

  public void setReplacementInfo(Any a, Any b)
  {
    replacementInfo_ = a;
  }
  
  public Any  getReplacementInfo(Any a)
  {
    return replacementInfo_;
  }
  
  static public class SupportingInputStream extends BufferedInputStream
  {
    private InflatingInputStream   ifl_;
    private boolean                compressed_;
    
    public SupportingInputStream(InputStream in)
    {
      super(in);
      ifl_ = new InflatingInputStream(in);
    }
    
    public SupportingInputStream(InputStream in, int size)
    {
      super(in, size);
      ifl_ = new InflatingInputStream(in, size);
    }
    
    private synchronized void setCompressed(boolean compressed) throws IOException
    {
      if (!compressed_ && compressed)
      {
        // When going into compressed mode we must check
        // if there is any data already buffered and if so
        // place it in the InflatingInputStream.  This is
        // not necessary when going into normal mode because
        // the InflatingInputStream does not buffer any extra
        // data than it needs.
        if (pos < count)
        {
          ifl_.pushBack(buf, pos, count-pos);
          System.out.println("PUSH BACK " + (count-pos));
          pos = count = 0;
        }
      }
      compressed_ = compressed;
      System.out.println("COMPRESSION " + compressed);
    }
    
    // All BufferedInputStream methods below.  Perform on this or
    // delegate to ifl_ when we are acting as a facade for it in
    // compressed mode.
    public synchronized int available() throws IOException
    {
      //System.out.println("AVAILABLE");
      if (compressed_)
        return ifl_.available();
      else
        return super.available();
    }
    
    public synchronized void mark(int readlimit)
    {
      //System.out.println("MARK");
      if (compressed_)
        ifl_.mark(readlimit);
      else
        super.mark(readlimit);
    }
 
    public synchronized int read() throws IOException
    {
      //System.out.println("READ()");
      if (compressed_)
        return ifl_.read();
      else
        return super.read();
    }
    
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
      //System.out.println("READ(b,off,len)");
      if (compressed_)
        return ifl_.read(b, off, len);
      else
        return super.read(b, off, len);
    }
    
    public synchronized void reset() throws IOException
    {
      //System.out.println("RESET");
      if (compressed_)
        ifl_.reset();
      else
        super.reset();
    }
    
    public synchronized long skip(long n) throws IOException
    {
      //System.out.println("SKIP");
      if (compressed_)
        return ifl_.skip(n);
      else
        return super.skip(n);
    }
    
    public synchronized int read(byte[] b) throws IOException
    {
      //System.out.println("READ(b)");
      if (compressed_)
        return ifl_.read(b);
      else
        return super.read(b);
    }
  }
}



