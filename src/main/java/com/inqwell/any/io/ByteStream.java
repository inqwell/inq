/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/ByteStream.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-03 14:38:14 $
 */
 

package com.inqwell.any.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.CharI;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.FloatI;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;

/**
 * A PhysicalIO implementation that does not assume any particular
 * format.
 * <p>
 * When reading, an AnyByteArray is always returned and is subject
 * to a maximum buffer size of the value of
 * the <code>bufferSize</code> property.  The default buffer
 * size is 1024 bytes.  When writing
 * only fundamental types or AnyByteArray instances are supported.
 * 
 */

public class ByteStream extends AbstractStream
{
	private static final long serialVersionUID = 1L;

	private transient DataInputStream  dis_;
	private transient DataOutputStream dos_;
  
  private transient Writer w_;
  
  // The read buffer
  private transient AnyByteArray  array_;
	
  private transient MessageDigest digest_; 
  
  private IntI       bufferSize_ = new AnyInt();
  //private IntI       offset_     = new AnyInt(0);

  public ByteStream()
	{
    this(1024);
	}
	
  public ByteStream(int bufferSize)
  {
    bufferSize_.setValue(bufferSize);
  }
  
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		boolean ret = makeStreams(p, toOpen, mode);
    
    acceptStreams();
		
    if (dis_ != null && array_ == null)
      array_ = new AnyByteArray(new byte[bufferSize_.getValue()]);
      
