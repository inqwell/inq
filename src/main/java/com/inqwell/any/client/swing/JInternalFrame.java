/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JInternalFrame.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.event.InternalFrameListener;
import java.awt.event.WindowListener;
import javax.swing.event.InternalFrameEvent;
import java.awt.event.WindowEvent;
import javax.swing.JWindow;
import com.inqwell.any.Any;
import com.inqwell.any.AnyNull;
import com.inqwell.any.IntI;
import com.inqwell.any.client.swing.JFrame;
import com.inqwell.any.client.swing.JPanel;

/**
 * An extension of JInternalFrame simply to make some things easier from Inq,
 * notably setting the layout manager.  A BoxLayout object is used
 * exclusively as the layout manager.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class JInternalFrame extends    javax.swing.JInternalFrame
                            implements InqWindow
{
  private static JWindow dummy__ = new JWindow();

  /**
   * Create a new JInternalFrame with a Y axis BoxLayout
   */
  public JInternalFrame()
  {
    super("", true, true, true, true);
  	setLayout (JFrame.Y_AXIS);
  	//setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
  	setClosable(true);
    setFrameIcon(JFrame.inq__.getIcon());
    initGlassPane();
  }

  /**
   * Create a new JInternalFrame with the specified axis BoxLayout
   */
  public JInternalFrame(int axis)
  {
    super("", true, true, true, true);
  	setLayout (axis);
    setFrameIcon(JFrame.inq__.getIcon());
    initGlassPane();
  }

  /**
   * Create a new JInternalFrame with the specified axis BoxLayout and title
   */
  public JInternalFrame(String title, int axis)
  {
    super (title);
  	setLayout (axis);
    setFrameIcon(JFrame.inq__.getIcon());
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
    return (Container)getContentPane().getComponent(0);
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
  
  public void setState(int state) {}

  public boolean isFocused()
  {
    // A bit poor. Probably can be selected even if overall the
    // application does not have the focus. Should really go to some
    // effort to find out if the owner window has the focus
    return isSelected();
  }
  
  public boolean isActive()
  {
    return isSelected();
  }

  public void dispose()
  {
    // JInternalFrame is not a top level container, so
    // I guess we should really do this.  JDK source
    // for dispose is inert in this respect.
    super.dispose();
    Container c = getParent();
    if (c != null)
      c.remove(this);
  }

  public void addWindowListener(final WindowListener l)
  {
    // A highly suspect adaptation of internal frame events to
    // the WindowListener interface so that addWindowListener
    // can be specified in the InqWindow interface. Only used
    // internally to handle home-brew do-this-or-that-on-close
    // stuff. No need to use it externally because event listeners
    // are harmonised by the ListenerAdapter stuff and EventBinding
    this.addInternalFrameListener
      (new InternalFrameListener()
        {
          public void internalFrameActivated(InternalFrameEvent e)
          {
            l.windowActivated(makeWindowEvent(WindowEvent.WINDOW_ACTIVATED));
          }
          public void internalFrameClosed(InternalFrameEvent e)
          {
            l.windowClosed(makeWindowEvent(WindowEvent.WINDOW_CLOSED));
          }
          public void internalFrameClosing(InternalFrameEvent e)
          {
            l.windowClosing(makeWindowEvent(WindowEvent.WINDOW_CLOSING));
          }
          public void internalFrameDeactivated(InternalFrameEvent e)
          {
            l.windowDeactivated(makeWindowEvent(WindowEvent.WINDOW_DEACTIVATED));
          }
          public void internalFrameDeiconified(InternalFrameEvent e)
          {
            l.windowDeiconified(makeWindowEvent(WindowEvent.WINDOW_DEICONIFIED));
          }
          public void internalFrameIconified(InternalFrameEvent e)
          {
            l.windowIconified(makeWindowEvent(WindowEvent.WINDOW_ICONIFIED));
          }
          public void internalFrameOpened(InternalFrameEvent e)
          {
            l.windowOpened(makeWindowEvent(WindowEvent.WINDOW_OPENED));
          }

          private WindowEvent makeWindowEvent(int id)
          {
            WindowEvent we = new WindowEvent(dummy__, id);
            we.setSource(JInternalFrame.this);
            return we;
          }
        }
      );
  }

  private void initGlassPane()
  {
    this.getRootPane().setGlassPane(new DisabledGlassPane());
  }
}
