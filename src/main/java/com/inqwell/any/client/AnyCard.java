/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Iter;
import com.inqwell.any.client.swing.JPanel;

/**
 * 
 */
public class AnyCard extends AnyComponent
{
  private JPanel card_;
  
  public void setObject(Object o)
  {
    // Take this opportunity to put the card layout in
    JPanel j = (JPanel)o;
    j.setLayout(new CardLayout());
    
    card_ = (JPanel)o;
    
    super.setObject(o);
  }
  
  public Container getComponent()
  {
    return card_;
  }

  public void setChildVisible(AnyComponent child)
  {
    // set the specified child as the visible one for the card
    Container  c    = this.getComponent();
    CardLayout card = (CardLayout)c.getLayout();
    String     name = child.getNameInParent().toString();
    
    card.show(c, name);
	}
  
  public AnyComponent getVisibleCard()
  {
    // Descend and find the visible component
    return findVisibleCard(this);
  }

  public void initAsCellEditor()
  {
    if (card_.getClientProperty("Inq.editorWrapper") != null)
      return;
    
    // Delegate to immediate AnyComponent children (hierarchy requirement of
    // card usage)
    findAndInitAsEditors(this);
    
    card_.putClientProperty("Inq.editorWrapper", this);
  }
  
  /**
   * Implemented to return the renderInfo of the currently visible child,
   * primarily for cell editor support
   */
  public RenderInfo getRenderInfo()
  {
    RenderInfo ret = null;
    
    AnyComponent c = getVisibleCard();
    
    if (c != null)
      ret = c.getRenderInfo();
    
    return ret;
  }
  
  /**
   * Implemented as no operation, primarily for cell editor support
   */
  public void setRenderInfo(RenderInfo r)
  {
    
  }

  /**
   * Implemented to delegate to the currently visible child,
   * primarily for cell editor support
   */
  public void setRenderedValue(Any v) throws AnyException
  {
    AnyComponent c = getVisibleCard();
    
    if (c != null)
      c.setRenderedValue(v);
  }
  
  public Any getRenderedValue()
  {
    AnyComponent c = getVisibleCard();
    
    if (c != null)
      return c.getRenderedValue();
    
    return null;
  }
  
  public boolean forwardKeyBinding(KeyStroke ks,
                                   KeyEvent  e,
                                   int       condition,
                                   boolean   pressed)
  {
    boolean ret = false;
    
    AnyComponent c = getVisibleCard();
    
    if (c != null)
      ret = c.forwardKeyBinding(ks, e, condition, pressed);
    
    return ret;
  }
  
  public void setBounds(Rectangle r)
  {
    AnyComponent c = getVisibleCard();
    c.setBounds(r);
  }
  
  private void findAndInitAsEditors(Any parent)
  {
    Iter i = parent.createIterator();
    while (i.hasNext())
    {
      Any child = i.next();
      if (child instanceof AnyComponent)
      {
        AnyComponent childComp = (AnyComponent)child;
        childComp.initAsCellEditor();
      }
    }
  }

  private AnyComponent findVisibleCard(Any parent)
  {
    Iter i = parent.createIterator();
    while (i.hasNext())
    {
      Any child = i.next();
      if (child instanceof AnyComponent)
      {
        AnyComponent childComp = (AnyComponent)child;
        if (((Component)childComp.getAddee()).isVisible())
          return childComp;
      }
    }
    return null;
  }
}
