/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.RuntimeContainedException;

public class AnyMessageConsumer extends    DefaultPropertyAccessMap
                                implements MessageConsumerI
{

  protected MessageConsumer mc_;
  
  public AnyMessageConsumer(MessageConsumer mc)
  {
    mc_ = mc;
  }

  @Override
  public void close()
  {
    try
    {
      mc_.close();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public Any getMessageSelector()
  {
    try
    {
      return new AnyString(mc_.getMessageSelector());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public MessageI receive()
  {
    try
    {
      MessageI ret = null;
      
      Message m = mc_.receive();
      
      if (m != null)
        ret = AnyMessage.makeMessage(m);
      
      return ret;
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public MessageI receive(long timeout)
  {
    try
    {
      MessageI ret = null;
      
      Message m = mc_.receive(timeout);
      
      if (m != null)
        ret = AnyMessage.makeMessage(m);
      
      return ret;
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public MessageI receiveNoWait()
  {
    try
    {
      MessageI ret = null;
      
      Message m = mc_.receiveNoWait();
      
      if (m != null)
        ret = AnyMessage.makeMessage(m);
      
      return ret;
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setMessageListener(AnyMessageListener listener)
  {
    try
    {
      mc_.setMessageListener(listener);
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
      mc_.close();
      super.finalize();
    }
    catch (JMSException e)
    {
    }
  }
}
