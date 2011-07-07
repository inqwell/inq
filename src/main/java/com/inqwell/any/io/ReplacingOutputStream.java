/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/ReplacingOutputStream.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 

package com.inqwell.any.io;

import com.inqwell.any.*;
import com.inqwell.any.server.BOTDescriptor;

import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * Extends JDK serialization to support object replacement on output
 * according to a replacement map.
 */
public class ReplacingOutputStream extends    ObjectOutputStream
                                   implements ReplacingStream
{
	private java.util.Map          outputReplaceMap_;
	private Any                    replacementInfo_;
  private SupportingOutputStream out_;
  
  // the typedefs we have sent
  private Map                    descrs_;
	
//	public ReplacingOutputStream() throws IOException, SecurityException
//	{
//		outputReplaceMap_ = new ClassMap();
//		enableReplaceObject(true);
//	}
//	
//	public ReplacingOutputStream(OutputStream out) throws IOException,
//																												SecurityException
//	{
//		super(out);
//		outputReplaceMap_ = new ClassMap();
//		enableReplaceObject(true);
//	}
	
	public ReplacingOutputStream(OutputStream  out,
															 java.util.Map replacements)
																					throws IOException,
																								 SecurityException
	{
		super(out);
		outputReplaceMap_ = replacements;
    descrs_           = AbstractComposite.simpleMap();
		enableReplaceObject(true);
	}
	
	public ReplacingOutputStream(SupportingOutputStream out,
															 java.util.Map replacements)
																					throws IOException,
																								 SecurityException
	{
		super(out);
    out_              = out;
		outputReplaceMap_ = replacements;
    descrs_           = AbstractComposite.simpleMap();
		enableReplaceObject(true);
	}

  public void reset() throws IOException
  {
    super.reset();
  }
  
  public void flush() throws IOException
  {
    super.flush();
//    if (gzip_ != null)
//    {
//      //gzip_.finish();
//      gzip_.flush();
//    }
  }

  public synchronized void writeAny(Any a) throws IOException
  {
    if (out_ != null)
    {
      // Because ObjectInputStream buffers input data we can't send
      // anything until a request to go into compressed mode is
      // acknowledged by the receiver.  This implementation requires
      // that an input/output pair are read/written by separate threads
      // and that, although compression modes are independent on a
      // bi-directional channel, both ends do not request a mode change
      // simultaneously.  To do so risks data loss by having it buffered
      // in the wrong place.
      if (out_.compressionChanging_)
      {
        try
        {
          wait();
        }
        catch(InterruptedException e)
        {
          throw new RuntimeContainedException(e);
        }
        writeObject(a);
      }
      else
      {
        writeObject(a);
      }
    }
    else
    {
      writeObject(a);
    }
  }
/*  
  public synchronized void writeAny(Any a) throws IOException
  {
    if (out_ != null)
    {
      if (out_.compressionChanging_)
      {
        if (a != CompressedOnAck.instance() && a != CompressedOffAck.instance())
        {
          try
          {
            wait();
          }
          catch(InterruptedException e)
          {
            throw new RuntimeContainedException(e);
          }
          writeObject(a);
        }
        else
        {
          // Hmmm...
          writeObject(a);
          flush();
          reset();
        }
      }
      else
      {
        writeObject(a);
      }
    }
    else
    {
      writeObject(a);
    }
  }
*/
  
  public synchronized void setCompressed(boolean compressed) throws IOException
  {
    if (out_ == null)
      throw new IllegalStateException("Not initialised for compression");
    
    if (out_.compressionChanging_)
      throw new IllegalStateException("Awaiting compress ack");
    
    // If the compression mode is changing then send the appropriate
    // flag in the current mode and then set the new mode.
    if (out_.isCompressed() && !compressed)
    {
      // Order of events is VERY important here because
      // ObjectOutputStream.reset() writes the TC_RESET protocol
      // byte to the underlying stream.  We don't want that to
      // be stuck in the stream of the mode we are leaving but
      // put into the mode we are going to.
      writeAny(CompressedFalse.instance());
      flush();
      out_.setCompressed(false);
      reset();
      out_.compressionChanging_ = true;
    }
    else if (!out_.isCompressed() && compressed)
    {
      writeAny(CompressedTrue.instance());
      flush();
      out_.setCompressed(true);
      reset();
      out_.compressionChanging_ = true;
    }
  }
  
  public boolean isCompressed()
  {
    if (out_ == null)
      return false;
    
    return out_.isCompressed();
  }
  
  public long getTotalIn()
  {
    if (out_ != null)
      return out_.getTotalIn();
    else
      return -1;
  }
  
  public long getTotalOut()
  {
    if (out_ != null)
      return out_.getTotalOut();
    else
      return -1;
  }
  
	protected Object replaceObject(Object o) throws IOException
	{
    StreamFunc f = (StreamFunc)outputReplaceMap_.get(o.getClass());
    
    // If there's no func then just hope its OK to serialise as is.
    // If there is a map then must be an any.
    if (f != null)
    {
      Any a = (Any)o;

      o = f.exec(a, this);
//				System.out.println ("Replaced : " + o.getClass());
    }

    return o;
	}
	
	public void setOutputReplaceMap(java.util.Map m)
	{
		outputReplaceMap_ = m;
	}
	
  public void setReplacementInfo(Any a, Any b)
  {
    replacementInfo_ = a;
  }
  
  public Any getReplacementInfo(Any a)
  {
    // We have expanded the simple-minded interface now. Revisit
    // when returning to remote inq.
    // So, this is called when BOTDescriptor (typedef) instances are
    // sent from server to client. We only want to send them once.
    // Thereafter we send a proxy.
    Any d;
    if ((d = descrs_.getIfContains(a)) != null)
    {
      // Descriptors implement equals by the FQName. We are particular
      // about the instance that was serialised in case it gets reparsed,
      // so even if it is found, check if it's a new one.
      if (d == a)
        return BOTDescriptor.PROXY;
      
      descrs_.replaceItem(a, a);
      return BOTDescriptor.REPLACE;
    }
    else
    {
      descrs_.add(a, a);
      return BOTDescriptor.REPLACE;
    }
  }
  
  // When we receive the compression mode change acknowledgement
  // from the opposing input stream and this method is called to
  // enable the stream to start sending again.  It is unsafe, for
  // reasons of buffered data being in the wrong place, to send
  // on the stream when the compression mode is changing until the
  // acknowledgement is seen.
  // If the compression change and its acknowledgement are processed
  // by a single thread then this protocol must be followed by the
  // caller.  See writeAny(Any a).
  synchronized void compressAck()
  {
    //System.out.println("COMPRESS ACK 1");
    if (!out_.compressionChanging_)
      throw new IllegalStateException("Compression mode is not changing");

    //System.out.println("COMPRESS ACK 2");
    out_.compressionChanging_ = false;
    notify();
  }
  
  //
  static public class SupportingOutputStream extends BufferedOutputStream
  {
    private CompressingOutputStream com_;
    
    private boolean          compressionChanging_;
    private boolean          compressed_;
    
    public SupportingOutputStream(OutputStream out)
    {
      super(out);
      com_ = new CompressingOutputStream(out);
    }
    
    public SupportingOutputStream(OutputStream out,
                                  int          size)
    {
      super(out, size);
      com_ = new CompressingOutputStream(out, size);
    }
  
    public long getTotalIn()
    {
      if (compressed_)
        return com_.getTotalIn();
      else
        return -1;
    }
    
    public long getTotalOut()
    {
      if (compressed_)
        return com_.getTotalOut();
      else
        return -1;
    }
    
    private void setCompressed(boolean compressed)
    {
      //System.out.println("WRITE COMPRESSED " + compressed);
      compressed_ = compressed;
    }
    
    private boolean isCompressed()
    {
      return compressed_;
    }
    
    // All BufferedOutputStream methods below
    public void write(int b) throws IOException
    {
      if (compressed_)
        com_.write(b);
      else
        super.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
      if (compressed_)
        com_.write(b, off, len);
      else
        super.write(b, off, len);
    }
    
    public void flush() throws IOException
    {
      if (compressed_)
        com_.flush();
      else
        super.flush();
    }

    public void write(byte[] b) throws IOException
    {
      if (compressed_)
        com_.write(b);
      else
        super.write(b);
    }
    
    public void close() throws IOException
    {
      if (compressed_)
        com_.close();
      else
        super.close();
    }
  }

  /**
   */
  static class CompressedTrue extends    SimpleEvent
                              implements Cloneable
  {
    static CompressedTrue instance__;
    
    static
    {
      // Make the only instance ever allowed in this JVM
      instance__ = new CompressedTrue();
    }
  
    public static Any instance()
    {
      return instance__;
    }
    
    private CompressedTrue()
    {
      super(EventConstants.COMPRESS_ON);
    }
    
    public Object clone() throws CloneNotSupportedException
    {
      return this;
    }

    protected Object readResolve() throws ObjectStreamException
    {
      return CompressedTrue.instance();
    }
  }
  
  static class CompressedFalse extends    SimpleEvent
                               implements Cloneable
  {
    static CompressedFalse instance__;
    
    static
    {
      // Make the only instance ever allowed in this JVM
      instance__ = new CompressedFalse();
    }
  
    public static Any instance()
    {
      return instance__;
    }
    
    private CompressedFalse()
    {
      super(EventConstants.COMPRESS_OFF);
    }
	
    public Object clone() throws CloneNotSupportedException
    {
      return this;
    }

    protected Object readResolve() throws ObjectStreamException
    {
      return CompressedFalse.instance();
    }
  }
  
  static public class CompressedOnAck extends    SimpleEvent
                                      implements Cloneable
  {
    static CompressedOnAck instance__;
    
    static
    {
      // Make the only instance ever allowed in this JVM
      instance__ = new CompressedOnAck();
    }
  
    public static Any instance()
    {
      return instance__;
    }
    
    private CompressedOnAck()
    {
      super(EventConstants.ACK_COMPRESS_ON);
    }
	
    public Object clone() throws CloneNotSupportedException
    {
      return this;
    }

    protected Object readResolve() throws ObjectStreamException
    {
      return CompressedOnAck.instance();
    }
  }
  
  static public class CompressedOffAck extends    SimpleEvent
                                       implements Cloneable
  {
    static CompressedOffAck instance__;
    
    static
    {
      // Make the only instance ever allowed in this JVM
      instance__ = new CompressedOffAck();
    }
  
    public static Any instance()
    {
      return instance__;
    }
    
    private CompressedOffAck()
    {
      super(EventConstants.ACK_COMPRESS_OFF);
    }
	
    public Object clone() throws CloneNotSupportedException
    {
      return this;
    }

    protected Object readResolve() throws ObjectStreamException
    {
      return CompressedOffAck.instance();
    }
  }
}
