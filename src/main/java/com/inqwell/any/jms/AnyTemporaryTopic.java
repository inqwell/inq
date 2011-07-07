/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.TemporaryTopic;

import com.inqwell.any.RuntimeContainedException;

public class AnyTemporaryTopic extends    AnyTopic
                               implements TemporaryTopicI
{
  public AnyTemporaryTopic(TemporaryTopic topic)
  {
    super(topic);
  }

  @Override
  public void delete()
  {
    try
    {
      getTempTopic().delete();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  private TemporaryTopic getTempTopic()
  {
    return (TemporaryTopic)getTopic();
  }
}
