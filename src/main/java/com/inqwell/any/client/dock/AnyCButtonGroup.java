/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import bibliothek.gui.dock.common.action.CRadioButton;
import bibliothek.gui.dock.common.action.CRadioGroup;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Event;
import com.inqwell.any.Iter;
import com.inqwell.any.client.AbstractButtonGroup;
import com.inqwell.any.client.AnyView;
import com.inqwell.any.client.RenderInfo;
import com.inqwell.any.client.swing.SwingInvoker;

public class AnyCButtonGroup extends AbstractButtonGroup
{
  private CRadioGroup buttonGroup_;
  
  @Override
  public void setObject(Object o)
  {
    buttonGroup_ = (CRadioGroup) o;
    super.setObject(o);
  }

  @Override
  public void addToGroup(AnyView radio)
  {
    buttonGroup_.add((CRadioButton)radio.getObject());
    addButton(radio);
  }

  @Override
  public void removeFromGroup(AnyView radio)
  {
    buttonGroup_.remove((CRadioButton)radio.getObject());
    removeButton(radio);
  }

  @Override
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
          final AnyCRadio radio = (AnyCRadio) c;
          Any rd = radio.getRenderedValue();
          if (rd != null && dataItem.equals(rd))
          {
            SwingInvoker ss = new SwingInvoker()
            {
              protected void doSwing()
              {
                radio.setSelected(true);
              }
            };

            ss.maybeSync();
            break;
          }
        }
      }
    }
  }

  @Override
  public Object getObject()
  {
    // TODO Auto-generated method stub
    return buttonGroup_;
  }

}
