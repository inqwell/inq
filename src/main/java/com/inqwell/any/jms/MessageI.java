/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.Message;

import com.inqwell.any.Any;
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.DateI;
import com.inqwell.any.Map;

/**
 * Access to a JMS message within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface MessageI extends Any
{
  public void acknowledge();

  public void clearBody();

  public void clearProperties();

  /**
   * Get a named message property according to the supplied type. The
   * <code>type</code> argument is used to resolve the underlying property type
   * and takes its value from the appropriate method on
   * <code>javax.jms.Message</code>, for example
   * <code>getDoubleProperty(String name)</code>.
   * 
   * @param name
   *          the name of the property
   * @param type
   *          the value to which the property is assigned. This value is also
   *          returned.
   * @return the <code>type</code>argument.
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public Any getProperty(Any name, Any type);

  /**
   * Set a named message property according to the supplied type. The
   * <code>type</code> argument is used to resolve the underlying property type
   * to determine the method to call on
   * <code>javax.jms.Message</code>, for example
   * <code>setDoubleProperty(String name, double value)</code>.
   * 
   * @param name
   *          the name of the property
   * @param type
   *          the value to which the property is set.
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public void setProperty(Any name, Any type);
  
  /**
   * Returns all client properties contained within this JMS message as a {@link Map}.
   * The property values are universally converted to <code>StringI</code>s.
   * @return A {@link Map} containing the property names as keys and their
   * string representations as values.
   */
  public Map getClientProperties();
  
  /**
   * Indicates whether a property value exists.
   * 
   * @param name
   *          the name of the property
   * @return <code>true</code> if the property exists.
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public boolean propertyExists(Any name);

  /**
   * Gets the correlation ID for the message.
   * @return
   */
  public java.lang.String   getJMSCorrelationID();
  
  /**
   * Gets the correlation ID as an array of bytes for the message.
   * @return an {@link AnyByteArray}.
   */
  public Any getJMSCorrelationIDAsBytes();
  
  /**
   *  Gets the DeliveryMode value specified for this message.
   * @return
   */
  int  getJMSDeliveryMode();
  
  /**
   * Gets the Destination object for this message.
   * @return
   */
  public Any  getJMSDestination();
    
  /**
   *  Gets the message's expiration value.
   * @return
   */
  public long   getJMSExpiration();
   
  /**
   * Gets the message ID.
   * @return
   */
  public java.lang.String   getJMSMessageID();

  /**
   * Gets the message priority level.
   * @return
   */
  public int  getJMSPriority();
    
  /**
   * Gets an indication of whether this message is being redelivered.
   * @return
   */
  public boolean  getJMSRedelivered();
    
  /**
   * Gets the Destination object to which a reply to this message should be sent.
   * @return
   */
  public Any  getJMSReplyTo();
    
  /**
   * Gets the message timestamp.
   * @return
   */
  public long   getJMSTimestamp();
    
  /**
   * Gets the message type identifier supplied by the client when the message was sent.
   * @return
   */
  public Any getJMSType();

  /**
   * Sets the correlation ID for the message.
   * 
   * @param correlationID
   */
  public void setJMSCorrelationID(java.lang.String correlationID);

  /**
   * Sets the correlation ID as an array of bytes for the message.
   * 
   * @param correlationID
   */
  public void setJMSCorrelationIDAsBytes(Any correlationID);

  /**
   * Sets the DeliveryMode value for this message.
   * 
   * @param deliveryMode
   */
  public void setJMSDeliveryMode(int deliveryMode);

  /**
   * Sets the Destination object for this message.
   * 
   * @param destination
   */
  public void setJMSDestination(Any destination);

  /**
   * Sets the message's expiration value.
   * 
   * @param expiration
   */
  public void setJMSExpiration(long expiration);

  /**
   * Sets the message ID.
   * 
   * @param id
   */
  public void setJMSMessageID(java.lang.String id);

  /**
   * Sets the priority level for this message.
   * 
   * @param priority
   */
  public void setJMSPriority(int priority);

  /**
   * Specifies whether this message is being redelivered.
   * 
   * @param redelivered
   */
  public void setJMSRedelivered(boolean redelivered);

  /**
   * Sets the Destination object to which a reply to this message should be
   * sent.
   * 
   * @param replyTo
   */
  public void setJMSReplyTo(Any replyTo);

  /**
   * Sets the message timestamp.
   * 
   * @param timestamp
   */
  public void setJMSTimestamp(long timestamp);

  /**
   * Sets the message timestamp. This method has the same effect
   * as {@link #setJMSTimestamp(long)} but accepts a {@link DateI}
   * instead.
   * 
   * @param timestamp
   */
  public void setJMSDatestamp(DateI timestamp);

  /**
   * Sets the message type.
   * 
   * @param type
   */
  public void setJMSType(Any type);

  /**
   * Return the underlying message
   * @return
   */
  public Message getJMSMessage();
}
