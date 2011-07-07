/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/ChannelDriver.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;

/**
 * A channel driver defines the operational semantics of a
 * channel.  Implementations of the ChannelDriver interface
 * are passed to a channel on the channel's construction.
 * Such implementations define the type of exchange for the
 * channel, such as a memory buffer, io stream etc.
 */
public interface ChannelDriver extends Any
{
  /**
   * Read an object from this ChannelDriver.  Returns the object available
   * in the exchange, <code>null</code> if the channel is empty and
   * would not block or <code>InputChannel.shutdown__</code>
   * for the next read performed
   * after <code>shutdownInput</code> has been called.
   */
  public Any read() throws AnyException;
  
  /**
   * Write an object to the exchange
   */
  public void write(Any a) throws AnyException;
  
  /**
   * Test if the exchange has any objects to read
   * @return <code>true</code> if the exchange is
   * empty; <code>false</code> otherwise.
   */
  public boolean isEmpty();
  
  /**
   * Test if the exchange is capable of accepting objects to write
   * @return <code>true</code> if the exchange cannot accept an object at
   * this time;
   * <code>false</code> otherwise.
   */
  public boolean isFull();
  
  /**
   * Test if the exchange is in a state where it can be shutdown.
   * Some exchanges may, for example, enforce that they are empty
   * before allowing shutdown, to avoid lost messages
   */
  public boolean canShutdown();
  
  /**
   * Close this exchange. After calling this method the exchange may
   * no longer accept <code>read</code> and <code>write</code> requests.
   * If <code>purge</code> is <code>true</code> the implementation may
   * attempt to discard any input waiting to be read, depending on
   * its ability to do so.
   */
  public void close() throws AnyException;
  
  public void purgeReceived();
  
  /**
   * 
   */
  public boolean isClosed();

  /**
   * 
   */
	public void setKeepOpen(boolean keepOpen);
	
	/**
   * Reports whether this ChannelDriver will try to keep itself
   * open under exceptional conditions detected in the exchange
   * mechanism. Generally, this property of the driver is determined
   * automatically.
   */
	public boolean isKeepOpen();
	
  public void setProbeTimeout(long timeout);
  public long getProbeTimeout();
  
  public java.net.Socket getSocket();
  
	/**
   * Returns <code>true</code> if this driver can block on read
   * operations, or <code>false</code> if it would never block
   */
	public boolean isBlocking();
	
	/**
   * If possible, attempt to reopen this ChannelDriver. 
   */
	public boolean reOpen() throws AnyException;
	
	/**
	 * Can this channel driver be reopened?
	 */
	public boolean canReopen();
  
  /**
   * Shutdown this channel driver's read operation. Causes the exchange
   * to return <code>InputChannel.shutdown__</code> in a
   * subsequent <code>read</code>
   * operation.
   */
  public void shutdownInput() throws AnyException;
  
  /**
   * Reset this channel driver's write operation. The new channel
   * driver may be subsumed into this channel driver, or this
   * channel driver may choose to create and return a new
   * driver altogther.
   */
  public ChannelDriver resetOutput(ChannelDriver d) throws AnyException;

  /**
   * Perform any operation required to complete any output
   * pending on this channel driver.
   */
  public void flushOutput() throws AnyException;
  
  public void setSessionId(Any sessionId);
  public Any getSessionId();

  public boolean hasPendingOutput();
  
  /**
   * If possible, drain any pending input this channel driver
   * has waiting for the next reader
   */
  public void drainInput();
}
