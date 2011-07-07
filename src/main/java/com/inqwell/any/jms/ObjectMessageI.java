/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;

public interface ObjectMessageI extends MessageI
{
  /**
   * Gets the object containing this message's data. This method expects
   * the message content to be an Any
   * @return the Any implementation contained within the message
   * @throws AnyRuntimeException if any error occurs when fetching
   * the Any from the message. This includes the case if the
   * message content is not an Any 
   */
  public Any getObject();
  
  /**
   * Sets the Any containing this message's data.
   * @param a the Any 
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public void setObject(Any a);
  
  public Any getAny();

  public void setAny(Any a);
}
