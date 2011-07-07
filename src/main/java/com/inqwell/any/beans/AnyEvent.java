/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/AnyEvent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.beans;  
import com.inqwell.any.*;

/**
 * An <code>Event</code> implementation which wraps external
 * events based on <code>java.util.EventObject</code>
 */
public class AnyEvent extends AbstractEvent implements Cloneable
{               
  private java.util.EventObject evt_;
  private Any param_;
  
  public AnyEvent(java.util.EventObject evt, Any id)
  {
    super(id);
    evt_ = evt;
  }			       
  
  public void setParameter (Any a)
  {
    param_ = a;
  }
  
  public Any getParameter ()
  {
    return param_;
  }    

  public void consume()
  {
		super.consume();
		
		if (evt_ instanceof java.awt.event.InputEvent)
		{
			java.awt.event.InputEvent iv = (java.awt.event.InputEvent)evt_;
			iv.consume();
		}
  }

  public java.util.EventObject getUnderlyingEvent()
  {
    return evt_;
  }    
}
