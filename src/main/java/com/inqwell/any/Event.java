/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Event.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.util.EventObject;

/**
 * Definition of an event within the Any framework.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public interface Event extends Map
{
  /**
   * Puts the given Any into the event as data to carry.  This data
   * may change as the event propagates and is intended to be specific
   * to a given event type.  Put another (and more accurate) way, this
   * data is provided by the receiver of the event at the time
   * the interest was registered.  The receiver desires that this data
   * be returned in any event delivered to him.
   */
  public void setParameter(Any a);
  public Any getParameter();
  
  public Any getId();
  
  /**
   * Puts the given Any into the event as data to carry.  This data
   * is not intended to change as the event propagates.  Put another
   * (and more accurate) way, this data is provided by the event
   * originator at the point where the event leaves for its intended
   * recipients.
   */
  public void setContext(Any a);
  public Any getContext();
  
  /**
   * If the event is one of a series then this can be identified
   * by putting the same serial number into each event in the
   * series.
   */
  public void setSerialNumber(int serialNumber);
  
  /**
   * Get this event's global sequence number
   */
  public int getSequence();
  
  /**
   * Return this event's serial number. If an event does not
   * carry a serial number, less than zero is returned.
   */
  public int  getSerialNumber();
  
  public void reset (Any a);
  
  public Event cloneEvent();
  
  public boolean isConsumed();
  
  public void consume();
  
  /**
   * If the implementation carries an event from an underlying source
   * then return that event.  Returns null otherwise.
   */
  public EventObject getUnderlyingEvent();
}
