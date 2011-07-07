/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;

/**
 * Access to a JMS Connection Factory within the <code>Any</code> framework.
 * <p/>
 * 
 * @author tom
 * 
 */
public interface ConnectionFactoryI extends Any
{
  /**
   * Creates a connection with the default user identity.
   * @return
   */
  public ConnectionI createConnection();
  
  /**
   * Creates a connection with the specified user identity.
   * @param userName
   * @param password
   * @return
   */
  public ConnectionI createConnection(Any userName, Any password);
}
