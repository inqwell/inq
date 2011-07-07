/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/Setter.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;

import com.inqwell.any.*;
import com.inqwell.any.client.RenderInfo;
import java.text.Format;
import java.awt.Component;

/**
 * Expected interface to set values into and get values from Components
 */
public interface Setter extends Any, Cloneable
{
	/**
	 * Set the given value into the component given by
	 * object <code>o</code>.
	 * <P>
	 * The class of the given object should be that or a sub-class of
	 * the designated class for the specific implementation.  A
	 * <code>ClassCastException</code> will be thrown if this is not
	 * the case.
	 */
	public void set(Any val, Object o);
	
	/**
	 * Get the value from the component given by
	 * object <code>o</code>.
	 * <P>
	 * The class of the given object should be that or a sub-class of
	 * the designated class for the specific implementation.  A
	 * <code>ClassCastException</code> will be thrown if this is not
	 * the case.
	 */
	public Any get(Object o);
	
	/**
	 * Establish an appropriate formatter for the type of Any that will 
	 * be set to this component.  This is an optional operation since
	 * setting a value doesn't have to mean putting a text representation
	 * into a component.
	 */
	public void setFormat(Format f);
	
	/**
	 * Get a default value for this type of component.
	 */
	public Any getDefaultValue(RenderInfo r, Component c);
}