		return ret;
	}

  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;
      
    return false;
  }

  /**
   * Read at most <code>bufferSize</code> bytes from the
   * underlying stream.
   */
  public Any read () throws AnyException
  {
    Any ret = AnyNull.instance();
		
		if (dis_ != null)
		{
		  int read = 0;
		  int off = 0;

		  try
      {
        boolean isPipe = isPipe();
        do
        {
          read = dis_.read(array_.getValue(),
                           off,
                           bufferSize_.getValue() - off);
        } while ((read >= 0) &&
                 (off += read) < bufferSize_.getValue() &&
                 isPipe); 
      }
      catch (IOException iox)
      {
        throw new ContainedException(iox);
      }

      // Did we actually read anything?
      if (off > 0 || read >= 0)
      {
        if (off < bufferSize_.getValue())
        {
          // If we've read partially then shrink the array to that size
          byte[] b = new byte[off];
          System.arraycopy(array_.getValue(), 0, b, 0, off);
          ret = new AnyByteArray(b);
        }
        else
          ret = array_;
      }
    }

  	return ret;
  }

  /**
   * 
   */
  public int read (Map ioKey,
                   Map       outputProto,
                   Array     outputComposite,
                   int       maxCount) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read(Map,Map,Array)"));
  }

  /**
   * 
   */
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read(Map,Map)"));
	}

  /**
   * Write one or more items to this stream.  If the given argument
   * is an array then each item is written to the stream
   * successively.
   */
	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
    if (w_ == null)
      w_ = new Writer();
      
    outputItem.accept(w_);

    return true;
  }

	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " writeln(Any)"));
	}
	
  /**
   * 
   */
  public boolean write (Map ioKey,
	                      Map outputItem,
                        Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " write(Map,Map)"));
	}
	                      
  /**
   * 
   */
  public boolean delete (Map ioKey,
												 Map outputItem,
                         Transaction t) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete(Map,Map)"));
  }                     
	
  /**
   *
   */
	public boolean delete (Map outputItem, Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete(Map)"));
	}
	
  protected boolean doCloseRead()
  {
    try
    {
      if (dis_ != null)
        dis_.close();
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      array_       = null;
      dis_         = null;
    }
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    try
    {
      if (dos_ != null)
      {
        dos_.flush();
        dos_.close();
      }
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      dos_         = null;
      w_           = null;
    }
    return true;
  }

  protected void doFlush()
  {
    try
    {
      if (dos_ != null)
        dos_.flush();
    }
    catch(IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public void setBufferSize(Any bufferSize)
  {
    int siz = bufferSize_.getValue();
    
    bufferSize_.copyFrom(bufferSize);
    if (bufferSize_.isNull())
    {
      bufferSize_.setValue(siz);
      throw new AnyRuntimeException("Cannot set null buffer size");
    }
    
    array_      = new AnyByteArray(new byte[bufferSize_.getValue()]);
  }
  
  public Any getBufferSize()
  {
    return bufferSize_;
  }
  
//  public void setOffset(Any offset)
//  {
//    int off = offset_.getValue();
//    
//    offset_.copyFrom(offset);
//    if (offset_.isNull())
//    {
//      offset_.setValue(off);
//      throw new AnyRuntimeException("Cannot set null offset");
//    }
//  }
//  
//  public Any getOffset()
//  {
//    return offset_;
//  }
  
  // TODO: proper protected interface
  public Any getAvailable()
  {
    if (isOpenRead())
    {
      try
      {
        return new AnyInt(dis_.available());
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
  
  public void setDigest(Any digestAlgorithm)
  {
  	try
  	{
	  	digest_ = MessageDigest.getInstance(digestAlgorithm.toString());
  	}
  	catch(NoSuchAlgorithmException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }
  
  /**
   * If there is a digest in effect then return the current digest value
   * and reset the algorithm.
   * @return digest or <code>null</code> if there is no digest in effect.
   */
  public Any getDigest()
  {
  	Any ret = null;
  	
  	if (digest_ != null)
  		ret = new AnyByteArray(digest_.digest());
  	
  	return ret;
  }
  
  /*
  public Any getDigestAlgotirhm()
  {
  	Any ret = null;
  	
  	if (digest_ != null)
      ret = new AnyString(digest_.getAlgorithm());
  	
  	return ret;
  }
  */
  
  public Object clone() throws CloneNotSupportedException
  {
		ByteStream s = (ByteStream)super.clone();
		s.dos_         = null;
		s.dis_         = null;
		s.w_           = null;
		s.digest_      = null;
		s.array_       = null;
		return s;
  }

  protected void acceptStreams() throws AnyException
  {
		try
		{
			if (ostream_ != null)
			{
				if (digest_ == null)
					dos_ = new DataOutputStream(new BufferedOutputStream(ostream_));
				else
				  dos_ = new DataOutputStream(new DigestOutputStream(new BufferedOutputStream(ostream_),
				  		                                               digest_));
			}
      
      if (istream_ != null)
      {
      	if (digest_ == null)
	        dis_ = new DataInputStream(new BufferedInputStream(istream_));
      	else
      		dis_ = new DataInputStream(new DigestInputStream(new BufferedInputStream(istream_),
      				                                             digest_));
      }
		}
		catch(Exception e)
		{
			throw new ContainedException(e);
		}
  }

  private class Writer extends AbstractVisitor
  {
		private static final long serialVersionUID = 1L;

		public void visitAnyBoolean (BooleanI b)
    {
      try
      {
        dos_.writeBoolean(b.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyByte (ByteI b)
    {
      try
      {
        dos_.writeByte(b.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyChar (CharI c)
    {
      try
      {
        dos_.writeChar(c.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyInt (IntI i)
    {
      try
      {
        dos_.writeInt(i.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyShort (ShortI s)
    {
      try
      {
        dos_.writeShort(s.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyLong (LongI l)
    {
      try
      {
        dos_.writeLong(l.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyFloat (FloatI f)
    {
      try
      {
        dos_.writeFloat(f.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyDouble (DoubleI d)
    {
      try
      {
        dos_.writeDouble(d.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitDecimal (Decimal d)
    {
      // BigDecimal is written as a double.  May be this is OK
      try
      {
        dos_.writeDouble(d.getValue().doubleValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyString (StringI s)
    {
      try
      {
        dos_.writeUTF(s.getValue());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitAnyDate (DateI d)
    {
      try
      {
        dos_.writeLong(d.getTime());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
    public void visitArray (Array a)
    {
      if (a instanceof AnyByteArray)
      {
        try
        {
          // When writing byte arrays we assume the entire array
          // is to be written out.
          AnyByteArray b = (AnyByteArray)a;
          byte[] ba = b.getValue();
          dos_.write(ba, 0, ba.length);
        }
        catch (Exception e)
        {
          throw new RuntimeContainedException(e);
        }
      }
      else
      {
        for (int i = 0; i < a.entries(); i++)
          a.get(i).accept(this);
      }
    }
  
    public void visitMap(Map m)
    {
      Iter i = m.createIterator();
      while (i.hasNext())
      {
        Any a = i.next();
        a.accept(this);
      }
    }

    public void visitUnknown(Any o)
    {
      unsupportedOperation(o);
    }
  }
}
