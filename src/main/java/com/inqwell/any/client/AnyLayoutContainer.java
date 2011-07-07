/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JScrollPane;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.Set;

/**
 * Common functionality amongst layout containers
 */
public class AnyLayoutContainer extends AnyComponent
{
  private static Set     layoutProperties__;

  static
  {
    layoutProperties__ = AbstractComposite.set();
    layoutProperties__.add(enabled__);
  }
  
  /**
   * For captions
   */
  public void setRenderInfo(RenderInfo r)
  {
    super.setRenderInfo(r);
  }
  
  /**
   * Set the enabled state of all our (swing) child components
   */
  public void setEnabled(Any enabled)
  {
    b__.copyFrom(enabled);
    Container c = getComponent();
    processChildren(c, b__.getValue());
  }
  
  /**
   * Set the font of all our (swing) child components
   */
  public void setFont(Font f)
  {
    Container c = getComponent();
    processChildren(c, f);
  }
  
  protected void setValueToComponent(Any v)
  {
    // No-op for layout container but renderInfo property is supported to
    // allow a label
  }

  protected Object getPropertyOwner(Any property)
  {
    if (layoutProperties__.contains(property))
      return this;
    
    return super.getPropertyOwner(property);
  }
  
  protected Object getScroller()
  {
    // We don't keep the scroll pane ourselves.
    Container c = getComponent().getParent();
    if (c != null)
      c = c.getParent();
    
    if (c instanceof JScrollPane)
      return c;
    
    return null;
  }

  private void processChildren(Container c, boolean enabled)
  {
    int count = c.getComponentCount();
    for (int i = 0; i < count; i++)
    {
      Component child = c.getComponent(i);
      if (child instanceof com.inqwell.any.client.swing.JPanel)
        processChildren((Container)child, enabled);
      else
        child.setEnabled(enabled);
    }
  }
  
  private void processChildren(Container c, Font f)
  {
    int count = c.getComponentCount();
    for (int i = 0; i < count; i++)
    {
      Component child = c.getComponent(i);
      if (child instanceof com.inqwell.any.client.swing.JPanel)
        processChildren((Container)child, f);
      else
        child.setFont(f);
    }
  }
}
