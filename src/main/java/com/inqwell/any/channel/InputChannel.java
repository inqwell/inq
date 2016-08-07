/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/InputChannel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyObject;
import com.inqwell.any.ObjectI;

/**
 * An input channel defines the interface by which one thread
 * may receive input from another.  Such a thread is known as
 * the <i>consumer</i> thread.
 * <p>
 * The <code>InputChannel</code> interface defines one half of
 * any concrete implementation and serves to reinforce the
 * fact that a channel is unidirectional.  A given channel object
 * will be used for output from the producer thread and input
 * to the consumer thread and should be passed to these threads
 * as the appropriate interface.
 *
 * @see com.inqwell.any.OutputChannel
 */
public interface InputChannel extends Any
{
  /**
   * Returned by InputChannel.read() <B>NOTE</B> This is tested for
   * by reference so shoud not be passed through any copying channels!!!
   */
  public static ObjectI shutdown__ = new AnyObject();
  
  // Similarly timeout__
  public static ObjectI timeout__  = new AnyObject();
  
	/**
	 * Receive (or wait for until available) data in the channel
	 */
	public Any read () throws AnyException;
	
	/**
	 * Wait for the specified number of milliseconds for data to become available
	 * in the channel.  If zero then return immediately.  Less than zero indicates
	 * an indefinite wait equivalent to <code>read()</code>
	 * @return data or null if no data became available.
	 */
	public Any read (long waitTime) throws AnyException;	

	/**
	 * Close this channel.  The following semantics apply:
	 * <ul>
	 * <li>
	 * Any data still waiting to be read from the channel remains available and
	 * will be returned by subsequent calls to <code>read()</code>.
	 * <li>
	 * Once the channel has been emptied subsequent calls to <code>read()</code>
	 * generate a <code>ChannelClosedException</code>.
	 * <li>
	 * Whether or not the channel contains data, any subsequent calls
	 * to <code>write()</code> generate a <code>WriteClosedChannelException</code>.
	 * </ul>
	 * Note: this method is also defined on the interface OuputChannel.  Either
	 * readers or writers can close a channel with the same results.
	 */
	public void close () throws AnyException;
	
  /**
   * Purges the channel of any remaining data, provided the channel's
   * transport mechanism supports the operation. If <code>close</code>
   * is <code>true</code> the channel is also closed.
   */
  public void purgeReceived(boolean close) throws AnyException;

  /**
   * Check if this channel has been closed. If the channel has
   * been closed any pending data held within the channel may
   * still be read.
   * @return <code>true</code> if the channel has been closed
   */
	public boolean isClosed();

  /**
   * Test if this channel has any pending input waiting to
   * be read. Some implementations of the underlying channel
   * driver may be unable to determine whether input is
   * available or not.
   * @return <code>true</code> if input <i>may</i> be available.
   */
	public boolean isEmpty();

	/**
   * Reset this channel's input stream. Tells the channel that the
   * input stream must be re-established with the given
   * <code>ChannelDriver</code>, the existing one being
   * reset in in some way, if required.
   */
	public void shutdownInput(ChannelDriver d) throws AnyException;

  /**
   * If possible, drain any pending input this channel
   * has waiting for the next reader
   */
  public void drainInput();
  
  /**
   * Get this InputChannel's underlying channel.  Only returns
   * something other than <code>this</code> if the channel
   * driver is a WriteCopy.  See SpawnProcess.java
   */
  public InputChannel getUnderlyingChannel();

  /**
   * Determines whether an exception object received through the
   * channel will be thrown or returned from <code>read</code>
   * as is
   */
	public void setThrowsExceptions(boolean willThrow);
	
  public Any getSessionId();
  
}
