/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTabbedPane.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JTabbedPane;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.Icon;
import com.inqwell.any.client.swing.SwingInvoker;
import java.util.HashMap;

public class AnyTabbedPane extends AnySimpleComponent
{
  public static IntI TOP    = new ConstInt(SwingConstants.TOP);
  public static IntI BOTTOM = new ConstInt(SwingConstants.BOTTOM);
  public static IntI LEFT   = new ConstInt(SwingConstants.LEFT);
  public static IntI RIGHT  = new ConstInt(SwingConstants.RIGHT);
  
	private JTabbedPane    t_;
	private IntI           index_ = new AnyInt((Any)null);  // selected index
	private StringI        name_  = new AnyString(AnyString.null__); // selected name
	private StringI        old_   = new AnyString(AnyString.null__); // prev selected
	private Map            modelVars_ = AbstractComposite.managedMap();
  
	private Set            childVisibility_ = AbstractComposite.set();
  
  private boolean        keepOrder_;

	static private StringI tabChildSelected__ = new ConstString("tabChildSelected");

	private static Set      tabPaneProperties__;
	private static StringI  keepOrder__        = new ConstString("keepOrder");

	static
	{
    tabPaneProperties__ = AbstractComposite.set();
    tabPaneProperties__.add(keepOrder__);
	}
  
  public AnyTabbedPane()
  {
	}

	public void setObject(Object t)
	{
		if (!(t instanceof JTabbedPane))
			throw new IllegalArgumentException
									("AnyTabbedPane wraps javax.swing.JTabbedPane and sub-classes");
		
		t_ = (JTabbedPane)t;
		super.setObject(t);
		//setupEventSet(t_.getModel());
		
		// Put in initial tab titles
		initTitles();
	}
	
  public Container getComponent()
  {
    return t_;
  }

  public void setForegroundAt(AnyComponent ac, Color c)
  {
    int at = indexOfComponent(ac);
    if (at >= 0)
      t_.setForegroundAt(at, c);
	}
	
  public void setForegroundAt(int at, Color c)
  {
    t_.setForegroundAt(at, c);
	}
	
  public void setBackgroundAt(AnyComponent ac, Color c)
  {
    int at = indexOfComponent(ac);
    if (at >= 0)
      t_.setBackgroundAt(at, c);
	}
	
  public void setBackgroundAt(int at, Color c)
  {
    t_.setBackgroundAt(at, c);
	}
	
  public void setIconAt(AnyComponent c, Icon i)
  {
    int at = indexOfComponent(c);
    if (at >= 0)
      t_.setIconAt(at, i);
	}
	
  public void setIconAt(int at, Icon i)
  {
    t_.setIconAt(at, i);
	}
	
  public void setToolTipTextAt(AnyComponent c, String text)
  {
    int at = indexOfComponent(c);
    if (at >= 0)
      t_.setToolTipTextAt(at, text);
	}
	
  public void setToolTipTextAt(int at, String text)
  {
    t_.setToolTipTextAt(at, text);
	}
	
  public void setTitleAt(AnyComponent c, String text)
  {
    int at = indexOfComponent(c);
    if (at >= 0)
    {
      // JTabbedPane doesn't like null
      if (text == null)
        text = "";
      t_.setTitleAt(at, text);
    }
	}
	
  public void setTitleAt(int at, String text)
  {
    t_.setTitleAt(at, text);
	}
	
	public void setSelected(AnyComponent c)
	{
    int at = indexOfComponent(c);
    if (at >= 0)
      t_.setSelectedIndex(at);
	}

  public void setEnabledAt(AnyComponent c, boolean enabled)
  {
    int at = indexOfComponent(c);
    if (at >= 0)
      t_.setEnabledAt(at, enabled);
	}
	
  public void setEnabledAt(int at, boolean enabled)
  {
    t_.setEnabledAt(at, enabled);
	}
	
  public void setChildVisible(AnyComponent child, boolean visible)
  {
    // spoof visibility by add/removal from tab pane
    if (visible)
    {
      if (!childVisibility_.contains(child))
        childVisibility_.add(child);

      if (keepOrder_)
        reorderComponents();
      else
        t_.add(child.getBorderee());

//      if (t_.indexOfComponent(child.getBorderee()) < 0)
//        t_.add(child.getBorderee());
    }
    else
    {
      childVisibility_.remove(child);

      int indx = t_.indexOfComponent(child.getBorderee());
      if (indx >= 0)
        t_.removeTabAt(indx);
    }
	}
	
	public boolean getKeepOrder()
	{
    return keepOrder_;
  }
  
  public void setKeepOrder(boolean keepOrder)
  {
    keepOrder_ = keepOrder;
  }
	
