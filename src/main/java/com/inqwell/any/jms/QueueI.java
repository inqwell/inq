/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.Queue;

import com.inqwell.any.Any;

/**
 * Access to a JMS Queue within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface QueueI extends DestinationI
{
  /**
   * Gets the name of this queue.
   * @return
   */
  public Any getQueueName();
  
  /**
   * Returns a string representation of this object.
   * @return
   */
  public String   toString();

  /**
   * Returns the underlying Queue.
   * @return
   */
  public Queue getQueue();
}
