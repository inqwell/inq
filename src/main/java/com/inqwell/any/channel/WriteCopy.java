/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/WriteCopy.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;


/**
 * A channel driver that wraps up an OutputChannel instance so
 * that items written are cloned as they pass through.
 * <p>
 * Only the <code>write()</code> method is supported.  The
 * <code>read()</code> throws an UnsupportedOperationException.
 * Items written to the channel driver are cloned and then
 * passed to the <code>write()</code> method of the contained
 * OutputChannel.  The purpose of this driver is to allow such
 * channels built round FIFO drivers to support transport
 * between threads in a single JVM, where cloning is necessary,
 * as well as being connected to RMI drivers where cloning is
 * unnecessary and possibly undesirable.
 */
public class WriteCopy extends    AbstractChannelDriver
								       implements ChannelDriver
{
	private OutputChannel oc_;
	
	public WriteCopy(OutputChannel oc)
	{
	  oc_ = oc;
  }
	
  public Any read() throws AnyException
  {
    throw (new UnsupportedOperationException());
	}
    
  public void write(Any a) throws AnyException
  {
  	oc_.write(a.cloneAny());
  }
  
  public void purgeReceived()
  {
//    // Close is an assumption!
//    try
//    {
//      oc_.purgeReceived(true);
//    }
//    catch(Exception e)
//    {
//      throw new RuntimeContainedException(e);
//    }
  }
  
	public boolean isEmpty()
	{
	  // read is not supported anyway.
	  //return true;
	  return ((InputChannel)oc_).isEmpty();
	}
	
  public boolean isFull()
  {
    return false;
  }

  protected void doClose() throws AnyException
  {
		oc_.close();
	}

  public InputChannel getUnderlyingChannel()
  {
    return (InputChannel)oc_;
  }

  public Object clone()
  {
		return this;
  }
}
