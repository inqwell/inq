/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;

/**
 * Access to a JMS Message Producer within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface MessageProducerI extends Any
{
  /**
   * Closes the message producer.
   */
  public void close();
  
  /**
   * Gets the producer's default delivery mode.
   * @return
   */
  public int getDeliveryMode();
  
  /**
   * Gets the destination associated with this MessageProducer.
   * @return
   */
  public DestinationI getDestination();

  /**
   * Gets an indication of whether message IDs are disabled.
   */
  public boolean  getDisableMessageID();
  
  /**
   * Gets an indication of whether message timestamps are disabled.
   */
  public boolean  getDisableMessageTimestamp();

  /**
   * Gets the producer's default priority.
   * @return
   */
  public int getPriority();
  
  /**
   * Gets the default length of time in milliseconds from its dispatch time that a produced message should be retained by the message system.
   * @return
   */
  public long getTimeToLive();

  /**
   * Sends a message to a destination for an unidentified message producer.
   * @param destination
   * @param message
   */
  public void   send(DestinationI destination, MessageI message);
  
  /**
   * Sends a message to a destination for an unidentified message producer, specifying delivery mode, priority and time to live.
   * @param destination
   * @param message
   * @param deliveryMode
   * @param priority
   * @param timeToLive
   */
  public void   send(DestinationI destination, MessageI message, int deliveryMode, int priority, long timeToLive);

  /**
   * Sends a message using the MessageProducer's default delivery mode, priority, and time to live.
   * @param message
   */
  public void   send(MessageI message);

  /**
   * Sends a message to the destination, specifying delivery mode, priority, and time to live.
   * @param message
   * @param deliveryMode
   * @param priority
   * @param timeToLive
   */
  public void   send(MessageI message, int deliveryMode, int priority, long timeToLive);

  /**
   * Sets the producer's default delivery mode.
   * @param deliveryMode
   */
  public void   setDeliveryMode(int deliveryMode);

  /**
   * Sets whether message IDs are disabled.
   * @param value
   */
  public void   setDisableMessageID(boolean value);

  /**
   * Sets whether message timestamps are disabled.
   * @param value
   */
  public void   setDisableMessageTimestamp(boolean value);

  /**
   * Sets the producer's default priority.
   * @param defaultPriority
   */
  public void   setPriority(int defaultPriority);

  /**
   * Sets the default length of time in milliseconds from its dispatch time that a produced message should be retained by the message system.
   * @param timeToLive
   */
  public void   setTimeToLive(long timeToLive);
}
