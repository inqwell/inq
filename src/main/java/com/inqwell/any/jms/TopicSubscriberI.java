/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;


/**
 * Access to a JMS TopicSubscriber within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface TopicSubscriberI extends MessageConsumerI
{
  /**
   * Gets the NoLocal attribute for this subscriber.
   * @return true if NoLocal
   */
  boolean   getNoLocal();

  /**
   * Gets the Topic associated with this subscriber.
   * @return the Topic
   */
  TopicI  getTopic();
}
