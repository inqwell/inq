/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.Topic;

import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.RuntimeContainedException;


public class AnyTopic extends    AnyDestination
                      implements TopicI
{
  public AnyTopic(Topic topic)
  {
    super(topic);
  }
  
  public Topic getTopic()
  {
    return (Topic)getDestination();
  }

  @Override
  public Any getTopicName()
  {
    try
    {
      return new AnyString(getTopic().getTopicName());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
}
