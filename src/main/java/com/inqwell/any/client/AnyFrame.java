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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventListener;
import com.inqwell.any.IntI;
import com.inqwell.any.Map;
import com.inqwell.any.Set;
import com.inqwell.any.beans.FrameF;
import com.inqwell.any.client.dock.AnyCControl;
import com.inqwell.any.client.swing.JFrame;

public class AnyFrame extends    AnyWindow
                      implements FrameF
{
	private JFrame      f_;

	private boolean     bypassModality_;
	
	private AnyMenuBar  menuBar_;

	private static Set   frameProperties__;
  private static Set   preferredListenerTypes__;

  public  static Any   icon__ = new ConstString("icon");
  public  static Any   bypassModality__ = new ConstString("bypassModality");

  static
  {
    frameProperties__ = AbstractComposite.set();
    frameProperties__.add(menuBar__);
    frameProperties__.add(toolBar__);
    frameProperties__.add(icon__);
    frameProperties__.add(bypassModality__);

    preferredListenerTypes__ = AbstractComposite.set();
    preferredListenerTypes__.add(ListenerConstants.WINDOW);
    preferredListenerTypes__.add(ListenerConstants.CONTEXT);
    preferredListenerTypes__.add(ListenerConstants.COMPONENT);
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

	public Object getAddIn()
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

  protected Composite getPreferredListenerTypes()
  {
    return AnyFrame.preferredListenerTypes__;
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

	public JFrame getJFrame()
	{
	  return (JFrame)getObject();
	}
	
	public void setSize(int width, int height)
	{
		JComponent c = (JComponent)f_.getContentPane();
		c.setPreferredSize(new Dimension(width, height));
	}
	
	public void saveState(Map m)
	{
	  // Place a map in m specifying our state.
	  Rectangle r = f_.getBounds();
	  Map state = AbstractComposite.simpleMap();
    state.add(AnyView.x__,      new ConstInt(r.x));
    state.add(AnyView.y__,      new ConstInt(r.y));
    state.add(AnyView.width__,  new ConstInt(r.width));
    state.add(AnyView.height__, new ConstInt(r.height));
    
    AnyCControl dock = (AnyCControl)getIfContains(AnyCControl.cControl__);
    if (dock != null)
      dock.saveLayout(state);
    
    m.add(getNameInParent(), state);
	}
	
	public void restoreState(Map m)
  {
	  // If the map contains a value with our name assume it is
	  // our restore data
	  Any k = getNameInParent();
	  Map state;
	  if ((state = (Map)m.getIfContains(k)) != null)
    {
	    // TODO Reconcile against current screen size etc
      IntI x      = (IntI)state.getIfContains(AnyView.x__);
      IntI y      = (IntI)state.getIfContains(AnyView.y__);
      IntI width  = (IntI)state.getIfContains(AnyView.width__);
      IntI height = (IntI)state.getIfContains(AnyView.height__);
      if (x      != null &&
          y      != null &&
          width  != null &&
          height != null)
      {
	      Rectangle r = new Rectangle(x.getValue(),
                                    y.getValue(),
                                    width.getValue(),
                                    height.getValue());
	      f_.setBounds(r);
      }
      
      // If there is a docklayout then restore that
      Any dock;
      if ((dock = state.getIfContains(AnyCControl.dockLayout__)) != null)
      {
        AnyCControl c = AnyCControl.getCControl(this, false);
        c.restoreLayout(dock);
      }
    }
  }
}

