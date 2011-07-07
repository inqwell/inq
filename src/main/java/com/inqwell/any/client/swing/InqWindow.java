/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/InqWindow.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.JRootPane;

import com.inqwell.any.Any;

/**
 * A utility interface that brings together the methods common to
 * JFrame and JInternalFrame for the purposes of simplifying the
 * Inq wrapper structure for these classes.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public interface InqWindow
{
  public void dispose();

  public void hide();

  public void show();
  
  public void toFront();

  public void pack();

  public void setVisible(boolean isVisible);

  public void setDefaultCloseOperation(int operation);

  public JRootPane getRootPane();

  public Container getContentPane();

  public Component getGlassPane();

  public void addWindowListener(WindowListener l);

  public void setClosed(boolean b) throws PropertyVetoException;
  public void setIcon(boolean b) throws PropertyVetoException;
  public void setFrameIcon(Icon icon);
  public void setState(int state);
  public void setSelected(boolean isSelected) throws PropertyVetoException;

  public int getWidth();
  public int getHeight();
  public void setSize(int width, int height);
  public void setPreferredSize(Dimension d);
  public void setMinimumSize(Dimension d);
  
  public boolean isFocused();
  public boolean isActive();

  public String getTitle();

  public Font getFont();
  public FontMetrics getFontMetrics(Font f);

  public void setDisabledText(Any text);
  
  public void revalidate();
}
