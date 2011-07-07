/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EventGenerator.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Definition of an event generator within the Any framework.  Unlike the
 * Java Beans/Swing event structure, we prescribe that any class generating
 * events implements this interface, because we are not interested in using
 * introspection to determine the addXXXListener methods, or in using
 * specific class types to indicate the type of event we are 
 * <p>
 * An event generator's function is to deliver events to registered targets.
 * Implementations are free to define things like their own thread policy,
 * whether they filter or augment events passing through them and so on.
 * <p>
 * The event generator is similar to an adaptor in the Java Beans design,
 * or to a channel in corba.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public interface EventGenerator extends Any
{
  /**
   * Send the given event to all registered listeners.
   */
  public void fireEvent (Event e) throws AnyException;
  
  /**
   * Add an event listener.  When fireEvent is called the event may be
   * propagated to the registered listeners.  The event listener may
   * parameterise the event it receives with the supplied Any.
   */
  public void addEventListener (EventListener l, Any eventParam);

  public void addEventListener (EventListener l);
  
  /**
   * Remove an event listener.
   */
  public void removeEventListener (EventListener l);

  public void removeAllListeners ();

	/**
	 * Return the event types this generator can emit.  This method is
	 * of use to event listeners (or those setting up listeners) where
	 * each event type will be processed in a different way. 
	 */
  public Array getGeneratedEventTypes();
  
  /**
	 * An optional operation which is implemented by true event originators,
	 * as opposed to those generators which merely act as propagators.
	 * Yields an event of the specified type provided that type is valid
	 * for this EventGenerator.
	 */
  public Event makeEvent(Any eventType);
  
  /**
   * Whether, during iteration over a node space, this <code>EventGenerator</code>
   * advises events may be raised against its children.
   * @param e The event that would be raised.
   * @return <code>true</code> if it is valid to raise events against
   * this <code>EventGenerator</code>'s children, false if it is not.
   */
  public boolean raiseAgainstChildren(Event e);
}
