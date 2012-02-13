/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyInternalFrame.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
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
import com.inqwell.any.client.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import com.inqwell.any.client.swing.SwingInvoker;
import com.inqwell.any.client.AnyIcon;

public class AnyInternalFrame extends    AnyWindow
                              implements FrameF
{
	private JInternalFrame  f_;
	private JComponent      c_;

  private static Set      preferredListenerTypes__;
	private static Set      iFrameProperties__;
  
  static
  {
    iFrameProperties__ = AbstractComposite.set();
    iFrameProperties__.add(AnyWindow.menuBar__);
    iFrameProperties__.add(AnyWindow.toolBar__);
    iFrameProperties__.add(AnyFrame.icon__);
    
    preferredListenerTypes__ = AbstractComposite.set();
    preferredListenerTypes__.add(ListenerConstants.IWINDOW);
    preferredListenerTypes__.add(ListenerConstants.CONTEXT);
  }

	public void setObject(Object f)
	{
		if (!(f instanceof JInternalFrame))
			throw new IllegalArgumentException
									("AnyInternalFrame wraps com.inqwell.any.client.swing.JInternalFrame and sub-classes");
		
		f_ = (JInternalFrame)f;
		
		c_ = (JComponent)f_.getContentPane();
		super.setObject(f);
	}
	
	public void addComponent(Container c)
	{
    f_.getContentPane().add(c);
	}
	
	public void addAdaptedEventListener(EventListener l, Any eventParam)
  {
    if (l.getDesiredEventTypes().contains(EventConstants.W_CLOSING))
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      
    super.addAdaptedEventListener(l, eventParam);
  }

	public Object     getAddee()
	{
    return f_;
	}
	
	public Object getAddIn()
	{
    return f_.getChildPane();
	}
  
  public JInternalFrame getInternalFrame()
  {
    return f_;
  }
	
  public Container getComponent()
  {
    return f_;
  }

	public void setIconImage(AnyIcon icon)
	{
		//System.out.println ("setIconImage............");
    if (icon != null)
      f_.setFrameIcon(icon.getIcon());
    else
      f_.setFrameIcon(null);
	}

  // Slip in code to inherit parent's icon when possible.
	public void evaluateContext()
	{
    super.evaluateContext();

    FrameF f = (FrameF)AnyWindow.getParentWindow(this);
    if (f != null)
    {
      setIcon(f.getIcon());
    }
  }
  	
	protected Object getPropertyOwner(Any property)
	{
		if (iFrameProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}

  protected Composite getPreferredListenerTypes()
  {
    return AnyInternalFrame.preferredListenerTypes__;
  }
  
	public Icon getIcon()
	{
		return f_.getFrameIcon();
	}
	
	
	public void setIcon(Icon i)
	{
		f_.setFrameIcon(i);
	}
	
	public AnyComponent getMenuBar()
	{
		return null;
	}
	
	public void setMenuBar(AnyComponent menuBar)
	{
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
		//System.out.println ("AnyInternalFrame.setSize " + width + " " + height + "------");
		c_.setPreferredSize(new Dimension(width, height));
	}
}
	
