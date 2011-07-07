/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JTextPane.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.client.swing;

import javax.swing.text.EditorKit;
import javax.swing.text.ViewFactory;
import javax.swing.text.View;
import java.awt.Dimension;

/**
 * An extension of JTextPane that operates with our own EditorKit
 * and Document implementations.
 */
public class JTextPane extends javax.swing.JTextPane
{
	public JTextPane()
	{
	}
  
  protected EditorKit createDefaultEditorKit()
  {
    EditorKit ek = super.createDefaultEditorKit();
    ViewFactory vf = ek.getViewFactory();
    return new StyledEditorKit(vf);
  }
  
  public Dimension getPreferredScrollableViewportSize()
  {
    // prevent unwanted resize of scrolling text pane when not
    // protected by enclosing geometry restrictions.
    return getParent().getSize();
  }  
}
