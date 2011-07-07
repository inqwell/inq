/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.Queue;

import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.RuntimeContainedException;

public class AnyQueue extends    AnyDestination
                      implements QueueI
{
  public AnyQueue(Queue queue)
  {
    super(queue);
  }
  
  public Queue getQueue()
  {
    return (Queue)getDestination();
  }

  @Override
  public Any getQueueName()
  {
    try
    {
      return new AnyString(getQueue().getQueueName());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
}
