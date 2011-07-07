/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/PrintStream.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-05-02 20:06:45 $
 */
 

package com.inqwell.any.io;

import com.inqwell.any.*;
import com.inqwell.any.Process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Perform IO to the underlying stream in optionally formatted printable
 * characters
 * <p>
 * 
 */

public class PrintStream extends AbstractStream
{
	private static final long serialVersionUID = 1L;

	private transient java.io.PrintStream ps_;
	private transient BufferedReader      reader_;
	
	private boolean existing_ = false;
	
	public PrintStream()
	{
	}
	
	/**
   * Wrap around an existing stream - used for things like
   * System.out
   */
	public PrintStream(java.io.PrintStream ps)
	{
    existing_ = true;
    ps_       = ps;
    ostream_  = ps;
  }
	
  /**
   * Wrap around an existing stream - used for things like
   * System.out
   */
  public PrintStream(java.io.InputStream is)
  {
    existing_ = true;
    istream_  = is;
    try
    {
      acceptStreams();
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		boolean ret = makeStreams(p, toOpen, mode);
    
    acceptStreams();
		
		return ret;
	}

  public Any read () throws AnyException
  {
		Any ret = AnyNull.instance();
		
		if (reader_ != null)
		{
      try
      {
        String s = reader_.readLine();
        if (s != null)
          ret = new AnyString(s);
      }
      catch (IOException iox)
      {
        throw new ContainedException(iox);
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
		throw (new UnsupportedOperationException());
  }

  /**
   * 
   */
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
	{
		throw (new UnsupportedOperationException());
	}

  /**
   * 
   */
	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
    /*
		if (outputItem instanceof Vectored)
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

	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
		write (outputItem, t);
		ps_.println("");
		return true;
	}
	
  /**
   * 
   */
  public boolean write (Map ioKey,
	                      Map outputItem,
                        Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException());
	}
	                      
  /**
   * 
   */
  public boolean delete (Map ioKey,
												 Map outputItem,
                         Transaction t) throws AnyException
  {
		throw (new UnsupportedOperationException());
  }                     
	
  /**
   *
   */
	public boolean delete (Map outputItem, Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException());
	}
	
  protected boolean doCloseRead()
  {
    if (!existing_)
    {
      try
      {
        if (reader_ != null)
          reader_.close();
      }
      catch(Exception e)
      {
        throw new RuntimeContainedException(e);
      }
      finally
      {
        reader_ = null;
      }
    }
    return !existing_;
  }
  
  protected boolean doCloseWrite()
  {
    if (!existing_)
    {
      try
      {
        if (ps_ != null)
          ps_.close();
      }
      catch(Exception e)
      {
        throw new RuntimeContainedException(e);
      }
      finally
      {
        ps_ = null;
      }
    }
    return !existing_;
  }

  protected void doFlush()
  {
    if (ps_ != null)
      ps_.flush();
  }
  
  public Object clone() throws CloneNotSupportedException
  {
		PrintStream s = (PrintStream)super.clone();
		
		s.ps_     = null;
		s.reader_ = null;
		
		return s;
  }
/*  
  protected void doCopyFrom(Any a)
  {
    if (!(a instanceof PrintStream))
      throw new IllegalArgumentException("Not a PrintStream");
    
    PrintStream ps = (PrintStream)a;
    
    this.ps_       = ps.ps_;
    this.reader_   = ps.reader_;
    this.existing_ = ps.existing_;
  }
*/  
  protected void acceptStreams() throws AnyException
  {
		try
		{
			if (ostream_ != null)
				ps_ = new java.io.PrintStream(ostream_);
      
      if (istream_ != null)
      {
        reader_ = new BufferedReader(new InputStreamReader(istream_));
      }
		}
		catch(Exception e)
		{
			throw new ContainedException(e);
		}
  }
  
	private void writeIt(Any outputItem) throws AnyException
	{
    if (ps_ == null)
      throw new AnyException("Not open for writing");
    
		try
		{
			ps_.print(outputItem);
		}
		catch (Exception e)
		{
			throw new ContainedException(e);
		}
	}
}
