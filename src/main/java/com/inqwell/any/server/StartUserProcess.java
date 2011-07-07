/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/StartUserProcess.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.server;

import com.inqwell.any.*;
//import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.ChannelConstants;

/**
 * An <code>EventListener</code> that starts a user process.  The event
 * is passed from the initiator of a connection to a remote
 * <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup>
 * environment.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 */
public class StartUserProcess extends    AbstractAny
							                implements EventListener,
																				 Cloneable
{
  private static final long serialVersionUID = 1L;

  static private Array eventTypes__;
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.START_USERPROCESS);
		eventTypes__.add(EventConstants.START_WEBPROCESS);
  }
	
	/**
	 * 
	 */
  public StartUserProcess()
  {
  }

  public boolean processEvent(Event e) throws AnyException
  {
		StartProcessEvent spe = (StartProcessEvent)e;
	  
	  AnyChannel processInput = new AnyChannel(new FIFO(0,
																											ChannelConstants.REFERENCE));

	  //AnyChannel readInput = new AnyChannel(spe.getSocket(), processInput);
	  AnyChannel readInput = new AnyChannel(null);
	  Socket s = spe.getSocket();
	  AnyChannel processOutput = new AnyChannel(s);
	  
	  try
	  {
			// If there is a session request then request the ID
			if (spe.getContext() != null)
	    {
				SessionManager.instance().establishSessionId(processInput,
				                                             readInput,
	                                                   processOutput,
	                                                   spe.getSocket(),
	                                                   spe.getCipher());
	    }

		  Transaction t = new PrimaryTransaction();
			
			EventDispatcher      ed   = new EventDispatcher();
      BasicProcess.RootMap root = new BasicProcess.RootMap();
      
      // Dispatcher for service requests - Inq clients
			ed.addEventListener(InvokeService.makeInvokeService
                             (EventConstants.INVOKE_SVC,
                              t,
                              root));
			
			
      // Dispatcher for service requests - web container clients
			// Although this and the one for Inq clients are not used
			// at the same time at present.
			ed.addEventListener(InvokeWebService.makeInvokeService(t, root, processOutput));

      InvokeService invokeLogin = new InvokeLoginService();
      invokeLogin.setTransaction(t);
      invokeLogin.setRoot(root);
      ed.addEventListener(invokeLogin);

			// Set up an event dispatcher to cope with resume writes
			// in case we are using a stateless protocol
      // TODO Remove
			ed.addEventListener (new ResumeRead(readInput,
			                                    processInput));
			ed.addEventListener (new ResumeWrite(processInput,
                                           processOutput));
	
			UserProcess p = new UserProcess (processInput,
                                       processOutput,
                                       new ExceptionToClient(processOutput),
                                       t,
                                       root,
                                       ed,
                                       e.getId().equals(EventConstants.START_USERPROCESS)
                                         ? ClientPropagator.makeClientPropagator(processOutput)
                                         : null);

      root.setProcess(p);
      
      // Safe to start the reader thread now we've set any session id
			// and sent the session event to the client.
      p.add(UserProcess.lastFromClient__, new AnyDate());
			readInput.startReader(s, processInput, true, p);
			readInput.setThreadName(spe.getParameter().toString() + ".Socket");
			
      p.startThread();
      return true;
    }
    catch (AnyException aex)
    {
      aex.printStackTrace();
      processOutput.write(aex);
      processOutput.flushOutput();
    	processOutput.close();
		  throw (aex);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      ContainedException cex = new ContainedException(ex);
      processOutput.write(cex);
      processOutput.flushOutput();
    	processOutput.close();
		  throw (cex);
    }
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes__;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
		AbstractAny.cloneNotSupported(this);
		return null;
  }
}
