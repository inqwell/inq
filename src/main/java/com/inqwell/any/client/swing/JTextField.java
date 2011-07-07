/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JTextField.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.inqwell.any.Any;
import com.inqwell.any.client.AnyIcon;

/**
 * 
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class JTextField extends javax.swing.JTextField
{
  private Image hint_;
  /**
   *
   */
  public JTextField()
  {
  }

  /** 
   * Override not to consume the enter event
   */
  public boolean processKeyBinding(KeyStroke ks,
                                   KeyEvent  e,
                                   int       condition,
                                   boolean pressed)
  {
    //if true event consumed
    boolean ret = super.processKeyBinding(ks,e,condition,pressed);
    
    if (ret &&
        (condition == JComponent.WHEN_FOCUSED ||
         condition == JComponent.WHEN_IN_FOCUSED_WINDOW) &&
        ks.getKeyCode() == KeyEvent.VK_ENTER)
    {
      ret = false;
    }
    return ret;
  }
  
  public void setHintImage(Any hint)
  {
    // Based on Craig Wood's posting here:
    // http://www.coderanch.com/t/343579/GUI/java/Inserting-Image-JTextField
    if (hint instanceof AnyIcon)
    {
      AnyIcon i = (AnyIcon)hint;
      Image ii = i.getImage();
      if (ii != null)
      {
        hint_ = ii;
        Border border = UIManager.getBorder("TextField.border");
        int x1 = 0;
//        if (border != null)
//          x1 = border.getBorderInsets(this).right;
        setMargin(new Insets(0, 0, 0, x1 + hint_.getWidth(this)));
      }
    }
  }
  
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if (hint_ != null)
    {
      int y = (getHeight() - hint_.getHeight(this)) / 2;
//      Border border = UIManager.getBorder("TextField.border");
//      int x1 = 0;
//      if (border != null)
//        x1 = border.getBorderInsets(this).right;
//      int x = (getWidth() - hint_.getWidth(this) - x1);
      int x = (getWidth() - hint_.getWidth(this));
      // g.drawImage(image, x0, y, this);
      g.drawImage(hint_, x, y, this);
    }
  }
}
