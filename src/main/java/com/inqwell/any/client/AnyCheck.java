/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyCheck.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-07 22:03:22 $
 */

package com.inqwell.any.client;

import java.awt.Container;

import javax.swing.AbstractButton;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.Func;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.SwingInvoker;

public class AnyCheck extends AnySimpleComponent
{
  private AbstractButton b_;

  private Any            checkedValue_   = AnyBoolean.TRUE;
  private Any            uncheckedValue_ = AnyBoolean.FALSE;
  
	private static Set   checkBoxProperties__;

	static
  {
    checkBoxProperties__ = AbstractComposite.set();
    checkBoxProperties__.add(checkedValue__);
    checkBoxProperties__.add(uncheckedValue__);
	}


	public void setObject(Object o)
	{
		if (!(o instanceof AbstractButton))
			throw new IllegalArgumentException
									("AnyCheck wraps javax.swing.AbstractButton and sub-classes");

    b_ = (AbstractButton)o;

		super.setObject(b_);
	}
	
  public Container getComponent()
  {
    return b_;
  }
  
  public void setRenderInfo(RenderInfo r)
  {
    if (r != null && !isRenderer())
    {
      String s = r.getLabel();
      if (s.equals(AnyString.EMPTY.toString()))
        s = null;
      b_.setText(s);
    }
    
    super.setRenderInfo(r);
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
  
  protected void setValueToComponent(Any v)
  {
    setSelected(checkedValue_.equals(v));
  }

  protected Object getPropertyOwner(Any property)
	{
		if (checkBoxProperties__.contains(property))
		  return this;

		return super.getPropertyOwner(property);
	}

   /**
	 * Perform any processing required to update the model data
	 * this component is viewing.
	 */
	protected void initUpdateModel()
	{
    addAdaptedEventListener(new ModelUpdateListener(actionEventType__));
	}

  private void setCheckModel()
  {
    // The button got clicked - update the model.
  	try
  	{
      if (getRenderInfo() != null)
      {
        Any dataNode = getRenderInfo().resolveDataNode(getContextNode(), false);

        if (dataNode != null)
        {
          if (b_.isSelected())
          {
            //System.out.println("AnyCheck TRUE");
            dataNode.copyFrom(checkedValue_);
          }
          else
          {
            dataNode.copyFrom(uncheckedValue_);
            //System.out.println("AnyCheck FALSE");
          }
        }
      }
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }
  
  private void setSelected(final boolean b)
  {
    SwingInvoker ss = new SwingInvoker()
    {
      protected void doSwing()
      {
        boolean cb = b_.isSelected();
        b_.setSelected(b);
        //if (b && !cb || !b && cb)
        //  b_.doClick();
      }
    };
    ss.maybeSync();
  }
  
  private class ModelUpdateListener extends EventBinding
  {
    public ModelUpdateListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      setCheckModel();
      return null;
    }
  }

}

