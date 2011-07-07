/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;

public interface BytesMessageI extends MessageI
{
  /**
   * Gets the number of bytes of the message body when the message
   * is in read-only mode. The value returned can be used to allocate a
   * byte array. The value returned is the entire length of the message body,
   * regardless of where the pointer for reading the message is currently located. 
   * @return number of bytes in the message
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public Any getBodyLength();
  
  /**
   * Read a value from the bytes message stream according to the type of the
   * given argument.
   * @param type a value to characterise the type of read performed
   * on the underlying bytes message. The argument is assigned to and returned.
   * @return the value read - the <code>type</code> argument is returned.
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public Any read(Any type);
  
  /**
   * Reads an unsigned 8-bit number from the bytes message stream.
   * @return An AnyInt
   */
  public Any readUnsignedByte();

  /**
   * Reads an unsigned 16-bit number from the bytes message stream.
   * @return An AnyInt
   */
  public Any readUnsignedShort();
  
  /**
   * Writes a value to the underlying bytes message. One of the
   * underlying writeXXX methods is called, according to the type
   * of the argument. 
   * @param value
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public void write(Any value);
  
  /**
   * Puts the message body in read-only mode and repositions the stream of bytes to the beginning.
   */
  public void reset();
}
