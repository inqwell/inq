/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class CCheckBox extends bibliothek.gui.dock.common.action.CCheckBox
{  
  private List<ActionListener> listeners_ = new ArrayList<ActionListener>();
  
  @Override
  protected void changed()
  {
    fire();
  }

  /**
   * Adds <code>listener</code> to this button, <code>listener</code> will be called
   * whenever this button it triggered.
   * @param listener the new listener, not <code>null</code>
   */
  public void addActionListener( ActionListener listener )
  {
    if( listener == null ){
      throw new IllegalArgumentException("listener must not be null");
    }
    listeners_.add(listener);
  }
  
  /**
   * Removes <code>listener</code> from this button.
   * @param listener the listener to remove
   */
  public void removeActionListener(ActionListener listener)
  {
    listeners_.remove(listener);
  }
  
  protected void fire()
  {
    ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
    
    for(int i = 0; i < listeners_.size(); i++)
    {
      listeners_.get(i).actionPerformed(event);
    }
  }
}
