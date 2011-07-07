/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.client.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * MySwing: Advanced Swing Utilites Copyright (C) 2005 Santhosh Kumar T <p/>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. <p/> This library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 */

public class ComponentBorder implements Border,
                                        MouseListener,
                                        MouseMotionListener,
                                        PropertyChangeListener,
                                        ItemListener,
                                        SwingConstants
{
  int        offset = 10;

  private Component  comp;

  private JComponent container;

  private Rectangle  rect;

  private Border     border;

  private boolean mIsIn = false;
  private boolean mCursorHasJustEntered = false;
  private boolean mCursorHasJustExited = true;

  // Space between the border and the component's edge
  static protected final int EDGE_SPACING = 2;

  // Space between the border and text
  static protected final int TEXT_SPACING = 2;

  public ComponentBorder(Component comp, JComponent container, Border border)
  {
    this.comp = comp;
    this.container = container;
    if (border == null)
      border = UIManager.getBorder("TitledBorder.border");
    
    comp.setForeground(UIManager.getColor("TitledBorder.titleColor"));
    comp.setBackground(container.getBackground());
    comp.setFont(UIManager.getFont("TitledBorder.font"));
    this.border = border;
    container.addMouseListener(this);
    container.addMouseMotionListener(this);
    comp.addPropertyChangeListener(this);
    if (comp instanceof JComponent)
      ((JComponent)comp).setOpaque(true);
    if (comp instanceof AbstractButton)
    {
      ((AbstractButton)comp).getModel().addItemListener(this);
    }
  }

  public boolean isBorderOpaque()
  {
    return true;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width,
      int height)
  {
    Insets borderInsets = null;
    if (border != null)
      borderInsets = border.getBorderInsets(c);
    
    Insets insets = getBorderInsets(c);
    int temp = (borderInsets != null) ? (insets.top - borderInsets.top) / 2
                                      : 0;
    
    if (border != null)
      border.paintBorder(c, g, x, y + temp, width, height - temp);
    
    Dimension size = comp.getPreferredSize();
    rect = new Rectangle(offset, 0, size.width, size.height);
    SwingUtilities.paintComponent(g, comp, (Container) c, rect);
  }

  public Insets getBorderInsets(Component c)
  {
    Insets insets = null;
    Dimension size = comp.getPreferredSize();
    if (border != null)
    {
      insets = border.getBorderInsets(c);
      insets.top = Math.max(insets.top, size.height);
    }
    else
      insets = new Insets(size.height, 0, 0, 0);
    
    return insets;
  }

  private void dispatchEvent(MouseEvent me)
  {
    if (rect != null && rect.contains(me.getX(), me.getY()))
    {
      Point pt = me.getPoint();
      pt.translate(-offset, 0);
      comp.setBounds(rect);
      comp.dispatchEvent(new MouseEvent(comp, me.getID(), me.getWhen(), me
          .getModifiers(), pt.x, pt.y, me.getClickCount(), me.isPopupTrigger(),
          me.getButton()));
      if (!comp.isValid())
        container.repaint();
    }
  }

  public void mouseClicked(MouseEvent me)
  {
    dispatchEvent(me);
  }

  public void mouseEntered(MouseEvent me)
  {
    //dispatchEvent(me);
  }

  public void mouseExited(MouseEvent me)
  {
    //dispatchEvent(me);
    if (mIsIn)
    {
      mIsIn = false;
      mCursorHasJustEntered = false;
      mCursorHasJustExited = true;
      Point lPoint = me.getPoint();
      lPoint.translate(-offset, 0);
      comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_EXITED, me
          .getWhen(), me.getModifiers(), lPoint.x, lPoint.y, me
          .getClickCount(), me.isPopupTrigger(), me.getButton()));
      if (!comp.isValid())
      {
        container.repaint(rect);
      }
    }
  }

  public void mousePressed(MouseEvent me)
  {
    dispatchEvent(me);
  }

  public void mouseReleased(MouseEvent me)
  {
    dispatchEvent(me);
  }

  public void mouseDragged(MouseEvent iE)
  {
    dispatchEvent(iE);
  }

  public void mouseMoved(MouseEvent iE)
  {
    Point lPoint = iE.getPoint();
    
    if (rect == null)
      return;
    
    mIsIn = rect.contains(lPoint);
    
    lPoint.translate(-offset, 0);
    if (mIsIn)
    {
      mCursorHasJustExited = false;
      if (!mCursorHasJustEntered)
      {
        mCursorHasJustEntered = true;
        comp.dispatchEvent(new MouseEvent(comp,
                                          MouseEvent.MOUSE_ENTERED,
                                          iE.getWhen(),
                                          iE.getModifiers(),
                                          iE.getX(),
                                          iE.getY(),
                                          iE.getClickCount(),
                                          iE.isPopupTrigger()));
        if (!comp.isValid())
          container.repaint(rect);
      }
    }
    else
    {
      mCursorHasJustEntered = false;
      if (!mCursorHasJustExited)
      {
        mCursorHasJustExited = true;
        comp.dispatchEvent(new MouseEvent(comp,
                                           MouseEvent.MOUSE_EXITED,
                                           iE.getWhen(),
                                           iE.getModifiers(),
                                           iE.getX(),
                                           iE.getY(),
                                           iE.getClickCount(),
                                           iE.isPopupTrigger()));
        if (!comp.isValid())
          container.repaint(rect);
      }
    }
    dispatchEvent(iE);
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    Dimension size = comp.getPreferredSize();
    rect = new Rectangle(offset, 0, size.width, size.height);
    container.repaint();
  }

  public void itemStateChanged(ItemEvent e)
  {
    container.repaint();
  }
}
