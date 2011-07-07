/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/ListenerAdaptee.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;
import com.inqwell.any.*;

/**
 * Define the operations to be supported by classes which adapt
 * events from external sub-systems, usually written to the
 * JDK event idiom of event type == java class, to that of the Any
 * framework, where the event type is carried inside implementations
 * of the <code>Event</code> interface.
 */
public interface ListenerAdaptee
{
	/**
	 * Receive an Any event from an adapter to an external component.
	 */
  public void adaptEvent(Event e);

	/**
	 * Route an adapted event received at this to the given listener
	 */
  public void addAdaptedEventListener(EventListener l);
  
	/**
	 * Route an adapted event received at this to the given listener
	 * with the supplied parameter.
	 */
	public void addAdaptedEventListener(EventListener l, Any eventParam);
	
	public void removeAdaptedEventListener(EventListener l);
}

