/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import com.inqwell.any.Any;
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.RuntimeContainedException;

public class AnySession extends    DefaultPropertyAccessMap
                        implements SessionI
{
  private Session sess_;
  
  public AnySession(Session sess)
  {
    sess_ = sess;
  }


  @Override
  public void close()
  {
    try
    {
      sess_.close();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void commit()
  {
    try
    {
      sess_.commit();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public BytesMessageI createBytesMessage(AnyByteArray bytes)
  {
    try
    {
      BytesMessage bm = sess_.createBytesMessage();
      
      if (bytes != null)
        bm.writeBytes(bytes.getValue());
      
      return new AnyBytesMessage(bm);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public MapMessageI createMapMessage()
  {
    try
    {
      return new AnyMapMessage(sess_.createMapMessage());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public MessageI createMessage()
  {
    try
    {
      return new AnyMessage(sess_.createMessage());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public ObjectMessageI createObjectMessage()
  {
    try
    {
      return new AnyObjectMessage(sess_.createObjectMessage());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public ObjectMessageI createObjectMessage(Any object)
  {
    try
    {
      return new AnyObjectMessage(sess_.createObjectMessage(object));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public AnyStreamMessage createStreamMessage()
  {
    try
    {
      return new AnyStreamMessage(sess_.createStreamMessage());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public AnyTextMessage createTextMessage()
  {
    try
    {
      return new AnyTextMessage(sess_.createTextMessage());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public AnyTextMessage createTextMessage(Any text)
  {
    try
    {
      return new AnyTextMessage(sess_.createTextMessage(text.toString()));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public int getAcknowledgeMode()
  {
    try
    {
      return sess_.getAcknowledgeMode();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public boolean getTransacted()
  {
    try
    {
      return sess_.getTransacted();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void recover()
  {
    try
    {
      sess_.recover();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void rollback()
  {
    try
    {
      sess_.rollback();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void unsubscribe(Any name)
  {
    try
    {
      sess_.unsubscribe(name.toString());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

// TODO: I guess these methods return the subtype appropriate to the
// type of destination. We should wrap in the appropriate wrapper subtype also.
  @Override
  public MessageConsumerI createConsumer(DestinationI destination)
  {
    try
    {
      return new AnyMessageConsumer(sess_.createConsumer(destination.getDestination()));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public MessageConsumerI createConsumer(DestinationI destination,
                                           Any messageSelector)
  {
    try
    {
      return new AnyMessageConsumer(sess_.createConsumer(destination.getDestination(),
                                                         messageSelector.toString()));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public MessageConsumerI createConsumer(DestinationI destination,
                                         Any          messageSelector,
                                         boolean      noLocal)
  {
    try
    {
      String sel = (messageSelector != null) ? messageSelector.toString() : null;
      
      return new AnyMessageConsumer(sess_.createConsumer(destination.getDestination(),
                                                         sel,
                                                         noLocal));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public TopicSubscriberI createDurableSubscriber(TopicI topic, Any name)
  {
    try
    {
      return new AnyTopicSubscriber(sess_.createDurableSubscriber(topic.getTopic(), name.toString()));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public TopicSubscriberI createDurableSubscriber(TopicI  topic,
                                                  Any     name,
                                                  Any     messageSelector,
                                                  boolean noLocal)
  {
    try
    {
      return new AnyTopicSubscriber(sess_.createDurableSubscriber
                                       (topic.getTopic(),
                                        name.toString(),
                                        messageSelector != null ? messageSelector.toString()
                                                                : null,
                                        noLocal));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public QueueI createQueue(Any queueName)
  {
    try
    {
      return new AnyQueue(sess_.createQueue(queueName.toString()));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public TopicI createTopic(Any topicName)
  {
    try
    {
      return new AnyTopic(sess_.createTopic(topicName.toString()));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public TemporaryQueueI createTemporaryQueue()
  {
    try
    {
      return new AnyTemporaryQueue(sess_.createTemporaryQueue());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }


  @Override
  public TemporaryTopicI createTemporaryTopic()
  {
    try
    {
      return new AnyTemporaryTopic(sess_.createTemporaryTopic());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

//TODO: I guess this method return the subtype appropriate to the
//type of destination. We should wrap in the appropriate wrapper subtype also.
  @Override
  public MessageProducerI createProducer(DestinationI destination)
  {
    try
    {
      Destination d = null;
      
      if (destination != null)
        d = destination.getDestination();
      
      return new AnyMessageProducer(sess_.createProducer(d));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  @Override
  public QueueBrowserI createBrowser(QueueI queue, Any messageSelector)
  {
    QueueBrowserI b;
    
    try
    {
      if (messageSelector == null)
        b = new AnyQueueBrowser(sess_.createBrowser(queue.getQueue()));
      else
        b = new AnyQueueBrowser(sess_.createBrowser(queue.getQueue(),
                                messageSelector.toString()));
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    return b;
  }

  public void setMessageListener(AnyMessageListener listener)
  {
    try
    {
      sess_.setMessageListener(listener);
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
      sess_.close();
      super.finalize();
    }
    catch (JMSException e)
    {
    }
  }

}
