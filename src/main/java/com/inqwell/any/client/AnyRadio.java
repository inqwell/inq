/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyRadio.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-07 22:03:22 $
 */

package com.inqwell.any.client;

import java.awt.Container;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.Func;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.Facade;

// TODO: rebase away from AnyComponent...

public class AnyRadio extends AnyToggleButton
{
  private AbstractButton b_;
  private AnyButtonGroup g_;
  
	private static Set     radioProperties__;
	private static Any     buttonGroup__ = new ConstString("buttonGroup");

  static
  {
  	radioProperties__ = AbstractComposite.set();
  	radioProperties__.add(buttonGroup__);
  }
  
	public void setObject(Object o)
	{
		//System.out.println ("AnyRadio.setObject " + o.getClass());

		if (!(o instanceof AbstractButton))
			throw new IllegalArgumentException
									("AnyRadio wraps javax.swing.AbstractButton and sub-classes");


    b_ = (AbstractButton)o;
    
    Facade f = getParentComponent();
    if (f instanceof AnyButtonGroup)
      g_ = (AnyButtonGroup)f;
      

		super.setObject(b_);
		
		// Put an action listener on the button so we can notify
		// our parent AnyButtonGroup.
    addAdaptedEventListener(new ModelUpdateListener(actionEventType__));
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

	
	public void setButtonGroup(AnyButtonGroup g)
	{
    if (g_ != null)
    {
      ButtonGroup bg = g_.getButtonGroup();
      bg.remove(b_);
      g.removeButton(this);
    }
		g_ = g;
		if (g != null)
		{
		  g.getButtonGroup().add(b_);
      g.addButton(this);
		}
	}
	
	public AnyButtonGroup getButtonGroup()
	{
		return g_;
	}
	
	Any getRadioData() throws AnyException
	{
    Any ret = null;
    RenderInfo r = getRenderInfo();
    if (r != null)
    {
      // Get the value we represent
      ret = r.resolveDataNode(getContextNode(), true);
    }
    return ret;
	}
	
  protected void setValueToComponent(Any v)
  {
  }

  protected Object getPropertyOwner(Any property)
	{
		if (radioProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
	
	ButtonModel getButtonModel()
	{
    return b_.getModel();
  }
	
	private void notifyButtonGroup()
	{
    try
    {
      Any dataItem = null;
      RenderInfo r = getRenderInfo();
      if (r != null)
      {
        // Get the value we represent
        dataItem = r.resolveDataNode(getContextNode(), true);
        //System.out.println ("AnyRadio.notifyButtonGroup: 1 dataItem: " + dataItem);
      }
      if (g_ != null)
        g_.radioChanged(dataItem);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
	}
  
  private class ModelUpdateListener extends EventBinding
  {
    public ModelUpdateListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      notifyButtonGroup();
      return null;
    }
  }
}

