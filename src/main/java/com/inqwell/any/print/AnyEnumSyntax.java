/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/print/AnyEnumSyntax.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.print;

import com.inqwell.any.*;
import javax.print.attribute.*;

public class AnyEnumSyntax extends    AnyPrintAttribute
                           implements Cloneable
{
	public AnyEnumSyntax()
	{
		super(null);
	}

	/**
	 * Construct to wrap a given Attribute
	 */
	public AnyEnumSyntax(EnumSyntax a)
	{
		super(a);
	}

	/**
	 *
	 */
	public AnyEnumSyntax(Object attribute)
	{
    this((EnumSyntax)attribute);
	}

	public EnumSyntax getEnumSyntax()
	{
		return (EnumSyntax)getValue();
	}

  /**
   * Return the string table entry.  This is generally more useful than the
   * underlying attribute's toString() method and fits in with
   * copying these objects to AnyString instances better.
   */
	public String toString()
	{
    EnumSyntax a = getEnumSyntax();
    if (a == null)
      return "AnyEnumSyntax:null";
      
    //String[] strTable = a.getStringTable();
    //return strTable[a.getValue()];
    return a.toString();
	}
		
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (!(a instanceof AnyEnumSyntax))
				throw new IllegalArgumentException("AnyEnumSyntax.copyFrom()");

			AnyEnumSyntax pa = (AnyEnumSyntax)a;
			this.setValue(pa.getValue());
		}
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
