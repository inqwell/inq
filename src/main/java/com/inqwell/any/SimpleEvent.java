/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SimpleEvent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;  

/**
 * A general purpose concrete event.  The use of the
 * event parameter is not specified by this class itself, it is up
 * to clients and the event's listeners to agree on what it means.
 */
public class SimpleEvent extends    AbstractEvent
												 implements Cloneable
{               
  protected Any param_;
  
  public SimpleEvent(Any eventType)
  {
		this(eventType, null, null);
  }			       
  
  public SimpleEvent(Any eventType, Any context)
  {
		this(eventType, context, null);
  }			       
  
  public SimpleEvent(Any eventType, Any context, Any param)
  {
    super(eventType);
    setContext(context);
    param_ = param;
  }			       
  
  public void setParameter (Any a)
  {
    param_ = a;
  }
  
  public Any getParameter ()
  {
    return param_;
  }
}
