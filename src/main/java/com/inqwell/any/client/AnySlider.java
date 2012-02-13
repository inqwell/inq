/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnySlider.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JSlider;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.Array;
import com.inqwell.any.Event;
import com.inqwell.any.Func;
import com.inqwell.any.IntI;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.JPanel;

public class AnySlider extends AnySimpleComponent
{
  private JSlider         s_;
  private JComponent      borderee_;
  
  // Working integer for Any interfacing
  private IntI            i_ = new ModelInt();
  
	public void setObject(Object o)
	{
		if (!(o instanceof JSlider))
			throw new IllegalArgumentException
									("AnySlider wraps javax.swing.JSlider and sub-classes");

    s_ = (JSlider)o;
    
    borderee_ = new JPanel();
    borderee_.add(s_);

		super.setObject(s_);
	}
	
  public Container getComponent()
  {
    return s_;
  }

  public JComponent getBorderee()
  {
    return borderee_;
  }

  public Object getAddee()
  {
    return getBorderee();
  }

  public void updateModel() throws AnyException
	{
    // The value was changed by the user - update the model
    Any dataItem = getRenderInfo().resolveDataNode(getContextNode(), false);

    if (dataItem != null)
    {
      i_.setValue(s_.getValue());
      dataItem.copyFrom(i_);
    }
	}

  protected void setValueToComponent(Any v)
  {
    i_.roundFrom(v);
    s_.setValue(i_.getValue());
  }

	protected void initUpdateModel()
	{
		addAdaptedEventListener(new SliderChangedListener(changeEventType__));
	}

  class SliderChangedListener extends EventBinding
  {
    public SliderChangedListener(Array eventTypes)
    {
      super(eventTypes, false, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      updateModel();
			return null;
		}
  }
  
  private class ModelInt extends AnyInt
  {
    private RoundFrom r_;
    
    public Any roundFrom(Any a)
    {
      if (a != null)
      {
        if (a != this)
        {
          if (r_ == null)
            r_ = new RoundFrom();
          
          r_.round(a);
        }
      }
      else
        setNull();
        
      return this;
    }
  }
}

