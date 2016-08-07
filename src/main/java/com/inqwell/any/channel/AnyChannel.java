/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/channel/AnyChannel.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import java.io.ObjectStreamException;
import java.util.Timer;
import java.util.TimerTask;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DateI;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.EventListener;
import com.inqwell.any.Globals;
import com.inqwell.any.Process;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.UserProcess;
import com.inqwell.any.io.AnyIOException;

/**
 * This class implements Inq's InputChannel and OutputChannel
 * interfaces.  It offers synchronisation between reader
 * and writer threads of the channel whose physical transport is
 * determined by the given <code>ChannelDriver</code>.  Readers
 * and writers rendevous at empty/full channels, although what
 * constitutes full and empty depends on the driver implementation
 * also.
 * <p>
 * Read operations on this channel may be handled by a separate
 * thread started within this channel for those cases where the
 * ultimate receiver has multiple input sources and there is no
 * thread provided by the Java runtime to deliver them.   Anything
 * read by this thread is then forwarded to the specified channel
 * for eventual consumption.  In addition, if a channel is so created
 * there is no need to maintain a reference to it externally as it
 * becomes 'owned' by the internal reader thread.
 */
public class AnyChannel extends    AbstractAny
												implements InputChannel,
																	 OutputChannel,
																	 Cloneable
{
	private  Object        readMonitor_     = new Object();
	private  Object        writeMonitor_    = new Object();
	private  Object        shutdownMonitor_ = new Object();
	private  boolean       blockOnFull_     = true;
	private  boolean       willThrow_       = true;
	private  boolean       closeOnFinalize_ = true;
	private  volatile boolean  isClosedRead_    = false;
	private  volatile boolean  isClosedWrite_   = false;
	private  Reader        reader_;
	private  Probe         keepOpenProbe_;
  
  private  TimerTask     keepAlive_;
  private  long          lastSent_;

	private  volatile ChannelDriver channelDriver_;

  static private Array  resumeReadEventTypes__;
  static private Array  sessionResumeEventTypes__;
  static private Array  sessionDefunctEventTypes__;
  
  static
  {
    resumeReadEventTypes__ = AbstractComposite.array();
    resumeReadEventTypes__.add(EventConstants.RESUME_READ);

    sessionResumeEventTypes__ = AbstractComposite.array();
    sessionResumeEventTypes__.add(EventConstants.SESSION_RESUME);

    sessionDefunctEventTypes__ = AbstractComposite.array();
    sessionDefunctEventTypes__.add(EventConstants.SESSION_DEFUNCT);
  }

	/**
	 * Make a channel around the given driver.  If the driver indicates
	 * that it is full then <code>write()</code> will block until
	 * the driver is able to accept more data once another thread has
	 * performed a <code>read()</code>
	 */
	public AnyChannel (ChannelDriver d)
	{
		this(d, true);
	}

	/**
	 * Make a channel around the given driver.
	 * If <code>blockOnFull</code> is <code>true</code> and
	 * the driver indicates
	 * that it is full then <code>write()</code> will block until
	 * the driver is able to accept more data once another thread has
	 * performed a <code>read()</code>.  Otherwise
	 * an <code>AnyIOException</code> is thrown.
	 */
	public AnyChannel (ChannelDriver d,
										 boolean       blockOnFull)
	{
		channelDriver_ = d;
		blockOnFull_   = blockOnFull;
	}

	public AnyChannel (Socket        s,
										 OutputChannel forwardTo)
	{
		this(s, forwardTo, true);
	}

  public AnyChannel (Socket        s,
                     OutputChannel forwardTo,
                     Process       p,
                     String        threadName)
  {
    this(s, forwardTo, p, true, threadName);
  }

  public AnyChannel (Socket        s,
                     OutputChannel forwardTo,
                     Process       p,
                     boolean       blockOnFull,
                     String        threadName)
  {
    channelDriver_   = s;
    blockOnFull_     = blockOnFull;
    closeOnFinalize_ = false;
    if (forwardTo != null)
    {
      reader_ = new Reader(forwardTo, s, p);
      reader_.setDaemon(true);
      reader_.start();
      if (threadName != null)
        reader_.setName(threadName);
    }
  }


	/**
	 * Make a channel around the given driver with optional block on full.
	 * A separate thread will be started to perform <code>read()</code>
	 * operations from this channel and forward them to that specified.
	 * Thus, it doesn't make sense for the consumer (or any other) thread
	 * to do <code>read()</code>s on this channel, rather they perform
	 * their <code>read()</code>s on <code>forwardTo</code>.
	 * <p>
	 * This facility supports a single read point for the ultimate consumer
	 * thread where the data source is not already provided with a thread
	 * by Java runtime.  For example, data received at an RMI remote object
	 * or from the AWT is handled by a system thread, but data at a socket
	 * must be read by the consumer.
	 */
	public AnyChannel (Socket        s,
										 OutputChannel forwardTo,
										 boolean       blockOnFull)
	{
		channelDriver_   = s;
		blockOnFull_     = blockOnFull;
		closeOnFinalize_ = false;
		if (forwardTo != null)
		{
			reader_ = new Reader(forwardTo, s);
			reader_.setDaemon(true);
			reader_.start();
		}
	}

  public boolean isConst()
  {
    // Just means that if we put our input channels into a
    // managed instance that gets copied-to AnyChannel fields
    // would be benign
    return true;
  }

	public Any read () throws AnyException
	{
		return read(-1);
	}

	public Any read (long timeout) throws AnyException
	{
		if (isClosedRead_)
		  throw (new ChannelClosedException ("Read from closed channel"));

    return doRead(timeout);
	}

  public void drainInput()
  {
		synchronized (readMonitor_)
		{
			synchronized (this)
			{
				channelDriver_.drainInput();
				notifyAll();  // Notify waiting writer thread
			}
		}
  }

	private Any doRead(long timeout) throws AnyException
	{
		synchronized (readMonitor_)
		{
			synchronized (this)
			{
				while (channelDriver_.isEmpty() && !isClosed())
				{
					if (timeout == 0)
						return null;

					try
					{
						if (timeout > 0)
						{
							wait(timeout);
              timeout = 0;
						}
						else
						{
							wait();
						}
					}
					catch (InterruptedException e)
					{
//						if (timeout > 0)
//						{
//							// we timed out of our own accord
//							return null;
//						}

//						AnyException.throwExternalException (e); // we were interrupted
            return null;

					}
				}

				Any a = null;
				if (!isClosedRead_)
				  a = channelDriver_.read();
				else
				  a = new ChannelClosedException("Channel is closing");

				// Notify anyone awaiting go-ahead to shutdown.
				synchronized(shutdownMonitor_)
				{
          shutdownMonitor_.notifyAll();
				}

				notifyAll();  // Notify waiting writer thread

				// Check to see if we've received an exception through the channel.
				// If we have then throw it
				if (a instanceof AnyException)
				{
          if (a instanceof ChannelClosedException)
            isClosedRead_  = true;

          if (willThrow_)
            throw (AnyException)a;
				}
				if ((a instanceof AnyRuntimeException) && willThrow_)
				{
					throw (AnyRuntimeException)a;
				}
				return a;
			}
		}
	}

	public void write (Any a) throws AnyException
	{
    doWrite(a);
    lastSent_ = System.currentTimeMillis();
    if (keepOpenProbe_ != null)
    {
      // restart the probe timeout
      keepOpenProbe_.timerChannel_.write(AnyNull.instance());
    }
	}

  private void doWrite(Any a) throws AnyException
  {
    if (isClosedWrite_)
      throw (new WriteClosedChannelException ("Write to closed channel"));
    
    synchronized (writeMonitor_)
    {
      synchronized (this)
      {
        while (channelDriver_.isFull())
        {
          if (blockOnFull_)
          {
            try
            {
              wait();
            }
            catch (InterruptedException e)
            {
              AnyException.throwExternalException (e);
            }
          }
          else
          {
            throw (new AnyIOException ("Write to full channel"));
          }
        }
        channelDriver_.write(a);
        notifyAll();  // Notify waiting reader thread
      }
    }
  }
  
	public void flushOutput() throws AnyException
	{
    synchronized(writeMonitor_)
    {
      channelDriver_.flushOutput();
    }
	}

	public void close () throws AnyException
	{
    synchronized(writeMonitor_)
    {
      if (reader_ != null)
      {
        reader_.interrupt();
        reader_ = null;
      }
  
      stopKeepOpenProbe();
      stopKeepAliveTimer();
  
      if (!isClosedWrite_)
      {
        try
        {
          write(new ChannelClosedException("Channel is closing"));
          flushOutput();
          channelDriver_.close();
        }
        catch(Exception e) {/*e.printStackTrace();*/}
        finally
        {
          isClosedWrite_ = true;
          try
          {
            channelDriver_.close();
          }
          catch (Exception e) {/*e.printStackTrace();*/}
        }
      }
    }
	}
  
  public void purgeReceived(boolean close) throws AnyException
  {
    synchronized (writeMonitor_)
    {
      synchronized (this)
      {
        channelDriver_.purgeReceived();
        if (close)
          this.close();
      }
    }
  }
  
  public void sendPurge() throws AnyException
  {
    write(PurgeBuffer.instance());
  }

//	public void closeDriver () throws AnyException
//	{
//    synchronized(writeMonitor_)
//    {
//      if (reader_ != null)
//      {
//        reader_.interrupt();
//        reader_ = null;
//      }
//  
//      stopKeepOpenProbe();
//  
//      if (!isClosedWrite_)
//      {
//        try
//        {
//          channelDriver_.close();
//        }
//        catch(AnyException e) {/*e.printStackTrace();*/}
//        finally
//        {
//          isClosedWrite_ = true;
//          try
//          {
//            channelDriver_.close();
//          }
//          catch (AnyException e) {/*e.printStackTrace();*/}
//        }
//      }
//    }
//	}

  public boolean isClosed()
  {
  	return channelDriver_.isClosed();
  }

  public boolean isEmpty()
  {
  	synchronized(this)
  	{
  		return channelDriver_.isEmpty();
  	}
  }

  public boolean hasPendingOutput()
  {
    return channelDriver_.hasPendingOutput();
  }

  public Any getSessionId()
  {
  	return channelDriver_.getSessionId();
  }

  public InputChannel getUnderlyingChannel()
  {
    InputChannel ret = this;
    
    if (channelDriver_ instanceof WriteCopy)
    {
      WriteCopy wc = (WriteCopy)channelDriver_;
      ret = wc.getUnderlyingChannel();
    }
    return ret;
  }

	public void shutdownInput(ChannelDriver d) throws AnyException
	{
		synchronized(shutdownMonitor_)
		{
			while (!channelDriver_.canShutdown())
			{
        try
        {
          shutdownMonitor_.wait();
        }
        catch (InterruptedException e)
        {
          AnyException.throwExternalException (e);
        }
			}

      if (channelDriver_.isBlocking())
      {
        ChannelDriver current = channelDriver_;
        channelDriver_        = d;
        current.shutdownInput();
      }
      else
      {
        synchronized(this)
        {
          if (!d.isBlocking())
            d.write(InputChannel.shutdown__);

          if (d != channelDriver_)
          {
            // Establish new driver first then...
            ChannelDriver current = channelDriver_;
            channelDriver_        = d;

            // ...cause any waiting read on old driver to be bumped.
            // That read returns InputChannel.shutdown__.
            // Note that drivers based on a socket will, while
            // waiting for data, hold the synchronization monitor
            // on this, but calling shutdown should cause them
            // to release it, allowing us to obtain it. We need to
            // notify readers that they can wake up and read
            // the shutdown notification from non-socket based
            // channels
            current.shutdownInput();
          }
          notifyAll();
        }
      }

      // Notify anyone alse awaiting shutdown go-ahead.
      shutdownMonitor_.notifyAll();
		}
	}

  public void resetOutput(ChannelDriver d) throws AnyException
  {
		synchronized (writeMonitor_)
		{
      synchronized(this)
      {
        isClosedRead_  = false;
        isClosedWrite_ = false;
				while (channelDriver_.isFull())
				{
          try
          {
            wait();
          }
          catch (InterruptedException e)
          {
            AnyException.throwExternalException (e);
          }
				}

        ChannelDriver newCd = channelDriver_.resetOutput(d);

        if (channelDriver_ != newCd)
          channelDriver_ = newCd;

				notifyAll();  // Notify waiting writer thread
      }
    }
  }

	public void setKeepOpen(boolean keepOpen)
	{
		channelDriver_.setKeepOpen(keepOpen);
	}

  public void startReader(Socket        s,
                          OutputChannel forwardTo,
                          boolean       blockOnFull,
                          Process       p)
  {
    channelDriver_ = s;
    blockOnFull_   = blockOnFull;
    if (forwardTo != null)
    {
      reader_ = new Reader(forwardTo, s, p);
      reader_.setDaemon(true);
      reader_.setName("SocketReader");
      reader_.start();
    }
  }
    
  public void setThreadName(String name)
  {
    if (reader_ != null)
    {
      reader_.setName(name);
    }
    if (keepOpenProbe_ != null)
      keepOpenProbe_.setName(name + ".KeepAlive");
  }

  /**
   * Return the total number of bytes sent so far on this channel
   * driver before compressing. Only relevant when the underlying
   * channel driver supports compression and is operating in compressed
   * mode.
   * @return the total number of uncompressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalIn()
  {
    if (channelDriver_ instanceof Socket)
    {
      Socket s = (Socket)channelDriver_;
      return s.getTotalIn();
    }
    else
      return -1;
  }
  
  /**
   * Return the total number of bytes sent so far on this channel
   * after compressing. Only relevant when the underlying
   * channel driver supports compression and is operating in compressed
   * mode.
   * @return the total number of compressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalOut()
  {
    if (channelDriver_ instanceof Socket)
    {
      Socket s = (Socket)channelDriver_;
      return s.getTotalOut();
    }
    else
      return -1;
  }
  
  public java.net.Socket getSocket()
  {
    return channelDriver_.getSocket();
  }
  
  public void setCompressed(boolean compressed)
  {
    if (channelDriver_ instanceof Socket)
    {
      Socket s = (Socket)channelDriver_;
      s.setCompressed(compressed);
    }
  }
  
  public boolean isCompressed()
  {
    if (channelDriver_ instanceof Socket)
    {
      Socket s = (Socket)channelDriver_;
      return s.isCompressed();
    }
    return false;
  }
  
	public void setThrowsExceptions(boolean willThrow)
	{
		willThrow_ = willThrow;
	}

  public void setSessionId(Any sessionId)
  {
		channelDriver_.setSessionId(sessionId);
  }

  public void startKeepOpenProbe()
  {
    if (keepOpenProbe_ == null)
    {
      keepOpenProbe_ = new KeepOpenProbe();
      keepOpenProbe_.setDaemon(true);
      keepOpenProbe_.start();
    }
  }
  
  public void startKeepAliveTimer(Process p)
  {
    // Use the process's timer to schedule a keep-alive
    // message if the driver transport requests it.
    
    // Third condition is just to avoid colliding with the
    // http tunneling code, though this is really defunct now
    // and will be removed.
    if (keepAlive_ == null &&
        channelDriver_.getProbeTimeout() > 0 &&
        !channelDriver_.isKeepOpen())
    {
      Timer t = p.getTimer().getTimer();
      t.scheduleAtFixedRate(keepAlive_ = new KeepAliveProbe(), 500, channelDriver_.getProbeTimeout());
    }
  }

  private void stopKeepOpenProbe() throws AnyException
  {
    synchronized (writeMonitor_)
    {
  		if (keepOpenProbe_ != null)
  		{
  			keepOpenProbe_.timerChannel_.close();
  			keepOpenProbe_ = null;
  		}
    }
  }
  
  public void stopKeepAliveTimer()
  {
    synchronized (writeMonitor_)
    {
      if (keepAlive_ != null)
      {
        keepAlive_.cancel();
        keepAlive_ = null;
      }
    }
  }

	protected void finalize() throws Throwable
	{
		super.finalize();
		if (closeOnFinalize_)
		  this.close();
	}

	public Object clone() throws CloneNotSupportedException
	{
		return this;
  }

	private class Reader extends Thread
	{
		private  OutputChannel   forwardTo_;

		// Only set to true when we are a server
		private  boolean         isShutdown_ = false;

		private  boolean         isDefunct_  = false;

		private  Socket          socket_;

		private  EventDispatcher ed_;
    
    // The process whose input channel we are forwarding to. Provided
    // so we can ensure the process is bumped out of any lock-wait it
    // may be in or we can interrupt a lon-running transaction.
    private  Process         p_;
    private  DateI           lastFromClient_;

		private Reader (OutputChannel forwardTo, Socket s)
		{
      this(forwardTo, s, null);
		}
    
    private Reader(OutputChannel forwardTo, Socket s, Process p)
    {
      super ();
      socket_     = s;
      forwardTo_  = forwardTo;
      p_          = p;
      if (p_ != null)
        lastFromClient_ = (DateI)p.getIfContains(UserProcess.lastFromClient__);
      
      // Set up a dispatcher and an event listener for resuming sessions
      // for unreliable/stateless protocols
      ed_ = new EventDispatcher();
      ed_.addEventListener(new ResumeRead());
      ed_.addEventListener(new SessionResume());
      ed_.addEventListener(new SessionDefunct());
    }

		public void run()
		{
			Reader r = this;
      AnyChannel.this.setThrowsExceptions(false);
			try
			{
				while(true)
				{
					try
					{
            Any a = null;

            if (isShutdown_)
            {
            	System.out.println ("Waiting for 5 mins");
              a = AnyChannel.this.read(300000L); // 5 mins
              if (a == null)
              {
                // we timed out
                try
                {
                  // clear the session
                  Any sessionId = socket_.getSessionId();
                  System.out.println ("Session Timeout " + sessionId);
                  handleClose(true);
                  break;
                }
                catch (Exception e) {}
//                finally
//                {
//                  break;
//                }
                  break;
              }
              else if (a == InputChannel.shutdown__)
              {
                // we were shutdown again - just wait for another timeout
                // period
                continue;
              }
              else
              {
                // When we are shutdown any events are handled
                // by the local dispatcher and not forwarded to
                // the process's input channel.
                ed_.processEvent((Event)a);

              	continue;
              }
            }
            else
            {
              //System.out.println ("Reading (not shutdown)");
              a = AnyChannel.this.read();
              //System.out.println("READ " + a);
            }
            
            if (a == AnyNull.instance())
            {
              //System.out.println("Probe...");
              continue;
            }

            if (a == InputChannel.timeout__)
            {
              handleClose(true);
              break;
            }
            
            if ((a == null || a == InputChannel.shutdown__) && AnyChannel.this.isClosed())
            {
            	// voluntary close
            	handleClose(true);
            	break;
            }

            if (AnyChannel.this.isClosedRead_)
            {
              //System.out.println("HANDLE FWD CLOSE");
              handleForwardClose();
              break;
            }

						if (a == InputChannel.shutdown__)
						{
              // Someone else called shutdownInput before this thread
              // detected any problems or may be we got EOF from
              // a stateless protocol.  Either:
              //   1) Client: try to reopen
              //   2) Server: flag ourselves as shutdown and
              //      go back to wait for the reconnect event.
              if (handleReopen())
                continue;
              else
                break;
            }
						if (!isShutdown_)
						{
              // Normal operation is to forward everything to the
              // process's input channel
						  if (lastFromClient_ != null)
						    lastFromClient_.setTime(System.currentTimeMillis());
              forwardTo_.write(a);
            }
            else
            {
              // When shutdown the thread is reused to await the
              // reconnect event.  We don't bother with a dispatcher,
              // we just assume that the next thing in is the desired
              // event.
              ed_.processEvent((Event)a);
            }
					}

          // Because we have switched off throwing of excptions
          // received through the (network) channel the only
          // thing we can catch are exceptions in the local
          // vm (concerning things like network failures)
					catch (ContainedException ce)
					{
            //ce.printStackTrace();
						Throwable t = ce.throwable_;
						if (t instanceof java.net.SocketException)
						{
              // The network connection has been dropped. If the connection
              // should be kept open then:
              //   1) If we are a client we can reopen it;
              //   2) If we are a server we go into the state to wait
              //      for the reconnection from the client
              // If the connection is not to be kept open then close down
              if (socket_.isKeepOpen() && !AnyChannel.this.isClosed())
              {
                // reOpen returns true if we have the connection parameters,
                // i.e. we are the client. Note, although this is the read
                // thread for a socket driver, it is convenient to reopen
                // it on behalf of the process thread here. May be that
                // thread never knows anything happened!
                System.out.println ("Keep open after exception");
                handleReopen();
                continue;
              }
              else
              {
                // Not keepOpen, just close down
                // Er...this is because we get socket exceptions even
                // when the socket is closed properly, most notably and
                // importantly with the Inqs client, which tells the
                // server to do a backwards close.
                handleClose(t.getMessage().indexOf("reset") >= 0);
                break;
              }
						}
            else //if (t instanceof java.io.ObjectStreamException)
            {
              // Some sort of other error eg serialisation problem
              forwardTo_.write(ce);
              handleClose(true);
              break;
            }
					}
					catch (AnyException e)
					{
            //e.printStackTrace();
						// If we received an exception through the channel then it will
						// have been thrown (see doRead() above) but this thread is
						// only for the purpose of reading the channel, it is not the
						// ultimate consumer.  Hence we must forward it.
						forwardTo_.write(e);
            handleClose(true);
            break;
					}
					catch (Exception e)
					{
            //e.printStackTrace();
						forwardTo_.write(new ContainedException(e));
            handleClose(true);
            break;
          }
          catch (Error e)
          {
            //e.printStackTrace();
            forwardTo_.write(new ContainedException(e));
            handleClose(true);
            break;
          }
				}
				stopKeepOpenProbe();
        stopKeepAliveTimer();
			}
			catch (AnyException e)
			{
        e.printStackTrace();
			}
      catch (Error e)
      {
        e.printStackTrace();
      }
      AnyChannel.this.reader_ = null;
		}

		private boolean handleReopen() throws AnyException
		{
      // Turn ourselves into a FIFO to suspend reading.
      // We check that this channel hasn't already been
      // shutdown - see SessionManager.java
      //System.out.println ("handleReopen 1");
      if (AnyChannel.this.channelDriver_ == socket_)
      {
        if (!socket_.isKeepOpen())
        {
          //System.out.println ("handleReopen 1.1");
          // not a keepOpen socket so close down
          handleClose(true);
          return false;
        }
        //System.out.println ("handleReopen 2");
        shutdownInput(new FIFO(0, ChannelConstants.REFERENCE));
      }
      isShutdown_ = true;

      // reOpen flushes any pending messages
      if (socket_.canReopen())
      {
        System.out.println ("Keep open attempt reconnect " + isDefunct_);
        if (!isDefunct_)
          forwardTo_.write(new SimpleEvent(EventConstants.SESSION_RECONNECT,
                                           socket_.getSessionId()));
      }
      else
      {
        // We are the server side. We can only await the
        // reconnect event from the client
        System.out.println ("Keep open await reconnect");

      }
      return true;
		}

		private void handleReconnectRead(Event e) throws AnyException
		{
			// Put the new tunnel driver into the socket
			HttpTunnel tunnel = (HttpTunnel)e.getContext();
			socket_.setReadChannelDriver(tunnel);

			//synchronized(AnyChannel.this)
			//{
	      AnyChannel.this.shutdownInput(socket_);
	      isShutdown_ = false;
			//}

      System.out.println("AnyChannel$Reader.handleReconnectRead");
		}

		private void handleReconnectSession(Event e) throws AnyException
		{
			// Put the new Socket driver into the channel
			// 'Shutdown' the channel with the new driver and clear shutdown
			// status

      System.out.println("AnyChannel$Reader.handleReconnectSession 1");
			Socket newSocket = (Socket)e.getContext();
      // Flush any queued messages awaiting output - server
      synchronized (AnyChannel.this)
      {
				socket_.copyFrom(newSocket);
	      AnyChannel.this.shutdownInput(socket_);
	      isShutdown_ = false;
      }
      System.out.println("AnyChannel$Reader.handleReconnectSession 2");
		}

		private void handleDefunctSession(Event e) throws AnyException
		{
			// The read thread is already shutdown thanks to the process
			// thread event processor for SESSION_DEFUNCT.
    	isDefunct_ = true;
    	stopKeepOpenProbe();
      stopKeepAliveTimer();
			handleClose(true);
		}

		private void handleClose(boolean interrupt)
		{
			try
			{
        stopKeepOpenProbe();
        stopKeepAliveTimer();
        Any sessionId = socket_.getSessionId();
        if (Globals.sessionList__ != null)
        {
          // server
          Globals.sessionList__.deleteSession(sessionId);
          AnyChannel.this.reader_ = null;
          if (!forwardTo_.isClosed())
          {
            forwardTo_.purgeReceived(true);
            //forwardTo_.close();
            if (p_ != null && interrupt)
              p_.interrupt();
          }
          AnyChannel.this.close();
          AnyChannel.this.isClosedRead_  = true;
          AnyChannel.this.isClosedWrite_ = true;
        }
        else
        {
          // client
          forwardTo_.write(new SimpleEvent(EventConstants.SERVER_LOST));
        }
        socket_.setSessionId(null);
			}
			catch (Exception e) {e.printStackTrace();}
		}

    // A close has been received from the peer process across the
    // network.  If we are a server, forward the close.  If we are
    // a client, treat as server lost.
    private void handleForwardClose()
    {
      handleClose(false);
    }

    /**
     * Events received here are sent by the HttpinqProtocolHandler
     * and will contain a new HttpTunnel that we can read from
     */
    private class ResumeRead extends    AbstractAny
                             implements EventListener
    {
      public boolean processEvent(Event e) throws AnyException
      {
        System.out.println("AnyChannel$Reader$ResumeRead.processEvent");
        handleReconnectRead(e);
        return true;
      }

      public Array getDesiredEventTypes()
      {
        return resumeReadEventTypes__;
      }
    }

    /**
     * Events received here are sent by the HttpinqProtocolHandler
     * and will contain a new HttpTunnel that we can read from
     */
    private class SessionResume extends    AbstractAny
                                implements EventListener
    {
      public boolean processEvent(Event e) throws AnyException
      {
        handleReconnectSession(e);
        return true;
      }

      public Array getDesiredEventTypes()
      {
        return sessionResumeEventTypes__;
      }
    }

    /**
     * Events received here are sent by via the process thread
     * and indicate that the session in the server is defunct
     * (server restart or session timeout)
     */
    private class SessionDefunct extends    AbstractAny
                                 implements EventListener
    {
      public boolean processEvent(Event e) throws AnyException
      {
        handleDefunctSession(e);
        return true;
      }

      public Array getDesiredEventTypes()
      {
        return sessionDefunctEventTypes__;
      }
    }
	}

  // This one is for http, which we don't really use any more.
	private class KeepOpenProbe extends Probe
	{
		private Event probe_ = new SimpleEvent(EventConstants.PING_KEEPALIVE);

		public void run()
		{
			try
			{
				while(!timerChannel_.isClosed())
				{
					Any a = timerChannel_.read(120000L);  // 2 mins
					if (a == null)
					{
            if (getSessionId() == null)
              break;  // no point in sending probes if the session is dud
						AnyChannel.this.write(probe_);
						AnyChannel.this.flushOutput();
					}
					a = null;  // for gc!
				}
				AnyChannel.this.keepOpenProbe_ = null;
			}
			catch (AnyException e)
			{
				AnyChannel.this.keepOpenProbe_ = null;
				e.printStackTrace();
			}
		}
	}
  
  // This one of for TCP sockets over the internet. To be retired
  private class KeepAliveProbe extends TimerTask
  {
    public void run()
    {
      try
      {
        //if ((System.currentTimeMillis() - lastSent_) > channelDriver_.getProbeTimeout())
        //{
          // Timed out because we didn't send anything.
          // Send keep alive message.
          //System.out.println("Sending probe");
          AnyChannel.this.doWrite(AnyNull.instance());
          AnyChannel.this.flushOutput();
        //}
      }
      catch(Throwable e)
      {
//        Throwable t = e.getThrowable();
//        
//        if ((t instanceof SocketException) ||
//            (t instanceof SSLException))
//        {
          AnyChannel.this.stopKeepAliveTimer();
//          try
//          {
            // Something went wrong writing to the socket and the periodic
            // probe has picked it up. Actually forget this as the probe
            // timeout will do the job for us.
//            if (channelDriver_.canReopen())
//            {
//              // client
//              AnyChannel.this.reader_.forwardTo_.write(new SimpleEvent(EventConstants.SERVER_LOST));
//            }
//            else
//            {
//              // server
//              AnyChannel.Reader r = AnyChannel.this.reader_;
//              if (r != null)
//              {
//                if (!r.forwardTo_.isClosed())
//                {
//                  r.forwardTo_.purgeReceived(true);
//                  //forwardTo_.close();
//                  if (r.p_ != null)
//                    r.p_.interrupt();
//                }
//                AnyChannel.this.close();
//                AnyChannel.this.isClosedRead_  = true;
//                AnyChannel.this.isClosedWrite_ = true;
//              }
//            }
//          }
//          catch(AnyException ae) {}
        }
//      }
//      catch (AnyException e)
//      {
//        AnyChannel.this.keepOpenProbe_ = null;
//        //e.printStackTrace();
//      }
    }
  }
  
  private class Probe extends Thread
  {
    protected AnyChannel timerChannel_;

    private Probe()
    {
      timerChannel_ = new AnyChannel(new FIFO(0, ChannelConstants.REFERENCE));
    }
  }
  
  // An object which, when passed through a channel, flushes any data
  // remaining to be read.
	static class PurgeBuffer extends    SimpleEvent
	                         implements Cloneable
	{
	  static PurgeBuffer instance__;

	  static
	  {
//	    Make the only instance ever allowed in this JVM
	    instance__ = new PurgeBuffer();
	  }

	  public static Any instance()
	  {
	    return instance__;
	  }

	  private PurgeBuffer()
	  {
	    super(EventConstants.PURGE_BUFFER);
	  }

	  public Object clone() throws CloneNotSupportedException
	  {
	    return this;
	  }

	  protected Object readResolve() throws ObjectStreamException
	  {
	    return PurgeBuffer.instance();
	  }
	}
}
