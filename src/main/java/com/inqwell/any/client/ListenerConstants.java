/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/ListenerConstants.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;
import com.inqwell.any.*;

/**
 * Constants relating to events as defined by external sub-systems
 * (i.e. swing)
 */
public interface ListenerConstants extends Any
{ 

	public final Any DEGENERATE = new ConstString("degenerate");	
	
	// These constants are the same values as the Java Beans event
	// sets.
	public final Any ACTION         = new ConstString("action");	
	public final Any FOCUS          = new ConstString("focus");	
	public final Any MENU           = new ConstString("menu");	
	public final Any POPUPMENU      = new ConstString("popupMenu");	
	public final Any WINDOW         = new ConstString("window");	
	public final Any IWINDOW        = new ConstString("internalFrame");	
	public final Any MOUSE          = new ConstString("mouse");
	public final Any MOUSEMOTION    = new ConstString("mouseMotion");
	public final Any KEY            = new ConstString("key");
	public final Any LISTSELECTION  = new ConstString("listSelection");
	public final Any TREESELECTION  = new ConstString("treeSelection");
	public final Any UNDOABLEEDIT   = new ConstString("undoableEdit");
	public final Any DOCUMENT       = new ConstString("document");
	public final Any DIALOG         = new ConstString("dialog");	
	public final Any TABLEEDIT      = new ConstString("tableEdit");	
	public final Any CONTEXT        = new ConstString("context");	
	public final Any SPINEDIT       = new ConstString("spinEdit");	
	public final Any FILECHOOSER    = new ConstString("fileChooser");	
	public final Any ITEM           = new ConstString("item");	
	public final Any CHANGE         = new ConstString("change");	
	public final Any TREEEXPANSION  = new ConstString("treeExpansion");	
	public final Any TREEWILLEXPAND = new ConstString("treeWillExpand");	
}

