/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.text.Format;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyObject;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Func;
import com.inqwell.any.IntI;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.Facade;
import com.inqwell.any.beans.UIFacade;
import com.inqwell.any.client.swing.SwingInvoker;

/**
 * The root of the hierarchy representing Swing components in
 * the Any framework.
 * <p/>
 * This class provides support common to components providing
 * property access to Inq, handling of captions, borders and such like
 */
public abstract class AnyComponent extends AnyView
{
	// The component we are wrapping
	//private Container comp_;

  public static Any modelKey__ = AbstractValue.flyweightString("model");

  private   static Array focusLostEventType__   = AbstractComposite.array();
  protected static Array changeEventType__      = AbstractComposite.array();
  protected static Array mouseMotionEventType__ = AbstractComposite.array();
  protected static Array mouseExitEventType__   = AbstractComposite.array();

  // spoof property for whether a component is editable
  // and others for various property support.
  private static Set   spoofProperties__;
  public  static Any   editable__      = AbstractValue.flyweightString("editable");
  public  static Any   renderer__      = AbstractValue.flyweightString("renderer");
//  public  static Any   isEditor__      = AbstractValue.flyweightString("isEditor");
  public  static Any   toolTipText__   = AbstractValue.flyweightString("tabToolTip");
  public  static Any   tabIcon__       = AbstractValue.flyweightString("tabIcon");
  public  static Any   tabText__       = AbstractValue.flyweightString("tabTitle");
  public  static Any   tabSelect__     = AbstractValue.flyweightString("tabSelect");
  public  static Any   tabVisible__    = AbstractValue.flyweightString("tabVisible");
  public  static Any   tabEnabled__    = AbstractValue.flyweightString("tabEnabled");
  public  static Any   tabForeground__ = AbstractValue.flyweightString("tabForeground");
  public  static Any   tabBackground__ = AbstractValue.flyweightString("tabBackground");
  public  static Any   openIcon__      = AbstractValue.flyweightString("openIcon");
  public  static Any   closedIcon__    = AbstractValue.flyweightString("closedIcon");
  public  static Any   leafIcon__      = AbstractValue.flyweightString("leafIcon");
  public  static Any   layoutVisible__ = AbstractValue.flyweightString("layoutVisible");
  public  static Any   rParagraphAttributes__ = AbstractValue.flyweightString("replaceParagraphAttributes");
  public  static Any   mParagraphAttributes__ = AbstractValue.flyweightString("mergeParagraphAttributes");
  public  static Any   contextNode__   = AbstractValue.flyweightString("contextNode");
  public  static Any   renderInfo__    = AbstractValue.flyweightString("renderInfo");
  public  static Any   size__          = AbstractValue.flyweightString("size");
  public  static Any   location__      = AbstractValue.flyweightString("location");
  public  static Any   margin__        = AbstractValue.flyweightString("margin");
  public  static Any   accelerator__   = AbstractValue.flyweightString("accelerator");
  public  static Any   font__          = AbstractValue.flyweightString("font");
  public  static Any   style__         = AbstractValue.flyweightString("style");
  public  static Any   caption__       = AbstractValue.flyweightString("caption");
  public  static Any   captionFont__   = AbstractValue.flyweightString("captionFont");
  public  static Any   captionColor__  = AbstractValue.flyweightString("captionColor");
  public  static Any   backgroundImage__  = AbstractValue.flyweightString("backgroundImage");
  public  static Any   upperLeft__     = AbstractValue.flyweightString("upperLeft");
  public  static Any   lowerLeft__     = AbstractValue.flyweightString("lowerLeft");
  public  static Any   upperRight__    = AbstractValue.flyweightString("upperRight");
  public  static Any   lowerRight__    = AbstractValue.flyweightString("lowerRight");
  public  static Any   upperLeading__  = AbstractValue.flyweightString("upperLeading");
  public  static Any   lowerLeading__  = AbstractValue.flyweightString("lowerLeading");
  public  static Any   upperTrailing__ = AbstractValue.flyweightString("upperTrailing");
  public  static Any   lowerTrailing__ = AbstractValue.flyweightString("lowerTrailing");
  public  static Any   cursor__        = AbstractValue.flyweightString("cursor");
  public  static Any   cursorByName__  = AbstractValue.flyweightString("cursorByName");
  public  static Any   visible__       = AbstractValue.flyweightString("visible");
  public  static Any   preferredSize__ = AbstractValue.flyweightString("prefSize");
  public  static Any   minimumSize__   = AbstractValue.flyweightString("minSize");
  public  static Any   maximumSize__   = AbstractValue.flyweightString("maxSize");
  public  static Any   border__        = AbstractValue.flyweightString("border");
  public  static Any   landfname__     = AbstractValue.flyweightString("lookAndFeelName");
  public  static Any   columns__       = AbstractValue.flyweightString("columns");
  public  static Any   rows__          = AbstractValue.flyweightString("rows");

  // Key modifier masks for parser access
  public static Any SHIFT_MASK = new ConstInt(InputEvent.SHIFT_MASK);
  public static Any CTRL_MASK  = new ConstInt(InputEvent.CTRL_MASK);
  public static Any META_MASK  = new ConstInt(InputEvent.META_MASK);
  public static Any ALT_MASK   = new ConstInt(InputEvent.ALT_MASK);

