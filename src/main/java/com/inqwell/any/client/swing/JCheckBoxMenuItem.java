/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JCheckBoxMenuItem.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.client.swing;

public class JCheckBoxMenuItem extends javax.swing.JCheckBoxMenuItem
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
