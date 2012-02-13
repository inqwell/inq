/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.Descriptor;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.client.AbstractButtonGroup;
import com.inqwell.any.client.AnyComponent;
import com.inqwell.any.client.RenderInfo;

public class AnyCRadio extends AnyCAction
{
  private AbstractButtonGroup g_;
  private RenderInfo          r_;

  private static Set     radioProperties__;
  private static Any     buttonGroup__ = AbstractValue.flyweightString("buttonGroup");

  static
  {
    radioProperties__ = AbstractComposite.set();
    radioProperties__.add(buttonGroup__);
    radioProperties__.add(AnyComponent.renderInfo__);
  }
  
  public void setObject(Object o)
  {
    if (!(o instanceof CRadioButton))
      throw new IllegalArgumentException
                  ("AnyCRadio wraps bibliothek.gui.dock.common.action.CRadioButton and sub-classes");

    ((CRadioButton)o).setWrapper(this);
    
    super.setObject(o);
    
    // Put an action listener on the button so we can notify
    // our parent AnyButtonGroup.
//    addAdaptedEventListener(new ModelUpdateListener(actionEventType__));
  }
  
  public void setRenderInfo(RenderInfo r)
  {
    // Radios set their text to either their enum external value or
    // their label, with the priority on the former
    CRadioButton b = (CRadioButton)getObject();
    
    if (!setTextFromButtonGroup(r))
    {
      String s = r.getLabel();
      if (s.equals(AnyString.EMPTY.toString()))
        s = null;
      b.setText(s);
    }

    r_ = r;
  }
  
  public RenderInfo getRenderInfo()
  {
    return r_;
  }
  
  public void setButtonGroup(Any g)
  {
    if (!(g instanceof AbstractButtonGroup))
      throw new AnyRuntimeException("Not a button group");
    
    if (g_ != null)
    {
      g_.removeFromGroup(this);
    }
    
    g_ = null;
    
    if (!AnyNull.isNull(g))
    {
      g_ = (AbstractButtonGroup)g;
      g_.addToGroup(this);
      
      setTextFromButtonGroup(getRenderInfo());
    }
  }
  
  protected Object getPropertyOwner(Any property)
  {
    if (radioProperties__.contains(property))
      return this;

    return super.getPropertyOwner(property);
  }

  void notifyButtonGroup()
  {
    try
    {
      Any dataItem = null;
      RenderInfo r = getRenderInfo();
      if (r != null)
      {
        // Get the value we represent
        dataItem = r.resolveDataNode(getContextNode(), true);
      }
      if (g_ != null)
        g_.radioChanged(dataItem);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  CRadioButton getCRadioButton()
  {
    return (CRadioButton)getObject();
  }
  
  void setSelected(boolean selected)
  {
    getCRadioButton().setSelected(selected);
  }
  
  // Check if the button group is rendering an enum and if so
  // whether any value our renderinfo represents is part of
  // that enum. If so then set our text property from to the
  // enum's external value and return true.
  private boolean setTextFromButtonGroup(RenderInfo myRenderInfo)
  {
    boolean ret = false;

    RenderInfo r = null;
    if (g_ != null)
      r = g_.getRenderInfo();
    
    if (r != null)
    {
      Descriptor d = r.getDescriptor();
      if (d != Descriptor.degenerateDescriptor__ && d != null)
      {
        Any f = r.getField();
        
        if (d.isEnum(f) && myRenderInfo != null)
        {
          try
          {
            Any v = myRenderInfo.resolveResponsibleData(getContextNode());
            Map enums = d.getEnums();
            enums = (Map)enums.get(f);
            
            if (enums.contains(v))
            {
              v = enums.get(v);
              CRadioButton b = (CRadioButton)getObject();
              b.setText(v.toString());
              ret = true;
            }
          }
          catch(AnyException e)
          {
            throw new RuntimeContainedException(e);
          }
        }
      }
    }
    
    return ret;
  }
}