  public boolean contains(Any key)
  {
    if (key.equals(AnyComponent.modelKey__))
      return true;
      
    return super.contains(key);
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get(Any key)
  {
    if (key.equals(AnyComponent.modelKey__))
    {
      return modelVars_;
    }
    else
    {
      return super.get(key); // throws
    }
  }
  
  public Any getIfContains(Any key)
  {
    if (key.equals(AnyComponent.modelKey__))
    {
      return modelVars_;
    }
    else
    {
      return super.getIfContains(key);
    }
  }
  
  public boolean isEmpty()
  {
    return false;
  }
  
	protected Object getPropertyOwner(Any property)
	{
		if (tabPaneProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
	
	private int indexOfComponent(Component c)
	{
		return t_.indexOfComponent(c);
	}

	private int indexOfComponent(AnyComponent c)
	{
    return t_.indexOfComponent(c.getBorderee());
	}

  /*
   * Perform child reordering in the Inq hierarchy and then
   * reflect the result in this Tab Pane's children.
   */
  public void sort (Array orderBy)
  {
	  super.sort(orderBy);
	  reorderComponents();
	}
	
  /*
   * Perform child reordering in the Inq hierarchy and then
   * reflect the result in this Tab Pane's children.
   */
  public void sort (OrderComparator c)
  {
	  super.sort(c);
	  reorderComponents();
  }

  /*
   * Perform child reordering in the Inq hierarchy and then
   * reflect the result in this Tab Pane's children.
   */
  public void sort (Array orderBy, OrderComparator c)
  {
	  super.sort(orderBy, c);
	  reorderComponents();
  }

	public void updateModel() throws AnyException
	{
		// Note - we get here after any tab child in the
	  // Inq hierarchy sense has been removed or the
    // seletced tab is changed by the user.
	  
	  // Check if there was a previously selected tab
	  // and if so whether it is still here.  If so, this
	  // is our previous selection.
	  if ((!name_.isNull()) && this.contains(name_))
	    old_.copyFrom(name_);
	  else
	    old_.setNull();
	    
    int index = t_.getSelectedIndex();
    index_.setValue(index);
		
		// In fact, index is not much use since vectored gets
		// are fragile.  It only serves to offer a test
		// as to whether there are any child tabs (not equal
		// to null).  Thus, establish the name of the
		// component as well.
		
		Iter i = createIterator();
		// We may not only have gui components as our children, not
		// least because we have put the modelVars_ member in,
		// so this loop will execute even if the underlying TabPane
		// has no tabs
		boolean hasNoTabs = true;
		while (i.hasNext())
		{
			Any a = i.next();
			if (a instanceof AnyComponent)
			{
				hasNoTabs = false;
				AnyComponent c = (AnyComponent)a;
		    BooleanI tabChildSelected = (BooleanI)c.get(tabChildSelected__);
				if (t_.getSelectedComponent() == c.getAddee())
				{
					name_.copyFrom(c.getNameInParent());
		      tabChildSelected.setValue(true);
				}
				else
				{
		      tabChildSelected.setValue(false);
				}
			}
		}
		if (hasNoTabs)
		{
	    name_.setNull();
		}
	}

  protected void setValueToComponent(Any v)
  {
    // If the specified named child exists and is a component
    // then select it
    if (contains(v) &&
        (get(v) instanceof AnyComponent))
    {
      SelectTab s = new SelectTab(v);
      s.maybeSync();
      // forces updateModel to be called as well
    }
  }

  //	protected Array initOrderBacking()
//	{
//    Array array = super.initOrderBacking();
//    
//    // For tab panes we group the component children together in the
//    // initial ordering.  In the case where a sort expression refers
//    // to nodes that will only be resolved under child components,
//    // this ensures that the comparator functions as required.
//    // (bit hacky)
//    
//    Array order = AbstractComposite.array(this.entries());
//
//    for (int i = 0; i < entries(); i++)
//    {
//      Any a = array.get(i);
//      if (a instanceof AnyComponent)
//        order.add(a);
//    }
//    
////    for (int i = 0; i < entries(); i++)
////    {
////      Any a = array.get(i);
////      if (!(a instanceof AnyComponent))
////        order.add(a);
////    }
//    
//    return order;
//
//	}
	
	protected void initUpdateModel()
	{
		//modelVars_.setTransactional(true);
		//this.add(AnyComponent.modelKey__, modelVars_);
		modelVars_.add(SelectionF.index__, index_);
    modelVars_.add(SelectionF.selection__, name_);
    modelVars_.add(SelectionF.prev__, old_);
		//modelVars_.add(SelectionF.prev__, old_);
		
		addAdaptedEventListener(new TabSelectedListener(changeEventType__));
	}

  /**
   * Custom processing for node removals from tab panes.
   * Remove the corresponding component from the tab gui
   * itself
   */
	protected void afterRemove(Any key, Any value)
	{
		if (value instanceof AnyComponent)
		{
			AnyComponent  ac = (AnyComponent)value;


      if (ac.contains(tabChildSelected__))
        ac.remove(tabChildSelected__);
      
      if (childVisibility_.contains(value))
      {
        Component c = (Component)ac.getAddee();
        childVisibility_.remove(value);
        RemoveSingleComponent r = new RemoveSingleComponent(c);
        r.maybeSync();
      }
		}
      
		super.afterRemove(key, value);
	}
	
	protected boolean beforeAdd(Any key, Any value)
	{
		if (value instanceof AnyComponent)
		{
			// put in the tab selected boolean
			AnyComponent c = (AnyComponent)value;
			c.add(tabChildSelected__, new AnyBoolean());
		}
		
		return super.beforeAdd(key, value);
	}
	
	protected void afterAdd(Any key, Any value)
  {
    super.afterAdd(key, value);
    
//    if (!doingAdd_)
//    {
//      doingAdd_ = true;
//      reorder(key);
//      return;
//    }
    
//    doingAdd_ = false;
    
		if (value instanceof AnyComponent)
		{
      // Handle initial visibility
			AnyComponent c = (AnyComponent)value;
      setChildVisible(c, c.isVisible());  // also adds to JTabbedPane if true
		}
  }

  // Match the order of the swing children to that of the Inq
  // children when the keepOrder property is true. Effective
  // only if the Inq children are in a maintained sort order.
	private void reorderComponents()
	{
    if (keepOrder_)
    {
      ReorderComponents r = new ReorderComponents();
      r.maybeSync();
    }
	}
	
	private void initTitles()
	{
    Iter i = createKeysIterator();
    while (i.hasNext())
    {
      Any ck = i.next();
      Any cv = get(ck);
      
      if (cv instanceof AnyComponent)
      {
        AnyComponent c = (AnyComponent)cv;
        c.setTabTitle(ck.toString());
      }
    }
  }

  private void setTabProperties(int at, AnyComponent child) throws AnyException
  {
    Any pv = child.getProperty(AnyComponent.toolTipText__);
    if (pv != null)
      setToolTipTextAt(at, pv.toString());

    pv = child.getProperty(AnyComponent.tabText__);
    if (pv != null)
      setTitleAt(at, pv.toString());

    pv = child.getProperty(AnyComponent.tabIcon__);
    if (pv != null)
    {
      AnyIcon ai = (AnyIcon)pv;
      setIconAt(at, ai.getIcon());
    }

    pv = child.getProperty(AnyComponent.tabForeground__);
    if (pv != null)
    {
      AnyColor ai = (AnyColor)pv;
      setForegroundAt(at, ai.getColor());
    }

    pv = child.getProperty(AnyComponent.tabBackground__);
    if (pv != null)
    {
      AnyColor ai = (AnyColor)pv;
      setBackgroundAt(at, ai.getColor());
    }

    pv = child.getProperty(AnyComponent.tabEnabled__);
    if (pv != null)
    {
      BooleanI ai = (BooleanI)pv;
      setEnabledAt(at, ai.getValue());
    }
  }
  
  private class ReorderComponents extends SwingInvoker
  {
		protected void doSwing()
		{
      Component cSel = t_.getSelectedComponent();
			t_.removeAll();
			try
			{
        int i    = 0;
        int tabs = 0;
				while (i < entries())
				{
					Any a = getByVector(i);
          
          if (!(a instanceof AnyComponent))
          {
            i++;
            continue;
          }
            
					AnyComponent c = (AnyComponent)a;
					
					if (childVisibility_.contains(c))
					{
            t_.add(c.getBorderee());
            setTabProperties(tabs, c);
            tabs++;
          }
	
          i++;
				}
        if (cSel != null)
          t_.setSelectedComponent(cSel);
				//updateModel();
			}
			catch (AnyException e)
			{
				throw new RuntimeContainedException(e);
			}
		}
	}

  private class RemoveSingleComponent extends SwingInvoker
  {
	  private Component c_;
	
	  RemoveSingleComponent(Component c)
	  {
		  c_ = c;
	  }
	
		protected void doSwing()
		{
			int index = t_.indexOfComponent(c_);
			if (index >= 0)
			{
				t_.removeTabAt(index);
			}
			try
			{
			  updateModel();
			}
			catch(AnyException e)
			{
				throw new RuntimeContainedException(e);
		  }
		}
	}

  private class SelectTab extends SwingInvoker
  {
    private Any namedChild_;
    
    SelectTab(Any namedChild)
    {
      namedChild_ = namedChild;
    }
    
    protected void doSwing()
    {
      // If the tab child has been set to invisible (by Inq) then
      // it is not in the Swing component.
      Component[] comps = t_.getComponents();
      AnyComponent c = (AnyComponent)AnyTabbedPane.this.get(namedChild_);
      JComponent select = (JComponent)c.getAddee();
      for (int i = 0; i < comps.length; i++)
        if (t_.getComponent(i) == select)
          t_.setSelectedComponent(select);
    }
  }
  
  class TabSelectedListener extends EventBinding
  {
    public TabSelectedListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
			updateModel();
			return null;
		}
  }
}
