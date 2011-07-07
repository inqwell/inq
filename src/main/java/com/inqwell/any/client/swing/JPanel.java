/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JPanel.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BoxLayout;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import com.inqwell.any.Any;
import com.inqwell.any.IntI;
import com.inqwell.any.client.AnyComponent;

/**
 * Extends JPanel to 1) support scrollable content; 2) forward
 * key strokes to its wrapper when used as the root of a complex
 * cell editor hierarchy.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 */
public class JPanel extends    javax.swing.JPanel
                    implements Scrollable,
                               MouseMotionListener
{
	static public int X_AXIS = BoxLayout.X_AXIS;
	static public int Y_AXIS = BoxLayout.Y_AXIS;
	
  private int maxUnitIncrement_ = 1;

  /**
   * Create a new JPanel with a double buffer and a Y axis BoxLayout
   */
  public JPanel()
  {
  	setLayout (new BoxLayout(this, Y_AXIS));
  }
  
  public JPanel(LayoutManager layout)
  {
    super(layout);
    init();
  }

  /**
   * Create a new JPanel with a double buffer and specified axis BoxLayout
   */
  public JPanel(int axis)
  {
  	setLayout (new BoxLayout(this, axis));
    init();
  }

  public JPanel(boolean isDoubleBuffered, int axis)
  {
    super (isDoubleBuffered);
  	setLayout (new BoxLayout(this, axis));
    init();
  }
  
  public void setAxis(int axis)
  {
    if (getBoxLayout() == null)
      throw new IllegalArgumentException("Not a box");
    
  	setLayout(new BoxLayout(this, axis));
  }
  
  //Methods required by the MouseMotionListener interface:
  public void mouseMoved(MouseEvent e) { }
  
  public void mouseDragged(MouseEvent e)
  {
    //The user is dragging us, so scroll!
    Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
    scrollRectToVisible(r);
  }

  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                        int       orientation,
                                        int       direction)
  {
    //Get the current position.
    int currentPosition = 0;

    if (orientation == SwingConstants.HORIZONTAL)
    {
      currentPosition = visibleRect.x;
    }
    else
    {
      currentPosition = visibleRect.y;
    }

    //Return the number of pixels between currentPosition
    //and the nearest tick mark in the indicated direction.
    if (direction < 0)
    {
      int newPosition = currentPosition -
                       (currentPosition / maxUnitIncrement_)
                        * maxUnitIncrement_;
      return (newPosition == 0) ? maxUnitIncrement_ : newPosition;
    }
    else
    {
      return ((currentPosition / maxUnitIncrement_) + 1)
             * maxUnitIncrement_
             - currentPosition;
    }
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction)
  {
    if (orientation == SwingConstants.HORIZONTAL)
    {
      return visibleRect.width - maxUnitIncrement_;
    }
    else
    {
      return visibleRect.height - maxUnitIncrement_;
    }
  }

  public boolean getScrollableTracksViewportWidth()
  {
    return true;
    // properties TODO
//    Component parent = getParent(); 
//
//    if (parent instanceof JViewport) 
//      return parent.getWidth() > getPreferredSize().width; 
//
//    return false;
  }

  public boolean getScrollableTracksViewportHeight()
  {
    Component parent = getParent(); 

    if (parent instanceof JViewport) 
      return parent.getHeight() > getPreferredSize().height; 

    return false; 
  }

  public void setMaxUnitIncrement(int pixels)
  {
    maxUnitIncrement_ = pixels;
  }

  public void setMinimumSize(Dimension d)
  {
//		System.out.println ("&&&&&&&&&&JPanel minimum: " + d);
		super.setMinimumSize(d);
  }
  
  public void setMaximumSize(Dimension d)
  {
//		System.out.println ("&&&&&&&&&&JPanel maximum: " + d);
		super.setMaximumSize(d);
  }
  
  public Dimension getPreferredSize()
  {
		Dimension d = super.getPreferredSize();
//		System.out.println ("JPanel preferred: " + d);
		return d;
  }
  
  /**
   *  Overridden to forward to the wrapper if the "Inq.editorWrapper"
   *  client property is set up.
   */
  public boolean processKeyBinding(KeyStroke ks,
                                   KeyEvent  e,
                                   int       condition,
                                   boolean pressed)
  {
    boolean ret = false;
    
    AnyComponent ac = (AnyComponent)getClientProperty("Inq.editorWrapper");

    if (ac != null)
      ret = ac.forwardKeyBinding(ks, e, condition, pressed);
    else
      ret = super.processKeyBinding(ks, e, condition, pressed);

    return ret;
  }
  
  public void setBounds(Rectangle r)
  {
    super.setBounds(r);
    
    AnyComponent ac = (AnyComponent)getClientProperty("Inq.editorWrapper");

    if (ac != null)
      ac.setBounds(r);
  }

  public int getNumRow()
  {
    return getTableLayout().getNumRow();
  }

  public int getNumColumn()
  {
    return getTableLayout().getNumColumn();
  }

  public int getHGap()
  {
    return getTableLayout().getHGap();
  }
  
  public void setHGap(int hGap)
  {
    getTableLayout().setHGap(hGap);
  }
  
  public int getVGap()
  {
    return getTableLayout().getVGap();
  }
  
  public void setVGap(int vGap)
  {
    getTableLayout().setVGap(vGap);
  }
  
  public int getAxis()
  {
    BoxLayout l = getBoxLayout();
    if (l == null)
      return -1;
    
    return l.getAxis();
  }
  
  protected TableLayout getTableLayout()
  {
    LayoutManager l = getLayout();
    if (!(l instanceof TableLayout))
      throw new IllegalStateException("Not a table layout");
    
    return (TableLayout)l;
  }
  
  protected BoxLayout getBoxLayout()
  {
    LayoutManager l = getLayout();
    if (!(l instanceof BoxLayout))
      return null; //throw new IllegalStateException("Not a box layout");
    
    return (BoxLayout)l;
  }
  
  private void init()
  {
    //Let the user scroll by dragging to outside the window.
    setAutoscrolls(true); //enable synthetic drag events
    addMouseMotionListener(this); //handle mouse drags
  }
}
