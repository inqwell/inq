/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyButtonGroup.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.ConstBoolean;
import com.inqwell.any.Set;
import com.inqwell.any.Iter;
import com.inqwell.any.Event;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ConstString;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.client.swing.SwingInvoker;
import javax.swing.ButtonGroup;
import javax.swing.AbstractButton;
import javax.swing.border.Border;
import javax.swing.JComponent;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wraps a <code>javax.swing.ButtonGroup</code> object. In the Inq
 * world, an AnyButtonGroup supports the <code>"action"</code>
 * event type whenever any of the buttons contained within the
 * group are selected.
 */
public class AnyButtonGroup extends AnyView
{
  private ButtonGroup  buttonGroup_;
	private RenderInfo   renderInfo_;

  private Set          ownedButtons_ = AbstractComposite.set();
  private NotifyAction actionGenerator_;

  private static Set   bgProperties__;

  static
  {
    bgProperties__ = AbstractComposite.set();
    bgProperties__.add(AnyComponent.renderInfo__);
    bgProperties__.add(enabled__);
	}

  /**
   * Implemented as an unsupported operation - a ButtonGroup is
   * not an awt container
   * @throws UnsupportedOperationException
   */
	public Container getComponent()
	{
    throw new UnsupportedOperationException("ButtonGroup is not a Container");
  }

	public Container getAddIn()
  {
    return getComponent();
  }

  public void addComponent(Container c)
	{
    if (c instanceof AbstractButton)
    {
      AbstractButton b = (AbstractButton)c;
      buttonGroup_.add(b);
		}
	}

	public void setObject(Object o)
	{
		buttonGroup_ = (ButtonGroup)o;
		//System.out.println ("AnyButtonGroup.setObject " + o.getClass());
    actionGenerator_ = new NotifyAction();
    setupEventSet(o);
    setupEventSet(actionGenerator_);
	}

  public Object getObject()
  {
    return buttonGroup_;
  }

  public ButtonGroup getButtonGroup()
  {
    return buttonGroup_;
  }

  /**
   * Provide information about the data node we are viewing
   * that should, when modified, be reflected in the state
   * of the underlying group of buttons.
   */
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
          //radioChanged(dataNode);
          //System.out.println("DATA NODE " + dataNode + " " + com.inqwell.any.Globals.process__.getLineNumber());
        }
      }
      catch(AnyException e)
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

  protected void contextEstablished()
  {
    if (renderInfo_ != null)
      setRenderInfo(renderInfo_);
  }

  public void setModelRoot(Any newRoot) throws AnyException
  {
    throw new UnsupportedOperationException("AnyButtonGroup.setModelRoot()");
  }

	public String getLabel()
	{
		//System.out.println ("AnyButtonGroup.getLabel() returning empty");
    return null;
  }

	public Object getAddee()
	{
		return buttonGroup_;
	}

  void addButton(AnyRadio b)
  {
	  ownedButtons_.add(b);
  }

  void removeButton(AnyRadio b)
  {
  	if (ownedButtons_.contains(b))
  	  ownedButtons_.remove(b);
  }

	protected Object getAttachee(Any eventType)
	{
    if (actionGenerator_ == null)
      actionGenerator_ = new NotifyAction();

    return actionGenerator_;
	}

	/**
   * Returns the underlying <code>javax.swing.ButtonGroup</code>
   * object, although this object itself has no properties.
   */
	protected Object getPropertyOwner(Any property)
	{
		if (bgProperties__.contains(property))
			return this;

    return buttonGroup_;
	}

	public JComponent getBorderee()
	{
    throw new UnsupportedOperationException("AnyButtonGroup.getBorderee()");
	}

	public void applyBorder(Border border)
	{
	}

	public void setEnabled(Any enabled)
	{
    ConstBoolean b = new ConstBoolean(enabled);
    boolean isEnabled = b.getValue();
    Iter i = ownedButtons_.createIterator();
    while (i.hasNext())
    {
      AnyComponent v = (AnyComponent)i.next();
      v.getComponent().setEnabled(isEnabled);
    }
	}

	public void requestFocus()
	{
    throw new UnsupportedOperationException("AnyButtonGroup.requestFocus()");
	}

  /**
   * Resolves the data element representing the model this view is
   * observing and attempts to select the appropriate child radio
   * object
   */
	protected void componentProcessEvent(Event e) throws AnyException
	{
    if (renderInfo_ != null)
    {
      Any dataItem = renderInfo_.resolveDataNode(getContextNode(), true);
      if (dataItem != null)
      {
        Iter i = ownedButtons_.createIterator();
        while (i.hasNext())
        {
          Any c = i.next();
          final AnyRadio radio = (AnyRadio)c;
          Any rd = radio.getRadioData();
          if (rd != null && dataItem.equals(rd))
          {
            SwingInvoker ss = new SwingInvoker()
            {
              protected void doSwing()
              {
                buttonGroup_.setSelected(radio.getButtonModel(), true);
                //radio.doClick();
              }
            };

            ss.maybeSync();
            break;
          }
        }
      }
    }
  }

  // Called by one of our child radio button objects when selection
  // changes.  Argument v is the value represented by the RenderInfo
  // object of the radio button.
  void radioChanged(Any v) throws AnyException
  {
  	//System.out.println("AnyButtonGroup.radioChanged");
    if (renderInfo_ != null)
    {
      Any dataItem = renderInfo_.resolveDataNode(getContextNode(), true);
      if (dataItem != null)
        dataItem.copyFrom(v);
    }
    doAction();
  }

	private void doAction()
	{
		actionGenerator_.fireActionPerformed
			(new ActionEvent(actionGenerator_,
											 ActionEvent.ACTION_PERFORMED,
											 "BG"));


	}

	public static class NotifyAction
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
				ActionListener a = (ActionListener)i.next();
				a.actionPerformed(e);
			}
		}
	}
}
