/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/AbstractChannelDriver.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.*;

public abstract class AbstractChannelDriver extends    AbstractAny
                                            implements ChannelDriver
{
	private boolean isClosed_ = false;
	
	public void setKeepOpen(boolean keepOpen) {}
  
	public boolean isKeepOpen()
  {
    return false;
  }

  public long getProbeTimeout()
  {
    return 0;
  }
  
  public void setProbeTimeout(long timeout)
  {
    throw new UnsupportedOperationException();
  }

  public java.net.Socket getSocket()
  {
    return null;
  }

  /**
   * Default implememnation write InputChannel.shutdown__ to the channel
   */
  public void shutdownInput() throws AnyException
  {
    write(InputChannel.shutdown__);
  }
  
  public void drainInput()
  {
  }
  
  public ChannelDriver resetOutput(ChannelDriver d) throws AnyException
  {
    return d;
  }
  
  public void flushOutput() throws AnyException {}

	public boolean reOpen() throws AnyException
	{
    return false;
  }

	public boolean canReopen()
	{
    return false;
  }

	public boolean isBlocking()
	{
    return false;
  }
  
  public boolean canShutdown()
  {
  	return isEmpty();
  }
  
	public void close () throws AnyException
	{
		isClosed_ = true;
		doClose();
	}
	
	public boolean isClosed()
	{
		return isClosed_;
	}

  public void setSessionId(Any sessionId) {}
  
  public Any getSessionId() { return null; }

  public boolean hasPendingOutput() { return false; }
  
  protected abstract void doClose() throws AnyException;
}
