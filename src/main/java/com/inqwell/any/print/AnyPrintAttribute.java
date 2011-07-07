/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/print/AnyPrintAttribute.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.print;

import com.inqwell.any.*;
import javax.print.attribute.*;

public class AnyPrintAttribute extends    AnyObject
                               implements Cloneable
{
	public static AnyPrintAttribute null__ = new AnyPrintAttribute((Attribute)null);

	public AnyPrintAttribute()
	{
		super(null);
	}

	/**
	 * Construct to wrap a given Attribute
	 */
	public AnyPrintAttribute(Attribute a)
	{
		super(a);
	}

	/**
	 * Construct around the possibility that <code>source</code> is
	 * a URL.
	 */
	public AnyPrintAttribute(Object attribute)
	{
    this((Attribute)attribute);
	}

	public Attribute getAttribute()
	{
		return (Attribute)getValue();
	}

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (!(a instanceof AnyPrintAttribute))
				throw new IllegalArgumentException("AnyPrintAttribute.copyFrom()");

			AnyPrintAttribute pa = (AnyPrintAttribute)a;
			this.setValue(pa.getValue());
		}
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
