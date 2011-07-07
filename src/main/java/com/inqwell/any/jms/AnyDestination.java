/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.Destination;

import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.PropertyAccessMap;

/**
 * Wrap a JMS <code>Destination</code>.
 * <p/>
// * This implementation supports scripted property access by extending
// * {@link PropertyAccessMap} however this is in anticipation of any
// * provider-specific implementations. The JMS Destination interface
// * itself defines no methods. For this reason all property names are
// * deemed to be owned by the Destination itself.
 * @author tom
 *
 */
public class AnyDestination extends DefaultPropertyAccessMap implements DestinationI
{
  private Destination dest_;
  
  private MessageProducerI producer_;
  private MessageConsumerI consumer_;
  
  public AnyDestination(Destination dest)
  {
    dest_ = dest;
  }
  
  public Destination getDestination()
  {
    return dest_;
  }
  
//  protected Object getPropertyOwner(Any property)
//  {
//    return dest_;
//  }
  
  public String toString()
  {
    return (dest_ != null) ? dest_.toString() : "null";
  }

  @Override
  public void setProducer(MessageProducerI producer)
  {
    producer_ = producer;
  }

  @Override
  public MessageProducerI getProducer()
  {
    if (producer_ == null)
      throw new AnyRuntimeException("No associated producer");

    return producer_;
  }

  @Override
  public MessageConsumerI getConsumer()
  {
    return consumer_;
  }

  @Override
  public void setConsumer(MessageConsumerI consumer)
  {
    consumer_ = consumer;
  }
}
