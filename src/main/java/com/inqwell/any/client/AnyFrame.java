/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyFrame.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.*;
import java.awt.Frame;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import com.inqwell.any.client.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import com.inqwell.any.client.swing.SwingInvoker;
import com.inqwell.any.client.AnyIcon;

public class AnyFrame extends    AnyWindow
                      implements FrameF
{
	private JFrame      f_;

	private boolean     bypassModality_;
	
	private AnyMenuBar  menuBar_;

	private static Set   frameProperties__;

  public  static Any   icon__ = new ConstString("icon");
  public  static Any   bypassModality__ = new ConstString("bypassModality");

  static
  {
    frameProperties__ = AbstractComposite.set();
    frameProperties__.add(menuBar__);
    frameProperties__.add(toolBar__);
    frameProperties__.add(icon__);
    frameProperties__.add(bypassModality__);
  }

  public AnyFrame(JFrame f)
  {
		super(f);
	}

  public AnyFrame()
  {
	}

	public void setObject(Object f)
	{
		if (!(f instanceof JFrame))
			throw new IllegalArgumentException
									("AnyFrame wraps com.inqwell.any.client.swing.JFrame and sub-classes");

		f_ = (JFrame)f;
		super.setObject(f);
	}

  public Container getComponent()
  {
    return f_;
  }

	public void addComponent(Container c)
	{
    f_.getContentPane().add(c);
	}

//	public Object     getAddee()
//	{
//    return f_.getContentPane();
//	}

	public void addAdaptedEventListener(EventListener l, Any eventParam)
  {
    if (l.getDesiredEventTypes().contains(EventConstants.W_CLOSING))
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    super.addAdaptedEventListener(l, eventParam);
  }

	public Container getAddIn()
  {
    return f_.getChildPane();
  }

  public void setBypassModality(boolean bypassModality)
  {
    bypassModality_ = bypassModality;
  }

  public boolean isBypassModality()
  {
    return bypassModality_;
  }

	public void setIconImage(AnyIcon icon)
	{
    if (icon != null)
      f_.setFrameIcon(icon.getIcon());
    else
      f_.setFrameIcon(null);
	}

	protected Object getPropertyOwner(Any property)
	{
		if (frameProperties__.contains(property))
		  return this;

		return super.getPropertyOwner(property);
	}

	public Icon getIcon()
	{
    Image i = f_.getIconImage();
    
    if (i != null)
		  return new ImageIcon(i)
                 {
                   public boolean equals(Object o)
                   {
                     if (o instanceof ImageIcon)
                     {
                       ImageIcon ii = (ImageIcon)o;
                       return this.getImage().equals(ii.getImage());
                     }
                     return super.equals(o);
                   }
                 };
    else
      return null;
	}


	public void setIcon(Icon i)
	{
    if (i != null)
    {
  		ImageIcon ii = (ImageIcon)i;
  		f_.setIconImage(ii.getImage());
    }
    else
      f_.setIconImage(null);
	}

	public AnyMenuBar getMenuBar()
	{
		return menuBar_;
	}

	public void setMenuBar(AnyMenuBar menuBar)
	{
	  menuBar_ = menuBar;
	  
		JMenuBar m = (JMenuBar)menuBar.getComponent();
		f_.setJMenuBar(m);
	}

	public AnyComponent getToolBar()
	{
		return null;
	}

	public void setToolBar(AnyComponent toolBar)
	{
		JToolBar m = (JToolBar)toolBar.getComponent();
		f_.setJToolBar(m);
    
    // If the toolbar doesn't have a parent then put it here to avoid
    // having to explicitly "lay it out". Then if the buttons within it
    // are children of the toolbar (as opposed to the context) they will
    // inherit the prevailing context.
    if (toolBar.getParentAny() == null)
      this.replaceItem(toolBarChild__, toolBar);
	}

	public void setSize(int width, int height)
	{
		//System.out.println ("AnyFrame.setSize " + width + " " + height + "------");
		JComponent c = (JComponent)f_.getContentPane();
		c.setPreferredSize(new Dimension(width, height));
	}
}

