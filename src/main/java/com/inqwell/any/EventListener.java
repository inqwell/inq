/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EventListener.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Definition of an event listener to which an com.inqwell.any.EventGenerator will
 * send events.  These interfaces together define a 'push' model of event
 * propagation, whereby action in a target is inspired directly by
 * com.inqwell.any.EventGenerator.fireEvent().
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public interface EventListener extends Any
{
  /**
   * Perform some action on event arrival.
   * @return <code>true</code> if the listener may have processed the
   * event, <code>false</code> if it did not 
   */
  public boolean processEvent(Event e) throws AnyException;

	/**
	 * Returns the event types this listener would like to listen to.
	 * Can be called by an <code>EventGenerator</code>'s
	 * <code>addEventListener()</code> methods for those cases where
	 * the generator supports subscription to specific event types
	 * and the listener wishes to specify the event types it wants
	 * to receive notification of.
	 */
  public Array getDesiredEventTypes();
}
