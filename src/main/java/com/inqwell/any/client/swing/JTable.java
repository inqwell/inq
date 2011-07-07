/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JTable.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.AnyAttributeSet;
import com.inqwell.any.client.AnyCellRenderer;
import com.inqwell.any.client.AnyComponent;
import com.inqwell.any.client.AnyComponentEditor;
import com.inqwell.any.client.AnyTable;
import com.inqwell.any.client.AnyTableModel;
import com.inqwell.any.client.AnyTreeNode;
import com.inqwell.any.client.AnyTreeTableModel;
import com.inqwell.any.client.AnyView;
import com.inqwell.any.client.RenderInfo;
import com.inqwell.any.client.TreeLevel;

public class JTable extends javax.swing.JTable
{
  //private NotifyEdit notifyEdit_;

  private Call       prepareEdit_;
  
  static private Map        fnArgs__;
  static private AnyInt     row__;
  static private AnyInt     col__;
  static private AnyBoolean isLeaf__;
  static private AnyBoolean expanded__;
  static private AnyBoolean isUser__;
  static private AnyBoolean after__;

  //private int        lastEditRow_ = -1;
  private boolean    editable_ = true;

  // Non-null if we've ever acted as a TreeTable
  private TreeTableCellRenderer tree_;
  
  private AnyTable   anyTable_;
  
  // A style function can be supplied via property access. If non-null
  // then updates to a cell will cause all cells in the row to be
  // re-rendered.
  private Call          styleFunc_;

  /*
	public JTable(TableModel m)
	{
		super(m);
		init(m);
	}
	*/

	public JTable()
	{
		super(new AnyTableModel(null));
    //this.setColumnModel(new GroupableTableColumnModel());
    //this.setTableHeader(new GroupableTableHeader((GroupableTableColumnModel)this.getColumnModel()));
		init((TableModel)getModel());
	}

  protected ListSelectionModel createDefaultSelectionModel()
  {
    return new MultiListSelectionModel();
  }

  protected TableColumnModel createDefaultColumnModel()
  {
    return new GroupableTableColumnModel();
  }

  protected javax.swing.table.JTableHeader createDefaultTableHeader()
  {
    //return new javax.swing.table.JTableHeader(this.getColumnModel());
    return new GroupableTableHeader((GroupableTableColumnModel)this.getColumnModel());
  }

  public void setAnyTable(AnyTable t)
  {
    anyTable_ = t;
  }
  
  public AnyTable getAnyTable()
  {
    return anyTable_;
  }
  
  public void editingStopped(ChangeEvent e)
  {
    // Carry out post-editing validation. As far as swing is concerned
    // editing has (almost) stopped...
    
    //System.out.println("editingStopped  " + e);
    
    // Could be a AnyComponentEditor or a TreeTableCellEditor when in
    // treetable mode...
    InqEditor ac = (InqEditor)getCellEditor();
    
    // Was editing stopped by the underlying UI or because of a real
    // user event from the editor component?
    boolean isUser = (e.getSource() != ac);
    
    // Convert the current editing column to model coordinates
    int editingColumn = getEditingColumn();
    editingColumn     = convertColumnIndexToModel(editingColumn);
    
    int editingRow    = getEditingRow();
    
    // Get the value before we call super so it isn't updated yet
    TableModel m = (TableModel)getModel();
    Any vOld = m.getResponsibleValueAt(editingRow, editingColumn);

    Any vNew = (Any)ac.getCellEditorValue();

    // Fire the TableEditEvent before calling super.  This means
    // that a script's event handler will be executed before the
    // value is updated and gets a chance to (say) save the old
    // value.
    Any rowRoot = m.getRowAt(editingRow);
    Any rowKey  = m.getRowKey(editingRow);
    Any colName = m.getNameOfColumn(editingColumn);
    
    if (isUser__ == null)
    {
      isUser__   = new AnyBoolean();
      after__    = new AnyBoolean();
    }

    isUser__.setValue(isUser);
    after__.setValue(false);
    
    ac.onStopEditing(anyTable_,
                     anyTable_.getContextNode(),
                     rowRoot,
                     vOld,
                     vNew,
                     editingRow,
                     rowKey,
                     editingColumn,
                     colName,
                     isUser__,
                     after__,
                     null,     // no level unless via a TreeTableEditor
                     null,     // no isLeaf/expanded unless via TreeTableEditor
                     null);
    
/*    if (notifyEdit_ != null)
    {
      notifyEdit_.fireEditDone(this,
                               rowRoot,
                               rowKey,
                               editingRow,
                               editingColumn,
                               vOld,
                               vNew,
                               isUI,
                               true);
    }
*/
    // Check if the event handler vetoed the edit by setting
    // vNew back to vOld
    boolean changed = false;
    
    if (vNew != AnyComponentEditor.noValue__)
      changed = !vOld.equals(vNew);
    
    after__.setValue(true);
    
    if (changed)
    {
      setValueAt(vNew, editingRow, editingColumn);
      ac.onStopEditing(anyTable_,
                       anyTable_.getContextNode(),
                       rowRoot,
                       vOld,
                       vNew,
                       editingRow,
                       rowKey,
                       editingColumn,
                       colName,
                       isUser__,
                       after__,
                       null,     // no TreeLevel unless via TreeTableEditor
                       null,     // no isLeaf/expanded unless via TreeTableEditor
                       null);
    }

    /*    if (notifyEdit_ != null)
    {
      notifyEdit_.fireEditDone(this,
          rowRoot,
          rowKey,
          editingRow,
          editingColumn,
          vOld,
          vNew,
          isUI,
          false);
    }
*/

    removeEditor();

    // Keep the focus on the table, as it has a tendency to wander off
    // when using the auto-complete combobox....
    this.requestFocusInWindow();
  }

  public void removeEditor()
  {
    super.removeEditor();
    this.requestFocusInWindow();
  }

  public void editingCanceled(ChangeEvent e)
  {
    System.out.println("editingCancelled " + e);
  	super.editingCanceled(e);
    
    /*
  	if (notifyEdit_ != null)
  	{
      TableModel m = (TableModel)getModel();
      notifyEdit_.fireEditCanceled(new TableEditEvent(this));
    }
    */
    this.requestFocusInWindow();
  }

  // Additional properties

