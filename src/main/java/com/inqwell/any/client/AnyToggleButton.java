/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyToggleButton.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import javax.swing.AbstractButton;
import javax.swing.JComponent;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.Set;

/**
 * Intercepts the <code>text</code> property and performs initial
 * text setting to get better layout characteristics.
 */
public class AnyToggleButton extends AnyComponent
{
	private static Set     toggleProperties__;
	private static Any     text__ = new ConstString("text");

  static
  {
  	toggleProperties__ = AbstractComposite.set();
  	toggleProperties__.add(text__);
  }

  public void setText(Any text)
  {
    AbstractButton b = (AbstractButton)getComponent();
    b.setText(text.toString());
  }
  
  public Any getText()
  {
    AbstractButton b = (AbstractButton)getComponent();
    return new AnyString(b.getText());
  }

  public void initAsCellEditor()
  {
    ((JComponent)getComponent()).setOpaque(true);
  }
  
	protected Object getPropertyOwner(Any property)
	{
		if (toggleProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
}