  private  static Any   upperLeft___     = AbstractValue.flyweightString("upperLeft__");
  private  static Any   lowerLeft___     = AbstractValue.flyweightString("lowerLeft__");
  private  static Any   upperRight___    = AbstractValue.flyweightString("upperRight__");
  private  static Any   lowerRight___    = AbstractValue.flyweightString("lowerRight__");
  private  static Any   lowerLeading___  = AbstractValue.flyweightString("lowerLeading__");
  private  static Any   lowerTrailing___ = AbstractValue.flyweightString("lowerTrailing__");
  private  static Any   upperLeading___  = AbstractValue.flyweightString("upperLeading__");
  private  static Any   upperTrailing___ = AbstractValue.flyweightString("upperTrailing__");
  
  public   static Any   path__           = AbstractValue.flyweightString("path");
  public   static Any   typedef__        = AbstractValue.flyweightString("typedef");
  public   static Any   field__          = AbstractValue.flyweightString("field");
  
  public static Any label__        = AbstractValue.flyweightString("label");
  public static Any formatString__ = AbstractValue.flyweightString("formatString");
  
  // If during the layout we have a label attached to us then
  // save it as a special Inq child of the component. When visibility
  // changes the property value is applied to the label as well.
  public static Any labelledBy__ = new ConstString("label__");
  
  public static Any component__ = AbstractValue.flyweightString("component");
  
  public static Insets noMargin__ = new Insets(0,0,0,0);

  static
  {
		focusLostEventType__.add(EventConstants.E_FOCUSLOST);
		changeEventType__.add(EventConstants.E_CHANGE);
    
    Map m = AbstractComposite.simpleMap();
    m.add(EventConstants.EVENT_TYPE, EventConstants.M_MOVED); 
    mouseMotionEventType__.add(m);
    m = AbstractComposite.simpleMap();
    m.add(EventConstants.EVENT_TYPE, EventConstants.M_EXITED); 
    mouseExitEventType__.add(m);


		spoofProperties__ = AbstractComposite.set();
		spoofProperties__.add(enabled__);
		spoofProperties__.add(editable__);
    spoofProperties__.add(renderer__);
    //spoofProperties__.add(isEditor__);
		spoofProperties__.add(toolTipText__);
		spoofProperties__.add(tabIcon__);
		spoofProperties__.add(tabText__);
		spoofProperties__.add(tabSelect__);
		spoofProperties__.add(tabEnabled__);
		spoofProperties__.add(tabVisible__);
		spoofProperties__.add(tabForeground__);
		spoofProperties__.add(tabBackground__);
    spoofProperties__.add(openIcon__);
    spoofProperties__.add(closedIcon__);
    spoofProperties__.add(leafIcon__);
		spoofProperties__.add(layoutVisible__);
		spoofProperties__.add(rParagraphAttributes__);
		spoofProperties__.add(mParagraphAttributes__);
		spoofProperties__.add(contextNode__);
		spoofProperties__.add(renderInfo__);
		spoofProperties__.add(size__);
		spoofProperties__.add(location__);
		spoofProperties__.add(margin__);
		spoofProperties__.add(accelerator__);
		spoofProperties__.add(font__);
		spoofProperties__.add(style__);
		spoofProperties__.add(caption__);
		spoofProperties__.add(captionFont__);
		spoofProperties__.add(captionColor__);
    spoofProperties__.add(backgroundImage__);
    spoofProperties__.add(upperLeft__);
    spoofProperties__.add(lowerLeft__);
    spoofProperties__.add(upperRight__);
    spoofProperties__.add(lowerRight__);
    spoofProperties__.add(upperLeading__);
    spoofProperties__.add(lowerLeading__);
    spoofProperties__.add(upperTrailing__);
    spoofProperties__.add(lowerTrailing__);
    spoofProperties__.add(cursor__);
    spoofProperties__.add(cursorByName__);
    spoofProperties__.add(visible__);
    spoofProperties__.add(preferredSize__);
    spoofProperties__.add(minimumSize__);
    spoofProperties__.add(maximumSize__);
    spoofProperties__.add(border__);
    spoofProperties__.add(landfname__);
	}
  
	public Container getComponent() // TODO when all direct component access in in derived make abstract
	{
    return null;  // So renderers can test and initialise
		//throw new UnsupportedOperationException();
	}

	public String getLabel()
	{
	  RenderInfo r = getRenderInfo();
	  
		if (r != null)
		{
			return r.getLabel();
		}
		else
		{
			return null;
		}
	}
  
  public void setCaption(Any caption)
  {
    TitledBorder tb = findTitledBorder();
    if (tb != null)
    {
      if (caption != null)
        tb.setTitle(caption.toString());
      else
        tb.setTitle("");
      
      // The caption is on the borderee, which may not be the same
      // swing object as the component.
      this.getBorderee().repaint();
    }
  }
  
  public Any getCaption()
  {
    TitledBorder tb = findTitledBorder();
    if (tb != null)
    {
      return new AnyString(tb.getTitle());
    }
    return null;
  }

  public void setCaptionFont(Any a)
  {
    if (!(a instanceof AnyFont))
      throw new IllegalArgumentException("Not an AnyFont");
    
    TitledBorder tb = findTitledBorder();
    if (tb != null)
    {
      AnyFont font = (AnyFont)a;
      tb.setTitleFont(font.getFont());
      this.getComponent().repaint();
    }
  }

  public Any getCaptionFont()
  {
    TitledBorder tb = findTitledBorder();
    if (tb != null)
    {
      return new AnyFont(tb.getTitleFont());
    }
    return null;
  }

