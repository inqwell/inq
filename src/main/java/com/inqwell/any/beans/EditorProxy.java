/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/EditorProxy.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;
import com.inqwell.any.*;
import javax.swing.CellEditor;

/**
 * Expected interface to create JDK editors from Components
 */
public interface EditorProxy extends Any
{
	/**
	 * Get a suitable <code>javax.swing.CellEditor</code> implementation
	 * for the given object
	 */
	public CellEditor getCellEditor(Object o);

}

