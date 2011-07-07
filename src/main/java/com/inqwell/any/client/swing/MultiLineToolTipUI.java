/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.swing;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * From the tame examples
 * 
 * @version 1.0 11/09/98
 */
public class MultiLineToolTipUI extends BasicToolTipUI
{
  private Vector strs;

  private int      maxWidth = 0;

  public void paint(Graphics g, JComponent c)
  {
//    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(
//        g.getFont());
    FontMetrics metrics = g.getFontMetrics();
    Dimension size = c.getSize();
    g.setColor(c.getBackground());
    g.fillRect(0, 0, size.width, size.height);
    g.setColor(c.getForeground());
    if (strs != null)
    {
      for (int i = 0; i < strs.size(); i++)
      {
        g.drawString(strs.get(i).toString(), 3, (metrics.getHeight()) * (i + 1));
      }
    }
  }

  public Dimension getPreferredSize(JComponent c)
  {
//    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(
//        c.getFont());
    FontMetrics metrics = c.getFontMetrics(c.getFont());
    String tipText = ((JToolTip) c).getTipText();
    if (tipText == null)
      tipText = "";

    strs.clear();
    BufferedReader br = new BufferedReader(new StringReader(tipText));
    String line;
    int maxWidth = 0;
    int pos = 0;
    int idx = 0;
    int len = tipText.length();
    
    while (pos < tipText.length() && (idx = tipText.indexOf('\n', idx)) >= 0)
    {
      line = tipText.substring(pos, idx);
      int width = SwingUtilities.computeStringWidth(metrics, line);
      maxWidth = (maxWidth < width) ? width : maxWidth;
      strs.addElement(line);
      pos = idx + 1;
    }
    if (pos < tipText.length())
    {
      line = tipText.substring(pos);
      int width = SwingUtilities.computeStringWidth(metrics, line);
      maxWidth = (maxWidth < width) ? width : maxWidth;
      strs.addElement(line);
    }
    
    int lines = strs.size();
    int height = metrics.getHeight() * lines;
    this.maxWidth = maxWidth;
    return new Dimension(maxWidth + 6, height + 4);
  }
}
