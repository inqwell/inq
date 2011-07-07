/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyStyle.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import javax.swing.text.Style;

public class AnyStyle extends    AnyObject
											implements Cloneable
{
	public static AnyStyle null__ = new AnyStyle((Style)null);

	/**
	 * Construct to wrap a pre-loaded Style
	 */
	public AnyStyle(Style s)
	{
		super(s);
	}

	public AnyStyle() {}

	public Style getStyle()
	{
		return (Style)getValue();
	}

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (!(a instanceof AnyStyle))
				throw new IllegalArgumentException("AnyStyle.copyFrom()");

			AnyStyle s = (AnyStyle)a;
			this.setValue(s.getValue());
		}
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
