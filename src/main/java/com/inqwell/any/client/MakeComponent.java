/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/MakeComponent.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-18 21:45:00 $
 */

package com.inqwell.any.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreeSelectionModel;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.ConstInt;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.FloatI;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.ObjectI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.client.swing.ComponentBorder;
import com.inqwell.any.client.swing.JComboBox;
import com.inqwell.any.client.swing.JTextField;
import com.inqwell.any.client.swing.JDateChooser;

/**
 * Some factory functions for gui components.
 * <p>
 * Given an <code>Any</code> make a suitable component to
 * render it.
 * <p>
 * Given an <code>Any</code> make a suitable component to
 * edit it.
 * <p>
 * Make a border.
 * <p>
 * Do geometry.
 */
public class MakeComponent extends AbstractAny
{
	private static ComponentMaker componentMaker_ = new ComponentMaker();

	private static Map      borderMap__;
  private static ClassMap wrapperMap__;

	public static final Any noBorder__      = AbstractValue.flyweightString("none");
	public static final Any bevelRaised__   = AbstractValue.flyweightString("bevelRaised");
	public static final Any bevelLowered__  = AbstractValue.flyweightString("bevelLowered");
	public static final Any etchedRaised__  = AbstractValue.flyweightString("etchedRaised");
	public static final Any etchedLowered__ = AbstractValue.flyweightString("etchedLowered");
	public static final Any line__          = AbstractValue.flyweightString("line");
	public static final Any none__          = AbstractValue.flyweightString("none");

	public static final int aboveTop__    = TitledBorder.ABOVE_TOP;
	public static final int top__         = TitledBorder.TOP;
	public static final int belowTop__    = TitledBorder.BELOW_TOP;
	public static final int aboveBottom__ = TitledBorder.ABOVE_BOTTOM;
	public static final int bottom__      = TitledBorder.BOTTOM;
	public static final int belowBottom__ = TitledBorder.BELOW_BOTTOM;

	public static final int left__        = TitledBorder.LEFT;
	public static final int centre__      = TitledBorder.CENTER;
	public static final int right__       = TitledBorder.RIGHT;

	public static final Border nullBorder__ = new EmptyBorder(0, 0, 0, 0);

	// layout/orientation directions
	public static IntI X_AXIS = new ConstInt(BoxLayout.X_AXIS);
	public static IntI Y_AXIS = new ConstInt(BoxLayout.Y_AXIS);

	// Table selection intervals
	public static final int singleSelect__          = ListSelectionModel.SINGLE_SELECTION;
	public static final int singleIntervalSelect__  = ListSelectionModel.SINGLE_INTERVAL_SELECTION;
	public static final int multiIntervalSelect__   = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
	public static final IntI SINGLE_SELECTION              = new ConstInt(ListSelectionModel.SINGLE_SELECTION);
	public static final IntI SINGLE_INTERVAL_SELECTION     = new ConstInt(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	public static final IntI MULTIPLE_INTERVAL_SELECTION   = new ConstInt(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

	// Table selection modes
	public static final IntI rowSelection__      = new ConstInt(0);
	public static final IntI columnSelection__   = new ConstInt(1);
	public static final IntI cellSelection__     = new ConstInt(2);

  // Tree selection modes
	public static final IntI CONTIGUOUS_TREE_SELECTION    = new ConstInt(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
	public static final IntI DISCONTIGUOUS_TREE_SELECTION = new ConstInt(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	public static final IntI SINGLE_TREE_SELECTION        = new ConstInt(TreeSelectionModel.SINGLE_TREE_SELECTION);

  // Dialog redirection possibilities
	public static final Any dialogOk__     = AbstractValue.flyweightString("ok");
  public static final Any dialogCancel__ = AbstractValue.flyweightString("cancel");
  
  // Orientations
	public static final IntI VERTICAL   = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.VERTICAL));
	public static final IntI HORIZONTAL = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.HORIZONTAL));