  /**
   * The global editable property.  Provides a single control
   * so that individual columns do not have to be accessed to
   * control overall editing.
   */
  public void setEditable(boolean editable)
  {
    editable_ = editable;
    if (!editable_)
      resetEditor();
  }

	public boolean isEditable()
  {
    return editable_;
  }

  // Set the Inq function that will be called if a cell is editable
  public void setPrepareEdit(Any prepareEditF)
  {
    Call prepareEdit = AnyComponent.verifyCall(prepareEditF);
    prepareEdit_ = prepareEdit;
  }

  public Component prepareEditor(TableCellEditor editor, int row, int column)
  {
    //int modelColumn = convertColumnIndexToModel(column);

    //System.out.println("prepareEditor " + row + " " + column);
    
    // Call any supplied prepare function
    // TODO. Probably don't need this as editors now hold start and stop functions
    callPrepareEditFunc(prepareEdit_, row, column);

    return super.prepareEditor(editor, row, column);
  }

  // Like removeEditor but reset the flag for our prepareEdit stuff.
  // Can't just override removeEditor because that method is called
  // on a cell (rather than on a row) basis.
  public void resetEditor()
  {
    //lastEditRow_ = -1;
    removeEditor();
  }

  public void setPreferredViewportSize(Dimension size)
  {
    preferredViewportSize = size;
  }
  
  public boolean isRowRefresh()
  {
    return styleFunc_ != null;
  }

  public void setRowStyle(Any styleFuncF)
  {
    Call styleFunc = AnyComponent.verifyCall(styleFuncF);
    styleFunc_ = styleFunc;
  }

  public Any getRowStyle()
  {
    return styleFunc_;
  }
  
  public void changeSelection(int     row,
                              int     column,
                              boolean toggle,
                              boolean extend)
  {
    //// Start editing cell immediately it gets the focus
    super.changeSelection(row, column, toggle, extend);
    //if (editCellAt(row, column))
      //getEditorComponent().requestFocusInWindow();
  }

  public boolean isCellEditable(int row,
                                int column)
  {
    return editable_ ? super.isCellEditable(row, column) : false;
  }

  /**
   * Supplant base class functionality.  The Inq TableModel
   * implementation supports the provision of TableCellEditor
   * instances, so that the two types of model supported, flat
   * and tree tables, can control the availability of editors.
   */
  public TableCellEditor getCellEditor(int row, int column)
  {
    // Check the TreeTable mode first. In this case determination of
    // the real editor is not done until later when we request it from
    // the model from within the TreeTableModel.class "editor"
    // Note, the editors-by-class mechanism is not used elsewhere.
    
    TableCellEditor editor = null;
    
    if (getColumnClass(column) == TreeTableModel.class)
      editor = getDefaultEditor(TreeTableModel.class);
    
    if (editor == null)
    {
      // Ask the model for the editor
      TableModel tableModel = anyTable_.getModel();
      column = convertColumnIndexToModel(column);
      editor = tableModel.getCellEditor(row, column);
    }

    return editor;
  }
  
  public TableCellRenderer getCellRenderer(int row, int column)
  {
    // See getCellEditor
    
    TableCellRenderer renderer = null;
    
    if (getColumnClass(column) == TreeTableModel.class)
      renderer = getDefaultRenderer(TreeTableModel.class);
    
    if (renderer == null)
    {
      // Ask the model for the editor
      TableModel tableModel = anyTable_.getModel();
      column = convertColumnIndexToModel(column);
      renderer = tableModel.getCellRenderer(row, column);
    }

    return renderer;
  }

  protected boolean processKeyBinding(KeyStroke ks,
                                      KeyEvent e,
                                      int condition,
                                      boolean pressed)
  {
    // Unfortunately the initiation of editing via the keyboard
    // does not pass the KeyEvent to the CellEditor.isCellEditable
    // method - it just passes null.  This means we cannot do
    // something different (eg clear cell or edit existing)
    // according to the key typed.  Do the minimum possible here
    // to avoid storing up problems and defer whenever possible.

    // If there is a binding, this will do something specific,
    // so defer to super. The action may be to start editing, so
    // is after calling super we are editing perform Inq-specific
    // actions relating to this.
    if (getActionForKeyStroke(ks) != null)
    {
      boolean ret = super.processKeyBinding(ks, e, condition, pressed);
      final AnyComponentEditor ac;
      if (editingRow >= 0)
      {
        TableModel m = anyTable_.getModel();
        ac = (AnyComponentEditor)m.getCellEditor(editingRow, convertColumnIndexToModel(editingColumn));
      }
      else
        ac = null;
      
      // If the table is surrending the focus as editing starts (default
      // true) then give the focus to the editor. Although super does this
      // as well it assumes the editor component is the editor itself. In
      // Inq this may not be the case, if we have a complex layout for our
      // editor. Do it again here on the actual editor component. (Note
      // if this isn't done the difference is only subtle for text fields.
      // the tf still accepts key strokes, though it may not have the
      // focus according to swing. This gives an incorrect "isUser" argument
      // to the stop function.
//      if (ac != null && getSurrendersFocusOnKeystroke())
//        ac.getEditingComponent().requestFocus();
      if (ac != null && getSurrendersFocusOnKeystroke())
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            ac.getEditingComponent().requestFocus();
          }
        });
      }
      
      return ret;
    }
    
    // If already editing then super
    if (isEditing())
      return super.processKeyBinding(ks, e, condition, pressed);

    // If the key is something other than an obvious editing key
    // try to ignore it. This way accelerators and editable cells
    // don't interfere with each other.
    if ((e.getModifiers() & (KeyEvent.ALT_GRAPH_MASK |
                             KeyEvent.CTRL_MASK      |
                             KeyEvent.META_MASK      |
                             KeyEvent.ALT_MASK)) != 0)
      return false;
    
    // When there's no binding (and cell is editable) super will
    // try to start editing.   Add the condition that the key must
    // be a unicode key and clear the editor value
    if (!e.isActionKey())
    {
      boolean ret = super.processKeyBinding(ks, e, condition, pressed);
      // Go to the model to get the real editor. In most cases this is the
      // same as that returned by this.getCellEditor() when editing has
      // started. However in the treetable case the editor installed in
      // the table is the fake one whose primary purpose is to dispatch
      // events to the tree renderer.
      //AnyComponentEditor ac = (AnyComponentEditor)getCellEditor();
      
      final AnyComponentEditor ac;
      if (editingRow >= 0)
      {
        TableModel m = anyTable_.getModel();
        ac = (AnyComponentEditor)m.getCellEditor(editingRow, convertColumnIndexToModel(editingColumn));
      }
      else
        ac = null;
      
      if (ac != null && pressed)
      {
        ac.clearEditingValue();
        
      }
      
      // If the table is surrending the focus as editing starts (default
      // true) then give the focus to the editor. Although super does this
      // as well it assumes the editor component is the editor itself. In
      // Inq this may not be the case, if we have a complex layout for our
      // editor. Do it again here on the actual editor component. (Note
      // if this isn't done the difference is only subtle for text fields.
      // the tf still accepts key strokes, though it may not have the
      // focus according to swing. This gives an incorrect "isUser" argument
      // to the stop function.
//      if (ac != null && getSurrendersFocusOnKeystroke())
//        ac.getEditingComponent().requestFocus();
      if (ac != null && getSurrendersFocusOnKeystroke())
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            ac.getEditingComponent().requestFocus();
          }
        });
      }
      
      return ret;
    }
    return super.processKeyBinding(ks, e, condition, pressed);
  }

