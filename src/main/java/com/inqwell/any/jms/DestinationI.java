/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.Destination;

import com.inqwell.any.Any;

/**
 * Access to a JMS message within the <code>Any</code> framework.
 * <p/>
 * A JMS <code>Destination</code> does not define any methods, so
 * this interface is really only for consistency with the other JMS
 * wrap-ups within the Any framework and value-added Inq features
 * 
 * @author tom
 * 
 */
public interface DestinationI extends Any
{
  /**
   * Return the underlying Destination
   * @return
   */
  public Destination getDestination();
  
  /**
   * Establish a producer for this destination. If a producer
   * has been set then messages can be sent to the destination
   * directly rather than via the associated producer
   * @param producer
   */
  public void setProducer(MessageProducerI producer);

  /**
   * Returns this destination's producer or null if no producer
   * has been set.
   * @return the producer
   */
  public MessageProducerI getProducer();

  /**
   * Establish a consumer for this destination. If a consumer
   * has been set then messages can be synchronously received
   * from the destination directly rather than via the associated
   * producer
   * @param producer
   */
  public void setConsumer(MessageConsumerI consumer);

  /**
   * Returns this destination's consumer or null if no consumer
   * has been set.
   * @return the producer
   */
  public MessageConsumerI getConsumer();
}
