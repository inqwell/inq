/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/Serialize.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ExceptionContainer;
import com.inqwell.any.io.ResolvingInputStream;
import com.inqwell.any.io.ReplacingOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.io.IOException;


/**
 * A channel driver that uses an <code>ObjectInputStream</code> and
 * <code>ObjectOutputStream</code> transfer Any structures
 * using Java serialization.  Most probably, the object streams
 * will be constructed around io streams returned from sockets.
 * <p>
 * The channel may support either or both of read/write depending
 * on what streams are supplied.  If unavailable a method 
 * throws an UnsupportedOperationException.
 * <p>
 * Classes within the structure to be written may, of course,
 * impose serialization semantics of their own design.
 */
public class Serialize extends    AbstractChannelDriver
											 implements ChannelDriver
{
	private ResolvingInputStream  is_;
	private ReplacingOutputStream os_;
	
	public Serialize(ResolvingInputStream is)
	{
		this(is, null);
  }
	
	public Serialize(ReplacingOutputStream os)
	{
		this(null, os);
  }
	
	public Serialize(ResolvingInputStream is, ReplacingOutputStream os)
	{
		is_ = is;
		os_ = os;
  }
	
  public Any read() throws AnyException
  {
		if (is_ == null)
	    throw (new UnsupportedOperationException());
		
		Any ret = null;
		
		try
		{
			ret = (Any)is_.readAny();
		}
		catch (EOFException eofe)
		{
      ret = InputChannel.shutdown__;
			// Somebody shutdown the stream for an unreliable protocol.
		}
		catch (Exception e)
		{
			// Exceptions from the underlying stream
			AnyException.throwExternalException (e);
		}
		
		return ret;
	}
    
  public void write(Any a) throws AnyException
  {
		if (os_ == null)
	    throw (new UnsupportedOperationException());
		
    try
    {
			if (a instanceof ExceptionContainer)
			{
				writeIt(((ExceptionContainer)a).collapseException());
			}
			else
			{
				writeIt(a);
			}
    }
    catch (Exception e)
    {
      throw (new ContainedException(e));
    }
  }
  
  public void purgeReceived()
  {
  }
  
	public boolean isEmpty()
	{
//	  if (is_ == null)
//			return true;
//		else
//			return false;
    return false;
	}
	
  public boolean isFull()
  {
//	  if (os_ == null)
//			return true;
//		else
//			return false;
    return false;
  }
  
  /**
   * Return the total number of bytes sent so far on this channel
   * driver before compressing. Only relevant when the underlying
   * output stream supports compression and is operating in compressed
   * mode.
   * @return the total number of uncompressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalIn()
  {
    if (os_ != null)
      return os_.getTotalIn();
    else
      return -1;
  }
  
  /**
   * Return the total number of bytes sent so far on this channel
   * after compressing. Only relevant when the underlying
   * output stream supports compression and is operating in compressed
   * mode.
   * @return the total number of compressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalOut()
  {
    if (os_ != null)
      return os_.getTotalOut();
    else
      return -1;
  }
  
  public void setCompressed(boolean compressed)
  {
    if (os_ != null)
    {
      try
      {
        os_.setCompressed(compressed);
      }
      catch(IOException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
  
  public boolean isCompressed()
  {
    if (os_ != null)
      return os_.isCompressed();
    
    return false;
  }
  
  void setInputStream(ResolvingInputStream is)
  {
    is_ = is;
  }

  ObjectInputStream getInputStream()
  {
    return is_;
  }

  protected void doClose() throws AnyException
  {
		Exception ie = null;
		Exception oe = null;
		
		try
		{
			if (os_ != null)
			{
				os_.close();
				os_ = null;
			}
		}
		catch (Exception oex)
		{
			oe = oex;
			os_ = null;
		}
		
		try
		{
			if (is_ != null)
			{
				// Only close the input stream if it is drained
				if (is_.available() == 0)
			  {
					is_.close();
					is_ = null;
			  }
			}
		}
		catch (Exception iex)
		{
			ie = iex;
		}
		
		if (ie != null)
			AnyException.throwExternalException (ie);
		if (oe != null)
			AnyException.throwExternalException (oe);
	}
	
	/**
	 * Define cloning.  In fact, this class is immutable and therefore
	 * cloning is not relevant and we just return
	 * <i>this</i>.  Cloning must be defined, however, as these objects 
	 * may be transmitted through local channels, which are at liberty
	 * to clone what they transport, if they so wish.
	 */  
  public Object clone()
  {
		return this;
  }

	protected void finalize() throws Throwable
	{
		super.finalize();
		this.close();
	}
	
	private void writeIt(Any a) throws Exception
	{
		//System.out.println ("Serialize.writeIt(a): " + a);
		os_.writeAny(a);
		os_.flush();
		os_.reset();
	}
}
