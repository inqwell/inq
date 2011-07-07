/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JMenuBar.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/**
 * An extension of JMenuBar simply to make some things easier from BML
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class JMenuBar extends javax.swing.JMenuBar
{
  public JMenuBar()
  {
    // Get rid of any F10 action
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "none");
  }
}
