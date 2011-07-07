/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import com.inqwell.any.AbstractIter;
import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.Iter;
import com.inqwell.any.RuntimeContainedException;

/**
 * QueueBrowserI implementation.
 * <p/>
 * To browse a queue in Inq script do the following:
 * <pre>
 *   any browser = mqcreatequeuebrowser(mySession, myQueue [, selector]);
 *   foreach(browser.properties.messages)
 *   {
 *     .
 *     .
 *     // each message is $loop
 *     writeln($catalog.system.out, $loop.properties.text); // eg text message
 *     .
 *   }
 *   mqclose(browser);
 * </pre>
 * 
 * @author tom
 *
 */
public class AnyQueueBrowser extends    DefaultPropertyAccessMap
                             implements QueueBrowserI
{
  private QueueBrowser b_;
  
  public AnyQueueBrowser(QueueBrowser browser)
  {
    b_ = browser;
  }

  @Override
  public void close()
  {
    try
    {
      b_.close();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public Any getMessages()
  {
    return new BrowserIter(b_);
  }
  
  @Override
  public Any getMessageSelector()
  {
    try
    {
      return new AnyString(b_.getMessageSelector());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public QueueI getQueue()
  {
    try
    {
      return new AnyQueue(b_.getQueue());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  // tidy JMS if GC
  protected void finalize() throws Throwable
  {
    try
    {
      b_.close();
      super.finalize();
    }
    catch (JMSException e)
    {
    }
  }

  /**
   * Wrap the enumeration provided by {@link QueueBrowser.getEnumeration}
   * in an Any {@link Iter} implementation.
   * <p/>
   * Although from its class hierarchy AnyQueueBrowser is a Map, it is
   * not a great idea to implement those iterator methods to browse the
   * queue. Firstly there are no keys and secondly inference of the
   * Map interface by ForEach would require this. Instead, require
   * script to access the <code>Iter</code> property, which returns one
   * of these. ForEach will call createIter on it, which we've implemented
   * to just return <code>this</code>.   
   * @author tom
   */
  static private class BrowserIter extends    AbstractIter
                                   implements Iter
  {

    private Enumeration enum_;
    
    private BrowserIter(QueueBrowser b)
    {
      try
      {
        enum_ = b.getEnumeration();
      }
      catch (JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    
    @Override
    public boolean hasNext()
    {
      return enum_.hasMoreElements();
    }

    @Override
    public Any next()
    {
      return AnyMessage.makeMessage((Message)enum_.nextElement());
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }

    @Override
    /**
     * Satisfy this method by returning self. Makes {@link ForEach} work.
     */
    public Iter createIterator()
    {
      return this;
    }
  }
}
