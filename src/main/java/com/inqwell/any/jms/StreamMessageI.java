/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;

public interface StreamMessageI extends MessageI
{
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
