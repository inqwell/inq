/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JMenuItem.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.client.swing;

public class JMenuItem extends javax.swing.JMenuItem
{
  public void setVisible(boolean isVisible)
  {
    boolean wasVisible = isVisible();
    
    super.setVisible(isVisible);
    
    if ((wasVisible || isVisible) && !(wasVisible && isVisible))
    {
      JMenu.adjustSeparators(getParent());
    }
  }
}
