/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;

import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.RuntimeContainedException;

public class AnyMessageProducer extends    DefaultPropertyAccessMap
                                implements MessageProducerI
{
  private MessageProducer producer_;
  
  public AnyMessageProducer(MessageProducer producer)
  {
    producer_ = producer;
  }

  @Override
  public void close()
  {
    try
    {
      producer_.close();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public int getDeliveryMode()
  {
    try
    {
      return producer_.getDeliveryMode();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public DestinationI getDestination()
  {
    try
    {
      return new AnyDestination(producer_.getDestination());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public boolean getDisableMessageID()
  {
    try
    {
      return producer_.getDisableMessageID();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public boolean getDisableMessageTimestamp()
  {
    try
    {
      return producer_.getDisableMessageTimestamp();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public int getPriority()
  {
    try
    {
      return producer_.getPriority();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public long getTimeToLive()
  {
    try
    {
      return producer_.getTimeToLive();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void send(DestinationI destination,
                   MessageI message)
  {
    try
    {
      producer_.send(destination.getDestination(), message.getJMSMessage());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void send(DestinationI destination,
                   MessageI     message,
                   int          deliveryMode,
                   int          priority,
                   long         timeToLive)
  {
    try
    {
      producer_.send(destination.getDestination(),
                     message.getJMSMessage(),
                     deliveryMode,
                     priority,
                     timeToLive);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void send(MessageI message)
  {
    try
    {
      producer_.send(message.getJMSMessage());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void send(MessageI message,
                   int      deliveryMode,
                   int      priority,
                   long     timeToLive)
  {
    try
    {
      producer_.send(message.getJMSMessage(),
                     deliveryMode,
                     priority,
                     timeToLive);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void setDeliveryMode(int deliveryMode)
  {
    try
    {
      producer_.setDeliveryMode(deliveryMode);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void setDisableMessageID(boolean value)
  {
    try
    {
      producer_.setDisableMessageID(value);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void setDisableMessageTimestamp(boolean value)
  {
    try
    {
      producer_.setDisableMessageTimestamp(value);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void setPriority(int defaultPriority)
  {
    try
    {
      producer_.setPriority(defaultPriority);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void setTimeToLive(long timeToLive)
  {
    try
    {
      producer_.setTimeToLive(timeToLive);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  protected void finalize() throws Throwable
  {
    try
    {
      producer_.close();
      super.finalize();
    }
    catch (JMSException e)
    {
    }
  }
}
