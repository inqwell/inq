/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/NativeStream.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-05-02 20:06:45 $
 */
 

package com.inqwell.any.io;

import com.inqwell.any.*;
import com.inqwell.any.Process;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import com.inqwell.any.io.ReplacingOutputStream;
import com.inqwell.any.io.ResolvingInputStream;

/**
 * Perform IO to the underlying stream in <code>&lt;inq&gt;</code> native
 * format.
 * <p>
 * The <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup> native
 * format is JDK serialization using the serialized form of a node
 * structure, which is summarised here.
 * <p>
 * <ul>
 * <li>The object structure is preserved and recreated from the stream when
 * read back.
 * <li>Managed objects are restored as non-transactional, unmanaged ones
 * as general streams do not represent persistence repositories of
 * application entities, unlike the i/o medium of a BOT definition.
 * <li>A Managed Object's Descriptor is written as a path reference to
 * its entry in the system catalog.  When restored this reference is
 * applied to the currently prevailing catalog.  If successfully
 * resolved, the BOT descriptor will set into the object.  If the
 * BOT descriptor cannot be found then the degenerate descriptor is
 * used in its place.  Thus, it is always possible to read
 * an <code>&lt;inq&gt;</code> structure irrespective of whether it
 * is restored in the same environment in which it was written.
 * </ul>
 * <p>
 * 
 */

public class NativeStream extends AbstractStream
{
	private static final long serialVersionUID = 1L;

	private transient ObjectOutputStream oos_;
	private transient ObjectInputStream  ois_;
	
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		boolean ret = makeStreams(p, toOpen, mode);
    
    try
    {
      acceptStreams();
    }
    catch(Exception e)
    {
      // Allow open to fail not with an exception but
      // by returning false. If this happens then also close
      // the streams. By this time they are made but the
      // xml parsing has barfed.
      close();
      ret = false;
    }
		return ret;
	}

  public Any read () throws AnyException
  {
		Any ret = null;
		try
		{
			ret = (Any)ois_.readObject();
		}
		catch (Exception e)
		{
			//throw new ContainedException(e);
			//return null silently - likely serialized form discrepancies.
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
   * Write one or
   * more <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup>
   * structures to this stream.  If the given argument is
   * an array then each item is written to the stream as a separate
   * object.  When read back, each of the array children will be
   * returned by successive calls to <code>read</code>.
   */
	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
    /*
		if (outputItem instanceof Array)
		{
			Iter i = outputItem.createIterator();
			while (i.hasNext())
			{
				writeIt(i.next());
			}
		}
		else
		{
			writeIt(outputItem);
		}
    */
    
    writeIt(outputItem);
  	return true;
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
	                      
	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException("writeln " + getClass());
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
      if (ois_ != null)
        ois_.close();
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      ois_ = null;
    }
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    try
    {
      if (oos_ != null)
        oos_.close();
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      oos_ = null;
    }
    return true;
  }

  protected void doFlush()
  {
    try
    {
      if (oos_ != null)
        oos_.flush();
    }
    catch(IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public Object clone() throws CloneNotSupportedException
  {
		NativeStream s = (NativeStream)super.clone();
		s.oos_ = null;
		s.ois_ = null;
		return s;
  }
/*  
  protected void doCopyFrom(Any a)
  {
    if (!(a instanceof NativeStream))
      throw new IllegalArgumentException("Not a NativeStream");
    
    NativeStream ns = (NativeStream)a;
    
    this.oos_  = ns.oos_;
    this.ois_  = ns.ois_;
  }
*/  
  protected void acceptStreams() throws AnyException
  {
		// The native format uses JDK serialization with the various
		// serialized forms imposed by the <inq>TM runtime.
		
		try
		{
			if (istream_ != null)
				ois_ = new ResolvingInputStream(istream_,
																				Globals.streamInputReplacements__);

			if (ostream_ != null)
				oos_ = new ReplacingOutputStream(ostream_,
																				 Globals.streamOutputReplacements__);
		}
		catch(Exception e)
		{
			throw new ContainedException(e);
		}
  }
  
	private void writeIt(Any outputItem) throws AnyException
	{
		try
		{
			oos_.writeObject(outputItem);
			oos_.flush();
			oos_.reset();
		}
		catch (Exception e)
		{
			throw new ContainedException(e);
		}
	}
}
