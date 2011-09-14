/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JFrame.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Container;
import com.inqwell.any.Any;
import com.inqwell.any.AnyNull;
import com.inqwell.any.IntI;
import com.inqwell.any.AnyURL;
import com.inqwell.any.client.swing.JPanel;
import com.inqwell.any.client.AnyIcon;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;

/**
 * An extension of JFrame simply to make some things easier from BML,
 * notably setting the layout manager.  A BoxLayout object is used
 * exclusively as the layout manager.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class JFrame extends    javax.swing.JFrame
                    implements InqWindow
{
	static public int X_AXIS = BoxLayout.X_AXIS;
	static public int Y_AXIS = BoxLayout.Y_AXIS;

	static public AnyIcon inq__;

  static
  {
    AnyURL u  = new AnyURL("classpath:///com/inqwell/any/tools/q16.png");
    URL    u1 = u.getURL();
    inq__  = new AnyIcon(u1);
  }
  
  /**
   * Create a new JFrame with a Y axis BoxLayout
   */
  public JFrame()
  {
  	setLayout (Y_AXIS);
    setFrameIcon(inq__.getIcon());
    initGlassPane();
  }

  /**
   * Create a new JFrame with the specified axis BoxLayout
   */
  public JFrame(int axis)
  {
  	setLayout (axis);
    setFrameIcon(inq__.getIcon());
    initGlassPane();
  }

  /**
   * Create a new JFrame with the specified axis BoxLayout and title
   */
  public JFrame(String title, int axis)
  {
    super (title);
    setFrameIcon(inq__.getIcon());
  	setLayout (axis);
    initGlassPane();
  }
  
  /**
   * Create a new JFrame with the specified axis BoxLayout and title
   */
  public JFrame(String title)
  {
    this(title, Y_AXIS);
    setFrameIcon(inq__.getIcon());
  	setLayout (Y_AXIS);
    initGlassPane();
  }
  
  public void setAxis(Any axis)
  {
  	IntI iAxis = (IntI)axis;
  	setLayout(iAxis.getValue());
  }
  
  public Any getAxis()
  {
  	return null;
  }
  
  public void setLayout(int axis)
  {
    // Note - assumes that the content JPanel will always be the
    // first child!
		Container c = getContentPane();
    if (c.getComponentCount() == 0)
    {
      c.add(new JPanel(axis), BorderLayout.CENTER, 0);
    }
    else
    {
      JPanel p = (JPanel)c.getComponent(0);
      p.setLayout(new BoxLayout(p, axis));
    }
	}
	
  public Container getChildPane()
  {
    Container c = getContentPane();
    if (c instanceof javax.swing.JDesktopPane)
      return c;
      
    return (Container)c.getComponent(0);
  }
  
	/**
	 * Provided for cast compatibility with templates/bml
	 */
  public void setJMenuBar(Container menubar)
  {
  	super.setJMenuBar((JMenuBar)menubar);
  }

  public void setJToolBar(JToolBar toolBar)
  {
		Container c = getContentPane();
    c.add(toolBar, BorderLayout.PAGE_START);
  }
  
  public void setDisabledText(Any text)
  {
    DisabledGlassPane g = (DisabledGlassPane)this.getRootPane().getGlassPane();
    
    if (AnyNull.isNull(text))
      g.deactivate();
    else
      g.activate(text.toString());
  }
  
  public void raiseComponentMovedEvent()
  {
    processComponentEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_MOVED));
  }
  
  public void setClosed(boolean b) throws PropertyVetoException {}
  public void setIcon(boolean b) throws PropertyVetoException {}
  public void setSelected(boolean isSelected) throws PropertyVetoException {}
  public void revalidate() {}
  public void setFrameIcon(Icon icon)
  {
    if (icon instanceof ImageIcon)
    {
      ImageIcon ii = (ImageIcon)icon;
      setIconImage(ii.getImage());
    }
  }

  private void initGlassPane()
  {
    this.getRootPane().setGlassPane(new DisabledGlassPane());
  }
}
