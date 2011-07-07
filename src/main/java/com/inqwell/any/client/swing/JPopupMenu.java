/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JPopupMenu.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Component;

public class JPopupMenu extends javax.swing.JPopupMenu
{
  public boolean somethingVisible()
  {
    Component c[] = getComponents();
    
    for (int i = 0; i < c.length; i++)
    {
      Component co = c[i];
      if (co.isVisible())
        return true;
    }
    return false;
  }
}
