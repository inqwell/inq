/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/sql/SqlStream.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 20:33:12 $
 */

package com.inqwell.any.io.sql;

import com.inqwell.any.*;
import com.inqwell.any.Process;

import java.io.InputStream;
import java.io.OutputStream;
import com.inqwell.any.io.PhysicalIO;

/**
 * Stream-style SQL i/o. This class supports I/O to an sql source/sink
 * such that it appears as a stream would, appending records as they
 * are written and returning them successively as they are read.
 * <p>
 */
public class SqlStream extends    PropertyAccessMap
                       implements PhysicalIO,
                                  Cloneable
{
  // When this is not null there is a result set outstanding and
  // subsequent records of it will be returned by read.
  private AnySql    anySql_;
  
  // Retrieval SQL statement.  Can be a prepared statement, a stored
  // proc or a simple sql statement.  This statement is executed when
  // the stream is opened for 'reading'
  private Any readSql_;
  
  // skeletal update/insert SQL statement
  private Any writeSql_;
  
  // skeletal SQL statement to be run when this stream is closed
  private Any closeSql_;

  // The resource id of the SQL resource to be used
  private Any       sqlLogin_;
  
  private ExceptionContainer e_ = new RuntimeContainedException(null);
  
  // Whether to use a prepared statement or a stored procedure call.
  // If both are false then a simple sql string parameterised by
  // a formatter will be used.  Otherwise one of st proc or a prepared
  // statement is used.  See property methods below.
  // Note that these properties apply to all the sql that will be
  // executed: read (executed on open), write and close.
  private boolean prepared_ = false;
  private boolean stproc_   = false;

  public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
  {
    //try
    //{
      // Acquire an SQL connection
      AnySql sql = (AnySql)SqlManager.instance().acquire(sqlLogin_);
      if (sql == null)
        throw new AnyException("Cannot acquire an SQL connection resource");//,
                               //sqlLogin_);
    //}
    return true;
  }
	
  /**
   * Streaming I/O.  Read the next item from the stream represented by this
   * object.
   * <p>
   * This is an optional operation and may not be implemented if streamed I/O
   * is not supported by the physical medium.
   * @return the object if an item was read, <code>null</code> otherwise
   * @exception AnyIOException if an exception is reported by the physical
   * medium.
   */
  public Any read () throws AnyException
  {
    if (anySql_ != null)
    {
      // result set open
      
    }
    return null;
  }

  public int read (Map       ioKey,
                   Map       outputProto,
                   Array     outputComposite,
                   int       maxCount) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
  {
    throw new UnsupportedOperationException();
  }


  /**
   * Streaming I/O.  Write the given item from the stream represented by this
   * object.
   * <p>
   * This is an optional operation and may not be implemented if streamed I/O
   * is not supported by the physical medium or if a separate key is required.
   * @return true if written successfully.
   * @exception AnyIOException if an exception is reported by the physical
   * medium.
   */
	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
    return false;
  }

  public boolean write (Map ioKey,
	                      Map outputItem,
	                      Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
	
	public boolean writeln (Any outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
	
  public void flush()
  {
  }
  
  public boolean delete (Map ioKey,
												 Map outputItem,
												 Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
	
  /**
   * Delete the given item using the given key.
   * <p>
   * Implemented to run the deleteSql statement, parameterised with
   * the given value. If the <code>prepared</code>
   * or <code>stproc</code> properties are set then the
   * supplied <code>Map</code> must carry ordering information.
   * <p>
   * The deleteSql may perform any operation, whether related or not
   * to any previous reads or writes and may delete zero, one or
   * many items.
   * @return true if deleted successfully.
   * @exception AnyException if an exception is reported by the physical
   * medium.
   */
	public boolean delete (Map outputItem, Transaction t) throws AnyException
  {
    return false;
  }
	
	/**
	 * Close the IO connection.  This implementation releases any
   * sql resources that may still be held.
	 */
	public void close()
  {
    if (anySql_ != null)
    {
      try
      {
        // Gah!
        e_.setThrowable(null);
        SqlManager.instance().release(sqlLogin_, anySql_, null, e_);
      }
      catch(Exception e)
      {
        throw new RuntimeContainedException(e);
      }
      
      if (e_.getThrowable() != null)
        throw new RuntimeContainedException(e_.getThrowable());
    }
  }
	
	/**
	 * Provide any auxilliary information that the connection requires
	 * to manage its operation
	 */
	public void setAuxInfo (Any a, Any subs) {}
  
  public void setStreams(InputStream is, OutputStream os) throws AnyException
  {
    // no-op for sql
  }
  
  // specific property methods
  public void setPrepared(boolean prepared)
  {
    prepared_ = prepared;
    
    // if we are prepared then we can't be stproc
    if(prepared)
      stproc_ = false;
  }
  
  public boolean isPrepared()
  {
    return prepared_;
  }
  
  public void setStProc(boolean stproc)
  {
    stproc_ = stproc;
    
    // if we are prepared then we can't be stproc
    if(stproc)
      prepared_ = false;
  }
  
  public boolean isStProc()
  {
    return prepared_;
  }
  
  // PropertyAccessMap stuff
  public boolean isEmpty() { return false; }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}
	public Iter createIterator () {return DegenerateIter.i__;}
	

}
