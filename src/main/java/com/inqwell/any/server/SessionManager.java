/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/SessionManager.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.ChannelConstants;
import com.inqwell.any.channel.ContentCipher;

/**
 * Manage session ids between Inq clients and this server.
 * By default, when an Inq client disconnects from an Inq
 * server the server process's input channel is closed,
 * resulting that process terminating. Under conditions of
 * unreliable network connectivity the client and server
 * may exchange a session id. In this case, when the server
 * detects that a client has terminated abnormally, the process
 * input channel is kept open, subject to a timeout, awaiting
 * reconnection by the client using the session id.
 * <p>
 * This class manages the session ids and maps them to the
 * necessary server entities, such as process channels, to
 * facilitate session management and reconnection. As an
 * event listener, it processes reconnect events, signaled
 * with the <code>SESSION_RESUME</code> event type.
 */
public class SessionManager extends    AbstractAny
                            implements SessionList
{
	static private SessionManager theSessionManager__;

  private Map sessionIds_;     // active sessions
  private Set deadSessionIds_; // no process but some pending data
  
	public static SessionManager instance()
	{
		if (theSessionManager__ == null)
		{
			synchronized (SessionManager.class)
			{
				if (theSessionManager__ == null)
					theSessionManager__ = new SessionManager();
			}
		}
		return theSessionManager__;
	}
	
	private SessionManager()
	{
    sessionIds_     = new SynchronizedMap(AbstractComposite.simpleMap());
    deadSessionIds_ = AbstractComposite.set();
	}

  /**
   * Establish a session id for the given process channels.
   * A <code>SESSION_SETID</code> event is sent on the
   * given output channel, to inform the client of the
   * session id value.
   */
	public void establishSessionId(InputChannel  pi,
	                               InputChannel  ni,
                                 OutputChannel oc,
                                 Socket        channelDriver,
                                 ContentCipher cc) throws AnyException
  {
    // The session id is the identity of the output channel
    Any sessionId = IdentityOf.identityOf(oc);
    
    if (sessionIds_.contains(sessionId))
    {
      // We've already got a session id.  Shouldn't happen but if
      // it does it is an error by the client so send an exception
      // there and abort
      AnyException e = new AnyException("Duplicate session request");
      oc.write(e);
			oc.flushOutput();
      throw e;
    }
    
    Event sessionIdEvent = new SimpleEvent(EventConstants.SESSION_SETID,
                                           sessionId);

    System.out.println ("Sending session id " + sessionId);
    
    oc.write(sessionIdEvent);
    oc.flushOutput();
    
    // if all ok then set up keepOpen and enter into session table.
    channelDriver.setKeepOpen(true);
    channelDriver.setSessionId(sessionId);
    
    Array sessionObjs = AbstractComposite.array(4);
    
    sessionObjs.add(pi);
    sessionObjs.add(ni);
    sessionObjs.add(oc);
    sessionObjs.add(channelDriver);
    sessionObjs.add(cc);
    
    sessionIds_.add(sessionId, sessionObjs);
  }

  /**
   * Close down the specified session.  Removes the session from this
   * manager and closes the associated channels, thus releasing
   * server-side resources.
   */
  public void deleteSession(Any sessionId) throws AnyException
  {
    if (sessionIds_.contains(sessionId))
    {
	    System.out.println ("Terminating idle session id " + sessionId);
      Array sessionObjs = (Array)sessionIds_.remove(sessionId);
      deadSessionIds_.remove(sessionId);
      // 1 is network input channel;
      // 2 is output channel;
      // 3 is original socket channel driver
      AnyChannel    ic = (AnyChannel)sessionObjs.get(1);
      ChannelDriver cd = (ChannelDriver)sessionObjs.get(3);
      ic.close();
      cd.close();
    }
  }

  public synchronized void zombieSession(Any sessionId)
  {
    if (sessionIds_.contains(sessionId))
    {
      if (!deadSessionIds_.contains(sessionId))
        deadSessionIds_.add(sessionId);
    }
  }

  public synchronized boolean isZombieSession(Any sessionId)
  {
    return deadSessionIds_.contains(sessionId);
  }

  public boolean isSessionActive(Any sessionId)
  {
    return (sessionIds_.contains(sessionId));
  }
  
  public AnyChannel getNetworkInputChannel(Any sessionId)
  {
  	AnyChannel ret = null;
  	
    if (sessionIds_.contains(sessionId))
    {
	    Array sessionObjs = (Array)sessionIds_.get(sessionId);
	    ret = (AnyChannel)sessionObjs.get(1);
    }
    return ret;	
  }
  
  public AnyChannel getNetworkOutputChannel(Any sessionId)
  {
  	AnyChannel ret = null;
  	
    if (sessionIds_.contains(sessionId))
    {
	    Array sessionObjs = (Array)sessionIds_.get(sessionId);
	    ret = (AnyChannel)sessionObjs.get(2);
    }
    return ret;	
  }
  
  public AnyChannel getProcessInputChannel(Any sessionId)
  {
  	AnyChannel ret = null;
  	
    if (sessionIds_.contains(sessionId))
    {
	    Array sessionObjs = (Array)sessionIds_.get(sessionId);
	    ret = (AnyChannel)sessionObjs.get(0);
    }
    return ret;	
  }

  public ContentCipher getCipher(Any sessionId)
  {
  	ContentCipher ret = null;
  	
    if (sessionIds_.contains(sessionId))
    {
	    Array sessionObjs = (Array)sessionIds_.get(sessionId);
	    ret = (ContentCipher)sessionObjs.get(4);
    }
    return ret;	
  }
}
