/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.Map;

/**
 * Access to a JMS Connection within the <code>Any</code> framework.
 * <p/>
 * Not all the underlying interface methods (on JMS Connection) are
 * replicated here, only the ones we (intend to) use.
 * 
 * @author tom
 * 
 */
public interface ConnectionI extends Any
{
  public final static Any JMS_MAJOR_VERSION          = AbstractValue.flyweightString("JMSMajorVersion");
  public final static Any JMS_MINOR_VERSION          = AbstractValue.flyweightString("JMSMinorVersion");
  public final static Any JMS_PROVIDER_NAME          = AbstractValue.flyweightString("JMSProviderName");
  public final static Any JMS_VERSION                = AbstractValue.flyweightString("JMSVersion");
  public final static Any JMSX_PROPERTY_NAMES        = AbstractValue.flyweightString("JMSPropertyNames");
  public final static Any JMS_PROVIDER_MAJOR_VERSION = AbstractValue.flyweightString("JMSProviderMajorVersion");
  public final static Any JMS_PROVIDER_MINOR_VERSION = AbstractValue.flyweightString("JMSProviderMinorVersion");
  public final static Any JMS_PROVIDER_VERSION       = AbstractValue.flyweightString("JMSProviderVersion");

  
  /**
   *   Closes the connection.
   */  
  public void close();
  
  /**
   * Creates a Session object.
   */
  public SessionI createSession(boolean transacted, int acknowledgeMode);

  /**
   * Gets the client identifier for this connection.
   */
  public Any   getClientID();

  /**
   * Sets the client identifier for this connection.
   */
  public void setClientID(Any clientID);

  // Gets the ExceptionListener object for this connection.
  // TODO ExceptionListener  getExceptionListener()
  // TODO  void   setExceptionListener(ExceptionListener listener)
  //  Sets an exception listener for this connection.

  /**
   * Gets the metadata for this connection.
   */
  public Map getMetaData();
  
  /**
   * Starts (or restarts) a connection's delivery of incoming messages.
   * */
  public void start();
  
  /**
   * Temporarily stops a connection's delivery of incoming messages.
   */
  public void stop();
}
