/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/SelectionF.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.Event;

/**
 * Represents a selection list of items
 */
public interface SelectionF extends ComponentFacade
{
	public static Any selection__      = AbstractValue.flyweightString("selection");
  public static Any selectCount__    = AbstractValue.flyweightString("selectCount");
  public static Any rowCount__       = AbstractValue.flyweightString("rowCount");
  public static Any levelSelection__ = AbstractValue.flyweightString("levelSelection");
	public static Any keySelection__   = AbstractValue.flyweightString("keySelection");
	//public static Any listSelection__  = new AbstractValue.flyweightString("listSelection");
  public static Any itemSelection__  = AbstractValue.flyweightString("itemSelection");
	public static Any indexSelection__ = AbstractValue.flyweightString("indexSelection");
	public static Any editedKeySelection__ = AbstractValue.flyweightString("editedKeySelection");
	public static Any editing__        = AbstractValue.flyweightString("editing");
	public static Any prev__           = AbstractValue.flyweightString("prev");
	public static Any index__          = AbstractValue.flyweightString("index");
	/**
	 * Called when the selection has changed.
	 */
	public void newSelection(Event e);
	
	/**
	 * Retrieve the current selection as a list of objects
	 */
	public Any getItemSelection();
	public void setItemSelection(Any a);
	
	public void setSelectionMode(int selectionInterval);
}

