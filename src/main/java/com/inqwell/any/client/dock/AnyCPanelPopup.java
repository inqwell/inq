/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import java.awt.Container;

import bibliothek.gui.dock.common.action.CPanelPopup;

import com.inqwell.any.AnyRuntimeException;

public class AnyCPanelPopup extends AnyCAction
{
  @Override
  public Object getAddIn()
  {
    return getContent();
  }

  @Override
  public void setObject(Object o)
  {
    if (!(o instanceof CPanelPopup))
      throw new AnyRuntimeException("Not a CPanelPopup");
    
    super.setObject(o);
  }
  
  private Container getContent()
  {
    return ((CPanelPopup)getObject()).getContent();
  }
}
