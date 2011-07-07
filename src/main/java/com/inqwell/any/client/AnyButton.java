/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyButton.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.text.Format;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;

import com.inqwell.any.Any;
import com.inqwell.any.Event;

public class AnyButton extends AnyComponent
{
  private static final long serialVersionUID = 1L;

  private AbstractButton b_;

  public void setObject(Object o)
  {

    if (!(o instanceof AbstractButton))
      throw new IllegalArgumentException
                  ("AnyButton wraps javax.swing.AbstractButton and sub-classes");


    b_ = (AbstractButton)o;      

    super.setObject(b_);
  }
  
  public Container getComponent()
  {
    return b_;
  }
  
  public void initAsCellEditor()
  {
    b_.setOpaque(true);
  }

  protected void setValueToComponent(Any v)
  {
    // For a button we assume the text property as the default
    // rendered value.
    Format f = getRenderInfo().getFormat(v);
    b_.setText(f.format(v));
  }
  protected boolean handleBoundEvent(Event e)
  {
    // Check if we are a default button and if so send a spoof
    // focus lost event to the current focus owner. Then any focus
    // handling that has been scripted will be performed. Remains
    // to be seen whether we need to specify the opposite
    // component (as the same) or whatever interference with the focus
    // subsystem this may cause.
    JRootPane r = b_.getRootPane();
    if (r != null)
    {
      if (b_ == r.getDefaultButton())
      {
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        if (c != null)
        {
          //System.out.println("Sending focus lost to " + c);
          c.dispatchEvent(new FocusEvent(c, FocusEvent.FOCUS_LOST, false, null));
        }
      }
    }
    return true && super.handleBoundEvent(e);
  }

}
