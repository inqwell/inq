/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyCursor.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import java.awt.Cursor;

public class AnyCursor extends    AnyObject
                       implements Cloneable
{
	public static AnyCursor null__ = new AnyCursor((Cursor)null);

	/**
	 */
	public AnyCursor(Cursor c)
	{
		super(c);
	}

	public Cursor getCursor()
	{
		return (Cursor)getValue();
	}

	public void setCursor(Cursor c)
	{
		setValue(c);
	}

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (!(a instanceof AnyCursor))
				throw new IllegalArgumentException("AnyCursor.copyFrom()");

			AnyCursor s = (AnyCursor)a;
			this.setValue(s.getValue());
		}
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
