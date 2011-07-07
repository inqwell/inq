/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-25 15:22:52 $
 */
package com.inqwell.any.jms;

import javax.jms.ConnectionFactory;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;

/**
 * Some static methods that, in the main, assume Sun Glassfish MQ
 * is the JMS provider (in the absence of JNDI etc)
 * @author tom
 *
 */
public class JMSHelper extends AbstractAny
{
  /**
   * Returns a JMS Connection Factory object. This implementation uses
   * the system property inq.jms.connectionfactory and has been tested
   * against Sun GlassFish Message Queue 4.4 connection
   * factory and does not require JNDI.
   * @param args
   * @return
   */
  static public Any getJMSConnectionFactory(Map args)
  {
    String factoryImpl = System.getProperty("inq.jms.connectionfactory");
    if (factoryImpl == null)
      throw new AnyRuntimeException("System property inq.jms.connectionfactory is not set");

    ConnectionFactory f;
    try
    {
      f = (ConnectionFactory)Class.forName(factoryImpl).newInstance();
    }
    catch (InstantiationException e)
    {
      throw new RuntimeContainedException(e);
    }
    catch (IllegalAccessException e)
    {
      throw new RuntimeContainedException(e);
    }
    catch (ClassNotFoundException e)
    {
      throw new RuntimeContainedException(e);
    }

    return new AnyConnectionFactory(f);
  }
}
