/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:23 $
 */
 
package com.inqwell.any.tools;

import com.inqwell.any.*;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.Socket;

/**
 * An <code>EventListener</code> that handles the reception of
 * a <code>SESSION_SETID</code> event received by a client
 * process from its server peer, when requested.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class SessionID extends    AbstractAny
                       implements EventListener,
                                  Cloneable
{
  private OutputChannel outputChannel_;
  
  static private Array eventTypes__;
  
  static
  {
		eventTypes__ = AbstractComposite.array();
		eventTypes__.add(EventConstants.SESSION_SETID);
	}
	
	/**
	 * 
	 */
  public SessionID(OutputChannel oc)
  {
  	outputChannel_ = oc;
  }

  public boolean processEvent(Event e) throws AnyException
  {
    outputChannel_.setSessionId(e.getContext());
    
  	System.out.println("SessionID.processEvent " + e);
    return true;
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
