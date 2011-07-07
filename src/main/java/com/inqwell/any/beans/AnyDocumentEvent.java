/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/AnyDocumentEvent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.beans;  
import com.inqwell.any.*;

/**
 * An <code>Event</code> implementation which wraps external
 * events based on <code>javax.swing.event.DocumentEvent</code>
 */
public class AnyDocumentEvent extends AbstractEvent implements Cloneable
{               
  private javax.swing.event.DocumentEvent evt_;
  private Any param_;
  
  public AnyDocumentEvent(javax.swing.event.DocumentEvent evt, Any id)
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
  
  public javax.swing.event.DocumentEvent getEvent()
  {
    return evt_;
  }    
}
