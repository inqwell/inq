/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;


public class CRadioButton extends bibliothek.gui.dock.common.action.CRadioButton
{

  private AnyCRadio wrapper_;
  
  void setWrapper(AnyCRadio w)
  {
    wrapper_ = w;
  }
  
  @Override
  protected void changed()
  {
    if (isSelected())
      wrapper_.notifyButtonGroup();
  }

}
