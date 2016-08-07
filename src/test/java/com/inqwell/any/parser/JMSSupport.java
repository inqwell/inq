/**
 * Copyright (C) 2012 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Some 
 * @author tom
 *
 */
public class JMSSupport extends InteractiveTestSupport
{
	private static final int IMBROKER_PORT = 7676;
	private static final String IMBROKER_JAR = "imq.jar";
	
	/**
	 * Check if the test message broker is running on localhost:IMBROKER_PORT
	 * and that we can see the glassfish imq.jar on the class path
	 * @return
	 */
	protected boolean brokerRunning() throws IOException
	{
		// Slip in the necessary property for the current state of (no) JNDI etc
		System.setProperty("inq.jms.connectionfactory", "com.sun.messaging.ConnectionFactory");
		
		boolean ret = pingMessageBroker();
		
		if (ret)
		{
  		String classPath = System.getProperty("java.class.path");
  		ret = classPath.contains(IMBROKER_JAR);
		}
		
		return ret;
	}
	
	private boolean pingMessageBroker() throws IOException
	{
		Socket s = null;
		try
		{
  		s = new Socket("localhost", IMBROKER_PORT);
		}
		catch (IOException e)
		{
			return false;
		}
		
		s.setSoTimeout(5000);
	  BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
	  while(r.readLine() != null);
	  r.close();
	  return true;
	}
}
