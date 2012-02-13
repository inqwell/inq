/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Event;
import com.inqwell.any.Iter;
import com.inqwell.any.client.swing.SwingInvoker;

/**
 * Wraps a <code>javax.swing.ButtonGroup</code> object. In the Inq world, an
 * AnyButtonGroup supports the <code>"action"</code> event type whenever any of
 * the buttons contained within the group are selected.
 */
public class AnyButtonGroup extends AbstractButtonGroup
{
  private ButtonGroup  buttonGroup_;

  public void setObject(Object o)
  {
    buttonGroup_ = (ButtonGroup) o;
    super.setObject(o);
  }

  public Object getObject()
  {
    return buttonGroup_;
  }

  public void addToGroup(AnyView radio)
  {
    buttonGroup_.add((AbstractButton)radio.getObject());
    addButton(radio);
  }
  
  public void removeFromGroup(AnyView radio)
  {
    buttonGroup_.remove((AbstractButton)radio.getObject());
    removeButton(radio);
  }

  /**
   * Resolves the data element representing the model this view is observing and
   * attempts to select the appropriate child radio object
   */
  protected void componentProcessEvent(Event e) throws AnyException
  {
    RenderInfo r = getRenderInfo();
    if (r != null)
    {
      Any dataItem = r.resolveDataNode(getContextNode(), true);
      if (dataItem != null)
      {
        Iter i = buttonsIter();
        while (i.hasNext())
        {
          Any c = i.next();
          final AnyRadio radio = (AnyRadio) c;
          Any rd = radio.getRenderedValue();
          if (rd != null && dataItem.equals(rd))
          {
            SwingInvoker ss = new SwingInvoker()
            {
              protected void doSwing()
              {
                buttonGroup_.setSelected(radio.getButtonModel(), true);
                // radio.doClick();
              }
            };

            ss.maybeSync();
            break;
          }
        }
      }
    }
  }
}