  public void setCaptionColor(Any a)
  {
    if (!(a instanceof AnyColor))
      throw new IllegalArgumentException("Not an AnyColour");
    
    TitledBorder tb = findTitledBorder();
    if (tb != null)
    {
      AnyColor colour = (AnyColor)a;
      tb.setTitleColor(colour.getColor());
      this.getComponent().repaint();
    }
  }

  public Any getCaptionColor()
  {
    TitledBorder tb = findTitledBorder();
    if (tb != null)
    {
      return new AnyColor(tb.getTitleColor());
    }
    return null;
  }
  
  public void setBackgroundImage(Any i)
  {
    if (i != null && (!(i instanceof AnyIcon)))
      throw new IllegalArgumentException("Not an AnyIcon");
    
    AnyIcon icon = (AnyIcon)i;
    BackgroundImageBorder bb = findImageBorder();
    if (bb != null)
    {
      if (i != null)
        bb.setImage(icon.getImageIcon());
      else
        bb.setImage(null);
    }
    else
    {
      if (i != null)
      {
        JComponent c = getBorderee();
        Border b = c.getBorder();
  
        if (b == null)
          c.setBorder(new BackgroundImageBorder(icon.getImageIcon()));
        else
          c.setBorder(new CompoundBorder(b, new BackgroundImageBorder(icon.getImageIcon())));
      }
    }
    this.getComponent().repaint();
  }

  public Any getBackgroundImage()
  {
    BackgroundImageBorder bb = findImageBorder();
    if (bb != null)
    {
      return new AnyIcon(bb.getImage());
    }
    return null;
  }
  
  /**
   * Initialise this wrapper with its delegate object.  Sub-classes may
   * override but should include a call
   * <code>to super.setObject(Object o)</code>
   */
	public void setObject(Object o)
	{
		init((Container)o);
	}

  public Object getObject()
  {
    return getComponent();
  }

	/**
	 * Provide information about the basic data this component is
	 * rendering.
	 * <p>
	 * This method is suitable for components that render a single
	 * item of data, such as labels and text fields.
	 * The <code>RenderInfo</code> object carries the information
	 * required to reference and optionally create the data node,
	 * as well as providing support for default component labeling.
	 */
	public void setRenderInfo(RenderInfo r)
	{
//    renderInfo_ = r;
//    
//    if (getContextNode() != null && r != null)
//    {
//      r.resolveNodeSpecs(getContextNode());
//  
//      // We must listen to the context node for events which will cause
//      // us to render our data.
//      setupDataListener(r.getNodeSpecs());
//      
//      // Try to render now.
//      try
//      {
//        Any a = renderInfo_.resolveDataNode(getContextNode(), true);
//        setValueToComponent(a);
//      }
//      catch (AnyException e)
//      {
//        throw new RuntimeContainedException(e);
//      }
//    }
	  throw new UnsupportedOperationException(this.getClass().getName() + " does not render things");
	}

	public RenderInfo getRenderInfo()
	{
//    return renderInfo_;
    return null;
  }

  public Any getFont()
  {
    return new AnyFont(getComponent().getFont());
  }
  
  public void setFont(Any a)
  {
    if (!(a instanceof AnyFont))
      throw new IllegalArgumentException("Not an AnyFont");
    
    AnyFont font = (AnyFont)a;
    
    getComponent().setFont(font.getFont());
  }

  public void setCursorByName(StringI name)
  {
    String s = name.toString();
    if (!s.endsWith("_CURSOR"))
      s = s + "_CURSOR";
      
    
    Class c = java.awt.Cursor.class;
    try
    {
      Field f = c.getField(s);
      getComponent().setCursor(Cursor.getPredefinedCursor(f.getInt(null)));
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public Any getCursor()
  {
    return new AnyCursor(getComponent().getCursor());
  }
  
  public void setCursor(Any cursor)
  {
    if (!(cursor instanceof AnyCursor))
      throw new AnyRuntimeException("Not a cursor " + cursor.getClass());
    
    Cursor c = ((AnyCursor)cursor).getCursor();
    
    getComponent().setCursor(c);
  }
  
  public void setStyle(Any a)
  {
    if (!(a instanceof AnyAttributeSet))
      throw new IllegalArgumentException("Not a style");
    
    AnyAttributeSet style = (AnyAttributeSet)a;
    
    AnyDocument.applyStyle(getComponent(), style.getAttributeSet());
  }

  public final void setLabelledBy(AnyLabel l)
  {
    if (this.contains(labelledBy__))
      this.remove(labelledBy__);
    
    if (l != null)
    {
      // Put the component in an AnyObject. Makes things easier in
      // setVisible and anyway we'd get duplicate parent otherwise.
      this.add(labelledBy__, new AnyObject(l.getComponent()));
      l.setVisible(getComponent().isVisible());
    }
  }
  
  public final void setLabelledBy(JLabel l)
  {
    if (this.contains(labelledBy__))
      this.remove(labelledBy__);
    
    if (l != null)
    {
      this.add(labelledBy__, new AnyObject(l));
      l.setVisible(getComponent().isVisible());
    }
  }
  
  public void setVisible(boolean isVisible)
  {
    getComponent().setVisible(isVisible);
    // If we have an associated label then set that too
    Any a = getIfContains(labelledBy__);
    if (a != null)
    {
      ((JLabel)((AnyObject)a).getValue()).setVisible(isVisible);
    }
  }
  
  public boolean isVisible()
  {
    return getComponent().isVisible();
  }
  
  public final void putClientProperty(Object key, Object value)
  {
    ((JComponent)getComponent()).putClientProperty(key, value);
  }
  
  /*
  public void setStyle(AnyAttributeSet style)
  {
    if (renderer_ != null)
      renderer_.getRenderInfo().setStyle(style);
  }

  public AnyAttributeSet getStyle()
  {
    if (renderer_ != null)
      return renderer_.getRenderInfo().getStyle();
    
    return null;
  }
  */

	public void setEnabled(Any enabled)
	{
	  b__.copyFrom(enabled);
	  getComponent().setEnabled(b__.getValue());
	}
	
	public Any getEnabled()
	{
	  return new AnyBoolean(getComponent().isEnabled());
	}

	public void setEditable(boolean editable)
	{
//		if (renderInfo_ == null)
//			return;
//
//		renderInfo_.setEditable(editable);
	}

	public void setTabTitle(String text)
	{
		// This method is provided for intercepting
		// tooltip property access so we can support tab
		// panes.  If our parent is an AnyTabbedPane then
		// we set the property there instead.

		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			// we have a tab pane for a parent.  Set there in
			// our position
			tabPane.setTitleAt(this,
			                   text);
		}
	}

	public String getTabTitle()
	{
		return null;
	}

	public void setTabToolTip(String text)
	{
		// This method is provided for intercepting
		// tooltip property access so we can support tab
		// panes.  If our parent is an AnyTabbedPane then
		// we set the property there instead.

		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			// we have a tab pane for a parent.  Set there in
			// our position
			tabPane.setToolTipTextAt(this,
			                         text);
		}
		else
		{
			((JComponent)getComponent()).setToolTipText(text);
		}
	}

