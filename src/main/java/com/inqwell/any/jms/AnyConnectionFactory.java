/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.Any;
import com.inqwell.any.RuntimeContainedException;

public class AnyConnectionFactory extends    AbstractAny
                                  implements ConnectionFactoryI
{

  private ConnectionFactory fac_;
  
  public AnyConnectionFactory(ConnectionFactory fac)
  {
    fac_ = fac;
  }
  
  @Override
  public ConnectionI createConnection()
  {
    try
    {
      return new AnyConnection(fac_.createConnection());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public ConnectionI createConnection(Any userName, Any password)
  {
    try
    {
      return new AnyConnection(fac_.createConnection(userName.toString(), password.toString()));
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public String toString()
  {
    return fac_.toString();
  }
}
