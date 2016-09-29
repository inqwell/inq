/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/ZipStream.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-03 14:38:14 $
 */
 

package com.inqwell.any.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFile;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DateI;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
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

public class ZipStream extends AbstractStream
{
	private static final long serialVersionUID = 1L;

	private transient ZipInputStream  zis_;
	private transient ZipOutputStream zos_;

  // The read buffer
  private transient AnyByteArray  array_;
  
  // The current ZipEntry
  private ZipEntry zipEntry_;
	
  private IntI     bufferSize_ = new AnyInt();
  
  private static Any NAME = AbstractValue.flyweightString("name");
  private static Any SIZE = AbstractValue.flyweightString("size");
  private static Any TIME = AbstractValue.flyweightString("time");
  private static Any FILE = AbstractValue.flyweightString("file");

  public ZipStream()
	{
    this(1024);
	}
	
  public ZipStream(int bufferSize)
  {
    bufferSize_.setValue(bufferSize);
  }
  
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		boolean ret = makeStreams(p, toOpen, mode);
    
    acceptStreams();
		
    if (zis_ != null && array_ == null)
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
		
		if (zis_ != null)
		{
		  int read = 0;
		  int off = 0;

		  try
      {
        boolean isPipe = isPipe();
        do
        {
          read = zis_.read(array_.getValue(),
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

	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
		try
		{
      // We assume the entire array
      // is to be written out.
      AnyByteArray b = (AnyByteArray)outputItem;
      byte[] ba = b.getValue();
      zos_.write(ba, 0, ba.length);
		}
		catch (IOException e)
		{
			throw new RuntimeContainedException(e);
		}
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
      if (zis_ != null)
        zis_.close();
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      array_       = null;
      zis_         = null;
    }
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    try
    {
      if (zos_ != null)
      {
      	if (zipEntry_ != null)
      		zos_.closeEntry();
      	zipEntry_ = null;
        zos_.flush();
        zos_.close();
      }
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      zos_         = null;
    }
    return true;
  }

  protected void doFlush()
  {
    try
    {
      if (zos_ != null)
        zos_.flush();
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
  
  /**
   * Set a new ZipEntry in the output stream, closing any existing.
   */
  public void setZipEntry(Any a)
  {
  	if (zos_ == null)
  		throw new AnyRuntimeException("Stream is not open for write");
  	
  	if (!(a instanceof Map))
  		throw new AnyRuntimeException("ZipEntry must be a map");
  	
  	ZipEntry z;
    Map m = (Map)a;
    
		z = new ZipEntry(m.get(NAME).toString());
		
		if (m.contains(TIME))
		{
			Any t = m.get(TIME);
			if (t instanceof DateI)
				z.setTime(((DateI)t).getTime());
			else if (t instanceof LongI)
				z.setTime(((LongI)t).getValue());
			else if (t instanceof IntI)
				z.setTime(((IntI)t).getValue());
		}
		
		if (m.contains(SIZE))
		{
			Any s = m.get(SIZE);
			if (s instanceof LongI)
				z.setTime(((LongI)s).getValue());
			else if (s instanceof IntI)
				z.setTime(((IntI)s).getValue());
		}
		
		if (m.contains(FILE))
		{
  		AnyFile f = (AnyFile)m.get(FILE);
  		z.setTime(f.getLastModified().getTime());
  		z.setSize(f.getFile().length());
		}
		
  	try
  	{
    	if (zipEntry_ != null)
    	{
    		zos_.closeEntry();
    	  zipEntry_ = null;
    	}
  	
    	zos_.putNextEntry(z);
    	zipEntry_ = z;
  	}
  	catch(IOException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }
  
  public Object clone() throws CloneNotSupportedException
  {
		ZipStream s = (ZipStream)super.clone();
		s.zos_         = null;
		s.zis_         = null;
		s.array_       = null;
		return s;
  }

  protected void acceptStreams() throws AnyException
  {
		try
		{
			if (ostream_ != null)
			{
				zos_ = new ZipOutputStream(new BufferedOutputStream(ostream_));
			}
      
      if (istream_ != null)
      {
        zis_ = new ZipInputStream(new BufferedInputStream(istream_));
      }
		}
		catch(Exception e)
		{
			throw new ContainedException(e);
		}
  }
}