	// Remaining SwingConstants
  public static final IntI BOTTOM      = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.BOTTOM));
  public static final IntI CENTER      = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.CENTER));
  public static final IntI EAST        = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.EAST));
  public static final IntI LEADING     = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.LEADING));
  public static final IntI LEFT        = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.LEFT));
  public static final IntI NEXT        = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.NEXT));
  public static final IntI NORTH       = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.NORTH));
  public static final IntI NORTH_EAST  = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.NORTH_EAST));
  public static final IntI NORTH_WEST  = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.NORTH_WEST));
  public static final IntI PREVIOUS    = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.PREVIOUS));
  public static final IntI RIGHT       = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.RIGHT));
  public static final IntI SOUTH       = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.SOUTH));
  public static final IntI SOUTH_EAST  = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.SOUTH_EAST));
  public static final IntI SOUTH_WEST  = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.SOUTH_WEST));
  public static final IntI TOP         = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.TOP));
  public static final IntI TRAILING    = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.TRAILING));
  public static final IntI WEST        = (IntI)AbstractValue.flyweightConst(new ConstInt(SwingConstants.WEST));

  public static final IntI TABLE = (IntI)AbstractValue.flyweightConst(new ConstInt(1));
  public static final IntI LIST  = (IntI)AbstractValue.flyweightConst(new ConstInt(2));
  
  static
	{
		borderMap__ = AbstractComposite.simpleMap();
		borderMap__.add(bevelRaised__,   new MakeBevelBorder(BevelBorder.RAISED));
		borderMap__.add(bevelLowered__,  new MakeBevelBorder(BevelBorder.LOWERED));
		borderMap__.add(etchedRaised__,  new MakeEtchedBorder(EtchedBorder.RAISED));
		borderMap__.add(etchedLowered__, new MakeEtchedBorder(EtchedBorder.LOWERED));
		borderMap__.add(line__,          new MakeLineBorder());
		borderMap__.add(none__,          new MakeNoneBorder());
    
    wrapperMap__ = new ClassMap();
    wrapperMap__.add(javax.swing.JLabel.class, new MakeLabel());
    wrapperMap__.add(javax.swing.JCheckBox.class, new MakeCheckBox());
    wrapperMap__.add(javax.swing.text.JTextComponent.class, new MakeText());
    wrapperMap__.add(javax.swing.JComboBox.class, new MakeCombo());
    wrapperMap__.add(javax.swing.JPanel.class, new MakeBox());
    wrapperMap__.add(javax.swing.JComponent.class, new MakeBasic());
	}

  // Make a new graphical object to render the given Any.  If
  // the Container is non-null a new object is only returned
  // if c is not able to render the given Any.  Otherwise c
  // is returned.
	public static Container makeRenderer(Any a, Container c)
	{
    return makeRenderer(a, c, null);
	}

  // Make a new graphical object to render the given Any.  If
  // the Container is non-null a new object is only returned
  // if c is not able to render the given Any.  Otherwise c
  // is returned. If renderType is one of LIST or TABLE then this
  // method may return a component optimised for the stated purpose.
	public static Container makeRenderer(Any a, Container c, Any renderType)
  {
		Container ct = componentMaker_.makeRenderer(a, c, renderType);
		componentMaker_.reset();
		return ct;
  }

	public static Container makeEditor(Any a, RenderInfo r, Any context)
	{
		Container c = componentMaker_.makeEditor(a, r, context);
		componentMaker_.reset();
		return c;
	}

	public static Container makeComboEditor(Any a, RenderInfo r, Any context)
	{
		Container c = componentMaker_.makeComboEditor(a, r, context);
		componentMaker_.reset();
		return c;
	}
  
  public static AnyComponent makeWrapper(Object o)
  {
    MakeWrapper f = (MakeWrapper)wrapperMap__.get(o);
    if (f == null)
      throw new AnyRuntimeException("Unsupported component class " + o.getClass());
    
    return f.getWrapper(o);
  }

  /*
  public static Border makeBorder(Any    borderType,
                                  String title,
                                  int    titlePosition,
                                  int    titleJustifcation,
                                  int    xMargin,
                                  int    yMargin)
  {
//  	System.out.println ("MakeComponent.makeBorder " + borderType + " " + title + " " + titlePosition + " " + titleJustifcation + " " + xMargin + " " + yMargin);
  	Border marginBorder = null;
  	if (xMargin != 0 || yMargin != 0)
  	  marginBorder = new EmptyBorder(yMargin, xMargin, yMargin, xMargin);

  	Border decorBorder = null;
  	if (borderMap__.contains(borderType))
  	{
//  	  System.out.println ("MakeComponent.makeBorder making " + borderType);
  		MakeBorder f = (MakeBorder)borderMap__.get(borderType);
  		try {f.execFunc(null);} catch(AnyException e){}
  		decorBorder = f.getBorder();
  	}
//  	  System.out.println ("MakeComponent.makeBorder decorBorder " + decorBorder);

  	Border actualBorder = null;
  	if (marginBorder != null && decorBorder != null)
  	  actualBorder = new CompoundBorder(marginBorder, decorBorder);
  	//else if(marginBorder != null)
  	  //actualBorder = marginBorder;
  	else if (decorBorder != null)
  	  actualBorder = decorBorder;
  	//else actualBorder = nullBorder__;

    // If there's only a margin and no decoration then treat it
    // separately, otherwise we lose the UIDefault "TitledBorder.border"
    
  	TitledBorder titledBorder = null;
  	if (title != null && !title.equals("none"))
  	{
      Border b = (actualBorder == nullBorder__) ? null
                                                : actualBorder;
  	  actualBorder = new TitledBorder
                        ((actualBorder == nullBorder__) ? actualBorder
                                                        : null,
                          title,
                          titleJustifcation,
                          titlePosition);
                                      
      if (b != null)
        actualBorder = new CompoundBorder(b, actualBorder);
  	}
    
    if (marginBorder != null && decorBorder == null && actualBorder != null)
      actualBorder = new CompoundBorder(marginBorder, actualBorder);
    else if (marginBorder != null && actualBorder == null)
      actualBorder = marginBorder;
          
  	return actualBorder;
  }
*/

  public static Border makeBorder(Any          borderType,
                                  String       title,
                                  AnyComponent borderComponent,
                                  JComponent   bordered,
                                  int          titlePosition,
                                  int          titleJustifcation,
                                  int          xMargin,
                                  int          yMargin)
  {
    // borderType: one of the keys within borderMap__
    // title: caption if non-null
    //   titlePosition, titleJustifcation: used if title is non null
    // xMargin, yMargin: margins left&right, top&bottom if non-zero
    
    Border decorBorder = null;
  	if (borderMap__.contains(borderType))
  	{
  		MakeBorder f = (MakeBorder)borderMap__.get(borderType);
  		try {f.execFunc(null);} catch(AnyException e){}
      // Will be emptyBorder__ if borderType is "none"
  		decorBorder = f.getBorder();
  	}
    
    Border titleBorder = null;
    if (title != null)
    {
      titleBorder = new TitledBorder(decorBorder,
                                     title,
                                     titleJustifcation,
                                     titlePosition);
    }
    
    Border componentBorder = null; 
    if (borderComponent != null)
      componentBorder =
        new ComponentBorder((Component)borderComponent.getAddee(),
                            bordered,
                            (titleBorder != null) ? titleBorder
                                : decorBorder);
                                            
    
    Border marginBorder = null;
    if (xMargin != 0 || yMargin != 0)
  	  marginBorder = new EmptyBorder(yMargin, xMargin, yMargin, xMargin);
    
    // Combine margin with any title or decor there is already
    Border actualBorder = (componentBorder != null) ? componentBorder
                                                    : (titleBorder != null) ? titleBorder
                                                                            : decorBorder;
                                                
    if (actualBorder != null && marginBorder != null)
      actualBorder = new CompoundBorder(marginBorder, actualBorder);
    else if (marginBorder != null)
      actualBorder = marginBorder;
    
    return actualBorder;
  }
  
  public static Component createSpace(int x, int y)
  {
		return Box.createRigidArea(new Dimension(x, y));
	}

  public static Dimension getMinimumSize(String x, String y, Dimension preferred)
  {
		//System.out.println ("**********MakeComponent.getMinimumSize() x:" + x + " y:" + y + " preferred: " + preferred);
  	Dimension d = new Dimension(preferred);

  	d.setSize(MakeComponent.getMinGeometry(x, d.width), MakeComponent.getMinGeometry(y, d.height));

    return d;
  }

  public static Dimension getMaximumSize(String x, String y, Dimension preferred)
  {
		//System.out.println ("**********MakeComponent.getMaximumSize() x:" + x + " y:" + y + " preferred: " + preferred);
  	Dimension d = new Dimension(preferred);

  	d.setSize(MakeComponent.getMaxGeometry(x, d.width), MakeComponent.getMaxGeometry(y, d.height));

    return d;
  }
  
  static private int getMinGeometry(String geom, int preferred)
  {
  	if (geom.equals("v")) return 0;
  	else if (geom.equals("f")) return preferred;
  	else if (geom.equals("i")) return preferred;
  	else if (geom.equals("d")) return 0;
  	else return 0;
  }

  static private int getMaxGeometry(String geom, int preferred)
  {
  	if (geom.equals("v")) return Integer.MAX_VALUE;
  	else if (geom.equals("f")) return preferred;
  	else if (geom.equals("i")) return Integer.MAX_VALUE;
  	else if (geom.equals("d")) return preferred;
  	else return 0;
  }

	static class ComponentMaker extends AbstractVisitor
	{
		private Container     component_;
		private boolean       makeRenderer_;
		private boolean       makeEnum_;
		private RenderInfo    r_;
		private Any           context_;
		private Any           renderType_;

		void reset()
		{
			// avoid dangling reference to the last component we
			// created.
			component_  = null;
			makeEnum_   = false;
			r_          = null;
			context_    = null;
			renderType_ = null;
		}

		Container makeRenderer(Any a, Container c, Any renderType)
		{
			makeRenderer_ = true;
			makeEnum_     = false;
      component_    = c;
      renderType_   = renderType;

			if (a != null)
				a.accept(this);
			else
				this.visitAnyString(null);

			return component_;
		}

		Container makeEditor(Any a, RenderInfo r, Any context)
		{
			makeRenderer_ = false;
			makeEnum_     = r.isEnum();
			r_            = r;
			context_      = context;
			a.accept(this);
			return component_;
		}

		Container makeComboEditor(Any a, RenderInfo r, Any context)
		{
			makeRenderer_ = false;
			makeEnum_     = false;  // never an enum when making an editor for a combo!
			r_            = r;
			context_      = context;
			a.accept(this);
			return component_;
		}

		public void visitAnyByte (ByteI b)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.LEFT);
			else
				component_ = editorComponent(JTextField.LEFT);
		}

		public void visitAnyBoolean (BooleanI b)
		{
			component_ = checkBox();
		}

		public void visitAnyInt (IntI i)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.RIGHT);
			else
				component_ = editorComponent(JTextField.RIGHT);
		}

		public void visitAnyShort (ShortI s)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.RIGHT);
			else
				component_ = editorComponent(JTextField.RIGHT);
		}

		public void visitAnyLong (LongI l)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.RIGHT);
			else
				component_ = editorComponent(JTextField.RIGHT);
		}

		public void visitAnyFloat (FloatI f)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.RIGHT);
			else
				component_ = editorComponent(JTextField.RIGHT);
		}

		public void visitAnyDouble (DoubleI d)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.RIGHT);
			else
				component_ = editorComponent(JTextField.RIGHT);
		}

		public void visitDecimal (Decimal d)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.RIGHT);
			else
				component_ = editorComponent(JTextField.RIGHT);
		}

		public void visitAnyDate (DateI d)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.LEFT);
			else
				component_ = new JDateChooser();
		}

		public void visitAnyString (StringI s)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.LEFT);
			else
				component_ = editorComponent(JTextField.LEFT);
		}

		public void visitAnyObject (ObjectI o)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.LEFT);
			else
				component_ = editorComponent(JTextField.LEFT);
		}

		public void visitUnknown (Any o)
		{
			if (makeRenderer_)
				component_ = label(SwingConstants.LEFT);
			else
				component_ = editorComponent(JTextField.LEFT);
		}

		public void visitMap (Map m)
		{
      if (m instanceof AnyComponent)
      {
        // If we've been given a component as the 'value' to render
        // then the renderer/editor is the component itself (at the
        // moment I reckon this is only used by renderers but croak
        // just in case its not)
				if (makeRenderer_)
        {
          AnyComponent ac = (AnyComponent)m;
					component_ = ac.getComponent();
          return;
        }
				else
					throw new AnyRuntimeException("Illegal editor request");
        
      }
      
			if (m.contains(NodeSpecification.atTxt__))
			{
				Any a = m.get(NodeSpecification.atTxt__);
				a.accept(this);
			}
			else
			{
				if (makeRenderer_)
					component_ = label(SwingConstants.LEFT);
				else
					component_ = editorComponent(JTextField.LEFT);
			}
		}

		private Container label(int horizontalAlignment)
		{
			JLabel l;
      if (component_ != null && TABLE.equals(renderType_) && component_ instanceof TableCell)
      {
        l = (JLabel)component_;
      }
      else if (component_ != null && LIST.equals(renderType_) && component_ instanceof ListCell)
      {
        l = (JLabel)component_;
      }
      else
      {
        // We want a label suitable for the cell but the current component
        // isn't one.
        if (TABLE.equals(renderType_))
          l = new TableCell();
        else if (LIST.equals(renderType_))
          l = new ListCell();
        else
          l = new JLabel();
      }
			l.setHorizontalAlignment(horizontalAlignment);
			return l;
		}

		private Container editorComponent(int horizontalAlignment)
		{
			Container c = null;

			if (makeEnum_)
			{
				AnyListModel lm = r_.getEditingList();
				if (lm != null)
				{
          lm.setContext(context_);
					JComboBox cb = new JComboBox(lm);
					c = cb;
				}
				else
				{
				  RenderInfo lr = new ListRenderInfo(r_.getDescriptor(), r_.getField());

					JComboBox cb = new JComboBox(new AnyListModel(lr));
					cb.setRenderer(new AnyCellRenderer(r_));
					c = cb;
				}
			}
			else
			{
				JTextField t = new JTextField();
				t.setHorizontalAlignment(horizontalAlignment);
				c = t;
			}
			return c;
		}

		private Container checkBox()
		{
      JCheckBox c;
      if (component_ != null && component_ instanceof JCheckBox)
      {
        c = (JCheckBox)component_;
      }
      else
      {
        if (TABLE.equals(renderType_))
          c = new CheckBoxCell();
        else if (LIST.equals(renderType_))
          c = new CheckBoxCell();
        else
          c = new JCheckBox();
      }
      c.setHorizontalAlignment(SwingConstants.CENTER);
			return c;
		}
  }
  
  // Just a rip off of the overridden methods (for performance reasons)
  // from JDK DefaultTableCellRenderer
  static public class TableCell extends JLabel
  {
    TableCell()
    {
      setOpaque(true);
    }
    
    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public boolean isOpaque()
    { 
      Color back = getBackground();
      Component p = getParent(); 
      if (p != null)
        p = p.getParent(); 
      // p should now be the JTable. 
      boolean colorMatch = (back != null) && (p != null) && 
                           back.equals(p.getBackground()) && 
                           p.isOpaque();
                           
      return !colorMatch && super.isOpaque(); 
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     *
     * @since 1.5
     */
    public void invalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
      if (propertyName.equals("text"))
      {
        super.firePropertyChange(propertyName, oldValue, newValue);
      }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }

    /**
     * A subclass of <code>TableCell</code> that
     * implements <code>UIResource</code>.
     * <code>TableCell</code> doesn't implement
     * <code>UIResource</code>
     * directly so that applications can safely override the
     * <code>cellRenderer</code> property with
     * <code>DefaultTableCellRenderer</code> subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends TableCell 
        implements javax.swing.plaf.UIResource
    {
    }

  }

  // Just a rip off of the overridden methods (for performance reasons)
  // from JDK DefaultTableCellRenderer
  static private class ListCell extends JLabel
  {
    ListCell()
    {
      setOpaque(true);
    }
    
    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     *
     * @since 1.5
     * @return <code>true</code> if the background is completely opaque
     *         and differs from the JList's background;
     *         <code>false</code> otherwise
     */
    public boolean isOpaque()
    { 
      Color back = getBackground();
      Component p = getParent(); 
      if (p != null)
      { 
        p = p.getParent(); 
      }
      
      // p should now be the JList. 
      boolean colorMatch = (back != null) && (p != null) && 
                           back.equals(p.getBackground()) && 
                           p.isOpaque();
                           
      return !colorMatch && super.isOpaque(); 
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void validate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void invalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void repaint() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void revalidate() {}
   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(long tm, int x, int y, int width, int height) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(Rectangle r) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
      if (propertyName.equals("text"))
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

    /**
     * A subclass of DefaultListCellRenderer that implements UIResource.
     * DefaultListCellRenderer doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with DefaultListCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends ListCell
        implements javax.swing.plaf.UIResource
    {
    }
  }

  static private class CheckBoxCell extends JCheckBox
  {
    CheckBoxCell()
    {
      setOpaque(true);
    }
    
    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     *
     * @since 1.5
     * @return <code>true</code> if the background is completely opaque
     *         and differs from the JList's background;
     *         <code>false</code> otherwise
     */
    public boolean isOpaque()
    { 
      Color back = getBackground();
      Component p = getParent(); 
      if (p != null)
      { 
        p = p.getParent(); 
      }
      
      // p should now be the JList. 
      boolean colorMatch = (back != null) && (p != null) && 
                           back.equals(p.getBackground()) && 
                           p.isOpaque();
                           
      return !colorMatch && super.isOpaque(); 
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void validate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void invalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void repaint() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void revalidate() {}
   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(long tm, int x, int y, int width, int height) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(Rectangle r) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
      if (propertyName.equals("text"))
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

    /**
     * A subclass of DefaultListCellRenderer that implements UIResource.
     * DefaultListCellRenderer doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with DefaultListCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends ListCell
        implements javax.swing.plaf.UIResource
    {
    }
  }
  
  static abstract class MakeBorder extends AbstractFunc
  {
  	protected Border border_;

  	Border getBorder()
  	{
  		Border ret = border_;
  		border_ = null;
  		return ret;
  	}
  }

  static class MakeBevelBorder extends MakeBorder
  {
  	private int bevelType_;

  	MakeBevelBorder(int bevelType) {bevelType_ = bevelType;}

  	public Any exec(Any a) throws AnyException
  	{
  		border_ = new PBevelBorder(bevelType_);
  		return null;
  	}
  }

  static class MakeEtchedBorder extends MakeBorder
  {
  	private int etchType_;

  	MakeEtchedBorder(int etchType) {etchType_ = etchType;}

  	public Any exec(Any a) throws AnyException
  	{
  		border_ = new PEtchedBorder(etchType_);
  		return null;
  	}
  }

  static class MakeLineBorder extends MakeBorder
  {
  	MakeLineBorder() {}

  	public Any exec(Any a) throws AnyException
  	{
  		border_ = new LineBorder(Color.black);
  		return null;
  	}
  }

  static class MakeNoneBorder extends MakeBorder
  {
  	MakeNoneBorder() {}

  	public Any exec(Any a) throws AnyException
  	{
  		border_ = nullBorder__;
  		return null;
  	}
  }

  public static class PBevelBorder extends BevelBorder
  {
  	public PBevelBorder(int bevelType)
  	{
  		super(bevelType);
  	}

    public void paintBorder(Component c,
                            Graphics  g,
                            int       x,
                            int       y,
                            int       width,
                            int       height)
    {
    	//System.out.println ("PBevelBorder painting.......................");
    	//System.out.println (c.getParent());
    	//System.out.println (c.getParent().getBackground());
    	//System.out.println (c);
    	//System.out.println (c.getBackground());
    	super.paintBorder(c.getParent(), g, x, y, width, height);
    }
  }

  public static class PEtchedBorder extends EtchedBorder
  {
  	public PEtchedBorder(int etchType)
  	{
  		super(etchType);
  	}

    public void paintBorder(Component c,
                            Graphics  g,
                            int       x,
                            int       y,
                            int       width,
                            int       height)
    {
    	super.paintBorder(c.getParent().getParent(), g, x, y, width, height);
    }
  }
  
  // Some functions to create Inq GUI wrappers. We can't simply
  // clone prototypical instances because they don't support cloning.
  static abstract class MakeWrapper extends AbstractFunc
  {
    AnyComponent getWrapper(Object o)
    {
      try
      {
        AnyComponent ret = (AnyComponent)execFunc(null);
        ret.setRenderer(true);
        ret.setObject(o);
        return ret;
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }

  static class MakeLabel extends MakeWrapper
  {
    public Any exec(Any a) throws AnyException
    {
      return new AnyLabel();
    }
  }

  static class MakeCheckBox extends MakeWrapper
  {
    public Any exec(Any a) throws AnyException
    {
      AnyCheck c = new AnyCheck();
      
      return c;
    }
  }

  static class MakeText extends MakeWrapper
  {
    public Any exec(Any a) throws AnyException
    {
      return new AnyText();
    }
  }

  static class MakeCombo extends MakeWrapper
  {
    public Any exec(Any a) throws AnyException
    {
      return new AnyComboBox();
    }
  }

  static class MakeBox extends MakeWrapper
  {
    public Any exec(Any a) throws AnyException
    {
      return new AnyBox();
    }
  }

  static class MakeBasic extends MakeWrapper
  {
    public Any exec(Any a) throws AnyException
    {
      return new AnySimpleComponent();
    }
  }
}
