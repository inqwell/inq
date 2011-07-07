/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;
import com.inqwell.any.Iter;

/**
 * Access to a JMS QueueBrowser within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface QueueBrowserI extends Any
{
  /**
   * Closes the QueueBrowser.
   */
  public void  close();

  /**
   * Gets a collection for browsing the current queue messages in the order they
   * would be received.
   * @return An implementation of {@link Iter} whose results browse the queue. 
   */
  public Any  getMessages();
  
  /**
   * Gets this queue browser's message selector expression.
   * @return
   */
  public Any getMessageSelector();
  
  /**
   * Gets the queue associated with this queue browser.
   */
  public QueueI  getQueue();
}
