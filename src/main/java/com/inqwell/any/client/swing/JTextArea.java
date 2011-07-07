/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JTextArea.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.View;

/**
 * An extension of JTextArea.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class JTextArea extends javax.swing.JTextArea
{
  private boolean fitText_;
  private int     maxLineHeight_;
  /**
   *
   */
  public JTextArea()
  {
  }

  public Dimension getPreferredScrollableViewportSize()
  {
    if (fitText_)
    {
      
      Dimension p = getPreferredSize();
      Dimension s = super.getPreferredScrollableViewportSize();
      
      int lineHeight = 0;
      if (maxLineHeight_ > 0)
      {
        FontMetrics fm = getFontMetrics(getFont());
        lineHeight = fm.getHeight() * maxLineHeight_;
      }
      
      if (s.height < p.height)
        s.height = p.height;
      
      if (lineHeight > 0 && s.height > lineHeight)
        s.height = lineHeight;
      
      if (s.width < p.width)
        s.width = p.width;
      
      //getTextAreaHeight();
      
      return s;
    }
    else
      return super.getPreferredScrollableViewportSize();
  }
  
  public void setMaxLineHeight(int maxLineHeight)
  {
    maxLineHeight_ = maxLineHeight;
  }
  
  public int getMaxLineHeight()
  {
    return maxLineHeight_;
  }
  
  public void setFitText(boolean trackHeight)
  {
    fitText_ = trackHeight;
  }
  
  public boolean getFitText()
  {
    return fitText_;
  }
  
  public void setBasicUI(boolean basicUI)
  {
    if (basicUI)
    {
      setUI(new BasicTextAreaUI());
      invalidate();
    }
    else
      updateUI();
  }
  
  public int getTextAreaHeight()
  {
    View view = getUI().getRootView(this);
    int viewHeight = (int) Math.ceil( (double) view.getPreferredSpan( View.Y_AXIS ) );
    int currentWidth = getWidth();
    if (currentWidth == 0)
      return 0;
    int currentHeight = getHeight();
    String text = getText();
    FontMetrics fm = getFontMetrics( getFont() );
    int neededWidth = fm.stringWidth( text );
    int defaultHeight = fm.getHeight();
    int neededHeight = 0;
    int numRows = neededWidth / currentWidth + 1;
    if ( numRows <= 1 )
      neededHeight = defaultHeight;
    else
      neededHeight = numRows * defaultHeight;
    
    // Make sure we use the height recommended by the view if
    // necessary;
    // we don't always use it because sometimes it doesn't make sense for
    // some reason.  Dunno why, but the first line typically wants to be
    // too big.
    if (viewHeight > neededHeight && currentWidth < neededWidth)
      neededHeight = viewHeight;
    
    // TS
    //neededHeight = viewHeight;
    // End TS
    
    Insets insets = getInsets();
    neededHeight += insets.top + insets.bottom;
    return neededHeight;
  }
}