	public String getTabToolTip()
	{
		return null;
	}

	public void setTabIcon(Icon icon)
	{
		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			// we have a tab pane for a parent.  Set there in
			// our position
			tabPane.setIconAt(this,
			                  icon);
		}
	}

	public Icon getTabIcon()
	{
		return null;
	}

	public void setTabSelect(boolean select)
	{
		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			// We have a tab pane for a parent.  Set us as the
			// currently selected tab
			tabPane.setSelected(this);
		}
	}

	public boolean getTabSelect()
	{
		return false;
	}

	public void setTabEnabled(boolean enabled)
	{
		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			// We have a tab pane for a parent.  Set us as the
			// currently selected tab
			tabPane.setEnabledAt(this, enabled);
		}
	}

	public boolean getTabEnabled()
	{
		return false;
	}

	public void setTabVisible(boolean visible)
	{
		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			tabPane.setChildVisible(this, visible);
		}
	}

	public boolean getTabVisible()
	{
		return false;
	}
  
  public void setLayoutVisible(boolean visible)
  {
    // Additional property method that handles the possibility
    // we may be the child of a card layout. (Tab version kept
    // for compatibility).  In the case of a card layout
    // it doesn't make sense to set visibility to false...
    
	  Facade f = getParentComponent();

	  if (f instanceof AnyTabbedPane)
    {
      AnyTabbedPane t = (AnyTabbedPane)f;
      t.setChildVisible(this, visible);
    }
    else if (f instanceof AnyCard)
    {
      if (visible)
      {
        AnyCard c = (AnyCard)f;
        c.setChildVisible(this);
      }      
    }

	  // Do nothing
  }

	public void updateModel() throws AnyException
	{
    throw new UnsupportedOperationException();
	}

  public Any getRenderedValue()
  {
    throw new UnsupportedOperationException(this.getClass().getName() + " does not render things");
  }
  
  /**
   * Set the rendered data node to the given value and update the
   * component to show it.
   * @param v The new value
   * @throws AnyException
   */
  public void setRenderedValue(Any v) throws AnyException
  {
    RenderInfo r = getRenderInfo();
    if (r != null)
    {
      Any a = r.resolveResponsibleData(getContextNode());
      if (a != null && a != AnyNull.instance())
        a.copyFrom(v);
      a = r.resolveDataNode(getContextNode(), false);
			setValueToComponent(a);
    }
    else
    {
      // If there is no rendering information we cannot update the
      // component's associated data node. Try to set the given
      // value into the component as is
      setValueToComponent(v);
    }
  }
  
	public void setTabForeground(Color fg)
	{
		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			// we have a tab pane for a parent.  Set there in
			// our position
			tabPane.setForegroundAt(this,
			                        fg);
		}
	}

	public Color getTabForeground()
	{
		return null;
	}

	public void setTabBackground(Color bg)
	{
		AnyTabbedPane tabPane = getTabParent();

		if (tabPane != null)
		{
			// we have a tab pane for a parent.  Set there in
			// our position
			tabPane.setBackgroundAt(this,
			                        bg);
		}
	}

	public Color getTabBackground()
	{
		return null;
	}

	public boolean isEditable()
	{
//		if (renderInfo_ == null)
//			return false;
//
//		return renderInfo_.isEditable();
	  return false;
	}

  public boolean isRenderer()
  {
    // Only returns true when the wrapper was created as part of the
    // automatic rendering process.
    return this.contains(renderer__);
  }
  
  public void setRenderer(boolean renderer)
  {
    if (renderer)
      this.replaceItem(renderer__, AnyBoolean.TRUE);
    else
      this.remove(renderer__);
  }
  
