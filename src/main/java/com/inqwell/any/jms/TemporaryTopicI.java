/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

/**
 * Access to a JMS TemporaryTopic within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface TemporaryTopicI extends TopicI
{
  public void delete();
}
