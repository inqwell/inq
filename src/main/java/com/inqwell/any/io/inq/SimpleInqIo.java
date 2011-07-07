/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/inq/SimpleInqIo.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 20:32:51 $
 */

package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.AnyIOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Perform i/o to an inq source.
 * <p>
 * Instances of this class to not represent inq connections
 * directly, instead they acquire the required connection from
 * the environment's <code>InqIoManager</code>, which has been separately
 * configured.
 * <p>
 * This class is thread-safe.
 */
public class SimpleInqIo extends    AbstractAny
												 implements PhysicalIO,
                                    Cloneable
{
  // Contains the resource identity that should
  // be used for this instance.
  private Any        resourceId_;
  
  private Descriptor descriptor_;
  
	/** 
	 * 
	 */
  public SimpleInqIo (Descriptor descriptor)
  {
    descriptor = descriptor_;
  }

	/**
	 * No operation.
	 */
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		return false;
	}

  public int read (Map   ioKey,
                   Map   outputProto,
                   Array outputComposite,
                   int   maxCount) throws AnyException
  {
		InqIo inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
		int count = 0;
		try
		{
			Array a = (Array)inqIo.read(descriptor_, ioKey);

			Map result = (Map)outputProto.cloneAny();
		  count = a.entries();

			for (int i = 0; i < count; i++)
			{
				outputComposite.add (a.get(i).cloneAny());
			}
//			System.out.println ("SimpleInqIo.read result is " + count + " " + outputComposite);
		}
		
		finally
		{
			InqIoManager.instance().release(resourceId_, inqIo, null, null);
		}
		return count;
  }

  public Any read () throws AnyException
  {
    throw new UnsupportedOperationException (getClass() + " read()");
	}
	
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
	{
		//System.out.println ("SimpleInqIo.read " + resourceId_);
		InqIo inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
		
		Map result = null;

		try
		{
			Any m = inqIo.read(descriptor_, ioKey);

      if (m != null)
      {
        result = (Map)outputProto.cloneAny();
        result.copyFrom(m);
      }
		}

		finally
		{
			InqIoManager.instance().release(resourceId_, inqIo, null, null);
		}
		
		return result;
	}

	public boolean write (Map ioKey,
                        Map outputItem,
                        Transaction t) throws AnyException
	{
		return write(outputItem, t);
	}
	
	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException("writeln " + getClass());
	}
	
  public boolean write (Any outputItem, Transaction t) throws AnyException
  {
		Map m = (Map)outputItem;
		boolean written = false;
		
		InqIo inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
			
    try
    {
      // In the SQL implementation we don't require a separate key as
      // we assume that the key and non-key fields are held in the
      // output item.
      written = inqIo.write(descriptor_, (Map)outputItem);
    }

    finally
    {
			InqIoManager.instance().release(resourceId_, inqIo, null, null);
    }
    
    return written;
	}
	
  public boolean delete (Map ioKey,
												 Map outputItem,
												 Transaction t) throws AnyException
	{
		return delete(outputItem, t);
	}
	
	public boolean delete (Map outputItem, Transaction t) throws AnyException
	{

		boolean written = false;
		
		InqIo inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
			
    try
    {
      written = inqIo.delete(descriptor_, outputItem);
    }

    finally
    {
			InqIoManager.instance().release(resourceId_, inqIo, null, null);
    }
    
		return written;
	}
	
	public void useInqServer(Any a) throws AnyException
	{
		resourceId_ = EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
                                    Catalog.instance().getCatalog(),
                                    a);
	}
	
	public void setAuxInfo (Any a, Any subs)
	{
	}
	
  public void setStreams(InputStream is, OutputStream os) throws AnyException
  {
		throw new UnsupportedOperationException("setStreams " + getClass());
  }
  
	/**
	 * No operation.
	 */
	public void close()
	{
	}
	
  public void flush()
  {
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    SimpleInqIo s = (SimpleInqIo)super.clone();
    
    // only needs shallow copy as all members are read-only
    return s;
  }
}
