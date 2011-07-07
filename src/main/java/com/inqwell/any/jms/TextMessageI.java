/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;

public interface TextMessageI extends MessageI
{
  /**
   * Gets the text of this message's data. This method expects
   * the message content to be an Any
   * @return the text contained within the message
   * @throws AnyRuntimeException if any error occurs when fetching
   * the text from the message. 
   */
  public Any getText();
  
  /**
   * Sets the text for this message's data.
   * @param a the Any 
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public void setText(Any a);
}
