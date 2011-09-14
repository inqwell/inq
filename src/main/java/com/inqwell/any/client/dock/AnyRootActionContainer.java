/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import java.util.ArrayList;

import javax.swing.JComponent;

import bibliothek.gui.dock.common.action.CAction;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Event;
import com.inqwell.any.client.AnyView;
import com.inqwell.any.client.RenderInfo;


public class AnyRootActionContainer extends AnyView
{
  private RootActionContainer actions_;
  
  @Override
  protected void componentProcessEvent(Event e) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Object getAttachee(Any eventType)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Object getPropertyOwner(Any property)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected RenderInfo getRenderInfo()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getAddIn()
  {
    return getObject();
  }

  @Override
  public Object getAddee()
  {
    throw new UnsupportedOperationException();
  }
  
  public ArrayList<CAction> getActions()
  {
    return actions_.getActions();
  }

  @Override
  public JComponent getBorderee()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLabel()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void requestFocus()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabled(Any enabled)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRenderInfo(RenderInfo r)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getObject()
  {
    return actions_;
  }

  @Override
  public void setObject(Object o)
  {
    actions_ = (RootActionContainer)o;
  }
}
