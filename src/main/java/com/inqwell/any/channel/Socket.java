/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/channel/Socket.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.io.ReplacingOutputStream;
import com.inqwell.any.io.ResolvingInputStream;
import com.inqwell.any.net.InqURLConnection;


/**
 * A channel driver that uses an <code>InputStream</code> and
 * <code>OutputStream</code> based on a <code>Socket</code>
 * to transfer Any structures.
 * Currently only ObjectInput/OutputStreams are supported.
 * This channel driver implementation also manages the socket
 * from which the streams were derived.
 * <p>
 * The channel may support either or both of read/write depending
 * on what streams are supplied.  If unavailable a method
 * throws an UnsupportedOperationException.
 * <p>
 * <b>NOTE:</b> Although the socket driver can be created to
 * support bi-directional I/O it should <i>not</i> be placed inside
 * a single <code>AnyChannel</code> instance on which concurrent
 * reads and writes will be performed, or deadlock across the channel
 * will occur.
 */
public class Socket extends    AbstractAny
										implements ChannelDriver
{
  // Only initialised if supplied on construction. Otherwise
  // we are based on a URL.
	private java.net.Socket  socket_;

	// Only initialised if we are an outbound connection
	private URL              url_;
	private InqURLConnection urlConnection_;

	private boolean          socketClosed_ = false;
	private boolean          keepOpen_     = false;
	private ChannelDriver    writeChannelDriver_;
	private ChannelDriver    readChannelDriver_;
	private boolean          isClosed_ = false;

	// Initialised should we require a temporary queue for output
	// messages in the face of exceptions and keepOpen_ == true
	private AnyChannel       pendingOutput_;

	private Any              sessionId_;

  // Period after which a probe message will be sent to keep
  // the socket from seizing (for want of a better word)
  private long   keepAliveTime_ = 0;

	/**
	 * Make an input-only channel around the given stream which was created
	 * by the given socket.
	 */
	public Socket(java.net.Socket s, ResolvingInputStream is)
	{
		this(s, is, null);
  }

	/**
	 * Make an output-only channel around the given stream which was created
	 * by the given socket.
	 */
	public Socket(java.net.Socket s, ReplacingOutputStream os)
	{
		this(s, null, os);
  }

	/**
	 * Make an input/output channel around the given streams which were created
	 * by the given socket.
	 */
	public Socket(java.net.Socket s, ResolvingInputStream is, ReplacingOutputStream os)
	{
		socket_ = s;
		
		// Shouldn't do this unless we actually start the timer that
		// sends keepalive messages! 
//    try
//    {
//      if (getProbeTimeout() > 0)
//        socket_.setSoTimeout((int)getProbeTimeout() + 15000);
//    }
//    catch(SocketException se)
//    {
//      throw new RuntimeContainedException(se);
//    }
    
		readChannelDriver_  = new Serialize(is, os);
		writeChannelDriver_ = readChannelDriver_;
  }

	public Socket(ChannelDriver readChannelDriver,
	              ChannelDriver writeChannelDriver)
	{
		readChannelDriver_  = readChannelDriver;
		writeChannelDriver_ = writeChannelDriver;
	}

  public Socket (URL url, Any cert) throws AnyException
  {
		openURL(url, cert);
  }

  public Socket (URL url) throws AnyException
  {
    this(url, null);
  }

  // Just so we can split the creation and connection of the Socket
  // when logging in from the client
  public Socket () {}

  public Any read() throws AnyException
  {
    Any ret = null;
    try
    {
      ret = readChannelDriver_.read();
    }
    catch(ContainedException ce)
    {
      Throwable t = ce.getThrowable();
      if (t instanceof java.net.SocketTimeoutException)
        ret = InputChannel.timeout__;
      else
        throw ce;
    }
    
		return ret;
	}

  public void write(Any a) throws AnyException
  {
  	boolean enqueue = false;

  	if (urlConnection_ != null)
  	{
  		// Client.  What this means is that, for
  		// speakinq (isPermanent() == true) we honour the
  		// keepOpen_ flag on the client side.  Then, for
  		// permanent protocols, we can choose whether to
  		// enqueue for unreliable connections that we will
  		// reestablish.  For non-permanent protocols, like
  		// http, we will establish a connection every time
  		// anyway, so keepOpen_ is not relevant for the
  		// client.
  		//if (urlConnection_.isPermanent())
  		  enqueue = keepOpen_;
  	}
  	else
  	{
  		// For the server, we only have the keepOpen_ flag,
  		// which was initialised by the client's login
  		// message.
  		enqueue = keepOpen_;
  	}

  	write(a, enqueue);
  }

	public boolean isEmpty()
	{
		return readChannelDriver_.isEmpty();
	}

  public boolean isFull()
  {
		return writeChannelDriver_.isFull();
  }

  public boolean canShutdown()
  {
    if (socket_ != null)
      return true;

    return readChannelDriver_.canShutdown();
  }

  public void purgeReceived()
  {
  }
  
  public void close() throws AnyException
  {
  	isClosed_ = true;

		AnyException cdex   = null;
		Exception    sockex = null;

		try
		{
			if (readChannelDriver_ != null)
			  readChannelDriver_.close();
			if (writeChannelDriver_ != null)
			  writeChannelDriver_.close();
		}
		catch (AnyException cex)
		{
			cdex = cex;
		}

		// Even if we get an exception on closing the streams
		// we want a chance to close the socket
		if (!socketClosed_)
		{
			socketClosed_ = true;
			try
			{
				if (socket_ != null)
				{
				  socket_.shutdownOutput();
				  socket_.shutdownInput();
				}
			}
			catch (Exception sex)
			{
				sockex = sex;
			}
		}

		if (cdex != null)
			throw cdex;

		if (sockex != null)
			AnyException.throwExternalException (sockex);
	}

	public boolean isClosed()
	{
		return isClosed_;
	}

	public void setSoTimeout(int timeout)
	{
	  try
	  {
      socket_.setSoTimeout(timeout);
	  }
	  catch(SocketException e)
	  {
	    throw new RuntimeContainedException(e);
	  }
	}
	
  public void shutdownInput() throws AnyException
  {
    // Should bump out anyone reading from the socket's input stream
    // with EOF
    if (socket_ != null)
    {
      try
      {
        readChannelDriver_.shutdownInput();
        socket_.shutdownInput();
      }
      catch (IOException e)
      {
        AnyException.throwExternalException (e);
      }
    }
    else
    {
    	if (readChannelDriver_ != null)
        readChannelDriver_.shutdownInput();
    }
  }

  public void drainInput()
  {
  	throw new UnsupportedOperationException("AbstractChannelDriver.drainInput");
  }

  public ChannelDriver resetOutput(ChannelDriver d) throws AnyException
  {
  	if (d instanceof Socket)
  	  return d;

    if (d != this)
    {
      writeChannelDriver_ = writeChannelDriver_.resetOutput(d);
      //writeChannelDriver_ = d;
    }

    if (!d.isClosed())
      flushPendingMessages();

    return this;
  }

  public void flushOutput() throws AnyException
  {
  	/*
  	// If we have a URL connection then perform this
  	// operation via it, so that it knows to initialise
  	// its write driver next time.  Otherwise just tell
  	// the write driver directly (server-side)
  	if (urlConnection_ != null)
  	  urlConnection_.flushOutput();
  	else
  	*/
  	writeChannelDriver_.flushOutput();
  }

  public synchronized void setSessionId(Any sessionId)
  {
  	//System.out.println ("Socket setting session id " + sessionId);
    sessionId_ = sessionId;
    readChannelDriver_.setSessionId(sessionId);
    writeChannelDriver_.setSessionId(sessionId);
    if (urlConnection_ != null)
      urlConnection_.setSessionId(sessionId);
  }

  public synchronized Any getSessionId()
  {
    return sessionId_;
  }

  public synchronized void setReadChannelDriver(ChannelDriver d)
  {
  	readChannelDriver_ = d;
  }

	public boolean reOpen() throws AnyException
	{
    // We can only reopen if we initiated the connection in the
    // first place
    if (urlConnection_ == null)
      return false;

    try
    {
      urlConnection_.disconnect();
    }
    catch(Exception e) {}

    openURL(url_, null);

    // if successful in reopening and we are a 'permanent'
    // connection then try to write any pending messages
    if (urlConnection_.isPermanent())
      flushPendingMessages();

    return true;
	}

  public InqURLConnection getInqURLConnection()
  {
    return urlConnection_;
  }

  public java.net.Socket getSocket()
  {
    return socket_;
  }
  
	public boolean canReopen()
	{
		if (urlConnection_ == null)
      return false;
    else
      return true;
  }

	public void setKeepOpen(boolean keepOpen)
	{
		keepOpen_ = keepOpen;
	}

	public boolean isBlocking()
	{
    return true;
  }

	public boolean isKeepOpen()
	{
		return keepOpen_;
	}

  public long getProbeTimeout()
  {
    return keepAliveTime_;
  }

  public void setProbeTimeout(long timeout)
  {
    keepAliveTime_ = timeout;
  }

  public boolean isClient()
	{
		return url_ != null;
	}

  public boolean hasPendingOutput()
  {
    return pendingOutput_ != null;
  }

  /**
   * Return the total number of bytes sent so far on this channel
   * driver before compressing. Only relevant when the underlying
   * output stream supports compression and is operating in compressed
   * mode.
   * @return the total number of uncompressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalIn()
  {
    if (writeChannelDriver_ instanceof Serialize)
    {
      Serialize s = (Serialize)writeChannelDriver_;
      return s.getTotalIn();
    }
    else
      return -1;
  }

  /**
   * Return the total number of bytes sent so far on this channel
   * after compressing. Only relevant when the underlying
   * output stream supports compression and is operating in compressed
   * mode.
   * @return the total number of compressed bytes sent or -1 if
   * not setup for output or not compressing.
   */
  public long getTotalOut()
  {
    if (writeChannelDriver_ instanceof Serialize)
    {
      Serialize s = (Serialize)writeChannelDriver_;
      return s.getTotalOut();
    }
    else
      return -1;
  }

  public void setCompressed(boolean compressed)
  {
    if (writeChannelDriver_ instanceof Serialize)
    {
      Serialize s = (Serialize)writeChannelDriver_;
      s.setCompressed(compressed);
    }
  }

  public boolean isCompressed()
  {
    if (writeChannelDriver_ instanceof Serialize)
    {
      Serialize s = (Serialize)writeChannelDriver_;
      return s.isCompressed();
    }
    return false;
  }

	public void openURL (URL u, Any cert) throws AnyException
	{
		url_ = u;
    try
    {
    	if (urlConnection_ == null)
    	{
        urlConnection_ = (InqURLConnection)u.openConnection();
	      keepOpen_      = urlConnection_.isUnreliable();
    	}
      urlConnection_.setTrusted(cert);

      urlConnection_.connect();
      // review this - keepOpen is only important for servers?

      writeChannelDriver_ = urlConnection_.getWriteChannelDriver();
      readChannelDriver_  = urlConnection_.getReadChannelDriver();
      
      // Shouldn't do this unless we actually start the timer that
      // sends keepalive messages! 
//      java.net.Socket s = urlConnection_.getSocket();
//      if (getProbeTimeout() > 0)
//        s.setSoTimeout((int)getProbeTimeout() + 15000);
    }
    catch (Exception e)
    {
      AnyException.throwExternalException (e);
    }
	}

	/**
   * For internal use only.  Limited semantics to support session
   * resumption
   */
	public Any copyFrom(Any a)
	{
		if (a == this)
		{
      try
      {
        flushPendingMessages();
      }
      catch(AnyException e)
      {
      	throw new RuntimeContainedException(e);
      }
		  return this;
		}

    if (!(a instanceof Socket))
      super.copyFrom(a);
    else
    {
      Socket from = (Socket)a;

      socket_             = from.socket_;

      url_                = from.url_;
      urlConnection_      = from.urlConnection_;
      socketClosed_       = from.socketClosed_;
      keepOpen_           = from.keepOpen_;

      writeChannelDriver_ = from.writeChannelDriver_;
      readChannelDriver_  = from.readChannelDriver_;
      isClosed_           = from.isClosed_;

      sessionId_          = from.sessionId_;

      try
      {
        flushPendingMessages();
      }
      catch(AnyException e)
      {
      	throw new RuntimeContainedException(e);
      }
    }
    return this;
	}

	/**
	 * Define cloning.  In fact, this class is immutable and therefore
	 * cloning is not relevant and we just return
	 * <i>this</i>.  Cloning must be defined, however, as these objects
	 * may be transmitted through local channels, which are at liberty
	 * to clone what they transport, if they so wish.
	 */
  public Object clone()
  {
		return this;
  }

	protected void finalize() throws Throwable
	{
		super.finalize();
		this.close();
	}

  private void write(Any a, boolean enqueue) throws AnyException
  {
		//System.out.println ("Socket.write(a) " + a);

		// If we have a pending output queue while the session is
		// re-established then put the message there
		if (pendingOutput_ != null)
		{
      //System.out.println("pending 1 " + getSessionId() + " " + a);
      pendingOutput_.write(a);
      return;
    }

		if (!enqueue)
		{
      writeChannelDriver_.write(a);
    }
    else
    {
      try
      {
        writeChannelDriver_.write(a);
      }
      catch (ContainedException ce)
      {
        Throwable t = ce.throwable_;
        if (t instanceof java.io.IOException)
        {
        	// Bumps the read thread - this does the reOpen duties
        	// (note that calling this method is only belt+braces
        	//
          //shutdownInput();

          // Set up the queue for pending output messages.
          if (pendingOutput_ == null)
          {
	          pendingOutput_ = new AnyChannel(new FIFO(0,
	                                                   ChannelConstants.COPY));
	          pendingOutput_.setThrowsExceptions(false);
          }

          // and put this message on it
          //System.out.println("pending 2 " + getSessionId() + " " + a);
          pendingOutput_.write(a);
        }
        else
        {
          throw ce;
        }
      }
    }
  }

  private void flushPendingMessages() throws AnyException
  {
    System.out.println("Socket.flushPendingMessages 1");
    if (pendingOutput_ != null)
    {
      System.out.println("Socket.flushPendingMessages 2");
    	AnyChannel pendingOutput = pendingOutput_;
    	pendingOutput_ = null;

      Any a = null;

      while ((a = pendingOutput.read(0)) != null)
      {
        write(a);
        //System.out.println("Socket.flushPendingMessages " + getSessionId() + " " + a);
        // note - if anything goes wrong while flushing we
        // will create a new FIFO channel and continue to
        // retry.
      }
      flushOutput();
    }
  }
}
