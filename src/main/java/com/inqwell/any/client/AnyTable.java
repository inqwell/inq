/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTable.java $
 * $Author: sanderst $
 * $Revision: 1.14 $
 * $Date: 2011-05-07 22:15:33 $
 */

package com.inqwell.any.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractEvent;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyComparator;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyOrderedMap;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.AnyURL;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ConstInt;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Descriptor;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventListener;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.Vectored;
import com.inqwell.any.beans.TableF;
import com.inqwell.any.client.swing.ColumnGroup;
import com.inqwell.any.client.swing.GroupableTableColumnModel;
import com.inqwell.any.client.swing.JPanel;
import com.inqwell.any.client.swing.JTable;
import com.inqwell.any.client.swing.JTree;
import com.inqwell.any.client.swing.TableEditEvent;
import com.inqwell.any.client.swing.TableModel;

public class AnyTable extends    AnySelection
											implements TableF
{
	private JTable        t_;
	private JScrollPane   s_;

  // The current table model
	private TableModel    model_;

  // The two models we can have, one of which will be active
  // and set into model_
	private TableModel    flatModel_;
	private TableModel    treeModel_;

	private Map           selection_;
  private Array         keySelection_;
  private Array         indexSelection_;
  private IntI          selectCount_;
  private IntI          rowCount_;

  // The largest rendered with each column has seen since the
  // last complete replacement of model rows.
	private Array     maxWidths_;

  // The column indices, in model coordinates, that interactive
  // sorting is using, or null if not sorting.  The first
  // entry of -1 indicates the end of the sort columns
	private int[]         orderItems_;

  private boolean       sortDescending_ = false;
  private boolean       clickSortEnabled_ = true;
  
	private JComponent    borderee_;
  
  // Used for mouse motion events
  private int           lastRow_    = -1;
  private int           lastColumn_ = -1;

  private static AnyInt     eventRow__    = new AnyInt();
  private static AnyInt     eventColumn__ = new AnyInt();
  private static AnyInt     iX__          = new AnyInt();
  private static AnyInt     iY__          = new AnyInt();
  private static AnyInt     iCellX__      = new AnyInt();
  private static AnyInt     iCellY__      = new AnyInt();
  private static AnyInt     iCellRectX__  = new AnyInt();
  private static AnyInt     iCellRectY__  = new AnyInt();
  private static AnyInt     iCellRectW__  = new AnyInt();
  private static AnyInt     iCellRectH__  = new AnyInt();
  private static Map        iCellRect__   = AbstractComposite.simpleMap();
  private static AnyInt     anInt__       = new AnyInt();
  private static BooleanI   isBefore__    = new AnyBoolean();
  private static BooleanI   isUI__        = new AnyBoolean();

	private static String paste__  = "Paste";
	private static String copy__   = "Copy";
	private static String cut__    = "Cut";
	private static String delete__ = "Delete";

//	private static AnyIcon sortDescending__;
//	private static AnyIcon sortAscending__;

	private static Set     tableProperties__;
	public  static Any     row__           = AbstractValue.flyweightString("row");
	public  static Any     rowRoot__       = AbstractValue.flyweightString("rowRoot");
	public  static Any     rowKey__        = AbstractValue.flyweightString("rowKey");
	public  static Any     before__        = AbstractValue.flyweightString("before");
  public  static Any     uI__            = AbstractValue.flyweightString("isUI");
	public  static Any     column__        = AbstractValue.flyweightString("column");
  public  static Any     columnName__    = AbstractValue.flyweightString("columnName");
  public  static Any     mouseCell__     = AbstractValue.flyweightString("isMouseCell"); 
  public  static Any     cellX__         = AbstractValue.flyweightString("cellX");
  public  static Any     cellY__         = AbstractValue.flyweightString("cellY");
  public  static Any     cell__          = AbstractValue.flyweightString("cell");
  public  static Any     isUser__        = AbstractValue.flyweightString("isUser");
  public  static Any     after__         = AbstractValue.flyweightString("after");
	private static Any     columnGroups__  = AbstractValue.flyweightString("columnGroupings");
	private static Any     scrollable__    = AbstractValue.flyweightString("scrollable");
  private static Any     visibleRows__   = AbstractValue.flyweightString("visibleRows");
  private static Any     visibleCols__   = AbstractValue.flyweightString("visibleColumns");
	private static Any     totalsTable__   = AbstractValue.flyweightString("totalsTable");
	public  static Any     selectionMode__ = AbstractValue.flyweightString("selectionMode");
	private static Any     selectionAxis__ = AbstractValue.flyweightString("selectionAxis");
	private static Any     editColumn__    = AbstractValue.flyweightString("editColumn");
	private static Any     rootVisible__   = AbstractValue.flyweightString("rootVisible");
	private static Any     tableMode__     = AbstractValue.flyweightString("tableMode");
	private static Any     rowHeader__     = AbstractValue.flyweightString("rowHeader");
  private static Any     rowHeight__     = AbstractValue.flyweightString("rowHeight");
  private static Any     rowFunction__   = AbstractValue.flyweightString("rowFunction");
  private static Any     linkedVertical__ = AbstractValue.flyweightString("linkedVertical");
  private static Any     redraw__        = AbstractValue.flyweightString("redraw");
  private static Any     renderOnMouseMotion__ = AbstractValue.flyweightString("renderOnMouseMotion");
  private static Any     renderOnMouseMotionListener__ = AbstractValue.flyweightString("renderOnMouseMotionListener__");
  private static Any     clickSortEnabled__    = AbstractValue.flyweightString("clickSortEnabled");
  
  public static  Any     TREE_TABLE      = new ConstInt(1);
  public static  Any     FLAT_TABLE      = new ConstInt(0);

  static public Any renderer__   = AbstractValue.flyweightString("renderer");
  static public Any renderinfo__ = AbstractValue.flyweightString("renderinfo");
  static public Any editor__     = AbstractValue.flyweightString("editor");
  static public Any widths__     = AbstractValue.flyweightString("widths");
  static public Any sortable__   = AbstractValue.flyweightString("sortable");
  
  static public AnyIcon tableMenu__;

//  public AnyTable(JScrollPane s)
//  {
//		s_ = s;
//	}
//
//  public AnyTable(JTable t)
//  {
//		super(t);
//	}
//
//  public AnyTable(JTable t, Any context)
//  {
//		super(t, context);
//	}

  static
  {
    //URL arrowup      = cl.getResource("com/inqwell/any/client/arrowup.gif");
    //URL arrowdown    = cl.getResource("com/inqwell/any/client/arrowdown.gif");
//    AnyURL u = new AnyURL("classpath:///com/inqwell/any/client/arrowup.gif");
//    URL arrowup      = u.getURL();
//    u = new AnyURL("classpath:///com/inqwell/any/client/arrowdown.gif");
//    URL arrowdown    = u.getURL();
//
//    sortAscending__  = new AnyIcon(arrowdown);
//    sortDescending__ = new AnyIcon(arrowup);

    tableProperties__ = AbstractComposite.set();
    tableProperties__.add(columns__);
    tableProperties__.add(AnyTree.levels__);
    tableProperties__.add(AnyTree.expandToLevel__);
    tableProperties__.add(AnyTree.expandAll__);
    tableProperties__.add(columnGroups__);
    tableProperties__.add(AnySelection.modelSort__);
    tableProperties__.add(scrollable__);
    tableProperties__.add(visibleRows__);
    tableProperties__.add(visibleCols__);
    tableProperties__.add(totalsTable__);
    tableProperties__.add(selectionMode__);
    tableProperties__.add(selectionAxis__);
    tableProperties__.add(editColumn__);
    tableProperties__.add(rootVisible__);
    tableProperties__.add(tableMode__);
    tableProperties__.add(rowHeader__);
    tableProperties__.add(rowHeight__);
    tableProperties__.add(rowFunction__);
    tableProperties__.add(linkedVertical__);
    tableProperties__.add(redraw__);
    tableProperties__.add(renderOnMouseMotion__);
    tableProperties__.add(clickSortEnabled__);
    
    iCellRect__.add(x__, iCellRectX__);
    iCellRect__.add(y__, iCellRectY__);
    iCellRect__.add(width__, iCellRectW__);
    iCellRect__.add(height__, iCellRectH__);

    AnyURL u  = new AnyURL("classpath:///com/inqwell/any/client/tablemenu.png");
    URL    u1 = u.getURL();
    tableMenu__  = new AnyIcon(u1);
  }

	/**
	 * No-args constructor allows prototypical instances in factories
	 */
  public AnyTable()
  {
	}

	public void setObject(Object o)
	{
		if (o instanceof JPanel)
		{
			JComponent borderee = (JComponent)o;
			borderee_ = borderee;
			setObject(borderee.getComponent(0));
			return;
		}

		if (borderee_ == null)
			borderee_ = (JComponent)o;

		if ((!(o instanceof JTable)) && (!(o instanceof JScrollPane)))
			throw new IllegalArgumentException
									("AnyTable wraps com.inqwell.any.client.swing.JTable/JScrollPane and sub-classes");


		if (o instanceof JTable)
		{
			t_ = (JTable)o;
		}
		else
		{
			s_ = (JScrollPane)o;
			t_ = (JTable)s_.getViewport().getView();
			t_.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
			t_.setPreferredScrollableViewportSize(t_.getPreferredSize());
//			t_.setPreferredScrollableViewportSize(new Dimension(300,300));
//			t_.setAlignmentX(0);
		}
    
    // Set wrapper into JTable
    t_.setAnyTable(this);

    // Initially we assume we are flat
		model_ = (TableModel)t_.getModel();
    flatModel_ = model_;
    flatModel_.setTable(this);

  	setScrollable(true);
		super.setObject(t_);
    t_.setSurrendersFocusOnKeystroke(true);

		// Tables must be rendering something so do the data
		// event listening stuff here.
		// The RenderInfo objects in the TableModel will already
		// be set up with their node specs.
		//setupDataEvents();

		setupCopy();

		t_.getTableHeader().addMouseListener(new SortMouseListener());

		setupHeaderRenderers(t_.getFont());
	}

	public Container getComponent()
	{
    /*
		if (s_ != null)
			return s_;
		else
			return t_;
    */
    
    return t_;
	}

	public JComponent getBorderee()
	{
    if (borderee_ == null)
      return t_;
    
		return borderee_;
	}

	public void evaluateContext()
	{
    super.evaluateContext();

    AnyTable totalsTable = (AnyTable)getTotalsTable();
    if (totalsTable != null)
      totalsTable.evaluateContext();

		model_.setContext(getContextNode());
	}

  protected Object getScroller()
  {
    return s_;
  }
  
	protected Object getPropertyOwner(Any property)
	{
		if (tableProperties__.contains(property))
		  return this;

		return super.getPropertyOwner(property);
	}

	public void setScrollable(JScrollPane o)
	{
		//System.out.println("AnyTable.setScrollable: " + o);
		if (!(o instanceof JScrollPane))
			throw new IllegalArgumentException
									("setScrollable not a javax.swing.JScrollPane");


		s_ = (JScrollPane)o;

		if (t_ != null)
		{
			s_.setViewportView(t_);
			t_.setPreferredScrollableViewportSize(t_.getPreferredSize());
		}
	}

  public void setColumns(Vectored columns)
  {
    // Receive a vector of RenderInfo objects.  Because it is
    // so typed the parameter could be a concrete array or a
    // concrete map.  If it is a map then the columns become
    // known by the map's key names for future use in:
    //   1) mouse events delivered to users (handleBoundEvent)
    //   2) property access to various column attributes.
    // If it is an array then these features are not available
    // and the columns are used only to initialise the table.

    model_.setColumns((Vectored)columns);

    Font widthGauge = t_.getFont();

    boolean totalsTable = false;

    if (getParentComponent() instanceof AnyTable &&
        this.getNameInParent().equals(totalsTable__))
    {
      // We have a parent table, and we are a totals table.
      // Because we may have a bolded font use that of the parent
      // table to calculate the column widths or they won't be
      // the same.
      AnyTable t = (AnyTable)getParentComponent();
      widthGauge = t.t_.getFont();

      // Tie these together, so that column resize via the header
      // of the main table is reflected in the totals table
      t_.setColumnModel(t.t_.getColumnModel());

      totalsTable = true;
    }

    t_.reinit((TableModel)t_.getModel(), widthGauge);

    // See if we can remember the columns for use in selection events
    rememberColumns(columns);

    if (!totalsTable)
      setupHeaderRenderers(widthGauge);

    setupDataEvents();

  }

  public Vectored getColumns()
  {
    // Only returns non-null if we were successful in
    // rememberColumns()
    return model_.getColumnProperties();
  }
  
  public void setRowFunction(Any rowFuncF)
  {
    model_.setRowFunction(rowFuncF);
    setupDataEvents();
  }
  
  public void setRedraw(boolean redraw)
  {
    if (redraw)
    {
      try
      {
        model_.translateEvent(null, t_, null);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
  
  public void setRowHeight(int rowHeight)
  {
    t_.setRowHeight(rowHeight);
  }

  public int getRowHeight()
  {
    return t_.getRowHeight();
  }

  public void setRowHeader(AnyComponent rowHeader) throws AnyException
  {
    // Must be scrollable
    if (s_ == null)
      throw new AnyRuntimeException("Table is not scrollable");


    if (rowHeader instanceof AnyTable)
    {
      AnyTable rht = (AnyTable)rowHeader;
      
      // Row header tables aren't, themselves, scrolled
      rht.setScrollable(false);
      
      // See AnyTable.setScrollable - the table is put into a JPanel (for
      // bordering purposes if I remember). We don't want this in the rowheader
      // case.
      //rht.borderee_.removeAll();
      //rht.borderee_ = null;
      
      if (this.getModelRoot() != null)
        rht.setModelRoot(this.getModelRoot());

      s_.setRowHeaderView((Component)rowHeader.getAddee());
      s_.setCorner(JScrollPane.UPPER_LEFT_CORNER, rht.t_.getTableHeader());
    }
    else
      s_.setRowHeaderView((Component)rowHeader.getAddee());

    this.add(rowHeader__, rowHeader);
  }

  public AnyComponent getRowHeader()
  {
    if (this.contains(rowHeader__))
      return (AnyComponent)this.get(rowHeader__);

    return null;
  }
  
  public boolean getRenderOnMouseMotion()
  {
    return getIfContains(renderOnMouseMotionListener__) != null;
  }
  
  public void setRenderOnMouseMotion(boolean renderOnMouseMotion)
  {
    boolean renderingOnMouseMotion = getRenderOnMouseMotion();
    
    if (renderOnMouseMotion && !renderingOnMouseMotion)
    {
      // Must call super - see override in this class...
      MotionRenderingListener m = new MotionRenderingListener();
      super.addAdaptedEventListener(m, null);
      this.add(renderOnMouseMotionListener__, m);
    }
    else if (!renderOnMouseMotion && renderingOnMouseMotion)
    {
      MotionRenderingListener m = (MotionRenderingListener)this.remove(renderOnMouseMotionListener__);
      removeAdaptedEventListener(m.exitListener_);
      removeAdaptedEventListener(m);
      lastRow_    = -1;
      lastColumn_ = -1;
    }
  }
  
  /**
   * Returns whether the specified cell (in model coordinates) is the
   * current mouse-over cell. Only valid when renderOnMouseMotion is
   * in effect.
   * @param row
   * @param col
   * @return
   */
  public boolean isMouseCell(int row, int col)
  {
    return row == lastRow_ && col == lastColumn_ && lastRow_ >= 0;
  }

  public void setRootVisible(boolean rootVisible)
  {
    if (treeModel_ != null)
    {
      treeModel_.getTreeRenderer().setRootVisible(rootVisible);
    }
  }

  public Map getLevels()
  {
    if (model_ == treeModel_)
      return model_.getLevels();

    throw new AnyRuntimeException("Not a TreeTable");
  }

  public void setLevels(Map treeLevels)
  {
    // Establish a treetable configuration.

    // Process the levels config map
    Map       namedLevels  = AbstractComposite.simpleMap();
    TreeLevel levels       = AnyTree.processTreeLevels(treeLevels,
                                                       getContextNode(),
                                                       this,
                                                       namedLevels);

    AnyTreeTableModel attm = getTreeTableModel();

    // Put the levels in.  Causes a structure event on the tree root
    attm.setLevels(levels,
                   namedLevels);

    // This does a JTable.setModel().  If the model object really
    // changes (i.e. we are going from flat to tree) the
    // JTable is cleared down.
    //treeModel_ = t_.reinit(treeModel_, attm, t_.getFont());

    // Local state
    //model_ = treeModel_;
    treeModel_.setContext(getContextNode());

    //setupDataEvents(); TODO: why not?
  }
  
  public void setClickSortEnabled(boolean enabled)
  {
    clickSortEnabled_ = enabled;
    if (!enabled)
    {
      // If disabling then cancel any sort currently in effect
      orderItems_ = null;
      t_.getTableHeader().repaint();
    }
  }
  
  public boolean isClickSortEnabled()
  {
    return clickSortEnabled_;
  }

  public void setExpandToLevel(Any expandToLevel)
  {
    AnyTreeTableModel attm = getTreeTableModel();
    attm.setExpandToLevel(expandToLevel);
  }

  public void setExpandAll(boolean expandAll)
  {
    AnyTreeTableModel attm = getTreeTableModel();
    attm.setExpandToLevelAll(expandAll);
  }

  /**
   * Return the column name for the column index, in model coordinates.
   */
  public Any getColumnName(int col)
  {
    Vectored v = null;
    if ((v = model_.getColumnProperties()) == null)
      throw new AnyRuntimeException("No column names available");

    ColumnProperty c = (ColumnProperty)v.getByVector(col);

    return c.columnName_;
  }

  public int getColumnIndex(Any name)
  {
    Vectored v = null;
    if ((v = model_.getColumnProperties()) == null)
      throw new AnyRuntimeException("No column indices available");

    return v.indexOf(name);
  }

  RenderInfo getColumnRenderInfo(Any name)
  {
    int idx = getColumnIndex(name);

    // Use the prevailing model?

    return model_.getRenderInfo(idx);
  }

  // Get the renderinfo for the specified table column in model
  // coordinates.
  RenderInfo getColumnRenderInfo(int col)
  {
    return model_.getRenderInfo(col);
  }
  
  int getRealRowCount()
  {
    return model_.getRealRowCount();
  }

  public void setColumnGroupings(Map groups)
  {
    // Note, these cannot be got back out yet.
    // Columns must be set first.
    // Each child of 'groups' is a map containing a column grouping
    if (model_.getColumnProperties() == null)
      throw new AnyRuntimeException("Please set named columns before groups");

    processColumnGroups(groups,
                        (GroupableTableColumnModel)t_.getColumnModel());

  }

  public void setVisibleRows(int visibleRows)
  {
    if (visibleRows < 1)
      throw new IllegalArgumentException("minimum visible rows is 1");

    Dimension d = t_.getPreferredScrollableViewportSize();
    Dimension dNew = new Dimension(d);

    int h = t_.getRowHeight() * visibleRows;
    h += t_.getTableHeader().getPreferredSize().height;

    dNew.height = h;

    t_.setPreferredScrollableViewportSize(dNew);

    model_.setVisibleRows(visibleRows);
  }

  public void setVisibleColumns(int visibleColumns)
  {
    Dimension d = t_.getPreferredScrollableViewportSize();
    Dimension dNew = new Dimension(d);

    int w =   getTotalColumnWidth(visibleColumns);

    dNew.width = w;

    //t_.setPreferredSize(dNew);
    t_.setPreferredScrollableViewportSize(dNew);
  }

  public int getVisibleRows()
  {
    return model_.getVisibleRows();
  }

  /**
   * Sets the <i>scrollable</i> property.  This is a
   * <i>synthetic property</i> provided by <code>inq</code>
   * that can only be set prior to layout. Setting this
   * property once the table has been placed in the
   * <code>awt</code> component hierarchy will have
   * undefined results.
   */
  public void setScrollable(boolean scrollable)
  {
  	if (s_ == null && scrollable)
  	{
  		if (borderee_ != null && borderee_ != t_)
  		  borderee_.remove(t_);

  	  s_ = new JScrollPane(t_);
  	  borderee_ = s_;
			t_.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
			t_.setPreferredScrollableViewportSize(t_.getPreferredSize());
      
      /*
       * To be added for user config of table properties
      JButton b = new JButton();
      b.setMargin(noMargin__);
      b.setIcon(tableMenu__.getIcon());
      b.setFocusable(false);
      s_.setCorner(JScrollPane.UPPER_RIGHT_CORNER, b);
      s_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      */
  	}
  	else if (s_ != null && !scrollable)
  	{
  		s_.setViewportView(null);
  	  s_ = null;
  	  borderee_ = new JPanel();
      borderee_.setLayout(new BorderLayout());
  	  borderee_.add(t_, BorderLayout.CENTER);
  	}
  }

  public boolean getScrollable()
  {
  	return s_ != null;
  }

  /**
   * Sets the <i>totalsTable</i> property.  This is a
   * <i>synthetic property</i> provided by <code>inq</code>
   * that can only be set prior to layout. Setting this
   * property once the table has been placed in the
   * <code>awt</code> component hierarchy will have
   * undefined results.
   */
  public void setTotalsTable(Any t)
  {
    if (t == this)
      throw new IllegalArgumentException("Must be a different table");
    
    if (!(t instanceof AnyTable))
      throw new IllegalArgumentException("Not a table");

    AnyTable totalsTable = (AnyTable)t;

    // Combined with totals, we must be scrollable.
    this.setScrollable(true);

    // Set up desired properties in tables and link the horizontal scrolling
    t_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    totalsTable.t_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    totalsTable.t_.setSelectionMode(t_.getSelectionModel().getSelectionMode());

    // The totals table is responsible for displaying the horizontal scroll bar
    s_.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // The main table always displays a vertical scroll so the two
    // tables always look good
    s_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    JScrollPane fixedScroll = new JScrollPane()
                              {
                                public void setColumnHeaderView(Component view) {}
                              };

    totalsTable.setScrollable(fixedScroll);
    fixedScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

/*
    JScrollBar bar = fixedScroll.getVerticalScrollBar();
    JScrollBar dummyBar = new JScrollBar() {
      public void paint(Graphics g) {}
    };
    dummyBar.setPreferredSize(bar.getPreferredSize());
    fixedScroll.setVerticalScrollBar(dummyBar);
*/
    final JScrollBar bar1 = s_.getHorizontalScrollBar();
    JScrollBar bar2 = fixedScroll.getHorizontalScrollBar();
    bar2.addAdjustmentListener(new AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        bar1.setValue(e.getValue());
      }
    });

    Dimension d = new Dimension(s_.getPreferredSize());
    int h = totalsTable.getModel().getRowCount() * totalsTable.t_.getRowHeight() +
                                                   2 * totalsTable.t_.getRowMargin();
    d.height = h;
    d.height += bar2.getPreferredSize().height;
    fixedScroll.setPreferredSize(d);  // Hmm...

    borderee_ = new JPanel(new BorderLayout());
    borderee_.add(s_, BorderLayout.CENTER);
    borderee_.add(fixedScroll, BorderLayout.SOUTH);

    this.add(totalsTable__, totalsTable);
  }

  /**
   * Set table <code>t</code> as the vertical scroller for <code>this</code>.
   * Has the effect of
   * <ul>
   * <li>//suppressing the vertical scroll bar of <code>this</code></li>
   * <li>listening to the vertical scroll bar of <code>t</code> and
   * applying its value to <code>this</code>.</li>
   * </ul>
   * Todo: Leave both v scroll visible. Perhaps we could make these accessible
   * via properties so that they can be made visible or otherwise from
   * script?
   * @param t
   */
  public void setLinkedVertical(Any t)
  {
    if (!(t instanceof AnyTable))
      throw new IllegalArgumentException("Not a table");

    AnyTable linkedTable = (AnyTable)t;
    
    // Do nothing unless both tables are scrollable
    if(s_ == null || linkedTable.s_ == null)
      return;
    
    final JScrollBar bar1 = this.s_.getVerticalScrollBar();
    final JScrollBar bar2 = linkedTable.s_.getVerticalScrollBar();
    
    bar2.addAdjustmentListener(new AdjustmentListener()
    {
      public void adjustmentValueChanged(AdjustmentEvent e)
      {
        bar1.setValue(e.getValue());
      }
    });
    
    bar1.addAdjustmentListener(new AdjustmentListener()
    {
      public void adjustmentValueChanged(AdjustmentEvent e)
      {
        bar2.setValue(e.getValue());
      }
    });


  }
  
  public Any getTotalsTable()
  {
    return getIfContains(totalsTable__);
  }

	public void newSelection(Event e)
	{
    Any levelSelection = model_.newSelection(t_.getSelectionModel(),
                                             selection_,
                                             keySelection_,
                                             indexSelection_,
                                             selectCount_);

    if (levelSelection != null)
      modelVars_.replaceItem(levelSelection__, levelSelection);
	}

  /**
   * Return the current key selection as a shallow copy of the
   * model we maintain. Can be used later in setItemSelection
   */
	public Any getItemSelection()
	{
		return keySelection_.shallowCopy();
	}
  
  public Any getIndexSelection()
  {
    return indexSelection_.shallowCopy();
  }

	public void setItemSelection(Any selection)
	{
		t_.clearSelection();

    Array a;
    if (!(selection instanceof Array))
    {
      a = AbstractComposite.array();
      a.add(selection);
    }
    else
      a = (Array)selection;

    if (a.entries() != 0)
    {
      model_.setItemSelection(t_.getSelectionModel(), a, selection_);
    }
	}

  public void setIndexSelection(Any selection)
  {
    t_.clearSelection();

    if (selection instanceof Array)
    {
      Array a = (Array)selection;

      if (a.entries() != 0)
      {
        model_.setIndexSelection(t_.getSelectionModel(), a, selection_);
      }
    }
  }

	public boolean setEditColumn(Any columnName)
	{
    // Set up the editing column.  There must be a selected
    // row (the first of) which defines the editing row

    t_.resetEditor();

    // If there's no selected row then silently quit.
    final int row = t_.getSelectedRow();
    if (row < 0)
      return false;

    // Must have remembered the column names
    Vectored v = null;
    if ((v = model_.getColumnProperties()) == null)
      throw new AnyRuntimeException("Columns not established");

    final int column = t_.convertColumnIndexToView(v.indexOf(columnName));
    if (column < 0)
      throw new AnyRuntimeException("No such column " + columnName);

    System.out.println("Editing cell at " + row + " " + column);

    //System.out.println(" R1 " + t_.getCellRect(row, column, true));
    //System.out.println(" R2 " + t_.getCellRect(row, column, false));
    t_.requestFocus();
    /*
    t_.changeSelection(row, column, false, false);
    */
    boolean ret = false;
    if (ret = t_.editCellAt(row, column))
    {
      t_.getEditorComponent().requestFocusInWindow();
      ListSelectionModel rsm = t_.getSelectionModel();
      ListSelectionModel csm = t_.getColumnModel().getSelectionModel();
      rsm.setLeadSelectionIndex(row);
      csm.setLeadSelectionIndex(column);
      rsm.setAnchorSelectionIndex(row);
      csm.setAnchorSelectionIndex(column);
    }

    /*
    SwingUtilities.invokeLater(new Runnable()
                               {
                                 public void run()
                                 {
                                 }
                               });
  */
    //clickEditor(t_.getCellRect(row, column, true));

    return ret;
	}

  /*
	public void setEditable(boolean isEditable)
	{
		if (isEditable)
		  setupPaste();
	}
*/

  public void addAdaptedEventListener(EventListener l, Any eventParam)
  {
    // Slightly messy but for M_MOVED (mouse motion) events we ensure
    // the MotionRenderingListener is established first. This is because
    // the values used in handleBoundEvent (lastRow_ and lastColumn_)
    // are maintained by this lisetener. Note, though, that if script
    // subsequently set renderOnMouseMotion to false the functionality
    // of dispatching only when the containing cell changed would be lost
    // and events would be delivered according to Java movement (i.e. much
    // more rapidly). This would be a no-no as documented behaviour would
    // cease! Note that Inq guarantees that adapted event dispatchers are
    // messaged in the order they were added.
    if (AbstractEvent.isDispatchingBaseType(l, EventConstants.M_MOVED))
    {
      setRenderOnMouseMotion(true);
    }
    super.addAdaptedEventListener(l, eventParam);
  }
  
  public void addAdaptedEventListener(EventListener l)
  {
    addAdaptedEventListener(l, null);
  }
	
  public Object getAddee()
	{
		return getBorderee();
	}

	public void setSelectionMode(int selectionInterval)
	{
		setSelectionMode(selectionInterval,
                     MakeComponent.rowSelection__.getValue());
	}

  public void setSelectionAxis(IntI axis)
  {
		setSelectionMode(t_.getSelectionModel().getSelectionMode(),
                     axis.getValue());
  }

	private void setSelectionMode(int selectionInterval, int selectionMode)
	{
		t_.getSelectionModel().setSelectionMode(selectionInterval);
		t_.setRowSelectionAllowed(false);
		t_.setColumnSelectionAllowed(false);
		t_.setCellSelectionEnabled(false);

		if (selectionMode == MakeComponent.rowSelection__.getValue() ||
				selectionMode == MakeComponent.cellSelection__.getValue())
		{
			t_.setRowSelectionAllowed(true);
		}
		else if (selectionMode == MakeComponent.columnSelection__.getValue() ||
						 selectionMode == MakeComponent.cellSelection__.getValue())
		{
			t_.setColumnSelectionAllowed(true);
		}

		if (selectionMode == MakeComponent.cellSelection__.getValue())
		{
			t_.setCellSelectionEnabled(true);
		}
	}

  public void setModelRoot(Any newRoot) throws AnyException
  {
    // Implies the flat model, because the tree levels stuff contains
    // all the references in that case.

    // Pass on to model
    NodeSpecification n = null;
    LocateNode l = null;
    if (newRoot instanceof NodeSpecification)
    {
    	n = (NodeSpecification)newRoot;
      n = n.resolveIndirections(getContextNode(), Globals.process__.getTransaction());
      l = new LocateNode(n);
    }
    else
      throw new AnyRuntimeException("Not a path");

    flatModel_.setContext(getContextNode());
    flatModel_.setModelRoot(l, t_);

    //if (model_ == flatModel_) TODO: why was this anyway?
      setupDataEvents();

    if (this.contains(rowHeader__))
    {
      Any rowHeader = this.get(rowHeader__);
      if (rowHeader instanceof AnyTable)
      {
        AnyTable rht = (AnyTable)rowHeader;
        rht.setModelRoot(newRoot);
      }
    }
  }

  public void setMaxRenderedWidth(int column, int width)
  {
    // Determine whether the given width is larger than
    // that seen previously and if so remember it for use
    // when springing the columns to a size suitable to
    // display the longest item.
  	if (maxWidths_ != null)
  	{
	    AnyInt i = (AnyInt)maxWidths_.get(column);
	    if (i.getValue() < width)
	      i.setValue(width);
  	}
  }

  public int getMaxRenderedWidth(int column)
  {
  	// Totals tables don't use this
  	if (maxWidths_ != null)
  	{
	    AnyInt i = (AnyInt)maxWidths_.get(column);
	    return i.getValue();
  	}
  	return 0;
  }

  /**
   * Toggle between Tree and Flat modes.  There must be a
   * prevailing model for the chosen mode.
   */
  public void setTableMode(Any mode)
  {
    if (mode.equals(TREE_TABLE))
    {
      if (treeModel_ == null)
        getTreeTableModel();

      if (model_ == treeModel_)
        return;

      //AnyTreeTableModel attm = (AnyTreeTableModel)treeModel_.getTreeModel();
      //t_.reinit(treeModel_, attm, t_.getFont());
      t_.reinit(treeModel_, t_.getFont());
      model_ = treeModel_;
    }
    else if (mode.equals(FLAT_TABLE))
    {
      if (model_ == flatModel_)
        return;

      t_.reinit(flatModel_, t_.getFont());
      model_ = flatModel_;
    }
    else
      throw new AnyRuntimeException("Unknown table mode");

    setupHeaderRenderers(t_.getFont());
    setupDataEvents();
  }

  public Any getModelRoot()
  {
  	return model_.getModelRoot();
  }

  // Override AnyComponent and forward
	public void setEditable(boolean editable)
	{
    t_.setEditable(editable);
	}

	public boolean isEditable()
	{
    return t_.isEditable();
	}

  public Array getPrefSize()
  {
    Array size = AbstractComposite.array(2);
    Dimension d = t_.getPreferredScrollableViewportSize();
    
    size.add(new AnyInt(d.width));
    size.add(new AnyInt(d.height));
    return size;
  }
  
  public void setMaxSize(Array size)
  {
    IntI wI = (IntI)size.get(0);
    IntI hI = (IntI)size.get(1);
    int w = wI.getValue();
    int h = hI.getValue();
    Dimension d = new Dimension(w, h);
    t_.setMaximumSize(d);
    //validate();
  }

  protected void applySort(AnyComparator c)
  {
    model_.sort(c);
    AnyTable totalsTable = (AnyTable)getTotalsTable();
    if (totalsTable != null)// && totalsTable.isClickSortEnabled())
      totalsTable.getModel().sort(c);
  }

  public TableModel getModel()
  {
  	return model_;
  }

	/**
	 * Place the selected cells as textual, tabular data on the clipboard
	 */
	public void copy()
	{
		try
		{
			StringBuffer clipboardString = new StringBuffer();

			// Check to ensure we have selected only a contiguous block of
			// cells
	//		int numcols=jTable1.getSelectedColumnCount();
	//		int numrows=jTable1.getSelectedRowCount();
	//		int[] rowsselected=jTable1.getSelectedRows();
	//		int[] colsselected=jTable1.getSelectedColumns();
	//
	//		if (!((numrows-1==rowsselected[rowsselected.length-1]-rowsselected[0] &&
	//		numrows==rowsselected.length) &&
	//		(numcols-1==colsselected[colsselected.length-1]-colsselected[0] &&
	//		numcols==colsselected.length)))
	//		{
	//			JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
	//				"Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
	//			return;
	//		}

			boolean editable = false;

			// determine the selection range in the table.
			// Rows are start to end + 1
			int firstRow = getMinSelectedRow(false);
			int lastRow  = getMaxSelectedRow(false);

			// Cols are start to end inclusive
			int firstCol = getMinSelectedColumn(firstRow, editable);
			int lastCol  = getMaxSelectedColumn(firstRow, editable);

			// We can't copy if there are no rows with selected cells
			if ((lastRow - firstRow) == 0)
				return;

      // Do the headers
      for (int column = firstCol; column <= lastCol; column++)
      {
        clipboardString.append(t_.getColumnName(column));
        if (column < lastCol)
          clipboardString.append("\t");
      }
      clipboardString.append("\n");


			for (int row = firstRow; row < lastRow; row++)
			{
				for (int column = firstCol; column <= lastCol; column++)
				{
					if (!t_.isCellSelected(row, column))
						continue;

					Any cellValue = (Any)t_.getValueAt(row, column);

          if (cellValue != null)
            clipboardString.append(cellValue.toString());

					if (column < lastCol)
						clipboardString.append("\t");
				}
				clipboardString.append("\n");
			}

			StringSelection stsel = new StringSelection(clipboardString.toString());
			java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stsel, stsel);
		}
		catch (Exception e)
		{
			throw new RuntimeContainedException(e);
		}
	}

	/**
	 * Delete any rows which have selected cells on them
	 */
   /*
	public void remove()
	{
		try
		{
			// determine the selection range in the table.
			// Rows are start to end + 1
			int firstRow = getMinSelectedRow(false);
			int lastRow  = getMaxSelectedRow(false);

			if ((lastRow - firstRow) == 0)
				return;

			int removeAt = firstRow;

			for (int removeCount = firstRow;
					 removeCount < lastRow;
					 removeCount++)
			{
				model_.removeRow(removeAt);
			}
			// Just take the easy way out for now.  Maybe we should work out what's
			// been updated, what's been inserted etc.
			model_.fireTableDataChanged();
		}
		catch (Exception e)
		{
			throw new RuntimeContainedException(e);
		}
	}
  */

	/**
	 * Paste data on the clipboard into the table.  The system clipboard
	 * must contain tabular data which this method attempts to put into
	 * the table.  If there is suitable data on the clipboard it is pasted
	 * as follows:
	 * <ul>
	 * <li>If there is no selection the data is pasted on to the the end
	 * of the table model.
	 * <li>If there is a selection the data overwrites it until the
	 * selected area is completed; if there are extra rows these are
	 * inserted after the selection; if there are fewer rows the remaining
	 * selection is deleted.
	 * <li>Only editable cells are pasted to.  If there is a selection and
	 * there are more columns on the clipboard than editable ones in the
	 * selection the remaining columns are ignored.  If there is no selection
	 * and there are more columns on the clipboard than editable ones
	 * in the table then, again, they are ignored.
	 * <li>Insertion and deletion in the table only happens on a row basis
	 * via the paste function.  Columns have to be inserted manually prior
	 * to pasting.
	 * <li>Any table selection is assumed to be a single rectangle of cells
	 * so configure your selection possibilities to only permit this!
	 * </ul>
	 */
   /*
	public void paste()
	{
		try
		{
			Clipboard clipboard = new Clipboard();
			int clipboardRows = clipboard.entries();
			int clipboardCols = 0;

			System.out.println ("Pasting... " + clipboard);
			if (clipboardRows > 0)
			{
				Vectored row0 = (Vectored)clipboard.getByVector(0);
				clipboardCols = row0.entries();

				if (clipboardCols == 0)
					return;

				boolean editable = true;

				// determine the selection range in the table.
				// Rows are start to end + 1
				int firstRow = getMinSelectedRow(true);
				int lastRow  = getMaxSelectedRow(true);

				// Cols are start to end inclusive
				int firstCol = getMinSelectedColumn(firstRow, editable);
				int lastCol  = getMaxSelectedColumn(firstRow, editable);

				// We can't paste if there are no editable columns, selected
				// or otherwise.
				if (lastCol < 0)
					return;

				int colsToPaste = getNumPastable(firstCol, lastCol, editable);
				System.out.println ("NumPastable: " + firstCol + ", " + lastCol + ", " + colsToPaste);

				// If there aren't any pastable columns then nothing to do
				if (colsToPaste == 0)
					return;

				if (clipboardCols < colsToPaste)
					colsToPaste = clipboardCols;

				if (((firstRow + clipboardRows) > model_.getRealRowCount()) ||
						(clipboardRows > (lastRow - firstRow + 1)) ||
						(model_.getRealRowCount() == 0))
				{
					// Model must be capable of expansion
					for (int j = 0; j < model_.getColumnCount(); j++)
					{
						if (!model_.isBuildable(j))
						{
							// show error via gui
							return;
						}
					}
				}

				// Finally ready to do pasting
				int pasteRow = 0;
				for (int i = 0; i < clipboardRows; i++)
				{
					Vectored clipboardRow = (Vectored)clipboard.getByVector(i);
					pasteRow = firstRow + i;

					Map rowRoot = null;

					if ((pasteRow >= lastRow) || (model_.getRealRowCount() == 0))
					{
						System.out.println ("Paste: inserting new row ");
						rowRoot = model_.insertRow(pasteRow);
					}
					else
					{
						System.out.println ("Paste: getting row " + pasteRow);
						rowRoot = (Map)model_.getRowAt(pasteRow);
					}

					int pasted = 0;

					for (int pasteCol = firstCol;
							 (pasteCol <= lastCol) && (pasted < colsToPaste);
							 pasteCol++)
					{
						if (editable && !model_.isCellEditable(pasteRow, pasteCol))
							continue;

						Any clipboardCell = clipboardRow.getByVector(pasted);

						System.out.println ("Pasting " + clipboardCell + " to " + pasteRow + ", " + pasteCol);

						model_.pasteCell(pasteRow,
														 pasteCol,
														 clipboardCell);
						pasted++;
					}
				}

				if (clipboardRows < lastRow - firstRow)
				{
					int removeAt = firstRow + clipboardRows;

					for (int removeCount = firstRow + clipboardRows;
							 removeCount < lastRow;
							 removeCount++)
					{
						model_.removeRow(removeAt);
					}
				}
			}

			// Just take the easy way out for now.  Maybe we should work out what's
			// been updated, what's been inserted etc.
			model_.fireTableDataChanged();
		}
		catch (Exception e)
		{
			throw new RuntimeContainedException(e);
		}
	}
*/
/*
	public void setupPaste()
	{
    KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,
																						 ActionEvent.CTRL_MASK,
																						 false);
		t_.getInputMap().put(paste, paste__);
		t_.getActionMap().put(paste__,
												  new AbstractAction()
													{
														public void actionPerformed(ActionEvent e)
														{
															paste();
														}
													});

    KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X,
																					 ActionEvent.CTRL_MASK,
																					 false);
		t_.getInputMap().put(cut, cut__);
		t_.getActionMap().put(cut__,
												  new AbstractAction()
													{
														public void actionPerformed(ActionEvent e)
														{
															copy();
															remove();
														}
													});

    KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
																							0,
																							false);
		t_.getInputMap().put(delete, delete__);
		t_.getActionMap().put(delete__,
												  new AbstractAction()
													{
														public void actionPerformed(ActionEvent e)
														{
															remove();
														}
													});
	}
*/

	public void setupCopy()
	{
    KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
		t_.getInputMap().put(copy, copy__);
		t_.getActionMap().put(copy__,
												  new AbstractAction()
													{
														public void actionPerformed(ActionEvent e)
														{
															copy();
														}
													});
	}
  
  public Any getViewProperties()
  {
    Map m = AbstractComposite.orderedMap();
    
    // TODO: visibility/ordering
    
    Vectored v = getColumns();
    if (v != null)
    {
      for (int i = 0; i < v.entries(); i++)
      {
        ColumnProperty cp = (ColumnProperty)v.getByVector(i);
        m.add(cp.getColumnName(), cp.toMap());
      }
    }
    
    return m;
  }
  
	/**
	 * Override base functionality.  We adapt the inq event to a table
	 * event.
	 */
	protected void componentProcessEvent(Event e) throws AnyException
	{
		if (model_.translateEvent(e, t_, getTreeRenderer()))
      resetMaxRenderedWidths();
		
		if (getRealRowCount() != rowCount_.getValue())
		{
		  rowCount_.setValue(getRealRowCount());
		  Transaction t = Globals.getProcessForCurrentThread().getTransaction();
		  boolean active = t.isActive();
      t.copyOnWrite(modelVars_);
      t.fieldChanging(modelVars_, rowCount__, null);
      
      // Commit here to dispatch the node event to listeners. Generally this
      // does not happen further up the stack at the moment so do it here
      // to handle this particular case. If more such cases are added then
      // consider moving to the RenderingListener
      if (!active)
        t.commit();
		}
	}

  JTree getTreeRenderer()
  {
    if (treeModel_ != null && model_ == treeModel_)
      return treeModel_.getTreeRenderer();

    return null;
  }

	protected Object getAttachee(Any eventType)
	{
		if (eventType.equals(ListenerConstants.LISTSELECTION))
			return t_.getSelectionModel();
//    else if (eventType.equals(ListenerConstants.TABLEEDIT))
//      return t_.getEditGenerator();
    else if (eventType.equals(ListenerConstants.TREEEXPANSION))
    {
      // Must have a tree table
      if (treeModel_ != null)
        return treeModel_.getTreeRenderer();
      else
        throw new AnyRuntimeException("Not configured for TreeTable use");
    }
		else
			return super.getAttachee(eventType);
	}

  protected boolean handleBoundEvent(Event e)
  {
    // Hmmm... a lot of functionality in one method - may be bad...

    if (e.getUnderlyingEvent() instanceof MouseEvent)
    {
      if ((model_.getRealRowCount() == 0))
        return false;
      
      // For mouse events on a table, try to establish the row and column
      // for the event and also the column name, if there is are
      // column properties established.
      MouseEvent me = (MouseEvent)e.getUnderlyingEvent();
      Point p = me.getPoint();
      int eventRow    = t_.rowAtPoint(p);
      
      // If the event is not on a row then veto it. Seems like the
      // right thing to do. Allow one such case through so that
      // listeners have the opportunity to note they have left the building
      if (lastRow_ < 0 &&
          eventRow < 0 &&
          me.getID() == MouseEvent.MOUSE_MOVED)
      {
        return false;
      }
      
      int viewColumn   = t_.columnAtPoint(p);
      int eventColumn  = t_.convertColumnIndexToModel(viewColumn);
      
      // For mouse motion/drag only deliver the event to Inq script if the
      // cell has changed
      int mouseId = me.getID();
      if ((mouseId == MouseEvent.MOUSE_MOVED ||
           mouseId == MouseEvent.MOUSE_DRAGGED) &&
          eventColumn == lastColumn_ &&
          eventRow    == lastRow_)
        return false;
      
      // Its ok to reuse these - code is single-threaded!
      eventRow__.setValue(eventRow);
      eventColumn__.setValue(eventColumn);

      e.add(row__, eventRow__);
      e.add(column__, eventColumn__);
      
      
      // These are all view coordinates. The cell rectangle is a
      // convenience
      iX__.setValue(me.getX());
      iY__.setValue(me.getY());
      e.add(x__, iX__);
      e.add(y__, iY__);
      
      // There are some things that are not possible without a row
      if (eventRow >= 0)
      {
        e.add(rowRoot__,  model_.getRowAt(eventRow));
        
        Rectangle cellRect = t_.getCellRect(eventRow,
            viewColumn,
            false);

        iCellX__.setValue(me.getX() - cellRect.x);
        iCellY__.setValue(me.getY() - cellRect.y);
        e.add(cellX__, iCellX__);
        e.add(cellY__, iCellY__);
        
        iCellRectX__.setValue(cellRect.x);
        iCellRectY__.setValue(cellRect.y);
        iCellRectW__.setValue(cellRect.width);
        iCellRectH__.setValue(cellRect.height);
        e.add(cell__, iCellRect__);
        
        // If we are a treetable then try to pass the level
        if (model_ == treeModel_)
        {
          TreeLevel level = model_.getTreeLevel(eventRow);
          e.add(AnyTree.levelArg__, level);
        }
      }
      
      // If there are columns remembered then put in the name of the event
      // column (if possible)
      Vectored v;
      if ((v = model_.getColumnProperties()) != null && eventColumn >= 0)
      {
        ColumnProperty cp =
          (ColumnProperty)v.getByVector(eventColumn);
        e.add(columnName__, cp.columnName_);
        //System.out.println("COLUMN NAME : " + cp.columnName_);
      }

    }

    // TODO. Retire. This functionality is now handled by functions
    // that script can establish on a per column (that is editor) basis.
    if (e.getUnderlyingEvent() instanceof TableEditEvent)
    {
      // The TableEditEvent event carries data that we would like
      // to make available in the Inq event
      TableEditEvent te = (TableEditEvent)e.getUnderlyingEvent();
      int eventColumn = te.getColumn();
      if (eventColumn >= 0)
      {
        // valid column means other things are OK too
        eventColumn__.setValue(eventColumn);
        eventRow__.setValue(te.getRow());
        isBefore__.setValue(te.isBefore());
        isUI__.setValue(te.isUI());
        e.add(column__, eventColumn__);
        e.add(row__, eventRow__);
        Vectored v;
        if ((v = model_.getColumnProperties()) != null)
        {
          ColumnProperty cp =
            (ColumnProperty)v.getByVector(eventColumn);
          e.add(columnName__, cp.columnName_);
          e.add(Descriptor.old__, te.getOldValue());
          e.add(Descriptor.new__, te.getNewValue());
          e.add(rowRoot__, te.getRowRoot());
          e.add(rowKey__, te.getRowKey());
          e.add(before__, isBefore__);
          e.add(uI__, isUI__);
        }
      }
    }

    return true;
  }

	protected void initUpdateModel()
	{
    // Tell base class about our list selection model...
    setupEventSet(t_.getSelectionModel());
    // ..and our table edit notifier
    //setupEventSet(t_.getEditGenerator());

		modelVars_.setTransactional(true);
		this.add(AnyComponent.modelKey__, modelVars_);

    selection_      = AbstractComposite.orderedMap();
    keySelection_   = AbstractComposite.array();
    indexSelection_ = AbstractComposite.array();
		modelVars_.add(selection__, selection_);
		modelVars_.add(keySelection__, keySelection_);
    modelVars_.add(indexSelection__, indexSelection_);
    modelVars_.add(selectCount__, selectCount_ = new AnyInt());
    modelVars_.add(rowCount__, rowCount_ = new AnyInt());

		//model_.setModelVars(modelVars_);
		//System.out.println ("AnyTable.initUpdateModel ");
		addAdaptedEventListener(new SelectionListener(selectionChangedEventType__));
	}

	private void setupDataEvents()
	{
    Map nodeSpecs = model_.getEventPaths();

		// Now we've aggregated the node specifications and added the root
		// specification, if any.  Use base class method to route any
		// appropriate model to our componentProcessEvent method!
		//System.out.println ("AnyTable.setupDataEvents " + nodeSpecs);
		//System.out.println ("AnyTable.setupDataEvents " + getContext());
		//System.out.println ("AnyTable.setupDataEvents " + getContextNode());
		setupDataListener(nodeSpecs);
	}

	private int getMinSelectedRow(boolean pasting) throws AnyException
	{
		int colCount = model_.getColumnCount();
		int rowCount = model_.getRealRowCount();

		for (int row = 0; row < rowCount; row++)
		{
			for (int col = 0; col < colCount; col++)
			{
				if (t_.isCellSelected(row, col))
					return row;
			}
		}
		return pasting ? rowCount : 0;
	}

	private int getMaxSelectedRow(boolean pasting) throws AnyException
	{
		int colCount = model_.getColumnCount();
		int rowCount = model_.getRealRowCount();

		for (int row = rowCount - 1; row >= 0; row--)
		{
			for (int col = 0; col < colCount; col++)
			{
				if (t_.isCellSelected(row, col))
					return row + 1;
			}
		}
		return pasting ? rowCount : 0;
	}

	// Find the lowest numbered selected column.  If editable is true
	// then the column must be editable.
	// Returns -1 if no editable cell is found.  Returns 0 if there
	// is no selection.
	private int getMinSelectedColumn(int row, boolean editable)
	{
		int colCount = model_.getColumnCount();

		for (int col = 0; col < colCount; col++)
		{
			if (t_.isCellSelected(row, col))
			{
				if (editable && !model_.isCellEditable(row, col))
					continue;
				return col;
			}
		}

		// There's no selection or no editable cells within the selection
		if (editable)
		{
			for (int col = 0; col < colCount; col++)
			{
				if (!model_.isCellEditable(row, col))
					continue;
				return col;
			}
			return -1;
		}

		return 0;
	}
	// Find the highest numbered selected column.  If editable is true
	// then the column must be editable.
	// Returns -1 if no editable cell is found.  Returns 0 if there
	// is no selection.
	private int getMaxSelectedColumn(int row, boolean editable)
	{
		int colCount = model_.getColumnCount();

		for (int col = colCount - 1; col >= 0; col--)
		{
			if (t_.isCellSelected(row, col))
			{
				if (editable && !model_.isCellEditable(row, col))
					continue;
				return col;
			}
		}

		// There's no selection or no editable cells within the selection
		if (editable)
		{
			for (int col = colCount - 1; col >= 0; col--)
			{
				if (!model_.isCellEditable(row, col))
					continue;
				return col;
			}
			return -1;
		}

		return colCount - 1;
	}

	// Return the number of pastable columns in the range firstCol - lastCol
	// constraining to editables if so desired
	private int getNumPastable(int firstCol, int lastCol, boolean editable)
	{
		if (!editable)
			return lastCol - firstCol + 1;

		int numPastable = 0;

		for (int col = firstCol; col <= lastCol; col++)
		{
			if (model_.isCellEditable(0, col))
				numPastable++;
		}

		return numPastable;
	}

  private int getTotalColumnWidth(int maxCols)
  {
    // Return the total preferred width for the specified number
    // of columns or all columns if out of range
    TableColumnModel tcm = t_.getColumnModel();
    int w = 0;
    
    if (maxCols > tcm.getColumnCount() || maxCols <= 0)
      maxCols = tcm.getColumnCount();
    
    for(int i = 0; i < maxCols; i++)
    {
      TableColumn tc = tcm.getColumn(i);
      w += tc.getPreferredWidth();
      //System.out.println("pref: " + tc.getPreferredWidth());
      //System.out.println("w: " + w);
    }
    
    //System.out.println("total w: " + w);
    
    if (s_ != null)
      w += s_.getVerticalScrollBar().getPreferredSize().width + 5; // dunno why I need 5
    
    //System.out.println("w: " + w);
    return w;
  }
  // Add the column at columnModelIndex to the list we are
  // sorting by.  If already sorting then add to the list
  // unless columnModelIndex is the only current column, in
  // which case invert the sort direction.
	private void addSortColumn(int        columnModelIndex,
                             boolean    inAddition) throws AnyException
	{
    // Check whether property access has disabled sorting on this column
    Vectored v = null;
    if ((v = model_.getColumnProperties()) != null)
    {
      ColumnProperty cp =
        (ColumnProperty)v.getByVector(columnModelIndex);
      
      BooleanI sortable = (BooleanI)cp.get(sortable__);
      if (!sortable.getValue())
        return;
    }
    
    // Optimise by not sorting if we don't have to
    boolean mustSort = false;

		if (orderItems_ == null)
		{
      orderItems_ = new int[model_.getColumnCount()];
      clearOrderItems();
		}

		if (!inAddition)
		{
      int entries = numOrderColumns();
      if ((entries == 1) && (columnModelIndex == orderItems_[0]))
      {
        sortDescending_ = !sortDescending_;
        mustSort        = true;
      }

		  clearOrderItems();
    }

		mustSort |= addOrderItem(columnModelIndex);

    if (mustSort)
    {
      model_.sort(orderItems_, sortDescending_, getTreeRenderer());
      AnyTable totalsTable = (AnyTable)getTotalsTable();
      if (totalsTable != null && totalsTable.isClickSortEnabled())
        totalsTable.getModel().sort(orderItems_, sortDescending_, getTreeRenderer());
      if (this.contains(rowHeader__))
      {
        Any rh = this.get(rowHeader__);
        if (rh instanceof AnyTable)
        {
          AnyTable rht = (AnyTable)rh;
            rht.getModel().sort(orderItems_, sortDescending_, getTreeRenderer());
        }
      }
    }
	}

  // How many columns are we ordering?
  private int numOrderColumns()
  {
    int ret = 0;
    if (orderItems_ != null)
    {
      int l = orderItems_.length;
      do
      {
        if (orderItems_[ret] < 0)
          return ret;
      }
      while (++ret < l);
    }
    return ret;
  }

  // Are we ordering the specified column?
  private boolean isOrderColumn(int columnModelIndex)
  {
    if (orderItems_ != null)
    {
      int i = 0;
      int l = orderItems_.length;
      while (i < l)
      {
        int col = orderItems_[i++];

        if (col < 0)
          return false;

        if (col == columnModelIndex)
          return true;
      }
    }
    return false;
  }


  // Clear down the order items (in anticipation that one will
  // be added).
  private void clearOrderItems()
  {
    if (orderItems_ != null)
      Arrays.fill(orderItems_, -1);
  }

  // Add the given columnModelIndex to the list we are ordering by.
  // If the index is already in the list then nothing happens.
  // Returns true if we added the index, false otherwise
  private boolean addOrderItem(int columnModelIndex)
  {
    if (orderItems_ != null)
    {
      int i = 0;
      int l = orderItems_.length;
      while (i < l)
      {
        int col = orderItems_[i];

        if (col == columnModelIndex)
          return false;

        if (col < 0)
        {
          orderItems_[i] = columnModelIndex;
          return true;
        }

        i++;
      }
    }
    return false;
  }

  // Note: not called for a totals table
	private void setupHeaderRenderers(Font widthGauge)
	{
		if (model_.getColumnCount() == 0)
			return;
		
		if (maxWidths_ == null)
			maxWidths_ = AbstractComposite.array();
		
    maxWidths_.empty();
    
    RenderInfo r;
    //int totW = 0;
		for (int i = 0; i < model_.getColumnCount(); i++)
		{
      r = model_.getRenderInfo(i);
      maxWidths_.add(new AnyInt());
			TableColumn tc = t_.getColumnModel().getColumn(i);
      TableCellRenderer headerRenderer = new SortHeaderRenderer(t_.getTableHeader().getDefaultRenderer());
			tc.setHeaderRenderer(headerRenderer);

      // Work out column widths
			FontMetrics fm = t_.getFontMetrics(widthGauge);
			//System.out.println("JTable.init: setting width: " + r.getWidth());
      int fontWidth = r.getWidth() * fm.charWidth('n');

//System.out.println("JTable.init: renderer 1 " + tc.getHeaderRenderer());
//System.out.println("JTable.init: renderer 2 " + tc.getHeaderRenderer().getTableCellRendererComponent(this, r.getLabel(), false, false, 0, i));
//System.out.println("JTable.init: preferred " + tc.getHeaderRenderer().getTableCellRendererComponent(this, r.getLabel(), false, false, 0, i).getPreferredSize());

      int headWidth =
        (int)headerRenderer.getTableCellRendererComponent(t_, r.getLabel(), false, false, 0, i).getPreferredSize().getWidth() +
        t_.getColumnModel().getColumnMargin()*2;
      if (headWidth > fontWidth)
        tc.setPreferredWidth(headWidth);
      else
        tc.setPreferredWidth(fontWidth);
      
      //totW += tc.getPreferredWidth();
      
      this.setMaxRenderedWidth(i, headWidth);
		}
    //System.out.println("totW " + totW);
	}
  
  private int getHeaderLabelWidth(int col)
  {
    TableColumn tc = t_.getColumnModel().getColumn(col);
    TableCellRenderer headerRenderer = tc.getHeaderRenderer();
    
    RenderInfo r = model_.getRenderInfo(col);
    
    int headWidth =
      (int)headerRenderer.getTableCellRendererComponent(t_,
                                                        r.getLabel(),
                                                        false,
                                                        false,
                                                        0, col).getPreferredSize().getWidth() +
                               t_.getColumnModel().getColumnMargin()*2;
    
    return headWidth;
  }

  private void resetMaxRenderedWidths()
  {
  	if (maxWidths_ != null)
  	{
	    int j = maxWidths_.entries();
	    for (int i = 0; i < j; i++)
	      ((AnyInt)maxWidths_.get(i)).setValue(getHeaderLabelWidth(i));
  	}
  }

  private void rememberColumns(Vectored columns)
  {
    // When we have a columns map we remember the column names for
    // future use in identifying the column of mouse events.  These
    // objects also provide a map interface to give access to
    // underlying editors/renderers for scripts.
    if (columns instanceof Map)
    {
      Map m = (Map)columns;
      ColumnMap rememberColumns = new ColumnMap();
      Iter i = m.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        rememberColumns.add(k, new ColumnProperty(k));
      }
      //columns_ = rememberColumns;
      model_.setColumnProperties(rememberColumns);
    }
    else
      model_.setColumnProperties(null);
  }

  private void processColumnGroups(Map                       groups,
                                   GroupableTableColumnModel cm)
  {
    if (!(groups instanceof Vectored))
      throw new AnyRuntimeException("Group specification cannot be accessed by vector");

    // Access them by name if we want to remember the names.  For now
    // just use vector.
    Vectored v = (Vectored)groups;

    for (int i = 0; i < v.entries(); i++)
    {
      cm.addColumnGroup(processColumnGroup((Map)v.getByVector(i)));
    }
  }

  private ColumnGroup processColumnGroup(Map grouping)
  {
    Vectored cols = null;

    if (!grouping.contains(columns__) ||
        ((cols = (Vectored)grouping.get(columns__)).entries() == 0))
      throw new AnyRuntimeException("Group specification does not specify any columns");

    String label = "";
    if (grouping.contains(label__))
      label = grouping.get(label__).toString();

    ColumnGroup cg = new ColumnGroup(label);

    for (int i = 0; i < cols.entries(); i++)
    {
      // Each entry in the columns vector must contain either
      // a ColumnProperty object or a map, assumed to be another
      // grouping.
      Any col = cols.getByVector(i);
      if (col instanceof ColumnProperty)
      {
        ColumnProperty cp = (ColumnProperty)col;
        TableColumn tc = cp.findTableColumn();
        cg.add(tc);
      }
      else if (col instanceof Map)
      {
        Map m = (Map)col;
        cg.add(processColumnGroup(m));
      }
    }
    return cg;
  }

  private AnyTreeTableModel getTreeTableModel()
  {
    AnyTreeTableModel attm = null;

    if (treeModel_ == null)
    {
      // Make an empty AnyTreeTableModel
      attm = new AnyTreeTableModel();

      // Make the actual TableModel for TreeTable mode
      treeModel_ = new AnyTreeTableModelAdapter(attm,
                                                t_.getTreeRenderer(attm));

      // chicken/egg - need to put any existing columns
      // in now as reinit needs them
      //if (flatModel_ != null && flatModel_.getColumns() != null)
        //attm.setColumns(flatModel_.getColumns());

      treeModel_.setTable(this);

      // Put an expansion listener on the underlying tree so we
      // can keep track of the expansion state in the tree model.
      setupEventSet(treeModel_.getTreeRenderer());
      addAdaptedEventListener(new TreeExpansionListener());
    }
    else
    {
      attm = (AnyTreeTableModel)treeModel_.getTreeModel();
    }
    return attm;
  }

	private class SortMouseListener extends MouseAdapter
	{
    public void mouseClicked(MouseEvent e)
    {
      int modifiers = e.getModifiers();
      if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == 0)
        return;

      Point pt = e.getPoint();
      JTableHeader header = t_.getTableHeader();
      int col = header.columnAtPoint(pt);

      if (col < 0)
        return;

      Rectangle r = header.getHeaderRect(col);
      int modelCol = t_.convertColumnIndexToModel(col);
      TableColumn tc = t_.getColumnModel().getColumn(modelCol);


      if (e.getClickCount() == 1)
      {
        // Sort functionality
        
        if (!clickSortEnabled_)
          return;

        // Check if the mouse click is in the right area. If
        // resizing is allowed the moust click must be outside
        // the resize area of the header. This is hard-coded in
        // Java as 3 pixels either way.
        if (header.getResizingAllowed() && tc.getResizable())
        {
          r.grow(-3, 0);
          if (!r.contains(pt))
            return;
        }

        //System.out.println("Mouse clicked on column " + col);
        try
        {
          addSortColumn(modelCol, e.isControlDown());
        }
        catch (AnyException ex)
        {
          throw new RuntimeContainedException(ex);
        }
        t_.getTableHeader().repaint();
      }
      else if (e.getClickCount() == 2)
      {
        // Spring to size functionality.
        // Resizing must be allowed
        if (header.getResizingAllowed() && tc.getResizable())
        {
          // If the mouse click is not in the resize region then
          // don't resize
          r.grow(-3, 0);
          if (r.contains(pt))
            return;

          //System.out.println("Autoresize " + col);
          
          // The column we want to resize is always the one "to the
          // left" of the click point
          int midPoint = r.x + r.width / 2;
          int columnIndex;
          if (header.getComponentOrientation().isLeftToRight())
          {
            columnIndex = (pt.x < midPoint) ? col - 1 : col;
          }
          else
          {
            columnIndex = (pt.x < midPoint) ? col : col - 1;
          }
          if (columnIndex == -1)
            return;

          tc = t_.getColumnModel().getColumn(columnIndex);

          // We only need one margin space but two looks nicer
          tc.setPreferredWidth(AnyTable.this.getMaxRenderedWidth(columnIndex) +
                               t_.getColumnModel().getColumnMargin() * 2);
          //header.repaint();
        }
      }
    }
	}

	private class SortHeaderRenderer implements TableCellRenderer
	{
    private TableCellRenderer tableCellRenderer_;
    
    private SortHeaderRenderer(TableCellRenderer r)
    {
      tableCellRenderer_ = r;
    }

    public Component getTableCellRendererComponent(javax.swing.JTable table,
		                                               Object  value,
		                                               boolean isSelected,
		                                               boolean hasFocus,
		                                               int     row,
		                                               int     column)
	  {
      Component c = tableCellRenderer_.getTableCellRendererComponent(table, 
                                                                     value,
                                                                     isSelected,
                                                                     hasFocus,
                                                                     row,
                                                                     column);
      JLabel l = null;
      if (c instanceof JLabel)
        l = (JLabel)c;
      
      if (table != null)
      {
        
//        JTableHeader header = table.getTableHeader();
//        if (header != null)
//        {
//          setForeground(header.getForeground());
//          setBackground(header.getBackground());
//          setFont(header.getFont());
//        }

        column = table.convertColumnIndexToModel(column);
        
        // Check if we are a sort column and if so, which
        // direction the sort is in

        if (isOrderColumn(column) && l != null)
        {
          if (sortDescending_)
          {
            //setIcon(sortDescending__.getIcon());
            l.setIcon(new Arrow(true, l.getFontMetrics(l.getFont()).getMaxAscent(), 0));
          }
          else
          {
            //setIcon(sortAscending__.getIcon());
            //setIcon(new Arrow(false, getFont().getSize(), 0));
            l.setIcon(new Arrow(false, l.getFontMetrics(l.getFont()).getMaxAscent(), 0));
          }
        }
        else if (l != null)
          l.setIcon(null);

      }

      //setText((value == null) ? "" : value.toString());
      //setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      if (l != null)
      {
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setHorizontalTextPosition(SwingConstants.LEFT);
        
        RenderInfo r = model_.getRenderInfo(column);
        
        // Tooltip handling
        if (!r.getLabel().equals(r.getDefaultLabel()))
          l.setToolTipText(r.getDefaultLabel());
        else
        {
          if (l.getPreferredSize().width > t_.getColumnModel().getColumn(column).getWidth())
            l.setToolTipText(r.getLabel());
          else
            l.setToolTipText(null);
        }
      }

      return c;
	  }
	}

  // Represents a table column within which children are
  // attribute(s) of that column.
  public class ColumnProperty extends AbstractMap
  {
    private Any        columnName_;
    
    private AnyBoolean sortable_;

    // Create an instance based on the name of the column.
    // Store the name so we can use it later in
    // events (see handleBoundEvent)
    private ColumnProperty(Any columnName)
    {
      columnName_ = columnName;
    }
    
    public Any getColumnName()
    {
      return columnName_;
    }

    public int getColumnIndex()
    {
      return model_.getColumnProperties().indexOf(columnName_);
    }

    public Any get(Any key)
    {
      Any ret = getWithKey(key);
      if (ret == null)
        handleNotExist(key);  // throws

      return ret;
    }

    public Any getIfContains(Any key)
    {
      Any ret = getWithKey(key);
      return ret;
    }

    public boolean contains (Any key)
    {
      return (//key.equals(editable__) ||
              key.equals(editor__)   ||
              key.equals(renderer__) ||
              key.equals(sortable__) ||
              key.equals(renderinfo__) ||
              key.equals(path__) ||
              key.equals(typedef__) ||
              key.equals(field__) ||
              key.equals(label__) ||
              key.equals(formatString__) ||
              key.equals(visible__) ||
              key.equals(widths__));
    }

    public boolean isEmpty() { return false; }

    public Any toMap()
    {
      Map m = AbstractComposite.simpleMap();
      
      m.add(sortable__, this.get(sortable__));
      m.add(visible__, this.get(visible__));
      
      Widths w = (Widths)this.get(widths__);
      
      m.add(widths__, w.toMap());
      
      return m;
    }

    protected boolean beforeAdd(Any key, Any value) { return true; }
    protected void afterAdd(Any key, Any value) {}
    protected void beforeRemove(Any key) {}
    protected void afterRemove(Any key, Any value) {}
    protected void emptying() {}
    public Iter createIterator () {return DegenerateIter.i__;}
    
    private Any getWithKey(Any key)
    {
      int column = model_.getColumnProperties().indexOf(columnName_);
      if (key.equals(editor__))
      {
        // When getting the editor through property access like this
        // the assumption is that, by accessing the editor, we intend
        // to configure one, so if there is no AnyComponentEditor
        // currently there, make one.
        AnyComponentEditor ace = null;

        if ((ace = (AnyComponentEditor)model_.getCellEditor(-1, column)) == null)
        {
          RenderInfo r = AnyTable.this.getColumnRenderInfo(column);
          r.setEditable(true);
          ace = new AnyComponentEditor(r);
          
          // We don't hold the editors in the TableColumn objects any more
          // as is possible, instead they are held in the model so that
          // the implementation (flat or tree) can provide for editors
          // per column or level etc.
          model_.setCellEditor(ace, -1, column);
        }
        return ace;
      }
      else if (key.equals(editable__))
      {
        return (AnyCellRenderer)model_.getCellRenderer(-1, column);
      }
      else if (key.equals(renderer__))
      {
        return (AnyCellRenderer)model_.getCellRenderer(-1, column);
      }
      else if (key.equals(sortable__))
      {
        if (sortable_ == null)
          sortable_ = new AnyBoolean(true);
        
        return sortable_;
      }
      else if (key.equals(renderinfo__))
      {
        return model_.getRenderInfo(column);
      }
      else if (key.equals(label__))
      {
        return AbstractValue.flyweightString(model_.getRenderInfo(column).getLabel());
      }
      else if (key.equals(formatString__))
      {
        {
          String s = model_.getRenderInfo(column).getFormatString();
          if (s == null)
            return AnyString.NULL;
          else
            return AbstractValue.flyweightString(s);
        }
      }
      else if (key.equals(visible__))
      {
        // Future expansion
        return AnyBoolean.TRUE;
      }
      else if (key.equals(path__))
      {
        // The renderinfo's path, if it has one. Throws if it does not.
        // This supports paths like
        //    myTable.properties.columns.myColumn.path
        // for reading the path only. It gives the same as
        //    myTable.properties.columns.myColumn.renderer.properties.path
        // however when using the latter the property is writeable
        return model_.getRenderInfo(column).getRenderPath();
      }
      else if (key.equals(typedef__))
      {
        return model_.getRenderInfo(column).getDescriptor();
      }
      else if (key.equals(field__))
      {
        return model_.getRenderInfo(column).getField();
      }
      else
      {
        TableColumn tc = findTableColumn();
        return getColumnProperty(tc, key);
      }
    }

    private TableColumn findTableColumn()
    {
      int column = model_.getColumnProperties().indexOf(columnName_);
      //column     = t_.convertColumnIndexToModel(column);

      return t_.getColumnModel().getColumn(column);
    }

    private Any getColumnProperty(TableColumn tc, Any key)
    {
      /*
      // Because copyFrom(BooleanI) will set the editable flag
      // in the renderer.  Still no good unless there is an editor,
      // in fact useless because if there is no editor available
      // swing will crash, so do not use! Note that when an editor
      // component is put into an AnyComponentEditor it sets the
      // RenderInfo's editable flag to true anyway.
      if (key.equals(editable__))
        return (AnyCellRenderer)tc.getCellRenderer();

      if (key.equals(renderer__))
        return (AnyCellRenderer)tc.getCellRenderer();
      */
      
      if (key.equals(widths__))
        return new Widths(tc);

      
      return null;
    }
    
    public class Widths extends DefaultPropertyAccessMap
    {
      private TableColumn tc_;
      
      private Widths(TableColumn tc)
      {
        tc_ = tc;
      }
      
      // Sets minimum width in pixels
      public void setMinimumWidth(Any width)
      {
        anInt__.copyFrom(width);
        tc_.setMinWidth(anInt__.getValue());
      }
      
      // Sets maximum width in pixels
      public void setMaximumWidth(Any width)
      {
        anInt__.copyFrom(width);
        tc_.setMaxWidth(anInt__.getValue());
      }
      
      // Sets preferred width in pixels
      public void setPreferredWidth(Any width)
      {
        anInt__.copyFrom(width);
        tc_.setPreferredWidth(anInt__.getValue());
      }
      
      public Any getMinimumWidth()
      {
        return new AnyInt(tc_.getMinWidth());
      }
      
      public Any getMaximumWidth()
      {
        return new AnyInt(tc_.getMaxWidth());
      }
      
      public Any getPreferredWidth()
      {
        return new AnyInt(tc_.getPreferredWidth());
      }

      // Sets minimum width in characters
      public void setMinimumCharWidth(Any width)
      {
        anInt__.copyFrom(width);
        tc_.setMinWidth(widthForCharacter(t_.getTableHeader(), anInt__.getValue()));
      }
      
      // Sets maximum width in characters
      public void setMaximumCharWidth(Any width)
      {
        anInt__.copyFrom(width);
        tc_.setMaxWidth(widthForCharacter(t_.getTableHeader(), anInt__.getValue()));
      }
      
      // Sets preferred width in characters
      public void setPreferredCharWidth(Any width)
      {
        anInt__.copyFrom(width);
        tc_.setPreferredWidth(widthForCharacter(t_.getTableHeader(), anInt__.getValue()));
      }

      public Any getMinimumCharWidth()
      {
        float f = tc_.getMinWidth();
        f /= widthForCharacter(t_.getTableHeader()) + 0.5f;
        
        return new AnyInt((int)f);
      }
      
      public Any getMaximumCharWidth()
      {
        float f = tc_.getMaxWidth();
        f /= widthForCharacter(t_.getTableHeader()) + 0.5f;
        
        return new AnyInt((int)f);
      }
      
      public Any getPreferredCharWidth()
      {
        float f = tc_.getPreferredWidth();
        f /= widthForCharacter(t_.getTableHeader()) + 0.5f;
        
        return new AnyInt((int)f);
      }
      
      public Any toMap()
      {
        Map m = AbstractComposite.simpleMap();
        
        m.add(AbstractValue.flyweightString("minimumWidth"), this.getMinimumWidth());
        m.add(AbstractValue.flyweightString("preferredWidth"), this.getPreferredWidth());
        m.add(AbstractValue.flyweightString("maximumWidth"), this.getMaximumWidth());
        
        return m;
      }
    }
  }
  
  private static class ColumnMap extends AnyOrderedMap
  {
    public void addByVector(Any value)
    {
      throw new UnsupportedOperationException();
    }
    
    public void addByVector(int at, Any value)
    {
      throw new UnsupportedOperationException();
    }
    
    public void addByVector(int at, Any key, Any value)
    {
      throw new UnsupportedOperationException();
    }

    // TODO will become dynamic columns?
//    public void add(Any key, Any value)
//    {
//      if (!(value instanceof RenderInfo))
//    }
  }

  protected class TreeExpansionListener extends EventBinding
  {
    public TreeExpansionListener()
    {
      super(AnyTree.treeExpansionEventTypes__, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      //System.out.println("EXPANSION " + e.getId());

      // We keep track of the expansion state in the AnyTreeNode
      // hierarchy. See also AnyTree.TreeExpansionListener
      AnyTreeNode node = (AnyTreeNode)e.get(AnyTreeLevel.node__);
      Array       path = (Array)e.get(AnyComponent.path__);
      node.setExpanded(e.getId().equals(EventConstants.E_TREEEXPANDED));
      //System.out.println("TreeExpansionListener NODE " + node);
      //System.out.println("TreeExpansionListener PATH " + path);

      if (path.entries() > 1)
      {
        // Step down the path from the current root in case there are
        // a new set of tree node objects in the model.
        // TODO: is this necessary as AnyTreeNode.setExpanded sets the
        // expanded flag back to the root? Check.
        node = (AnyTreeNode)treeModel_.getTreeModel().getRoot();
        Any lookFor;
        int max = path.entries();
        for (int i = 1; i < max; i++)
        {
          lookFor = ((AnyTreeNode)path.get(i)).getAny();
          node = node.findNode(lookFor);
          
          // Node may not be found as we move down the path - see below.
          if (node == null)
            break;
        }
        // There may be no matching node if the structure has been
        // replaced and the new is completely different from the old.
        if (node != null)
          node.setExpanded(e.getId().equals(EventConstants.E_TREEEXPANDED));
      }

      return null;
		}
  }

  // Listen to mouse motion events so that cells/rows can be re-rendered
  // as the mouse moves over them.
  // In general, 
  private class MotionRenderingListener extends EventBinding
  {
    private MotionRenderingExitListener exitListener_;
    
    public MotionRenderingListener()
    {
      super(mouseMotionEventType__, false, false);
      
      // Monitor mouse exit as well, just in case the exit/enter is on the
      // same cell.
      addAdaptedEventListener(exitListener_ = new MotionRenderingExitListener());
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      int lastRow = lastRow_;
      int lastColumn = lastColumn_;
      
      // Update the cell coordinates before the renderer is invoked so
      // that it knows whether it is rendering the cell the mouse pointer
      // is in or not.
      MouseEvent me = (MouseEvent)e.getUnderlyingEvent();
      Point p = me.getPoint();
      int eventRow    = t_.rowAtPoint(p);
      int eventColumn = t_.columnAtPoint(p);
      eventColumn  = t_.convertColumnIndexToModel(eventColumn);

      // Either of these could have changed to -1 as we exit...
      lastRow_    = eventRow;
      lastColumn_ = eventColumn;
      
      // We know that the cell has changed or the event binding would have
      // been vetoed (see handleBoundEvent()).
      // Repaint the cell(s) we have just left, then the one(s) we
      // have just entered (if any)
      if (t_.isRowRefresh())
      {
        // ...and check if this is the first time in
        if (lastRow >= 0)
          model_.fireTableRowsUpdated(lastRow, lastRow);
        
        if (lastRow_ >= 0)
          model_.fireTableRowsUpdated(lastRow_, lastRow_);
      }
      else
      {
        if (lastRow >= 0)
          model_.fireTableCellUpdated(lastRow, lastColumn);
        if (lastRow_ >= 0)
          model_.fireTableCellUpdated(lastRow_, lastColumn_);
      }
      return null;
    }
  }
  
  private class MotionRenderingExitListener extends EventBinding
  {
    public MotionRenderingExitListener()
    {
      super(mouseExitEventType__, false, false);
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      int lastRow = lastRow_;
      int lastColumn = lastColumn_;
      
      // We've left the component, so we're not in any cell....
      lastRow_    = -1;
      lastColumn_ = -1;
      
      // Repaint the last cell(s) we were in.
      if (t_.isRowRefresh())
      {
        // ...but as well we may have left the component from a region
        // where there was no cell.
        if (lastRow >= 0)
          model_.fireTableRowsUpdated(lastRow, lastRow);
      }
      else
      {
        if (lastRow >= 0)
          model_.fireTableCellUpdated(lastRow, lastColumn);
      }
      
      return null;
    }
  }

  private static class Arrow implements Icon
  {
    private boolean descending_;
    private int     size_;
    private int     priority_;

    public Arrow(boolean descending, int size, int priority)
    {
      this.descending_ = descending;
      this.size_       = size;
      this.priority_   = priority;
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
      Color color = c == null ? Color.gray : c.getBackground();             
      // In a compound sort, make each succesive triangle 20% 
      // smaller than the previous one. 
      int dx = (int)(size_ / 2 * Math.pow(0.8, priority_));
      int dy = descending_ ? dx : -dx;
      // Align icon (roughly) with font baseline. 
      y = y + 5 * size_ / 6 + (descending_ ? -dy : 0);
      int shift = descending_ ? 1 : -1;
      g.translate(x, y);

      // Right diagonal. 
      g.setColor(color.darker());
      g.drawLine(dx / 2, dy, 0, 0);
      g.drawLine(dx / 2, dy + shift, 0, shift);

      // Left diagonal. 
      g.setColor(color.brighter());
      g.drawLine(dx / 2, dy, dx, 0);
      g.drawLine(dx / 2, dy + shift, dx, shift);

      // Horizontal line. 
      if (descending_)
      {
        g.setColor(color.darker().darker());
      } else
      {
        g.setColor(color.brighter().brighter());
      }
      g.drawLine(dx, 0, 0, 0);

      g.setColor(color);
      g.translate(-x, -y);
    }

    public int getIconWidth()
    {
      return size_;
    }

    public int getIconHeight()
    {
      return size_;
    }
  }
}
