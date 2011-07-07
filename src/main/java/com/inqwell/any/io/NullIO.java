/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/NullIO.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 20:06:45 $
 */
 

package com.inqwell.any.io;

import com.inqwell.any.*;
import com.inqwell.any.Process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Always returns no objects and writes nothing.
 */

public class NullIO extends    AbstractAny
										implements PhysicalIO,
															 Cloneable
{

	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		return false;
	}

  /**
   * 
   */
  public Any read () throws AnyException
  {
  	return null;
  }

  /**
   * 
   */
  public int read (Map ioKey,
                   Map       outputProto,
                   Array     outputComposite,
                   int       maxCount) throws AnyException
  {
  	return 0;
  }

  /**
   * 
   */
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
	{
		return null;
	}

	
	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException("writeln " + getClass());
	}

  /**
   * 
   */
	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
  	return false;
  }

  public void setStreams(InputStream is, OutputStream os) throws AnyException
  {
		throw new UnsupportedOperationException("setStreams " + getClass());
  }
  
  /**
   * 
   */
  public boolean write (Map ioKey,
	                      Map outputItem,
	                      Transaction t) throws AnyException
	{
		return false;
	}
	                      
	
  /**
   * 
   */
  public boolean delete (Map ioKey,
												 Map outputItem,
												 Transaction t) throws AnyException
  {
  	return false;
  }                     
	
  /**
   *
   */
	public boolean delete (Map outputItem, Transaction t) throws AnyException
	{
		return false;
	}
	
	public void close()
	{
	}

  public void flush()
  {
  }
  
	public void setAuxInfo (Any a, Any subs) {}

  public Object clone() throws CloneNotSupportedException
  {
		return this;
  }
}
