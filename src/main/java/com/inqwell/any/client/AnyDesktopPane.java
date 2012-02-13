/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyDesktopPane.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Container;
import java.text.Format;

import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.Set;
import com.inqwell.any.Array;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Map;
import com.inqwell.any.Func;
import com.inqwell.any.Transaction;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.AnyException;
import com.inqwell.any.RuntimeContainedException;
import javax.swing.JDesktopPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class AnyDesktopPane extends AnySimpleComponent
{
  private JDesktopPane    d_;

	public void setObject(Object o)
	{
		if (!(o instanceof JDesktopPane))
			throw new IllegalArgumentException
									("AnyDesktopPane wraps javax.swing.JDesktopPane and sub-classes");

    d_ = (JDesktopPane)o;

		super.setObject(d_);
	}
	
  public Container getComponent()
  {
    return d_;
  }

  protected void setValueToComponent(Any v)
  {
    // If rendering represents the name of the selected window
    // and when assigned to will cause script-driven window
    // selection.
    if (this.contains(v))
    {
      Any c = this.get(v);

      if (c instanceof AnyInternalFrame)
      {
        AnyInternalFrame f = (AnyInternalFrame)c;
        d_.setSelectedFrame(f.getInternalFrame());
      }
    }
  }
}

