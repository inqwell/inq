/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/OutputChannel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.io.AnyIOException;
import com.inqwell.any.AnyException;

/**
 * An output channel defines the interface by which one thread
 * may send output to another.  Such a thread is known as
 * the <i>producer</i> thread.
 * <p>
 * The <code>OutputChannel</code> interface defines one half of
 * any concrete implementation and serves to reinforce the
 * fact that a channel is unidirectional.  A given channel object
 * will be used for output from the producer thread and input
 * to the consumer thread and should be passed to these threads
 * as the appropriate interface.
 * @see com.inqwell.any.InputChannel
 */
public interface OutputChannel extends Any
{
	/**
	 * Send (or wait until send is possible) data to the channel
	 * <p>
	 * According to implementation, writers may block when the channel
	 * becomes full, or an exception is thrown
	 * @param a The item to write to the channel
	 * @exception AnyIOException write to full channel, or some other IO
	 * error occurred.
	 */
	public void write (Any a) throws AnyException;
	
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
	 * Note: this method is also defined on the interface InputChannel.  Either
	 * readers or writers can close a channel with the same results.
	 */
	public void close () throws AnyException;
	
	/**
	 * Close this channel's driver.  Performs a forced close without
	 * the protocol offered by <code>close()</code>
	public void closeDriver () throws AnyException;
	 */
	
  /**
   * Causes any remaining data at the input side of the channel to be
   * discarded. The channel remains open and new data can subsequently be
   * sent.
   */
  public void sendPurge() throws AnyException;
  
  public void purgeReceived(boolean close) throws AnyException;

  /**
   * Check if this channel has been closed. If the channel has
   * been closed any write will generate
   * a <code>WriteClosedChannelException</code>.
   * @return <code>true</code> if the channel has been closed
   */
	public boolean isClosed();
	
	/** 
   * Requests that this channel driver try to maintain its connection
   * where connection parameters are known.
   */
	public void setKeepOpen(boolean keepOpen);

  /**
   * Assign a session id to the connection represented by this
   * channel. May be used to restore a virtual connection
   * with a new physical one.
   */
  public void setSessionId(Any sessionId);

  /**
   * Perform any operation required to complete any output
   * pending on this channel driver.
   */
  public void flushOutput() throws AnyException;
 
 	/**
   * Reset this channel's output stream. Tells the channel that the
   * output stream must be re-established with the given
   * <code>ChannelDriver</code>, the existing one being
   * reset in in some way, if required.
   */
  public void resetOutput(ChannelDriver d) throws AnyException;

  /**
   * Determine whether this channel has data waiting to be delivered
   * on restoration of a physical connection.
   */  
  public boolean hasPendingOutput();

  /**
   * Return the total number of bytes sent so far on this channel
   * before compressing. Only relevant when the underlying
   * channel driver supports compression and is operating in compressed
   * mode.
   * @return the total number of compressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalIn();

  /**
   * Return the total number of bytes sent so far on this channel
   * after compressing. Only relevant when the underlying
   * channel driver supports compression and is operating in compressed
   * mode.
   * @return the total number of uncompressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalOut();

  public Any getSessionId();
}
