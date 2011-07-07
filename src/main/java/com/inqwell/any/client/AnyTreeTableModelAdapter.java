/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-02 20:19:56 $
 */
package com.inqwell.any.client;

import javax.swing.ListSelectionModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstInt;
import com.inqwell.any.Event;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.JTable;
import com.inqwell.any.client.swing.JTree;
import com.inqwell.any.client.swing.TableModel;
import com.inqwell.any.client.swing.TreeTableModelAdapter;

public class AnyTreeTableModelAdapter extends    TreeTableModelAdapter
                                      implements TableModel
{
  private static final long serialVersionUID = 1L;

  // We could (some would say should) define an interface for these
  // methods in an Inq extended TreeModel, have AnyTreeModel implement
  // it and place the code in the methods of this class there.  Only
  // a problem if we define another TreeModel implementation.  
  private AnyTreeTableModel attm_;
  
  private Map levelSelection_ = AbstractComposite.orderedMap();
  
  public AnyTreeTableModelAdapter(AnyTreeTableModel attm, JTree tree)
  {
    super(attm, tree);
    attm_ = attm;
  }
  
  /**
   * Return the responsible value, as opposed to the rendered value,
   * for the given row and column.  The responsible value is only
   * different from the rendered value if a specific responsible value
   * has been established in the RenderInfo
   */
	public Any getResponsibleValueAt(int row, int column)
  {
    AnyTreeNode n = (AnyTreeNode)nodeForRow(row);
    return attm_.getResponsibleValueAt(n, column);
  }

  /**
   * Provide the node from which the table root will be evaluated
   */
  public void setContext(Any context)
  {
    attm_.setContext(context);
  }

  public Any getContext()
  {
    return attm_.getContext();
  }

  /**
   * Set the columns this table will be rendering.
   */
  public void setColumns(Vectored columns)
  {
    attm_.setColumns(columns);
  	fireTableStructureChanged();
  }

  public Vectored getColumns()
  {
    return attm_.getColumns();
  }
  
  public Map getLevels()
  {
    return attm_.getLevels();
  }

  /**
   * Returns the RenderInfo object that is rendering the specified
   * column.  The column number is specified in the model's column
   * index.  If the view reorders the columns it must convert the
   * view index to the model index.
   */
	public RenderInfo getRenderInfo(int c)
  {
    return attm_.getRenderInfo(c);
  }

  /**
   * Allows the table model to specify a number of visible rows hint.
   */
	public void setVisibleRows(int visibleRows)
	{
    attm_.setVisibleRows(visibleRows);
	}

	public int getVisibleRows()
  {
    return attm_.getVisibleRows();
  }

  /**
   * Returns the real number of rows in the model.  This may not be the
   * same as the value given by <code>super.getRowCount()</code>
   * as the model is at liberty to return the number of visible rows
   * when the root data node cannot be resolved.
   * <P>
   * For a tree table, the real row count depends on the expansion
   * state of the tree.
   */
  public int getRealRowCount()
  {
    return super.getRowCount();
  }

  public boolean isTreeTable()
  {
    return true;
  }
  
  public TreeModel getTreeModel()
  {
    return attm_;
  }

  /**
   * Leave objects representing the current selection, keySelection
   * and the count of selected items in the supplied variables
   * according to the state of the given ListSelectionModel.
   * <p>
   * In the case of a tree table, the 
   */
	public Map newSelection(ListSelectionModel lm,
                          Map                selection,
                          Array              keySelection,
                          Array              indexSelection,
                          IntI               selectCount)
  {
	  selection.empty();
	  levelSelection_.empty();
    selectCount.setValue(0);

	  if (keySelection != null)
	    keySelection.empty();
	  
	  if (indexSelection != null)
	    indexSelection.empty();
	  
	  if (getRealRowCount() != 0)
	  {
  		int minI = lm.getMinSelectionIndex();
  		int maxI = lm.getMaxSelectionIndex();
  
      int count = 0;
  		
  		if (minI >= 0)
  		{
  			for (int i = minI; i <= maxI; i++)
  			{
  				if (lm.isSelectedIndex(i))
  				{
            // TBD we would need to make provision for the root node,
            // which, for the moment we are not displaying, so it can't
            // yet be selected.
            
            AnyTreeNode n = (AnyTreeNode)nodeForRow(i);
            Any a = null;
            if (!n.isExpansion())  // Expansions TBD in this context
              a = n.getAny();
            else
              a = n.getAny();
  
            if (a != null)
  					{
              TreeLevel l = n.getTreeLevel(); // More expansions TBD
              
              Composite c = (Composite)a;
              Any k;
  						selection.add(k = c.getNameInParent(), a);
              levelSelection_.add(k, l);
              count++;
  						
  						if (keySelection != null)
              {
                keySelection.add(k);
              }
              
              if (indexSelection != null)
              {
                indexSelection.add(AbstractValue.flyweightConst(new ConstInt(i)));
              }
  					}
  				}
  			}
  		}
    
  		selectCount.setValue(count);
	  }
	  
    return levelSelection_;
  }

  /**
   * Establish the necessary selection intervals to
   * select the given items in the list model.
   * <p>
   * For a TreeTable the number of rows varies with the
   * expansion state of the tree. Nodes that are not
   * currently displayed will not be selected.
   */
  public void setItemSelection(ListSelectionModel l,
                               Array selection,
                               Map   newSelection)
  {
 		int size = this.getRealRowCount();
 		Array s = selection.shallowCopy();
    newSelection.empty();

 		for (int i = 0; i < size; i++)
 		{
 			Map m = (Map)getRowAt(i);
      Any k = null;
      
      // Different from the flat table implementation, which uses
      // m.getUniqueKey().  May be this is better.
      if (m != null)
        k = m.getNameInParent();

 			int index = s.indexOf(k);
      System.out.println("Index of " + k + " is " + index);
 			if (index >= 0)
 			{
 				l.addSelectionInterval(i, i);
 				s.remove(index);
 				//newSelection.add(k, m);  // If selection events go out then this not reqd?
        if (s.entries() == 0)
          break;
 			}
 		}
  }

  public void setIndexSelection(ListSelectionModel l,
                                Array selection,
                                Map   newSelection)
  {
    // Bit hasty and possibly too weak for trees - revisit later
    int size = selection.entries();
    AnyInt ii = new AnyInt();
    for (int i = 0; i < size; i++)
    {
      ii.copyFrom(selection.get(i));
      l.addSelectionInterval(ii.getValue(), ii.getValue());
    }
  }

  
  /**
   * Tell this model where to find the root of its data.  This
   * will remove any editor if the table is editing.
   * <p>
   * This is an undefined operation for a TreeTable and setLevels
   * must be used to establish the model.
   */
  public void setModelRoot(Any newRoot, JTable t) throws AnyException
  {
    throw new UnsupportedOperationException("TreeTable requires levels");
  }

  public Any getModelRoot()
  {
    throw new UnsupportedOperationException("TreeTable has levels");
  }

  public void setTable(AnyTable table)
  {
    attm_.setTable(table);
  }
  
  /**
   * Sort the model using the given comparator
   */
  public boolean sort (OrderComparator oc)
  {
    // TBD Er, tricky one as the comparator would likely not work
    // at all levels where the tree is heterogeneous.  If it is
    // recursive then would be OK.  What to do?
    return false;
  }

  /**
   * Sort the model according to the supplied items, which is a list
   * of column numbers in model coordinates, in the specified order.
   */
	public boolean sort(int[] orderItems, boolean isDescending, JTree tree) throws AnyException
  {
    // In contrast to sort(OrderComparator) this method can be applied
    // to a heterogeneous tree, as it only specifies column indices.
    // We can thus apply the appropriate rendering information, which
    // can be overridden at specific tree levels.
    attm_.sort(orderItems, isDescending, tree);
    
    return true;
  }

  public TableCellEditor getCellEditor(int row, int column)
  {
    // This method is called with row == -1 when fetching the
    // editor as a property of the (column of) the table.  In
    // TreeTable mode we must account for this and it means
    // the "global" (i.e. not level-specific) editor
    AnyTreeNode n = null;
    if (row >= 0)
      n = (AnyTreeNode)nodeForRow(row);
    
    return attm_.getCellEditor(n, column);
  }
  
  public void setCellEditor(TableCellEditor tce, int row, int column)
  {
    // AnyTreeNode n = (AnyTreeNode)nodeForRow(row);
    
    // In fact, this method can only affect the default editors, i.e
    // those that would be used if one is not provided by the
    // TreeLevel.  We don't really set editors by row anyway, and
    // there may be (probably are) no rows when this is called.
    
    attm_.setCellEditor(tce, row, column);
  }
  
  /**
   * Fetches the renderer for the specfied row and column. If
   * the row is < 0 then no node is determined and the renderer
   * is that configured at the table level. Otherwise the
   * node for the specified row may specify a particular renderer.
   * @return the cell renderer
   */
  public TableCellRenderer getCellRenderer(int row, int column)
  {
    // See getTableCellEditor
    
    AnyTreeNode n = null;
    if (row >= 0)
      n = (AnyTreeNode)nodeForRow(row);
    
    return attm_.getCellRenderer(n, column);
  }
  
  /**
   * Convert the received node event into a JTable event.  Only events
   * that relate to the given table will be dispatched to the model.
   * This method may cancel any edit in progress, if the event affects
   * the editing cell.
   */
  public boolean translateEvent(Event e, JTable table, JTree tree) throws AnyException
  {
    // Dummy - TreeTable is TODO
    if (e == null)
      return true;
    
    boolean ret = false;
    
    //System.out.println("AnyTreeTableModelAdapter.translateEvent " + e);
    
    // We have a new situation here, in that the tree column
    // and the other columns require separate dispatching.
    //
    // Events that arrive at translateEvent() should be
    // processed by the AnyTreeModel's algorithm for determining
    // the path to the affected descendant. Structural changes
    // will always apply to the tree, however modifications
    // need only result in a TreeModelEvent being fired if
    // the Inq event applies to column zero, the tree column.
    // In any other case the we need to determine the affected
    // column numbers from the fields contained in the Inq event.
    // Having established the tree path, we can use JTree.getRowForPath
    // and JTree.isPathExpanded to determine if the row is visible and
    // add the child index from the tree event to give the actual
    // row number.
    
    AnyTreeModel.AnyTreeModelEvent tme =
      attm_.translateEvent(e, table, tree);
    
    // If the event relates to the primary tree column the
    // AnyTreeModel will have processed the event and
    // dispatched it to all TreeModelListeners (that is the
    // the JTree and "this").  In this case "this" echoes a
    // suitable TableModelEvent(s) in the TreeModelListener methods.
    // If the event wasn't dispatched then we make the
    // necessary TableModelEvent(s) here.
    if (tme != null && !tme.getWasDispatched())
      echoTableDataUpdated(tme, table);
    
    //System.out.println("AnyTreeTableModelAdapter.translateEvent " + e);
    
    return ret; // TODO (just to satisfy the interface for now)
  }

  /**
   * Return a mapping of object paths to leaf node fields that should
   * be dispatched to views of this model.
   */
  public Map getEventPaths()
  {
    Map nodeSpecs = AbstractComposite.simpleMap();
    
		attm_.resolveNodeSpecs(NodeSpecification.NULLNS,
                           nodeSpecs,
                           getContext());
    
    return nodeSpecs;
  }

  /**
   * Returns the row-root node for the specified row.  In the tree
   * table case there is a node set at each level. Depending on
   * which level the row refers to, the node returned will be a
   * child of the node set at the tree path represented by the
   * selected node.
   */
	public Any getRowAt(int row)
  {
    AnyTreeNode n = (AnyTreeNode)nodeForRow(row);
    Any a = null;
    //if (!n.isExpansion())  // Expansions TBD in this context
    // NB... for expansions getAny() returns the node-set root
    // for the expansion. Revisit if this is not OK (may be we need
    // to pass expansion flag to things like rendering functions
      a = n.getAny();

    return a;
  }

  /**
   * Returns the key (that is the name) of the specified row.
   * See also <code>getRowAt</code>
   */
	public Any getRowKey(int row)
  {
    AnyTreeNode n = (AnyTreeNode)nodeForRow(row);
    Any a = null;
    //if (!n.isExpansion())  // Expansions TBD in this context
      a = n.getAny();
      // NB... see above
    
    if (a != null)
    {
      Composite rowRoot = (Composite)a;
      a = rowRoot.getNameInParent();
    }

    return a;
  }

  /**
   * Provide access to the JTree renderer
   */
  public JTree getTreeRenderer()
  {
    return tree;
  }
  
  public TreeLevel getTreeLevel(int row)
  {
    AnyTreeNode n = null;
    if (row >= 0)
      n = (AnyTreeNode)nodeForRow(row);
    
    return n.getTreeLevel();
  }

  protected void oldechoTableDataRemoved(TreeModelEvent e)
  {
    AnyTreeModel.AnyTreeModelEvent tme =
      (AnyTreeModel.AnyTreeModelEvent)e;

    TreePath path = tme.getEventPath();
    int row = rowForPath(path);
    
    if (row >= 0)
      fireTableRowsDeleted(row, row);
    else
    {
      // The tree is not expanded. Look back up the path to find the
      // first row available and just update it.
      while((path = path.getParentPath()) != null)
      {
        if ((row = rowForPath(path)) >= 0)
        {
          fireTableRowsUpdated(row, row);
          return;
        }
      }

      // Oh well...
      fireTableDataChanged();
    }
  }
  
  protected void echoTableDataRemoved(TreeModelEvent e)
  {
    // For removed events the tree path will never map to a row, because
    // the node has already been deleted. Instead, look to see if the
    // parent path has a row and is expanded. Use the child index+1 as an
    // offset. If the removed node is itself expanded then a range of
    // rows have been deleted.
    
    AnyTreeModel.AnyTreeModelEvent tme =
      (AnyTreeModel.AnyTreeModelEvent)e;

    TreePath path       = tme.getEventPath();
    TreePath parentPath = path.getParentPath();
    
    AnyTreeNode node       = (AnyTreeNode)path.getLastPathComponent();
    AnyTreeNode parentNode = (AnyTreeNode)parentPath.getLastPathComponent();
    
    int firstRow = rowForPath(parentPath);
    if (firstRow >= 0 && parentNode.isExpanded())
    {
      firstRow += tme.getIndex() + 1;
      int lastRow = firstRow;
      if (node.isExpanded())
        lastRow += node.entries();
      fireTableRowsDeleted(firstRow, lastRow);
    }
    else if (firstRow >= 0)
    {
      // Parent node is not expanded. Just update its row.
      fireTableRowsUpdated(firstRow, firstRow);
    }
    else
    {
      // The tree is not expanded. Look back up the path to find the
      // first row available and just update it.
      while((parentPath = parentPath.getParentPath()) != null)
      {
        if ((firstRow = rowForPath(parentPath)) >= 0)
        {
          fireTableRowsUpdated(firstRow, firstRow);
          return;
        }
      }
      
      // Oh well...
      fireTableDataChanged();
    }
  }
  
  protected void echoTableDataInserted(TreeModelEvent e)
  {
    AnyTreeModel.AnyTreeModelEvent tme =
      (AnyTreeModel.AnyTreeModelEvent)e;

    TreePath path = tme.getEventPath();
    int row = rowForPath(path);
    
    if (row >= 0)
      fireTableRowsInserted(row, row);
    else
    {
      // The tree is not expanded. Look back up the path to find the
      // first row available and just update it.
      while((path = path.getParentPath()) != null)
      {
        if ((row = rowForPath(path)) >= 0)
        {
          fireTableRowsUpdated(row, row);
          return;
        }
      }
      
      // Oh well...
      fireTableDataChanged();
    }
  }
  
  // Override base class brute force implementation.
  // This method can be called via ourselves in the capacity
  // as a TreeModelListener (see base class) and directly
  // from translateEvent() above.
  protected void echoTableDataUpdated(TreeModelEvent e, JTable t)
  {
    AnyTreeModel.AnyTreeModelEvent tme =
      (AnyTreeModel.AnyTreeModelEvent)e;

    // If we can determine the row number, fire one table event
    // for each column that could require re-rendering for the
    // given AnyTreeModelEvent.
    int row = rowForPath(tme.getEventPath());
    
    // If the row representing the parent of the affected node
    // is not visible (because it is under a collapsed node) then
    // there's nothing to do.  Likewise, if the parent of the
    // affected node is not itself expanded there's nothing to
    // do either.
    if (row >= 0 && isExpanded(e.getTreePath()))
    {
      // No longer needed because of new getEventPath() method
      // row += tme.getIndex();
      
      boolean fireRow = false;
      
      // The table can also be passed wvia the AnyTreeModelEvent.
      if (t == null)
        t = tme.getTable();
      
      if (t != null)
        fireRow = t.isRowRefresh();

      if (tme.getFields() == null)
      {
        // There are no fields when we've got here because of a tree
        // model event raised as a requirement of setValueAt or
        // valueForPathChanged. We can't know whether its a row
        // update either, so assume it is. This is OK because the
        // main source of this type of thing is user editing.
        fireTableRowsUpdated(row, row);
        return;
      }
      
      Iter i = tme.getFields().createIterator();
      int colStart = tme.getWasDispatched() ? 0 : 1;
      boolean fired = false;
      while (i.hasNext())
      {
        Any field = i.next();

        for (int col = colStart; col < getColumnCount(); col++)
        {
          RenderInfo r = getRenderInfo(col);
          
          // If we are procesisng column zero then we must have dispatched
          // to the tree already. In this case we know we must dispatch to
          // the table as well. Otherwise check if the current field 
          if (col == 0 || r.isDispatching(field))
          {
            if (fireRow)
            {
              // Note - can only be true when t != null
              if (t.isEditing() &&
                  t.getEditingRow() == row)
                t.resetEditor();
              
              //System.out.println("fireTreeTableRowRefresh " + row);
              fireTableRowsUpdated(row, row);
              fired = true;
              break;
            }
            else
            {
              if (t != null &&
                  t.isEditing() &&
                  t.getEditingRow() == row &&
                  t.getEditingColumn() == col)
                t.resetEditor();

              //System.out.println("fireTreeTableCellUpdated " + row + " " + col);
              fireTableCellUpdated(row, col);
            }
          }
        }
        
        // If we are firing for the whole row and we've already done so
        // then there is no need to process any more fields. Note, however
        // we are not attempting to optimise firing for the case when 
        // there are several fields in the event that relate to the
        // same *cell*.
        if (fired)
          break;
      }
    }
  }
}
