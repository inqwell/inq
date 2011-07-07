/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.TemporaryQueue;

import com.inqwell.any.RuntimeContainedException;

public class AnyTemporaryQueue extends    AnyQueue
                               implements TemporaryQueueI
{
  public AnyTemporaryQueue(TemporaryQueue queue)
  {
    super(queue);
  }

  @Override
  public void delete()
  {
    try
    {
      getTempQueue().delete();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  private TemporaryQueue getTempQueue()
  {
    return (TemporaryQueue)getQueue();
  }
}
