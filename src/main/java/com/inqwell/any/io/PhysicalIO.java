/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any.io;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines the operations supported by classes wishing to provide particular
 * I/O mechanisms such as relational databases or CSV files etc, for Any
 * structures.
 */

public interface PhysicalIO extends Any
{
	// open modes
	public static IntI read__   = new ConstInt(0);
	public static IntI write__  = new ConstInt(1);
	public static IntI append__ = new ConstInt(2);

  // A possible auxInfo entry
	static Any KEY_FIELDS  = new ConstString("key-fields");

	/**
	 * Open the IO connection.  The process opens the IO connection,
	 * if it is meaningful for the implementation to do so.  The
   * interpretation of the argument is implementation dependent.
	 */
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException;
	
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
  public Any read () throws AnyException;

  /**
   * Expect 0 or more result values according to the supplied key
   * and place the results as instances of the outputProto argument into
   * the Array container.
   * <P>
   * Optional operation.  Unsupported if the physical medium does not support
   * record-based I/O.
   * @return the number of items read
   * @exception AnyIOException if an exception is reported by the physical
   * medium.
   */
  public int read (Map       ioKey,
                   Map       outputProto,
                   Array     outputComposite,
                   int       maxCount) throws AnyException;

  /**
   * Expect at most one result value and return the result as an instance
   * of the outputProto argument.
   * <P>
   * Optional operation.  Unsupported if the physical medium does not support
   * record-based I/O.
   * @return the result value if an item was read; null otherwise.
   * @exception AnyIOException if an exception is reported by the physical
   * medium.
   */
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException;


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
	public boolean write (Any outputItem,
                        Transaction t) throws AnyException;

  /**
   * Write the given item to the record system using the given key.
   * <P>
   * Optional operation.  Unsupported if the physical medium does not support
   * record-based I/O.
   * @return true if written successfully.
   * @exception AnyIOException if an exception is reported by the physical
   * medium.
   */
  public boolean write (Map ioKey,
	                      Map outputItem,
                        Transaction t) throws AnyException;
	
	/**
	 * Write the output item and append the platform-specific line
	 * terminator
	 */
	public boolean writeln (Any outputItem,
                          Transaction t) throws AnyException;
	
	/**
	 * 
	 */
	public void flush();
	
  /**
   * Delete the given item using the given key.
   * <P>
   * Optional operation.  Unsupported if the physical medium does not support
   * record-based I/O.
   * @return true if deleted successfully.
   * @exception AnyIOException if an exception is reported by the physical
   * medium.
   */
  public boolean delete (Map ioKey,
												 Map outputItem,
                         Transaction t) throws AnyException;
	                      
	
  /**
   * Delete the given item using the given key.
   * <p>
   * This is an optional operation and may not be implemented if
   * a separate key is required.
   * @return true if deleted successfully.
   * @exception AnyIOException if an exception is reported by the physical
   * medium.
   */
	public boolean delete (Map outputItem,
                         Transaction t) throws AnyException;
	
	/**
	 * Close the IO connection.  Closes the IO connection, if it is
	 * meaningful for the implementation to do so.
	 */
	public void close();
	
	/**
	 * Provide any auxilliary information that the connection requires
	 * to manage its operation
	 */
	public void setAuxInfo (Any a, Any subs);
  
  public void setStreams(InputStream is, OutputStream os) throws AnyException;
  
}
