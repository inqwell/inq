/**
 * Copyright (C) 2012 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.scripts.interactive;

import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import com.inqwell.any.parser.JMSSupport;

/**
 * Tests to exercise Inq's JMS support by running the examples.
 * @author tom
 *
 */
public class JMSTest extends JMSSupport
{
  /**
   * 
   */
	@Test
  public void ackEquivExample() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/AckEquivExample.inq", "-queue", "Q", "-topic", "T" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void asynchTopicExample() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/AsynchTopicExample.inq", "-queue", "Q", "-topic", "T" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void durableSubscriberExample() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/DurableSubscriberExample.inq", "-topic", "T" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void messageFormats() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/MessageFormats.inq" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void messageHeadersTopic() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/MessageHeadersTopic.inq", "-queue", "Q", "-topic", "T" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void objectMessages() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/ObjectMessages.inq" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void synchTopicExample() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/SynchTopicExample.inq", "-name", "T" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void topicSelectors() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/TopicSelectors.inq", "-queue", "Q", "-topic", "T" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void transactedExample() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/TransactedExample.inq" };
  	run(args);
  }
  
  /**
   * 
   */
	@Test
  public void xmlTopicExample() throws IOException
  {
		Assume.assumeTrue(brokerRunning());
		
  	String args[] = { "-in", "src/main/examples/jms/XMLTopicExample.inq", "-queue", "Q", "-topic", "T" };
  	run(args);
  }
}
