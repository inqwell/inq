/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;

import com.inqwell.any.RuntimeContainedException;

public class AnyTopicSubscriber extends    AnyMessageConsumer
                                implements TopicSubscriberI
{
  public AnyTopicSubscriber(TopicSubscriber topicSub)
  {
    super(topicSub);
  }
  
  @Override
  public boolean getNoLocal()
  {
    try
    {
      return getConsumer().getNoLocal();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public TopicI getTopic()
  {
    try
    {
      return new AnyTopic(getConsumer().getTopic());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  private TopicSubscriber getConsumer()
  {
    return (TopicSubscriber)mc_;
  }
}
