/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyBox.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Container;

import com.inqwell.any.client.swing.JPanel;

/**
 * 
 */
public class AnyBox extends AnyLayoutContainer
{
  private JPanel box_;
  
	public void setObject(Object o)
	{
		if (!(o instanceof JPanel))
			throw new IllegalArgumentException
									("AnyBox wraps com.inqwell.any.client.swing.JPanel and sub-classes");
    
    box_ = (JPanel)o;
    
		super.setObject(o);
	}
	
  public Container getComponent()
  {
    return box_;
  }
}

