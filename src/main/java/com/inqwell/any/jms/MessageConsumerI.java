/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;

/**
 * Access to a JMS Message Consumer within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface MessageConsumerI extends Any
{
  /**
   * Closes the message consumer.
   */
  public void   close();
  
  /**
   * Gets the message consumer's MessageListener.
   * @return
   */
//TODO public MessageListener  getMessageListener()

  /**
   * Gets this message consumer's message selector expression.
   */
  public Any getMessageSelector();

  /**
   * Receives the next message produced for this message consumer.
   * @return the message
   */
  public MessageI  receive();

  /**
   * Receives the next message that arrives within the specified timeout interval.
   * @param timeout
   * @return
   */
  public MessageI receive(long timeout);

  /**
   * Receives the next message if one is immediately available.
   */
  public MessageI  receiveNoWait();

  /**
   * Sets the message consumer's MessageListener.
   * @param listener
   */

  /**
   * Sets the message consumer's MessageListener
   */
  public void setMessageListener(AnyMessageListener listener);


}
