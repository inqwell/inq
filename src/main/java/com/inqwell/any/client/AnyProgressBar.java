/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyProgressBar.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Container;
import java.text.Format;

import com.inqwell.any.Any;
import com.inqwell.any.Set;
import com.inqwell.any.Map;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.StringI;
import com.inqwell.any.AnyException;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.RuntimeContainedException;
import javax.swing.JComponent;
import com.inqwell.any.client.swing.JPanel;
import javax.swing.JProgressBar;

public class AnyProgressBar extends AnySimpleComponent
{
  private JProgressBar    b_;
	private JComponent      borderee_;
  private AnyInt          i_ = new AnyInt(0);

	private static Set      progressProperties__;
	private static Any      paintString__ = new ConstString("paintString");

	static
	{
    progressProperties__ = AbstractComposite.set();
    progressProperties__.add(paintString__);
  }
  
	public void setObject(Object o)
	{
		if (!(o instanceof JProgressBar))
			throw new IllegalArgumentException
									("AnyProgressBar wraps javax.swing.JProgressBar and sub-classes");

    b_ = (JProgressBar)o;

    borderee_ = new JPanel();
    borderee_.add(b_);
    
		super.setObject(b_);
	}

  protected void setValueToComponent(Any v)
  {
    i_.copyFrom(v);
    if (!i_.isNull())
      b_.setValue(i_.getValue());
  }

  protected Object getPropertyOwner(Any property)
	{
		if (progressProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
	
	public void setPaintString(StringI str)
	{
    if (!str.isNull())
      b_.setString(str.toString());
  }
  
  public StringI getPaintString()
  {
    return new AnyString(b_.getString());
  }
  
	public Object getAddee()
	{
		return getBorderee();
	}

  public Container getComponent()
  {
    return b_;
  }

	public JComponent getBorderee()
	{
		return borderee_;
	}
}