//  public boolean getIsEditor()
//  {
//    // Used from script when Inq needs help identifying a component
//    // as a cell editor
//    return this.contains(AnyTable.editor__);
//  }
//  
//  public void setIsEditor(boolean editor)
//  {
//    if (editor)
//      this.replaceItem(AnyTable.editor__, AnyBoolean.TRUE);
//    else
//      this.remove(AnyTable.editor__);
//  }
  
  /**
   * Perform any initialisation required when this component is used as a
   * cell editor.
   * <note>This method will be called once for each application as a
   * cell editor. It must therefore guard against this if required, for
   * example not establishing the same listener twice.</note>
   */
  public void initAsCellEditor()
  {
    // NoOp
  }

	/**
	 * Set up this component to have the given AnyPopupMenu.
	 * <p>
	 * Any number of popup menus can be associated with a
	 * component, each with a different event type.
	 */
	public void setPopupMenu(Any eventType, AnyPopupMenu iMenu)
	{
    PopupListener p = new PopupListener(eventType, iMenu);
    addAdaptedEventListener(p);
	}

  public void setUpperLeft(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.UPPER_LEFT_CORNER, null, upperLeft___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.UPPER_LEFT_CORNER, f, upperLeft___);
    }
  }
  
  public void setUpperRight(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.UPPER_RIGHT_CORNER, null, upperRight___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.UPPER_RIGHT_CORNER, f, upperRight___);
    }
  }
  
  public void setLowerLeft(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.LOWER_LEFT_CORNER, null, lowerLeft___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.LOWER_LEFT_CORNER, f, lowerLeft___);
    }
  }
  
  public void setLowerRight(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.LOWER_RIGHT_CORNER, null, lowerRight___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.LOWER_RIGHT_CORNER, f, lowerRight___);
    }
  }
  
  public void setUpperLeading(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.UPPER_LEADING_CORNER, null, upperLeading___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.UPPER_LEADING_CORNER, f, upperLeading___);
    }
  }
  
  public void setLowerLeading(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.LOWER_LEADING_CORNER, null, lowerLeading___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.LOWER_LEADING_CORNER, f, lowerLeading___);
    }
  }
  
  public void setUpperTrailing(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.UPPER_TRAILING_CORNER, null, upperTrailing___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.UPPER_TRAILING_CORNER, f, upperTrailing___);
    }
  }
  
  public void setLowerTrailing(Any comp)
  {
    JScrollPane s = checkScroller();
    
    if (AnyNull.isNullInstance(comp) || comp == null)
    {
      // remove any existing
      setScrollCorner(s, JScrollPane.LOWER_TRAILING_CORNER, null, lowerTrailing___);
    }
    else
    {
      if (!(comp instanceof UIFacade))
        throw new IllegalArgumentException("not a component");
      
      
      UIFacade f = (UIFacade)comp;
      
      setScrollCorner(s, JScrollPane.LOWER_TRAILING_CORNER, f, lowerTrailing___);
    }
  }
  
  public void setClosedIcon(Icon icon)
  {
  }

  public void setOpenIcon(Icon icon)
  {
  }
  
  public void setLeafIcon(Icon icon)
  {
  }
  
	public void requestFocus()
	{
    SwingInvoker reqFocus = new RequestFocus();
    reqFocus.maybeSync();
	}
  
  public void setBorder(Any border)
  {
    // Only null supported at the moment
    if (AnyNull.isNullInstance(border))
    {
      getBorderee().setBorder(MakeComponent.nullBorder__);
      if (getBorderee() != getComponent() && getComponent() instanceof JComponent)
        ((JComponent)getComponent()).setBorder(MakeComponent.nullBorder__);
    }
  }

	public void applyBorder(final Border border)
	{
  	//System.out.println ("AnyComponent.setBorder() I am :::::::::" + nameInContainer_);
		final JComponent c = getBorderee();

		if (c != null && border != MakeComponent.nullBorder__)
		{
      SwingInvoker sb = new SwingInvoker()
      {
        protected void doSwing()
        {
          Border     b = c.getBorder();
          if (b != null)
          {
            c.setBorder(new CompoundBorder(border, b));
          }
          else
          {
            c.setBorder(border);
          }
        }
      };

      sb.maybeSync();
		}
	}

  public void setMinimumSize(final Dimension d)
  {
		final Container c = getBorderee();

    SwingInvoker sm = new SwingInvoker()
    {
      protected void doSwing()
      {
        if (c instanceof JComponent)
        {
          JComponent j = (JComponent)c;
          j.setMinimumSize(d);
        }

        JComponent borderee = getBorderee();
        if (borderee != null && borderee != c)
        {
          borderee.setMinimumSize(d);
        }
      }
    };

    sm.maybeSync();
  }

  public void setMaximumSize(final Dimension d)
  {
		final Container c = getBorderee();

    SwingInvoker sm = new SwingInvoker()
    {
      protected void doSwing()
      {
        if (c instanceof JComponent)
        {
          JComponent j = (JComponent)c;
          j.setMaximumSize(d);
        }

        JComponent borderee = getBorderee();
        if (borderee != null && borderee != c)
        {
          borderee.setMaximumSize(d);
        }
      }
    };

    sm.maybeSync();
  }

  public Dimension getPreferredSize()
  {
		final Container c = getBorderee();
    return c.getPreferredSize();
  }

	public void setSize(final int width, final int height)
	{
    getComponent().setSize(width, height);
	}

	public void setSize(Array size)
	{
    IntI width  = (IntI)size.get(0);
    IntI height = (IntI)size.get(1);
    setSize(width.getValue(), height.getValue());
	}
  
	public Array getSize()
	{
	  Array size = AbstractComposite.array(2);
	  size.add(new AnyInt(getComponent().getWidth()));
	  size.add(new AnyInt(getComponent().getHeight()));
	  return size;
	}
	
  public void setPrefSize(Array size)
  {
    IntI wI = (IntI)size.get(0);
    IntI hI = (IntI)size.get(1);
    int w = wI.getValue();
    int h = hI.getValue();
    Dimension d = new Dimension(w, h);
    getBorderee().setPreferredSize(d);
    //validate();
  }

  public Array getPrefSize()
  {
    Array size = AbstractComposite.array(2);
    size.add(new AnyInt(getComponent().getWidth()));
    size.add(new AnyInt(getComponent().getHeight()));
    return size;
  }
  
  public void setMinSize(Array size)
  {
    IntI wI = (IntI)size.get(0);
    IntI hI = (IntI)size.get(1);
    int w = wI.getValue();
    int h = hI.getValue();
    Dimension d = new Dimension(w, h);
    getBorderee().setMinimumSize(d);
    //validate();
  }

  public void setMaxSize(Array size)
  {
    IntI wI = (IntI)size.get(0);
    IntI hI = (IntI)size.get(1);
    int w = wI.getValue();
    int h = hI.getValue();
    Dimension d = new Dimension(w, h);
    getBorderee().setMaximumSize(d);
    //validate();
  }

	public void setLocation(Array locn)
	{
    IntI x = (IntI)locn.get(0);
    IntI y = (IntI)locn.get(1);
    setLocation(x.getValue(), y.getValue());
	}

  public Array getLocation()
  {
    return null;
  }
  
  public Any getLookAndFeelName()
  {
    LookAndFeel l = UIManager.getLookAndFeel();
    
    if (l != null)
      return new AnyString(l.getName());
    
    return null;
  }
  
  public void setMargin(Array margin)
  {
    IntI top    = (IntI)margin.get(0);
    IntI left   = (IntI)margin.get(1);
    IntI bottom = (IntI)margin.get(2);
    IntI right  = (IntI)margin.get(3);
    
    Insets i = new Insets(top.getValue(),
                          left.getValue(),
                          bottom.getValue(),
                          right.getValue());
    setMargin(i);
    
  }
  
  public Array getMargin()
  {
    Array ret = null;
    Insets i = getInsets();
    if (i != null)
    {
      ret = AbstractComposite.array(4);
      ret.add(new AnyInt(i.top));
      ret.add(new AnyInt(i.left));
      ret.add(new AnyInt(i.bottom));
      ret.add(new AnyInt(i.right));
    }
    return ret;
  }
  
  public Any getSpacesForWidth(RenderInfo r)
  {
    FontMetrics fm = getComponent().getFontMetrics(getComponent().getFont());
    int width = 10;
    if (r != null)
      width = r.getWidth();
    int spaces = width * (int)(((float)fm.charWidth('m') / (float)fm.charWidth(' ')) + 1);
    char[] nullValue = new char[spaces];
    Arrays.fill(nullValue, ' ');
    return new ConstString(new String(nullValue));
  }
  
  public void setAccelerator(Array accelerator)
  {
    if (getComponent() instanceof JMenuItem)
    {
      IntI       kc      = (IntI)accelerator.get(0);
      IntI       mod     = (IntI)accelerator.get(1);
      BooleanI   release = (BooleanI)accelerator.get(2); 
      
      JMenuItem m = (JMenuItem)getComponent();
      KeyStroke k = KeyStroke.getKeyStroke(kc.getValue(),
                                           mod.getValue(),
                                           release.getValue());
      m.setAccelerator(k);
    }
    
  }
  
  public Array getAccelerator()
  {
    return null;
  }
  
  public void initAsRenderer(RenderInfo r)
  {
    
  }
  
	public void setLocation(int x, int y)
	{
	  getComponent().setLocation(x, y);
	}

  public void validate()
  {
    Container c = getComponent();

    if (c != null)
    {
      if (c instanceof JComponent)
      {
        ((JComponent)c).revalidate();
        c.validate();
      }
    }
  }
  
  // Any must be a Call statement or a func holder and that contains a
  // call statement.
  public static Call verifyCall(Any a)
  {
    if (a == null)
      return null;

    if (a == AnyNull.instance())
      return null;
    
    if (a instanceof Call)
      return (Call)a;

    if (!(a instanceof AnyFuncHolder.FuncHolder))
      throw new IllegalArgumentException("Not a function");
    
    AnyFuncHolder.FuncHolder func = (AnyFuncHolder.FuncHolder)a;
    Func f = func.getFunc();
    if (!(f instanceof Call))
      throw new IllegalArgumentException("Not a Call statement");
    
    return (Call)f;
  }
  
  public static AnyFuncHolder.FuncHolder verifyCallFuncHolder(Any a)
  {
    if (a == AnyNull.instance())
      return null;
    
    if (!(a instanceof AnyFuncHolder.FuncHolder))
      throw new IllegalArgumentException("Not a function");
    
    AnyFuncHolder.FuncHolder func = (AnyFuncHolder.FuncHolder)a;
    Func f = func.getFunc();
    if (!(f instanceof Call))
      throw new IllegalArgumentException("Not a Call statement");
    
    return func;
  }
  
  public static int widthForCharacter(Component c)
  {
    return widthForCharacter(c, 'm', 1);
  }
  
  public static int widthForCharacter(Component c, int num)
  {
    return widthForCharacter(c, 'm', num);
  }
  
  public int indexInContainer(Component contained, boolean remove)
  {
    Container c = contained.getParent();
    if (c == null)
      return -1;
    
    Component[] children = c.getComponents();
    for (int i = 0; i < children.length; i++)
    {
      if (contained == children[i])
      {
        if (remove)
          c.remove(i);
        return i;
      }
    }
    return -1;
  }
  
  public static int widthForCharacter(Component c, char ch)
  {
    return widthForCharacter(c, ch, 1);
  }
  
  public static int widthForCharacter(Component c, char ch, int num)
  {
    return c.getFontMetrics(c.getFont()).charWidth(ch) * num;
  }
  
  public boolean forwardKeyBinding(KeyStroke ks,
                                   KeyEvent  e,
                                   int       condition,
                                   boolean   pressed)
  {
    return false;
  }

  public void setBounds(Rectangle r)
  {
    getComponent().setBounds(r);
  }
  
  /**
	 * Default processing for data node events (as opposed to
	 * adapted GUI events) which are received at this node.  This
	 * implementation attempts to resolve the data node and
	 * render it into the component.
	 */
	protected void componentProcessEvent(Event e) throws AnyException
	{
//    Any a = getGUIRendered(e);
//    setValueToComponent(a);
	}
  
  /**
   * Fetch the value to be rendered into the GUI node.
   * @param e the node event giving rise to the refresh 
   * @return the value to be rendered to the GUI
   * @throws AnyException
  protected Any getGUIRendered(Event e) throws AnyException
  {
    Map id = (Map)e.getId();

    Any eventType = id.get(EventConstants.EVENT_TYPE);
    
    Any ret;

    if (eventType.equals(EventConstants.BOT_UPDATE))
    {
      ret = renderInfo_.resolveDataNode(getContextNode(), false);
    }
    else
    {
      boolean notDeleting = !(eventType.equals(EventConstants.NODE_REMOVED) ||
                              eventType.equals(EventConstants.NODE_REMOVED_CHILD));
      
      ret = renderInfo_.resolveDataNode(getContextNode(), true, notDeleting);
    }
    
    return ret;
  }
   */
  
  protected void setValueToComponent(Any v)
  {
    throw new UnsupportedOperationException();
  }

  protected void setTooltip(Any v)
  {
    RenderInfo r = getRenderInfo();
    if (r != null && v != null)
    {
      Format f = r.getFormat(v);
      ((JComponent)getComponent()).setToolTipText(f.format(v));
    }
    else
      ((JComponent)getComponent()).setToolTipText(null);
  }

  protected Object getScroller()
  {
    return null;
  }
  
  /**
   * 
   */
  protected void setMargin(Insets i)
  {
    // Don't bother with swing invoker.  Properties set
    // through a swing invoker anyway.
    // Remember to override this method for a tidier
    // implemenmtation if component-specific sub-classes
    // are created, as in AnyText
    if (getComponent() instanceof AbstractButton)
      ((AbstractButton)getComponent()).setMargin(i);
    else if (getComponent() instanceof JToolBar)
      ((JToolBar)getComponent()).setMargin(i);
    else if (getComponent() instanceof JMenuBar)
      ((JMenuBar)getComponent()).setMargin(i);
  }
  
  protected Insets getInsets()
  {
    if (getComponent() instanceof AbstractButton)
      return ((AbstractButton)getComponent()).getMargin();
    else if (getComponent() instanceof JToolBar)
      return ((JToolBar)getComponent()).getMargin();
    else if (getComponent() instanceof JMenuBar)
      return ((JMenuBar)getComponent()).getMargin();
    
    return null;
  }
  
	protected void focusLost(boolean isTemporary)
	{
	}

	protected void focusGained(boolean isTemporary) throws AnyException
	{
	}

	/**
	 * Perform any processing required to update the model data
	 * this component is viewing.
	 */
	protected void initUpdateModel()
	{
	}

	protected Object getAttachee(Any eventType) // TODO: Consider moving to AnyView
	{
		if (eventType.equals(ListenerConstants.CONTEXT))
    {
      if (notifyContext_ == null)
        notifyContext_ = new NotifyContext();
      return notifyContext_;
    }
    
		return getComponent();
	}

  protected void contextEstablished()  // TODO remove?
  {
//    setRenderInfo(getRenderInfo());
  }
  
  /**
   * Fetch the tab pane parent or null if we are not the child
   * of a tab pane
   */
  protected AnyTabbedPane getTabParent()
  {
	  Facade f = getParentComponent();

	  if (f instanceof AnyTabbedPane)
	    return (AnyTabbedPane)f;

	  return null;
	}

	public Object getAddee()
	{
		return getComponent();
	}

	public Object getAddIn()
  {
    return getComponent();
  }
  
	public JComponent getBorderee()
	{
		return (getComponent() instanceof JComponent) ? (JComponent)getComponent()
																				 : null;
	}

	protected Object getPropertyOwner(Any property)
	{
		if (spoofProperties__.contains(property))
			return this;

		return getComponent();
	}

  private JScrollPane checkScroller()
  {
    JScrollPane s = (JScrollPane)getScroller();
    
    if (s == null)
      throw new IllegalStateException("No scroller found");
    
    return s;
  }
  
  private void setScrollCorner(JScrollPane s, String corner, UIFacade f, Any anyCorner)
  {
    this.remove(anyCorner);

    // Put the component in, checking for null
    if (f == null)
    {
      s.setCorner(corner, null);
    }
    else
    {
      this.add(anyCorner, f);
      s.setCorner(corner, (Component)f.getAddee());
    }
  }

  
  private TitledBorder findTitledBorder()
  {
    // Look into any border structure on the component and
    // if we find a titled border then return it. Otherwise
    // return null.
    JComponent c = this.getBorderee();
    Border b = c.getBorder();
    if (c != null)
      return searchTitledBorder(b);
    
    return null;
  }
  
  private BackgroundImageBorder findImageBorder()
  {
    JComponent c = this.getBorderee();
    Border b = c.getBorder();
    if (c != null)
      return searchImageBorder(b);
    
    return null;
  }
  
  private TitledBorder searchTitledBorder(Border b)
  {
    if (b instanceof TitledBorder)
      return (TitledBorder)b;
    
    if (b instanceof CompoundBorder)
    {
      CompoundBorder cb = (CompoundBorder)b;
      Border nb = cb.getOutsideBorder();
      TitledBorder tb;
      if ((tb = searchTitledBorder(nb)) != null)
        return tb;
      
      nb = cb.getInsideBorder();
      if ((tb = searchTitledBorder(nb)) != null)
        return tb;
    }
    return null;
  }
  
  private BackgroundImageBorder searchImageBorder(Border b)
  {
    if (b instanceof BackgroundImageBorder)
      return (BackgroundImageBorder)b;
    
    if (b instanceof CompoundBorder)
    {
      CompoundBorder cb = (CompoundBorder)b;
      Border nb = cb.getOutsideBorder();
      BackgroundImageBorder ib;
      if ((ib = searchImageBorder(nb)) != null)
        return ib;
      
      nb = cb.getInsideBorder();
      if ((ib = searchImageBorder(nb)) != null)
        return ib;
    }
    return null;
  }
  
	private void init(Container c)
	{
		//System.out.println ("AnyComponent.init(c)");

    setupEventSet(c);
    
    // No point soliciting events from the component when it is
    // being used as a cell renderer.
    if (!isRenderer())
      initUpdateModel();
  }

  /*
  private class AnyFocusListener extends    AbstractAny
                                 implements EventListener
  {
		public void processEvent(Event e) throws AnyException
		{
      AnyBoolean isTemporary = (AnyBoolean)e.get(ListenerAdapterFactory.isTemporary__);
			focusLost(isTemporary.getValue());
		}

		public Array getDesiredEventTypes()
		{
			return focusLostEventType__;
		}
  }
  */
  
  class PopupListener extends EventBinding
  {
  	private AnyPopupMenu menu_;

  	// Create a popup listener for the specified event type.
  	// Generally, the event type will be a popup trigger
  	// mouse event.
  	public PopupListener(Any eventType, AnyPopupMenu iMenu)
    {
      super(eventType, false);
    	menu_ = iMenu;
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      // The popup menu has no parent (and therefore no context) the
      // first time it is used.  Adding it to the component against
      // which it is being popped up will give it a context.  In case
      // this is not the first time, remove it from any parent it has
      // now.
      menu_.removeInParent();
      
      // Only popup if the menu contains at least one visible component
      if (menu_.somethingVisible())
      {
        AnyComponent.this.add(menu_.getUniqueKey(), menu_);
        
        menu_.setLastPoppedUpOn(AnyComponent.this);
        
  			MouseEvent me = (MouseEvent)e.getUnderlyingEvent();
  			menu_.getPopupMenu().show(me.getComponent(),
  			           me.getX(),
  			           me.getY());
      }                   
      return null;
		}
  }

  private class RequestFocus extends SwingInvoker
  {
		protected void doSwing()
		{
			//((JComponent)getComponent()).requestFocus();
      //boolean done = getComponent().requestFocusInWindow();
      getComponent().requestFocusInWindow();
			//System.out.println("Requested Focus " + done);
		}
	}
  
  private class BackgroundImageBorder implements Border
  {
    private Icon image_;

    public BackgroundImageBorder(Icon image)
    {
      image_ = image;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
      int x0 = x + (width-image_.getIconWidth())/2;
      int y0 = y + (height-image_.getIconHeight())/2;
      image_.paintIcon(c, g, x0, y0);
    }

    public Insets getBorderInsets(Component c)
    {
      return new Insets(0,0,0,0);
    }

    public boolean isBorderOpaque()
    {
      return true;
    }
    
    public void setImage(Icon i)
    {
      image_ = i;
    }
    
    public Icon getImage()
    {
      return image_;
    }
  }

	protected class PaintValidate extends SwingInvoker
	{
		JComponent c_;

		PaintValidate(Component c)
		{
			c_ = (JComponent)c;
		}

		PaintValidate(Container c)
		{
			c_ = (JComponent)c;
		}

		PaintValidate(JComponent c)
		{
			c_ = c;
		}

		protected void doSwing()
		{
			c_.revalidate();
			c_.repaint();
		}
	}
}
