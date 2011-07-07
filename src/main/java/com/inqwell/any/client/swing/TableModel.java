/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.Event;
import com.inqwell.any.IntI;
import com.inqwell.any.Map;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.AnyTable;
import com.inqwell.any.client.RenderInfo;
import com.inqwell.any.client.TreeLevel;

/**
 * Extensions to the standard swing TableModel for implementations in
 * the Any framework
 */
public interface TableModel extends javax.swing.table.TableModel
{
  /**
   * Return the responsible value, as opposed to the rendered value,
   * for the given row and column.  The responsible value is only
   * different from the rendered value if a specific responsible value
   * has been established in the RenderInfo
   */
	public Any getResponsibleValueAt(int row, int column);

  /**
   * Provide the node from which the table root will be evaluated
   */
  public void setContext(Any context);

  public Any getContext();

  /**
   * Set the columns this table will be rendering.
   */
  public void setColumns(Vectored columns);

  /**
   * Set additional property information about the rendered
   * columns.
   */
  public void setColumnProperties(Vectored columns);
  
  public void setRowFunction(Any rowFuncF);
  
  public Vectored getColumnProperties();
  
  public Any getNameOfColumn(int col);
  
  public Vectored getColumns();

  public Map getLevels();
  
  public void setTable(AnyTable table);
  
  /**
   * Returns the RenderInfo object that is rendering the specified
   * column.  The column number is specified in the model's column
   * index.  If the view reorders the columns it must convert the
   * view index to the model index.
   */
	public RenderInfo getRenderInfo(int c);

  /**
   * Return the cell editor suitable for editing the cell
   * at the specified row and column.  If a row of -1 is
   * specified then the implementation may throw an exception
   * or create an editor for the column as a whole, as
   * appropriate.
   */
  public TableCellEditor getCellEditor(int row, int column);
  
  /**
   * Return the cell renderer suitable for rendering the cell
   * at the specified row and column.
   */
  public TableCellRenderer getCellRenderer(int row, int column);
  
  /**
   * Set the cell editor for the specified row and column.
   * The implementation may be mode limited than to support
   * editors on a cell by cell basis.
   * @param row the row for which the editor is being specified.
   * If <code>row == -1</code> then the row is not being specified.
   * @param column the column for which the editor is being specified.
   * @return the cell editor for the given column
   * or <code>null</code> if no editor has been configured.
  */
  public void setCellEditor(TableCellEditor tce, int row, int column);
  
  /**
   * Allows the table model to specify a number of visible rows hint.
   */
  public void setVisibleRows(int visibleRows);

  public int getVisibleRows();

  /**
   * Returns the real number of rows in the model.  This may not be the
   * same as the value given by <code>super.getRowCount()</code>
   * as the model is at liberty to return the number of visible rows
   * when the root data node cannot be resolved.
   */
	public int getRealRowCount();

  /**
   * Leave objects representing the current selection, keySelection,
   * indexSelection and the count of selected items in the supplied
   * variables according to the state of the given ListSelectionModel.
   */
	public Map newSelection(ListSelectionModel lm,
                          Map                selection,
                          Array              keySelection,
                          Array              indexSelection,
                          IntI               selectCount);

  /**
   * Establish the necessary selection intervals to
   * select the given items in the list model.
   * @param l the <code>ListSelectionModel</code> that will
   * be modified.
   * @param selection the names (map keys) of the row root
   * nodes to be selected.
   * @param newSelection the result of the map keys of the row root
   * nodes to the nodes themselves.
   */
  public void setItemSelection(ListSelectionModel l,
                               Array selection,
                               Map   newSelection);

  public void setIndexSelection(ListSelectionModel l,
                                Array selection,
                                Map   newSelection);

  /**
   * Tell this model where to find the root of its data.  This
   * will remove any editor if the table is editing.
   */
  public void setModelRoot(Any newRoot, JTable t) throws AnyException;

  public Any getModelRoot();
  
  /**
   * Sort the model using the given comparator. If the sort was
   * not performed false is returned, otherwise true.
   */
  public boolean sort (OrderComparator oc);

  /**
   * Sort the model according to the supplied items, which is a list
   * of column numbers in model coordinates, in the specified order.
   */
	public boolean sort(int[] orderItems, boolean isDescending, JTree tree) throws AnyException;

  /**
   * Convert the received node event into a JTable event.  Only events
   * that relate to the given table will be dispatched to the model.
   * This method may cancel any edit in progress, if the event affects
   * the editing cell.
   * <p>
   * The <code>tree<code> argument is only available when the implementation
   * supports the TreeTableModel and when the view provides it.
   */
  public boolean translateEvent(Event e, JTable table, JTree tree) throws AnyException;

  /**
   * Return a mapping of object paths to leaf node fields that should
   * be dispatched to views of this model.
   */
  public Map getEventPaths();

  /**
   * Returns the row-root node for the specified row.
   */
	public Any getRowAt(int row);

  /**
   * Returns the key (that is the name) of the specified row.
   */
	public Any getRowKey(int row);
  
  /**
   * Whether this model supports a tree-table or just a table.  In
   * general, a model implementation will be either a tree table
   * or a flat table, but not both.
   */
  public boolean isTreeTable();
  
  /**
   * Return the underlying tree model.  Optional operation, supported
   * only if the implementation is a tree table
   */
  public TreeModel getTreeModel();
  
  /**
   * Return the TreeLevel relevant to the given row. TODO: Expansions
   * @param row
   * @return The TreeLevel
   */
  public TreeLevel getTreeLevel(int row);

  /**
   * Return the renderer for the tree table.  Optional operation,
   * supported only if the implementation is a tree table.
   */
  public JTree getTreeRenderer();
  
  public void fireTableRowsUpdated(int firstRow, int lastRow);

  public void fireTableCellUpdated(int row, int col);

}
