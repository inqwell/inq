/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.DeliveryMode;
import javax.jms.Session;

import com.inqwell.any.Any;
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.ConstInt;

/**
 * Access to a JMS Session within the <code>Any</code> framework.
 * <p/>
 * Not all the underlying interface methods (on JMS Connection) are
 * replicated here, only the ones we (intend to) use.
 * 
 * @author tom
 * 
 */
public interface SessionI extends Any
{
  public static Any AUTO_ACKNOWLEDGE    = new ConstInt(Session.AUTO_ACKNOWLEDGE);
  public static Any CLIENT_ACKNOWLEDGE  = new ConstInt(Session.CLIENT_ACKNOWLEDGE );
  public static Any DUPS_OK_ACKNOWLEDGE = new ConstInt(Session.DUPS_OK_ACKNOWLEDGE );
  
  public static Any NON_PERSISTENT      = new ConstInt(DeliveryMode.NON_PERSISTENT);
  public static Any PERSISTENT          = new ConstInt(DeliveryMode.PERSISTENT);
  
  /**
   * Closes the session.
   */
  public void close();
  
  /**
   * Commits all messages done in this transaction and releases any locks currently held.
   */
  public void  commit();
  
  /**
   * Creates a QueueBrowser object to peek at the messages on the specified queue using a message selector.
   */
  public QueueBrowserI   createBrowser(QueueI queue, Any messageSelector);

  /**
   * Creates a BytesMessage object.
   * @param bytes TODO
   */
  public BytesMessageI  createBytesMessage(AnyByteArray bytes);
  
  /**
   * Creates a MessageConsumer for the specified destination.
   */
  public MessageConsumerI  createConsumer(DestinationI destination);
  
  
  /**
   * Creates a MessageConsumer for the specified destination, using a message selector.
   */
  public MessageConsumerI  createConsumer(DestinationI destination,
                                          Any messageSelector);
  
  
  /**
   * Creates MessageConsumer for the specified destination, using a message selector.
   */
  public MessageConsumerI  createConsumer(DestinationI destination,
                                          Any            messageSelector,
                                          boolean        noLocal);
  
  /**
   * Creates a durable subscriber to the specified topic.
   */
  public TopicSubscriberI  createDurableSubscriber(TopicI topic, Any name);

  /**
   * Creates a durable subscriber to the specified topic, using a message selector and specifying whether messages published by its own connection should be delivered to it.
   */
 public TopicSubscriberI  createDurableSubscriber(TopicI topic, Any name, Any messageSelector, boolean noLocal);

  /**
   * Creates a MapMessage object.
   */
  public MapMessageI   createMapMessage();
  
  /**
   * Creates a Message object.
   */
  public MessageI  createMessage();
  
  /**
   * Creates an ObjectMessage object.
   */
  public ObjectMessageI  createObjectMessage();
  
  /**
   * Creates an initialised ObjectMessage object.
   */
  public ObjectMessageI  createObjectMessage(Any object);
  
  /**
   * Creates a MessageProducer to send messages to the specified destination.
   */
  public MessageProducerI  createProducer(DestinationI destination);
  
  /**
   * Creates a queue identity given a Queue name.
   */
  public QueueI createQueue(Any queueName);

  /**
   * Creates a queue identity given a Queue name.
   */
  public TopicI createTopic(Any topicName);

  /**
   * Creates a StreamMessage object.
   */
  public AnyStreamMessage  createStreamMessage();
  
  /**
   * Creates a TemporaryQueue object.
   */
  public TemporaryQueueI   createTemporaryQueue();
  
  /**
   * Creates a TemporaryTopic object.
   */
  public TemporaryTopicI   createTemporaryTopic();
  
  /**
   * Creates a TextMessage object.
   */
  public AnyTextMessage  createTextMessage();

  /**
   * Creates an initialised TextMessage object.
   */
  public AnyTextMessage createTextMessage(Any text);  // suppose this must always be a string

  /**
   * Creates a topic identity given a Topic name.
   */
//  public Topic  createTopic(String topicName);

  /**
   * Returns the acknowledgement mode of the session.
   */
  public int  getAcknowledgeMode();

  /**
   * Returns the session's distinguished message listener (optional).
   */
//public  MessageListener  getMessageListener()

  /**
   * Indicates whether the session is in transacted mode.
   */
  public boolean  getTransacted();

  /**
   * Stops message delivery in this session, and restarts message delivery with the oldest unacknowledged message.
   */
  public void   recover();
  
  /**
   * Rolls back any messages done in this transaction and releases any locks currently held.
   */
  public void   rollback();

  /**
   * Optional operation, intended to be used only by Application Servers, not by ordinary JMS clients.
  public void   run();
   */

  /**
   * Sets the session's distinguished message listener (optional).
   */
  public void setMessageListener(AnyMessageListener listener);

  /**
   * Unsubscribes a durable subscription that has been created by a client.
   */
  public void unsubscribe(Any name);
}