//	public NotifyEdit getEditGenerator()
//	{
//		if (notifyEdit_ == null)
//			notifyEdit_ = new NotifyEdit();
//
//		return notifyEdit_;
//	}

  // For flat table initialisation
  public void reinit(TableModel m, Font widthGauge)
  {
    //System.out.println("REINIT AS FLAT TABLE");
    //initAsFlatTable(m);
    this.setModel(m);
    init(m, widthGauge);
  }

  // For tree table initialisation
  /*
  public TableModel reinit(TableModel        tm,
                           AnyTreeTableModel m,
                           Font              widthGauge)
  {
    //System.out.println("REINIT AS TREE TABLE");
    tm = initAsTreeTable(m, tm);
    init(tm, widthGauge);
    return tm;
  }
  */
  
  public boolean getScrollableTracksViewportHeight()
  {
    // The table will always be the height of its containing
    // viewport as a minimum.  Means that event handlers placed
    // on the table (eg popup menu) will work even if user clicks
    // outside the rows region (especially when the table model
    // has no rows!)
    Component parent = getParent();

    if (parent instanceof JViewport)
      return parent.getHeight() > getPreferredSize().height;

    return false;
  }

/*
  protected javax.swing.table.JTableHeader createDefaultTableHeader()
  {
    return new JTableHeader(columnModel);
  }
  */

  /**
   *
   */
  public JTree getTreeRenderer(AnyTreeTableModel treeTableModel)
  {
    if (tree_ == null)
    {
      // The (extension of) AnyTreeModel already has the levels
      // config in it.
      tree_ = new TreeTableCellRenderer(treeTableModel);
      tree_.setShowsRootHandles(true);
      tree_.setRootVisible(false);

      // This is mandatory at the moment, until we have column rendering
      // that we can specify for the root row.
      tree_.setRootVisible(false);

      // Create the table model implementation that delegates
      // to the tree model
      //tm = new AnyTreeTableModelAdapter(treeTableModel, tree_);

      // Force the JTable and JTree to share their row selection models.
      // Create the (extension of) the TreeSelectionModel...
      ListToTreeSelectionModelWrapper selectionWrapper = new
                              ListToTreeSelectionModelWrapper();

      // ...set it into the JTree
      tree_.setSelectionModel(selectionWrapper);

      // When we are put into TreeTable mode then we also put the
      // TreeSelectionModel's ListSelectionModel into the table.

      if (tree_.getRowHeight() < 1)
      {
        // Metal looks better like this.
        //setRowHeight(18);
        setRowHeight(getRowHeight());
      }
    }
    else
    {
      // Just allow for the fact that the model might be a new object.
      // See TreeTableCellRenderer.setModel(TreeModel)
      tree_.setModel(treeTableModel);
    }
    return tree_;
  }

  public void setModel(javax.swing.table.TableModel tableModel)
  {
    // Overridden to set up the appropriate selection model
    // dependent on whether the model is a TreeTable model.
    TableModel oldModel = (TableModel)this.getModel();

    if (oldModel == tableModel)
      return;

    TableModel newModel = (TableModel)tableModel;

    // Check to see if we need to reset the table's selection model
    if (newModel.isTreeTable() && (oldModel == null || !oldModel.isTreeTable()))
    {
      // Going to tree table mode.  Set table's list selection
      // model to that contained in the tree
      ListToTreeSelectionModelWrapper selectionWrapper =
        (ListToTreeSelectionModelWrapper)tree_.getSelectionModel();

      MultiListSelectionModel mlsm =
        (MultiListSelectionModel)this.getSelectionModel();

      mlsm.setTreeSelectionModel(selectionWrapper.getListSelectionModel());

      this.setDefaultRenderer(TreeTableModel.class, tree_);
      this.setDefaultEditor(TreeTableModel.class,
                            new TreeTableCellEditor());
    }
    else if (!newModel.isTreeTable() && (oldModel == null || oldModel.isTreeTable()))
    {
      // Going to flat mode.
      MultiListSelectionModel mlsm =
        (MultiListSelectionModel)this.getSelectionModel();
      mlsm.setFlatSelectionModel(new DefaultListSelectionModel());

      // Little bit messy but we come through here during initialisation
      // and the super class isn't ready for this yet.  OK because
      // we always start off in flat mode irrespective of whether we
      // later become a tree (and then go flat again etc)
      if (defaultRenderersByColumnClass != null)
      {
        this.setDefaultRenderer(TreeTableModel.class, null);
        this.setDefaultEditor(TreeTableModel.class, null);
      }
    }

    super.setModel(tableModel);
  }
  
  public boolean isMouseCell(int row, int col)
  {
    return anyTable_.isMouseCell(row, col);
  }
  
  /**
   * Overridden to message super and forward the method to the tree.
   * Since the tree is not actually in the component hieachy it will
   * never receive this unless we forward it in this manner.
   */
  public void updateUI()
  {
    super.updateUI();

    if(tree_ != null)
    {
	    tree_.updateUI();
    }

    // Use the tree's default foreground and background colors in the
    // table.
    // TS We need to reset this dynamically...
    LookAndFeel.installColorsAndFont(this,
                                     "Tree.background",
                                     "Tree.foreground",
                                     "Tree.font");
  }

  /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to
   * paint the editor. The UI currently uses different techniques to
   * paint the renderers and editors and overriding setBounds() below
   * is not the right thing to do for an editor. Returning -1 for the
   * editing row in this case, ensures the editor is never painted.
   */
  public int getEditingRow()
  {
    // TS - try removing this during testing
//    return (getColumnClass(editingColumn) == TreeTableModel.class)
//                  ? -1
//                  : editingRow;
    return editingRow;
  }

  /**
   * Overridden to pass the new rowHeight to the tree.
   */
  public void setRowHeight(int rowHeight)
  {
    super.setRowHeight(rowHeight);
    if (tree_ != null && tree_.getRowHeight() != rowHeight)
    {
      tree_.setRowHeight(getRowHeight());
    }
  }

  private Any callPrepareEditFunc(Call f, int row, int column)
  {
    Any ret = null;

    if (f != null)
        //&& lastEditRow_ != row)
    {
      Transaction t = Globals.process__.getTransaction();

      try
      {
        TableModel m = (TableModel)getModel();
        Any rowRoot = m.getRowAt(row);
        if (rowRoot == null)
          return null;
        
        column = this.convertColumnIndexToModel(column);
        AnyComponentEditor ace = (AnyComponentEditor)anyTable_.getModel().getCellEditor(row, column);

        if (fnArgs__ == null)
        {
          fnArgs__ = AbstractComposite.simpleMap();
          row__    = new AnyInt();
          col__    = new AnyInt();
        }

        fnArgs__.add(AnyTable.row__, rowRoot);
        fnArgs__.add(AnyView.component__, ace.getComponent());
        f.setArgs(fnArgs__);
        f.setTransaction(t);
        ret = f.exec(m.getContext());
        //lastEditRow_ = row;
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      finally
      {
        f.setArgs(null);
        fnArgs__.empty();
        f.setTransaction(null);
      }
    }
    return ret;
  }

  private void init(TableModel m)
  {
    init(m, null);
  }

	private void init(TableModel m, Font widthGauge)
	{
		//System.out.println("JTable.init: setting Renderers......");

		// Slam in the lamb!  Set up the table to render and edit
		// using the AnyRenderer class.  The magic AnyRenderer
		// does the rest!
    // Note, its OK to call this method with a null widthGauge
    // argument, as the default constructor does, as long as the
    // model has no columns.
		for (int i = 0; i < m.getColumnCount(); i++)
		{
			TableColumn tc = getColumnModel().getColumn(i);
			RenderInfo  r  = m.getRenderInfo(i);

			// Set up the renderer and editor
      // There should only be one column of class TreeTableModel.class
      if (m.getColumnClass(i) == TreeTableModel.class)
      {
        // TreeTable stuff uses column class mechanism to set the
        // renderer/editor
        tc.setCellRenderer(null);
        tc.setCellEditor(null);
      }
      else
      {
        //System.out.println("JTable.init: setting Renderer: " + i + " " + r);
        //AnyTableCellRenderer ar = new AnyTableCellRenderer(r);
        //AnyCellRenderer ar = new AnyCellRenderer(r);
        //tc.setCellRenderer(ar);

        // These are now set up on demand.
        //AnyComponentEditor ac = new AnyComponentEditor(r);
        //tc.setCellEditor(ac);
      }

			// Put the RenderInfo object into the column so we can
			// get it out again when detecting mouse events on
			// the table header.
			tc.setIdentifier(r);
/*
			// Work out column widths
			FontMetrics fm = getFontMetrics(widthGauge);
			System.out.println("JTable.init: setting width: " + r.getWidth());
      int fontWidth = r.getWidth() * fm.charWidth('n');

System.out.println("JTable.init: renderer 1 " + tc.getHeaderRenderer());
System.out.println("JTable.init: renderer 2 " + tc.getHeaderRenderer().getTableCellRendererComponent(this, r.getLabel(), false, false, 0, i));
System.out.println("JTable.init: preferred " + tc.getHeaderRenderer().getTableCellRendererComponent(this, r.getLabel(), false, false, 0, i).getPreferredSize());

      int headWidth =
        (int)tc.getHeaderRenderer().getTableCellRendererComponent(this, r.getLabel(), false, false, 0, i).getPreferredSize().getWidth() +
        getColumnModel().getColumnMargin()*2;
      if (headWidth > fontWidth)
        tc.setPreferredWidth(headWidth);
      else
        tc.setPreferredWidth(fontWidth);
        */
		}
	}

	public static class NotifyEdit
	{
		ArrayList listeners_ = new ArrayList();

		public void addTableEditListener(TableEditListener l)
		{
			listeners_.add(l);
		}

		public void removeTableEditListener(TableEditListener l)
		{
			int i = -1;
			if ((i = listeners_.indexOf(l)) >= 0)
				listeners_.remove(i);
		}

    public void fireEditDone(Object  source,
                             Any     rowRoot,
                             Any     rowKey,
                             int     row,
                             int     column,
                             Any     vOld,
                             Any     vNew,
                             boolean isUI,
                             boolean isBefore)
    {
      TableEditEvent e = null;
      for (int i = 0; i < listeners_.size(); i++)
      {
        if (e == null)
         e = new TableEditEvent(source,
                                rowRoot,
                                rowKey,
                                row,
                                column,
                                vOld,
                                vNew,
                                isUI,
                                isBefore);

        TableEditListener l = (TableEditListener)listeners_.get(i);
        l.editDone(e);
      }
    }

		public void fireEditCanceled(TableEditEvent e)
		{
      for (int i = 0; i < listeners_.size(); i++)
			{
				TableEditListener l = (TableEditListener)listeners_.get(i);
				l.editCanceled(e);
			}
		}
	}

  /**
   * A TableCellRenderer that displays a JTree.
   */
  public class TreeTableCellRenderer extends    JTree
                                     implements TableCellRenderer
  {
    /** Last table/tree row asked to renderer. */
    private int     visibleRow_;
    
    private boolean hasFocus_;
    private boolean isCellSelected_;

    public TreeTableCellRenderer(AnyTreeTableModel model)
    {
      super(model);
    }

    /**
     * updateUI is overridden to set the colors of the Tree's renderer
     * to match that of the table.
     */
    public void updateUI()
    {
      super.updateUI();

      // Make the tree's cell renderer use the table's cell selection
      // colors.
      // [TS this won't do anything if the Inq TreeCellRenderer is
      // not a DefaultTreeCellRenderer.]
      TreeCellRenderer tcr = this.getCellRenderer();
      if (tcr instanceof DefaultTreeCellRenderer)
      {
        DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr);
        // For 1.1 uncomment this, 1.2 has a bug that will cause an
        // exception to be thrown if the border selection color is
        // null.
        // dtcr.setBorderSelectionColor(null);
        dtcr.setTextSelectionColor
                     (UIManager.getColor("Table.selectionForeground"));

        dtcr.setBackgroundSelectionColor
                     (UIManager.getColor("Table.selectionBackground"));
      }
    }

    /**
     * Sets the row height of the tree, and forwards the row height to
     * the table.
     */
    public void setRowHeight(int rowHeight)
    {
      if (rowHeight > 0)
      {
        super.setRowHeight(rowHeight);
        if (JTable.this != null &&
            JTable.this.getRowHeight() != rowHeight)
        {
          JTable.this.setRowHeight(getRowHeight());
        }
      }
    }

    /**
     * This is overridden to set the height to match that of the JTable.
     */
    public void setBounds(int x, int y, int w, int h)
    {
      super.setBounds(x, 0, w, JTable.this.getHeight());
    }

    /**
     * Sublcassed to translate the graphics such that the last visible
     * row will be drawn at 0,0.
     */
    public void paint(Graphics g)
    {
      g.translate(0, -visibleRow_ * getRowHeight());
      super.paint(g);
    }

    // Override to optimise the case where the model object
    // is not changing.  JTable does this but JTree doesn't.
    public void setModel(TreeModel m)
    {
      if (m != treeModel)
        super.setModel(m);
    }

    /**
     * TableCellRenderer method. Overridden to update the visible row.
     */
    public Component getTableCellRendererComponent(javax.swing.JTable table,
                                                   Object             value,
                                                   boolean            isSelected,
                                                   boolean            hasFocus,
                                                   int                row,
                                                   int                column)
    {
//      if(isSelected)
//        setBackground(table.getSelectionBackground());
//      else
//        setBackground(table.getBackground());

      // Set default style to that of the table
      setFont(table.getFont());
      setBackground(table.getBackground());
      setForeground(table.getForeground());

      
      if (isSelected)
      {
        if (hasFocus)
        {
          setForeground(table.getSelectionForeground());
          setBackground(table.getSelectionBackground());
        }
        else
        {
          setBackground(Color.LIGHT_GRAY);
          setForeground(table.getForeground());
        }
      }
      else
      {
        com.inqwell.any.client.swing.JTable iT = (com.inqwell.any.client.swing.JTable)table;
        TableModel m = iT.getAnyTable().getModel();
        AnyAttributeSet rowStyle = null;
        AnyAttributeSet style = null;
        TreeLevel level = m.getTreeLevel(row);
        AnyCellRenderer r = level.getRenderer();
        if (iT.getRowStyle() != null || r.getStyle() != null)
        {
          int modelColumn = table.convertColumnIndexToModel(column);
          
          Any rowRoot = m.getRowAt(row);
          if (rowRoot != null)
          {
            Any contextNode     = m.getContext();
            Any colName         = m.getNameOfColumn(modelColumn);
            boolean isMouseCell = iT.isMouseCell(row, modelColumn);
            if (iT.getRowStyle() != null)
              rowStyle = callStyleFunc(
                                iT.getRowStyle(),
                                contextNode,
                                rowRoot,
                                (Any)value,
                                row,
                                modelColumn,
                                colName,
                                isMouseCell,
                                level);
            
            if (r.getStyle() != null)
              style = callStyleFunc(
                  r.getStyle(),
                  contextNode,
                  rowRoot,
                  (Any)value,
                  row,
                  modelColumn,
                  colName,
                  isMouseCell,
                  level);
            
            // If there is a background then for treetables to have the whole
            // cell filled in we set the tree's background colour
            if (style != null)
            {
              Color bg = (Color) style.getAttributeSet().getAttribute(StyleConstants.Background);
              
              if (bg != null && !bg.equals(getBackground()))
                setBackground(bg);
            }
            else
              if (rowStyle != null)
              {
                Color bg = (Color) rowStyle.getAttributeSet().getAttribute(StyleConstants.Background);
        
                if (bg != null && !bg.equals(getBackground()))
                  setBackground(bg);
              }
            
          }
        }
      }

      // Tell the table about our width. Note that these widths don't ever
      // shrink so the table cell will spring to the widest the tree has
      // ever been (as deeper nodes are expanded) even if its current state
      // is somewhat collapsed. This may be OK all the same though.
      com.inqwell.any.client.swing.JTable aTable = (com.inqwell.any.client.swing.JTable)table;
      aTable.getAnyTable().setMaxRenderedWidth(column, this.getPreferredSize().width);

      visibleRow_ = row;
      hasFocus_   = hasFocus;
      isCellSelected_ = isSelected;
      
      return this;
    }
    
    public AnyTable getAnyTable()
    {
      return anyTable_;
    }
    
    public Any getRowStyle()
    {
      return styleFunc_;
    }
    
    public boolean isTreeTableRenderer()
    {
      return true;
    }
    
    public boolean hasCellFocus()
    {
      return hasFocus_;
    }

    public boolean isCellSelected()
    {
      return isCellSelected_;
    }
    
    public JTable getJTable()
    {
      return JTable.this;
    }
    
    private AnyAttributeSet callStyleFunc(Any       styleFunc,
                                          Any       contextNode,
                                          Any       rowRoot,
                                          Any       value,
                                          int       row,
                                          int       column,
                                          Any       colName,
                                          boolean   isMouseCell,
                                          TreeLevel level)
    {
      Any ret = AnyCellRenderer.callCellFunc(
          (Call)styleFunc,
          getAnyTable(),
          null, // The cell renderer. We're the tree which is not wrapped...
          null,
          contextNode,
          rowRoot,
          value,
          null,  // No new value when rendering
          row,
          null, // TODO rowKey,
          column,
          colName,
          isMouseCell,
          null,  // No isUser when rendering
          null,  // No after when rendering
          level,
          null,  // No is leaf when calling as a table cell renderer
          null); // As above, no expanded
      
      if (ret == null || AnyNull.isNullInstance(ret))
        return null;
      
      if (!(ret instanceof AnyAttributeSet))
        throw new IllegalArgumentException("Style function must return a style or null");
      
      return (AnyAttributeSet)ret;
    }
  }

  /**
   * Handle varying selection models without the JTable having to
   * know which one is in effect at any given time. This means that
   * any event listeners setup on JTable.getSelectionModel() will
   * not be lost when the selection model changes.
   * <p>
   * Handles changes between flat and tree table modes without the
   * JTable's selection model actually changing.
   */
  public class MultiListSelectionModel implements ListSelectionModel,
                                                  ListSelectionListener
  {
    // The one of these that is non-null is the currently active one
    private ListSelectionModel flatSelectionModel_;
    private ListSelectionModel treeSelectionModel_;

    private ArrayList          listenerList_;

    private MultiListSelectionModel()
    {
      // Default to flat selection model
      setFlatSelectionModel(new DefaultListSelectionModel());
    }

    private void setFlatSelectionModel(ListSelectionModel lsm)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.removeListSelectionListener(this);

      if (treeSelectionModel_ != null)
      {
        treeSelectionModel_.removeListSelectionListener(this);
        treeSelectionModel_ = null;
      }

      lsm.addListSelectionListener(this);
      flatSelectionModel_ = lsm;
    }

    private void setTreeSelectionModel(ListSelectionModel lsm)
    {
      if (treeSelectionModel_ != null)
        treeSelectionModel_.removeListSelectionListener(this);

      if (flatSelectionModel_ != null)
      {
        flatSelectionModel_.removeListSelectionListener(this);
        flatSelectionModel_ = null;
      }

      lsm.addListSelectionListener(this);
      treeSelectionModel_ = lsm;
    }

    // ListSelectionListener.  Forward events to our listeners
    public void valueChanged(ListSelectionEvent e)
    {
      for (int i = 0; i < listenerList_.size(); i++)
        ((ListSelectionListener)listenerList_.get(i)).valueChanged(e);
    }

    public void addListSelectionListener(ListSelectionListener x)
    {
      if (listenerList_ == null)
        listenerList_ = new ArrayList(5);

      listenerList_.add(x);
    }

    public void addSelectionInterval(int index0, int index1)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.addSelectionInterval(index0, index1);
      else
        treeSelectionModel_.addSelectionInterval(index0, index1);
    }

    public void clearSelection()
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.clearSelection();
      else
        treeSelectionModel_.clearSelection();
    }

    public int getAnchorSelectionIndex()
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.getAnchorSelectionIndex();
      else
        return treeSelectionModel_.getAnchorSelectionIndex();
    }

    public int getLeadSelectionIndex()
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.getLeadSelectionIndex();
      else
        return treeSelectionModel_.getLeadSelectionIndex();
    }

    public int getMaxSelectionIndex()
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.getMaxSelectionIndex();
      else
        return treeSelectionModel_.getMaxSelectionIndex();
    }

    public int getMinSelectionIndex()
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.getMinSelectionIndex();
      else
        return treeSelectionModel_.getMinSelectionIndex();
    }

    public int getSelectionMode()
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.getSelectionMode();
      else
        return treeSelectionModel_.getSelectionMode();
    }

    public boolean getValueIsAdjusting()
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.getValueIsAdjusting();
      else
        return treeSelectionModel_.getValueIsAdjusting();
    }

    public void insertIndexInterval(int index, int length, boolean before)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.insertIndexInterval(index, length, before);
      else
        treeSelectionModel_.insertIndexInterval(index, length, before);
    }

    public boolean isSelectedIndex(int index)
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.isSelectedIndex(index);
      else
        return treeSelectionModel_.isSelectedIndex(index);
    }

    public boolean isSelectionEmpty()
    {
      if (flatSelectionModel_ != null)
        return flatSelectionModel_.isSelectionEmpty();
      else
        return treeSelectionModel_.isSelectionEmpty();
    }

    public void removeIndexInterval(int index0, int index1)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.removeIndexInterval(index0, index1);
      else
        treeSelectionModel_.removeIndexInterval(index0, index1);
    }

    public void removeListSelectionListener(ListSelectionListener x)
    {
      int i;
      if ((i = listenerList_.indexOf(x)) >= 0)
        listenerList_.remove(i);
    }

    public void removeSelectionInterval(int index0, int index1)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.removeSelectionInterval(index0, index1);
      else
        treeSelectionModel_.removeSelectionInterval(index0, index1);
    }

    public void setAnchorSelectionIndex(int index)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.setAnchorSelectionIndex(index);
      else
        treeSelectionModel_.setAnchorSelectionIndex(index);
    }

    public void setLeadSelectionIndex(int index)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.setLeadSelectionIndex(index);
      else
        treeSelectionModel_.setLeadSelectionIndex(index);
    }

    public void setSelectionInterval(int index0, int index1)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.setSelectionInterval(index0, index1);
      else
        treeSelectionModel_.setSelectionInterval(index0, index1);
    }

    public void setSelectionMode(int selectionMode)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.setSelectionMode(selectionMode);
      else
        treeSelectionModel_.setSelectionMode(selectionMode);
    }

    public void setValueIsAdjusting(boolean valueIsAdjusting)
    {
      if (flatSelectionModel_ != null)
        flatSelectionModel_.setValueIsAdjusting(valueIsAdjusting);
      else
        treeSelectionModel_.setValueIsAdjusting(valueIsAdjusting);
    }
  }

  /**
   * TreeTableCellEditor implementation. Component returned is the
   * JTree.
   */
  public class TreeTableCellEditor implements TableCellEditor,
                                              CellEditorListener,
                                              InqEditor
  {
    // If we are active editing the tree cell then this is the
    // editor configured for the level that is in effect.
    private TableCellEditor lastEditor_;
    
    private ArrayList cellEditorListeners_ = new ArrayList();
    
    public Component getTableCellEditorComponent(javax.swing.JTable table,
                                                 Object             value,
                                                 boolean            isSelected,
                                                 int                row,
                                                 int                column)
    {
      // We only get here if we've previously passed all the checks
      // for cell editablity. This implementation acts as a proxy
      // for the editor (if any) configured in the model so try
      // to get it.
      
      // The value is that yielded by the table model, which is a
      // tree table model adapter. Thus, it is (apart from not being the
      // *responsible* value OK for the editor (that is it
      // is not a tree node, in case you were wondering). The delegate
      // editor is responsible for fetching the responsible value from
      // the model.
      
      Component ret = null;
      
      TableModel m = anyTable_.getModel();
      lastEditor_ = m.getCellEditor(row, convertColumnIndexToModel(column));
      
      // There might not be an editor and at this late stage this is
      // the only remaining way of vetoing the editing process
      if (lastEditor_ != null)
      {
        ret = lastEditor_.getTableCellEditorComponent(table,
                                                      value,
                                                      isSelected,
                                                      row,
                                                      column);
        
        if (ret != null)
          lastEditor_.addCellEditorListener(this);
        else
          lastEditor_ = null;
      }
      
      return ret;
    }

    /**
     * Overridden to return false, and if the event is a mouse event
     * it is forwarded to the tree.<p>
     * The behavior for this is debatable, and should really be offered
     * as a property. By returning false, all keyboard actions are
     * implemented in terms of the table. By returning true, the
     * tree would get a chance to do something with the keyboard
     * events. For the most part this is ok. But for certain keys,
     * such as left/right, the tree will expand/collapse where as
     * the table focus should really move to a different column. Page
     * up/down should also be implemented in terms of the table.
     * By returning false this also has the added benefit that clicking
     * outside of the bounds of the tree node, but still in the tree
     * column will select the row, whereas if this returned true
     * that wouldn't be the case.
     * <p>By returning false we are also enforcing the policy that
     * the tree will never be editable (at least by a key sequence).
     */
    public boolean isCellEditable(EventObject e)
    {
      // Comments above by TreeTable authors at JavaSoft. In order
      // to get this far the method TableModel.isCellEditable()
      // must have returned true and this class returned as the
      // editor from JTable.getCellEditor(). This method is the
      // final arbiter of whether the cell is really editable but
      // doubles as the way of passing events through to the
      // JTree renderer.
      if (e instanceof MouseEvent)
      {
        MouseEvent me = (MouseEvent)e;
        
        // Unfortunately the editing row/column is not known yet
        // so work them out again here. [May need to make this possible
        // somehow for k/b events as there is to event supplied in that
        // case... but may be processKeyBindings can help.]
        Point p = me.getPoint();
        int row = rowAtPoint(p);
        int col = columnAtPoint(p);
        
        Rectangle treeRect = tree_.getRowBounds(row);
        
        // The treeRect does not seem to include the "handle" icon. While
        // this can be suppressed for the root (or top-level rows when the
        // root is not displayed) tree_ is initialised to show handles
        // and not show the root (best for list format). Save caveat for leaves.
        // Just means the "rectangle of insensivity" for table editor events
        // is a bit larger. No matter...
        boolean expanded = tree_.isExpanded(row);
        Icon icon;
        if (expanded)
          icon = UIManager.getIcon("Tree.expandedIcon");
        else
          icon = UIManager.getIcon("Tree.collapsedIcon");
        
        if (icon != null)
        {
          if (tree_.getComponentOrientation().isLeftToRight())
          {
            treeRect.x -= icon.getIconWidth();
            treeRect.x -= UIManager.getInt("Tree.leftChildIndent");
          }
          else
          {
            treeRect.width += icon.getIconWidth();
            treeRect.width += UIManager.getInt("Tree.leftChildIndent");
          }
        }
        
        //Rectangle tableRect = getCellRect(row, col, true);
        
        // If the event is inside the tree then dispatch it there
        if (treeRect.contains(p))
        {
          MouseEvent newME = new MouseEvent(tree_,
                                            me.getID(),
                                            me.getWhen(),
                                            me.getModifiers(),
                                            me.getX() - getCellRect(0, col, true).x,
                                            me.getY(),
                                            me.getClickCount(),
                                            me.isPopupTrigger());
          tree_.dispatchEvent(newME);
          return false;
        }
        else
        {
          // When outside the tree see if there is an editor for the cell
          // and if so ask that, otherewise return false
          TableModel      m      = (TableModel)JTable.this.getModel();
          TableCellEditor editor = m.getCellEditor(row, convertColumnIndexToModel(col));
          if (editor != null)
            return editor.isCellEditable(e);
          
          return false;
        }
      }
      else
      {
        // Keyboard events or other programmatic edit cell
        return true;
      }
    }
    
    // CellEditorListener
    public void editingStopped(ChangeEvent e)
    {
      lastEditor_.removeCellEditorListener(this);
      
      // If the event source is the current editor then change
      // the event supplying this as the source. See for example
      // JTable.editingStopped. This means that the reason editing
      // stopped was of the underlying UI, not a user event in the
      // component itself.
      
      if (e.getSource() == lastEditor_)
        e = new ChangeEvent(this);
      
      fireEditingStopped(e);
    }

    public void editingCanceled(ChangeEvent e)
    {
      lastEditor_.removeCellEditorListener(this);
      lastEditor_ = null;
      fireEditingCanceled(e);
    }
    
    public void removeCellEditorListener(CellEditorListener l)
    {
      int indx;
      if ((indx = cellEditorListeners_.indexOf(l)) >= 0)
        cellEditorListeners_.remove(indx);

      lastEditor_.removeCellEditorListener(this);
      lastEditor_ = null;
    }

    public Object getCellEditorValue() 
    {
      return lastEditor_.getCellEditorValue();
    }
    
    public boolean stopCellEditing()
    {
      return lastEditor_.stopCellEditing();
    }

    public void cancelCellEditing()
    {
      lastEditor_.cancelCellEditing();
    }

    public boolean shouldSelectCell(EventObject anEvent)
    {
      return true;
    }

    public void addCellEditorListener(CellEditorListener l)
    {
      // We take over the maintenance of the action listeners
      // registered (in fact only the combo box) as the
      // underlying editor component is allowed to change
      cellEditorListeners_.add(l);
    }

    public boolean canStartEditing(AnyComponent parentComponent,
                                   Any          contextNode,
                                   Any          rowRoot,
                                   Any          value,
                                   int          row,
                                   Any          rowKey,
                                   int          col,
                                   Any          colName,
                                   TreeLevel    level,
                                   BooleanI     isLeaf,
                                   BooleanI     expanded)
    {
      // Can't use lastEditor_ as its not set up yet
      TableModel m = anyTable_.getModel();
      AnyComponentEditor ace = (AnyComponentEditor)m.getCellEditor(row, col);

      return ace.canStartEditing(parentComponent,
                                 contextNode,
                                 rowRoot,
                                 value,
                                 row,
                                 rowKey,
                                 col,
                                 colName,
                                 level,
                                 isLeaf,
                                 expanded);
    }
    
    public boolean hasStopEdit()
    {
      boolean ret = false;
      
      // In fact, should always be called when lastEditor_ is set up,
      // as we are in the process of editing still
      if (lastEditor_ != null)
      {
        InqEditor ie = (InqEditor)lastEditor_;
        ret = ie.hasStopEdit();
      }
      
      return ret;
    }
    
    public void onStopEditing(AnyComponent parentComponent,
                              Any          contextNode,
                              Any          rowRoot,
                              Any          oldValue,
                              Any          newValue,
                              int          row,
                              Any          rowKey,
                              int          col,
                              Any          colName,
                              BooleanI     isUser,
                              BooleanI     after,
                              TreeLevel    level,
                              BooleanI     isLeaf,
                              BooleanI     expanded)
    {
      InqEditor ie = (InqEditor)lastEditor_;
      
      // Not that useful, probably, in the context of editing so just
      // for completeness. The arguments will always be null because we
      // are called from a table context.
      TreePath treePath = tree_.getPathForRow(row);
      AnyTreeNode n = (AnyTreeNode)treePath.getLastPathComponent();   
      if (isLeaf__ == null)
      {
        isLeaf__   = new AnyBoolean();
        expanded__ = new AnyBoolean();
      }
      isLeaf__.setValue(n.isLeaf());
      expanded__.setValue(n.isExpanded());
      
      ie.onStopEditing(parentComponent,
                       contextNode,
                       rowRoot,
                       oldValue,
                       newValue,
                       row,
                       rowKey,
                       col,
                       colName,
                       isUser__,
                       after__,
                       level,
                       isLeaf__,
                       expanded__);
    }
    
    private void fireEditingStopped(ChangeEvent ce)
    {
      for (int i = 0; i < cellEditorListeners_.size(); i++)
      {
        CellEditorListener cl = (CellEditorListener)cellEditorListeners_.get(i);
        cl.editingStopped(ce);
      }
    }

    private void fireEditingCanceled(ChangeEvent ce)
    {
      for (int i = 0; i < cellEditorListeners_.size(); i++)
      {
        CellEditorListener cl = (CellEditorListener)cellEditorListeners_.get(i);
        cl.editingCanceled(ce);
      }
    }
}

  /**
   * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
   * to listen for changes in the ListSelectionModel it maintains.  This
   * ListSelectionModel has been put into the JTable so as selection
   * changes take place, the paths are updated in the
   * DefaultTreeSelectionModel.
   */
  class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
  {
    /** Set to true when we are updating the ListSelectionModel. */
    protected boolean         updatingListSelectionModel;

    public ListToTreeSelectionModelWrapper()
    {
	    super();

      // The ListSelectionModel that this TreeSelectionModel contains
      // has been placed as the ListSelectionModel of the table.
      // Listen to the table selections and update the tree selection so
      // that its selected paths are in sync with the selected rows.
	    getListSelectionModel().addListSelectionListener
	                            (createListSelectionListener());
    }

    /**
     * Returns the list selection model. ListToTreeSelectionModelWrapper
     * listens for changes to this model and updates the selected paths
     * accordingly.
     */
    ListSelectionModel getListSelectionModel()
    {
      return listSelectionModel;
    }

    /**
     * This is overridden to set <code>updatingListSelectionModel</code>
     * and message super. This is the only place DefaultTreeSelectionModel
     * alters the ListSelectionModel.
     */
    public void resetRowSelection()
    {
	    if(!updatingListSelectionModel)
      {
        updatingListSelectionModel = true;
        try
        {
          super.resetRowSelection();
        }
        finally
        {
          updatingListSelectionModel = false;
        }
	    }

	    // Notice how we don't message super if
	    // updatingListSelectionModel is true. If
	    // updatingListSelectionModel is true, it implies the
	    // ListSelectionModel has already been updated and the
	    // paths are the only thing that needs to be updated.
    }

    /**
     * Creates and returns an instance of ListSelectionHandler.
     */
    protected ListSelectionListener createListSelectionListener()
    {
      return new ListSelectionHandler();
    }

    /**
     * If <code>updatingListSelectionModel</code> is false, this will
     * reset the selected paths from the selected rows in the list
     * selection model.
     */
    protected void updateSelectedPathsFromSelectedRows()
    {
	    if(!updatingListSelectionModel)
      {
        updatingListSelectionModel = true;
        try
        {
          // This is way expensive, ListSelectionModel needs an
          // enumerator for iterating.
          int        min = listSelectionModel.getMinSelectionIndex();
          int        max = listSelectionModel.getMaxSelectionIndex();

          // Clear the current tree selection
          clearSelection();

          if(min != -1 && max != -1)
          {
            for(int counter = min; counter <= max; counter++)
            {
              if(listSelectionModel.isSelectedIndex(counter))
              {
                TreePath selPath = tree_.getPathForRow(counter);

                if(selPath != null)
                  addSelectionPath(selPath);
              }
            }
          }
        }
        finally
        {
          updatingListSelectionModel = false;
        }
	    }
    }

    /**
     * Class responsible for calling updateSelectedPathsFromSelectedRows
     * when the selection of the list changse.
     */
    class ListSelectionHandler implements ListSelectionListener
    {
      public void valueChanged(ListSelectionEvent e)
      {
        // How about checking for isValueAdjusting in here?  TBI....
        updateSelectedPathsFromSelectedRows();
      }
    }
  }
}
