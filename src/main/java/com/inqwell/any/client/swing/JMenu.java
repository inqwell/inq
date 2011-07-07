/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JMenu.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JPopupMenu;
import javax.swing.JSeparator;


/**
 * 
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class JMenu extends javax.swing.JMenu
{
  static public void adjustSeparators(Container menuParent)
  {
    // Scan the contents looking for menu separators that have no adjacent
    // visible component (that isn't itself a separator)
    Component c[] = menuParent.getComponents();
    boolean visible = false;
    boolean nothingVisible = true;
    Component lastSepVisible = null;
    for (int i = 0; i < c.length; i++)
    {
      Component co = c[i];
      
      if (co instanceof JSeparator)
      {
        // If nothing visible prior to separator then make separator invisible too.
        // Otherwise we are starting a new section so reset the visibility flag
        if (!visible)
          co.setVisible(false);
        else
        {
          visible = false;
          co.setVisible(true);
          lastSepVisible = co;
        }
      }
      else
      {
        visible |= co.isVisible();
        if (visible)
        {
          lastSepVisible = null;
          nothingVisible = false;
        }
      }
    }
    if (lastSepVisible != null)
      lastSepVisible.setVisible(false);
    
    // See if we are in a JMenu. As there is nothing visible hide that as well
    if (menuParent instanceof JPopupMenu)
    {
      JPopupMenu m = (JPopupMenu)menuParent;
      Component invoker = m.getInvoker();
      if (invoker instanceof JMenu)
      {
        invoker.setVisible(!nothingVisible);
      }
    }
  }
}
