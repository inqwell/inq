/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/FIFO.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Composite;
import com.inqwell.any.Queue;
import com.inqwell.any.AnyException;

/**
 * A channel driver that uses a com.inqwell.any.Queue to act as a
 * buffer for communication.  By default, items sent through
 * the channel are deep-copied when the <code>write()</code>
 * operation is called.  This behaviour can be altered for those
 * rare occasions where this is not necessary (for example when
 * passing on an item de-serialized from a stream) by
 * constructing the driver appropriately.
 * <p>
 * Once the <code>write()</code> method completes, regardless of the
 * buffer's copy mode, the buffer assumes exclusive ownership of
 * the sent item.  As such, when <code>read()</code> deals with a
 * composite, only a shallow copy is performed.
 * <p>
 * The maximum size of the buffer may be set according to
 * specific requirements.
 * <bl>
 * <li>A size of zero (default) indicates that
 * there is no maximum size.  The queue can grow to an
 * indefinite size.
 * <li>A size of one will cause the participating threads to
 * rendevous at the channel.
 * </bl>
 */
public class FIFO extends    AbstractChannelDriver
									implements ChannelDriver
{
	private Queue   q_           = AbstractComposite.queue();
	private int     maxSize_     = 0; // indicates no maximum
	private int     copyMode_    = ChannelConstants.COPY;
	
	public FIFO()
	{
	  this(0, ChannelConstants.COPY);
  }

  public FIFO (int maxSize)
  {
    this(maxSize, ChannelConstants.COPY);
  }
  
    
	public FIFO (int maxSize, int copyMode)
	{
		maxSize_     = maxSize;
		copyMode_    = copyMode;
	}
	
	
  public Any read() throws AnyException
  {
		// Regardless of copy mode, just return the item
		// reference.
		return q_.removeFirst();
	}
    
  public void write(Any a) throws AnyException
  {
    // Since we are exclusively contained within the
    // synchronizing wrapper of the Channel implementation
    // once we get here we can just manipulate the queue
    if (a == AnyChannel.PurgeBuffer.instance())
      purgeReceived();
    else
    {
      Any toAdd = a;
      if (copyMode_ == ChannelConstants.COPY)
        toAdd = toAdd.cloneAny();
      q_.addLast(toAdd);
    }
  }
  
	public boolean isEmpty()
	{
	  return q_.isEmpty();
	}
	
  public boolean isFull()
  {
    // A queue can only be full if a maximum size has been
    // specified
    return (maxSize_ != 0) && (q_.entries() == maxSize_);
  }

  protected void doClose() throws AnyException
  {
	}

  public void purgeReceived()
  {
    q_.empty();
  }
  
  public void drainInput()
  {
  	q_.empty();
  }
}
