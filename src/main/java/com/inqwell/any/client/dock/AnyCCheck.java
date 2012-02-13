/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.Event;
import com.inqwell.any.Func;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.AnyCheck;
import com.inqwell.any.client.AnyComponent;
import com.inqwell.any.client.RenderInfo;

public class AnyCCheck extends AnyCAction
{
  private RenderInfo  renderInfo_;

  private Any         checkedValue_   = AnyBoolean.TRUE;
  private Any         uncheckedValue_ = AnyBoolean.FALSE;
  
  private static Set     checkProperties__;

  static
  {
    checkProperties__ = AbstractComposite.set();
    checkProperties__.add(AnyComponent.renderInfo__);
    checkProperties__.add(checkedValue__);
    checkProperties__.add(uncheckedValue__);
  }
  
  @Override
  public void setObject(Object o)
  {
    if (!(o instanceof CCheckBox))
      throw new AnyRuntimeException("Not a CCheckBox");
    
    super.setObject(o);
    addAdaptedEventListener(new ModelUpdateListener(actionEventType__));
  }

  @Override
  public void setRenderInfo(RenderInfo r)
  {
    renderInfo_ = r;
    
    if (getContextNode() != null && r != null)
    {
      r.resolveNodeSpecs(getContextNode());
  
      // We must listen to the context node for events which will cause
      // us to render our data.
      setupDataListener(r.getNodeSpecs());
      
      // Try to render now.
      try
      {
        Any a = renderInfo_.resolveDataNode(getContextNode(), true);
        setValueToComponent(a);
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }

      String s = r.getLabel();
      if (s.equals(AnyString.EMPTY.toString()))
        s = null;
      setActionText(s);
    }
  }
  
  @Override
  public RenderInfo getRenderInfo()
  {
    return renderInfo_;
  }
  
  /**
   * The value that should be copied to the rendered item when
   * the checkbox is checked. The supplied value must be assignment
   * compatible with that of the item being rendered.
   */
  public void setCheckedValue(Any checkedValue)
  {
    checkedValue_ = checkedValue;
  }
  
  public Any getCheckedValue()
  {
    return checkedValue_;
  }
  
  /**
   * The value that should be copied to the rendered item when
   * the checkbox is unchecked. The supplied value must be assignment
   * compatible with that of the item being rendered.
   */
  public void setUncheckedValue(Any uncheckedValue)
  {
    uncheckedValue_ = uncheckedValue;
  }

  public Any getUncheckedValue()
  {
    return uncheckedValue_;
  }
  
  /**
   * Default processing for data node events (as opposed to
   * adapted GUI events) which are received at this node.  This
   * implementation attempts to resolve the data node and
   * render it into the component.
   */
  @Override
  protected void componentProcessEvent(Event e) throws AnyException
  {
    Any a = getGUIRendered(e);
    setValueToComponent(a);
  }
  
  @Override
  protected void setValueToComponent(Any v)
  {
    CCheckBox b = (CCheckBox)getCAction(); 
    b.setSelected(checkedValue_.equals(v));
  }

  @Override
  protected Object getPropertyOwner(Any property)
  {
    if (checkProperties__.contains(property))
      return this;

    return super.getPropertyOwner(property);
  }

  @Override
  protected void contextEstablished()
  {
    setRenderInfo(getRenderInfo());
  }
  
  // TOOD: Add an action event binding to update the rendered value
  private class ModelUpdateListener extends EventBinding
  {
    public ModelUpdateListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      Any a = getRenderedValue();
      if (a != null)
      {
        CCheckBox c = (CCheckBox)getObject();
        if (c.isSelected())
          a.copyFrom(checkedValue_);
        else
          a.copyFrom(uncheckedValue_);
      }
      return null;
    }
  }
}
