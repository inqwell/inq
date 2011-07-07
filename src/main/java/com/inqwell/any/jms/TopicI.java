/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.Topic;

import com.inqwell.any.Any;

/**
 * Access to a JMS Topic within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface TopicI extends DestinationI
{
  /**
   * Gets the name of this topic.
   * @return
   */
  public Any getTopicName();
  
  /**
   * Returns a string representation of this object.
   * @return
   */
  public String toString();
  
  /**
   * Returns the underlying Topic.
   * @return
   */
  public Topic getTopic();
}
