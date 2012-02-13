/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Iter;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.beans.UIFacade;

public abstract class AbstractButtonGroup extends AnyView
{
  private NotifyAction actionGenerator_;

  private RenderInfo   renderInfo_;

  private Set          ownedButtons_ = AbstractComposite.set();

  private static Set   bgProperties__;

  static
  {
    bgProperties__ = AbstractComposite.set();
    bgProperties__.add(AnyComponent.renderInfo__);
    bgProperties__.add(AnyComponent.contextNode__);
    bgProperties__.add(enabled__);
  }

  public void setObject(Object o)
  {
    actionGenerator_ = new NotifyAction();
    setupEventSet(o);
    setupEventSet(actionGenerator_);
  }

  public void setRenderInfo(RenderInfo r)
  {
    renderInfo_ = r;

    if (getContextNode() != null)
    {
      r.resolveNodeSpecs(getContextNode());

      try
      {
        Any dataNode = r.resolveDataNode(getContextNode(), true);
        if (dataNode != null)
        {
          componentProcessEvent(null);
        }
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }

      setupDataListener(r.getNodeSpecs());
    }
  }

  public RenderInfo getRenderInfo()
  {
    return renderInfo_;
  }

  public void addButton(AnyView b)
  {
    ownedButtons_.add(b);
  }

  public void removeButton(AnyView b)
  {
    if (ownedButtons_.contains(b))
      ownedButtons_.remove(b);
  }

  protected void contextEstablished()
  {
    if (renderInfo_ != null)
      setRenderInfo(renderInfo_);
  }

  public String getLabel()
  {
    return null;
  }

  public Object getAddIn()
  {
    throw new UnsupportedOperationException();
  }

  public Object getAddee()
  {
    return null;
  }

  public void setEnabled(Any enabled)
  {
    Iter i = buttonsIter();
    while (i.hasNext())
    {
      UIFacade v = (UIFacade)i.next();
      v.setEnabled(enabled);
    }
  }

  public void requestFocus()
  {
    throw new UnsupportedOperationException();
  }

  // Called by one of our radio button objects when selection
  // changes. Argument v is the value represented by the RenderInfo
  // object of the radio button.
  public void radioChanged(Any v) throws AnyException
  {
    if (renderInfo_ != null)
    {
      Any dataItem = renderInfo_.resolveDataNode(getContextNode(), true);
      if (dataItem != null)
        dataItem.copyFrom(v);
    }
    doAction();
  }

  protected Object getAttachee(Any eventType)
  {
    if (actionGenerator_ == null)
      actionGenerator_ = new NotifyAction();
    
    return actionGenerator_;
  }

  /**
   * Returns the underlying <code>javax.swing.ButtonGroup</code> object,
   * although this object itself has no properties.
   */
  protected Object getPropertyOwner(Any property)
  {
    if (bgProperties__.contains(property))
      return this;

    return getObject();
  }

  public JComponent getBorderee()
  {
    throw new UnsupportedOperationException("AnyButtonGroup.getBorderee()");
  }

  protected Iter buttonsIter()
  {
    return ownedButtons_.createIterator();
  }
  
  public abstract void addToGroup(AnyView radio);
  
  public abstract void removeFromGroup(AnyView radio);
  
  private void doAction()
  {
    actionGenerator_.fireActionPerformed(new ActionEvent(actionGenerator_,
        ActionEvent.ACTION_PERFORMED, "BG"));

  }

  private static class NotifyAction
  {
    ArrayList listeners_ = new ArrayList();

    public void addActionListener(ActionListener l)
    {
      listeners_.add(l);
    }

    public void removeActionListener(ActionListener l)
    {
      int i = -1;
      if ((i = listeners_.indexOf(l)) >= 0)
        listeners_.remove(i);
    }

    public void fireActionPerformed(ActionEvent e)
    {
      Iterator i = listeners_.iterator();
      while (i.hasNext())
      {
        ActionListener a = (ActionListener) i.next();
        a.actionPerformed(e);
      }
    }
  }
}
