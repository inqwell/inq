/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import java.util.ArrayList;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyComparator;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Orderable;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.JTree;
import com.inqwell.any.client.swing.TreeTableModel;

/**
 * An extension of the standard Inq AnyTreeModel that provides
 * implementations for use as a TreeTable.
 */
public class AnyTreeTableModel extends    AnyTreeModel
                               implements TreeTableModel
{
  // An array that represents the columns in
  // the TreeTable.  The first entry (zero in model coordinates)
  // represents the column in which the tree is rendered. The
  // RenderInfo for this column is only used for the width and label.
  // Subsequent entries hold the cell renderers for the remaining
  // columns
	private Array     colInfo_;
  
	private ArrayList editors_;
  
  private AnyTable  table_;

  private int       visibleRows_ = 10;

  public AnyTreeTableModel()
  {
		colInfo_ = AbstractComposite.array();
  }
  
  public int getColumnCount()
  {
		return(colInfo_.entries());
  }

  /**
   * Returns the name for column number <code>column</code>.
   */
	public String getColumnName(int c)
	{
    // Use primary columns only (not any overrides) for column names
		RenderInfo r = getRenderInfo(c);
		return r.getLabel();
	}

  /**
   * Returns the type for column number <code>column</code>.
   */
  public Class getColumnClass(int column)
  {
    // In model space, column zero is always the tree so return
    // this class for the renderer setup
    if (column == 0)
      return TreeTableModel.class;
    
    return Any.class;
  }

  /**
   * Returns the value to be displayed for node <code>node</code>, 
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column)
  {
    //System.out.println("node.getClass() " + node.getClass());
    //System.out.println("node " + node);
    AnyTreeNode n = (AnyTreeNode)node;
    if (column == 0)
    {
      // For the tree column the tree config gives us the value.
      return n.getValueFor();
    }
    else
    {
      // For the remaining table columns, the RenderInfo for that
      // column is applied to the context in the node/expansion
      RenderInfo r = getColumnRenderInfo(n, column);
      try
      {
        //System.out.println("AnyTreeTableModel.getValueAt " + column + " " + r);
        if (!n.isExpansion())
          return r.resolveDataNode(n.getAny(), true);
        else
          return r.resolveDataNode(n.getContext(), true);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }

  /**
   * Indicates whether the the value for node <code>node</code>, 
   * at column number <code>column</code> is editable.
   */
  public boolean isCellEditable(Object node, int column)
  {
    // Called from TreeTableModelAdapter.
    // The tree cell is always editable at this (early) stage of
    // determination. The JTable continues to enquire of the
    // editability of the cell at CellEditor.isCellEditable(EventObject e)
    // See (extension of) JTable where this editor is installed.
    // Its implementation of this method dispatches the event to the
    // tree that is rendering the cell. 
    if (getColumnClass(column) == TreeTableModel.class)
      return true;
    
    
    // More stuff added for other columns
    AnyTreeNode n = (AnyTreeNode)node;
		RenderInfo r = getColumnRenderInfo(n, column);
    AnyComponentEditor ace = (AnyComponentEditor)getCellEditor(n, column);

		return r.isEditable() && ace != null && ace.getComponent() != null;
  }

  /**
   * Sets the value for node <code>node</code>, 
   * at column number <code>column</code>.
   */
  public void setValueAt(Object aValue, Object node, int column)
  {
		Any sv = (Any)aValue;
    Any copyTo = null;
    
    AnyTreeNode n = (AnyTreeNode)node;
    
    if (column == 0)
      copyTo = n.getResponsibleFor();
    else
    {
      RenderInfo r = getColumnRenderInfo(n, column);
      try
      {
        if (!n.isExpansion())
          copyTo = r.resolveResponsibleData(n.getAny());
        else
          copyTo = r.resolveResponsibleData(n.getContext());
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    
    copyTo.copyFrom(sv);
    
    // Kick out a node event so the tree re-renders appropriately
    nodeChanged(n.makeTreePath());
  }
  
  // Inq extensions

	public Any getResponsibleValueAt(AnyTreeNode n, int column)
  {
    if (column == 0)
    {
      // For the tree column the tree config gives us the value.
      return n.getResponsibleFor();
    }
    else
    {
      // For the remaining table columns, the RenderInfo for that
      // column is applied to the context in the node/expansion
      RenderInfo r = getColumnRenderInfo(n, column);
      try
      {
        if (!n.isExpansion())
          return r.resolveResponsibleData(n.getAny());
        else
          return r.resolveResponsibleData(n.getContext());
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }

  public void setColumns(Vectored columns)
  {
    //System.out.println("AnyTreeTableModel.setColumns " + columns);
  	colInfo_.empty();
    editors_ = null;
  	
  	for (int i = 0; i < columns.entries(); i++)
  	{
  	  RenderInfo r = (RenderInfo)columns.getByVector(i);
      
      // Column zero is not a renderer
      if (i == 0)
  	    colInfo_.add(r);
      else
        colInfo_.add(new AnyCellRenderer(r));
      
  	  r.resolveNodeSpecs(getContext());
    }
  }
  
  public Vectored getColumns()
  {
    return (Vectored)colInfo_;
  }
  
	public RenderInfo getRenderInfo(int c)
	{
    if (c == 0)
		  return (RenderInfo)colInfo_.get(c);
    else
    {
      AnyCellRenderer r = (AnyCellRenderer)colInfo_.get(c);
      return r.getRenderInfo();
    }
	}

	public void setVisibleRows(int visibleRows)
	{
		visibleRows_ = visibleRows;
		if (visibleRows_ == 0)
			visibleRows_ = 10;
	}
	
	public int getVisibleRows()
  {
    return visibleRows_;
  }
  
  public void setTable(AnyTable table)
  {
    table_ = table;
  }
  
  public AnyTable getTable()
  {
    return table_;
  }

  void resolveNodeSpecs(NodeSpecification rootPath,
                        Map               nodeSpecs,
                        Any               contextNode)
  {
    resolveNodeSpecs(rootPath, nodeSpecs, this, contextNode);
  }
  
  void setCellEditor(TableCellEditor tce, int row, int column)
  {
    // Establish the default editors that will be used if
    // none is available at a specific level.
    if (editors_ == null)
    {
      int cc = this.getColumnCount();
      editors_ = new ArrayList(cc);
      for (int i = 0; i < cc; i++)
        editors_.add(null);
    }
    editors_.set(column, tce);
  }

  TableCellEditor getCellEditor(AnyTreeNode n, int column)
  {
    // Note if we make the root node displayable in a TreeTable (at
    // the moment it doesn't work) then only consider the
    // editors we hold here (since there's no tree level)
    // [ Well, sort of handled already with n == null ]
    
    TableCellEditor tce = null;
    
    // If there's a tree node, check that first
    if (n != null)
    {
      if (!n.isExpansion())
      {
        tce = n.getTreeLevel().getEditor(column);
      }
      else
      {
        TreeNodeExpansion e = n.getExpansion();
        tce = e.getEditor(column);
      }
      
    }

    // if no editor for the level or no tree node specified,
    // check here
    if (tce == null && editors_ != null)
      tce = (TableCellEditor)editors_.get(column);
    
    return tce;
  }
  
  TableCellRenderer getCellRenderer(AnyTreeNode n, int column)
  {
    TableCellRenderer tcr = null;
    
    // If there's a tree node, check that first
    if (n != null)
    {
      if (!n.isExpansion())
      {
        tcr = n.getTreeLevel().getRenderer(column);
      }
      else
      {
        TreeNodeExpansion e = n.getExpansion();
        tcr = e.getRenderer(column);
      }
    }

    // if no renderer for the level or no tree node specified,
    // check here
    if (tcr == null)
      tcr = (TableCellRenderer)colInfo_.get(column);
    
    return tcr;
  }

  // Sort all the tree levels according to the columns prevalent at
  // each level and specified by the column model indices in orderItems.
  void sort(int[] orderItems, boolean isDescending, JTree tree) throws AnyException
  {
    AnyTreeNode root = (AnyTreeNode)getRoot();
    
    if (orderItems == null)
    {
      // If withdrawing a click sort apply any scripted comparator
      TreeLevel l = root.getTreeLevel();
      if (l != null)
      {
        l.sort(root, true);
      }
    }
    else
    {
      // Not really the root, rather the children of the root!
      TreeLevel rootLevel = root.getTreeLevel();
      Any       context   = getContext();
      
      sortChildLevel(rootLevel, context, orderItems, isDescending);
    }
    
    // Generate a tree structure event on the root
    Object[] pathArray    = new Object[1];
    pathArray[0] = root;
    // when the root is affected the children and their
    // indices are always null.
    TreePath treePath = new TreePath(pathArray);
    AnyTreeModelEvent tme = new AnyTreeModelEvent(this, treePath);
    
    // Clear down the root.  If we don't do this then the model
    // will try to reuse the AnyTreeNode instances that represent
    // the current (i.e. before sort) state of the model. Since
    // the actual nodes have moved around this won't work.
    resetRoot();
    
    //System.out.println("fireStructureEvent PATH 1 " + rootNode);
    //System.out.println("fireStructureEvent PATH 2 " + rootNode.makeTreePath());
    
    tme.setExpandPaths(true);
    fireStructureEvent(tme);
    
    // The tree collapses to the root children.
    // Once the model has been refreshed into the view we look
    // through the old model to see if there were any expanded paths
    // and if so, expand them now. (Make sure the root node is
    // expanded or we won't recurse in)
    //rootNode = (AnyTreeNode)getRoot();
    
    //root.setExpanded(true);
    //expandPaths(root, tree, 1);
  }
  
  private void sortChildLevel(TreeLevel   level,
                              Any         context,
                              int[]       orderItems,
                              boolean     isDescending) throws AnyException
  {
    // Check if there's anything at this level to sort
    // [ Note, returning in this way precludes the possibility
    //   of putting a long-lived comparator in the vector for
    //   this level, so that it can remain sorted. Could we easily
    //   distinguish between an empty vector and no vector anyway? ]
    
    // There are two types of children that we support: dynamic
    // children that reside underneath vectors and static children
    // that are explicitly set up in the model configuration (so
    // called "expansion" nodes).
    
    // The static children are not sorted (they remain in the order
    // they were specified in the script that configured them)
    // however there may be dynamic children beneath these static
    // ones so we descend into them to continue the sort.
    
    // We should not really assume that the static and dynamic
    // children are yielded in any particular order by the model,
    // however this would mean that we would need to loop over
    // all the children just to ensure we found all the expansions.
    // The only (significant) implementation is AnyTreeLevel and
    // since this returns all expansions first we make use of this
    // knowledge as an optimisation.

    //System.out.println("sortChildLevel " + level.getName());
    //System.out.println("num children is " + childCount);
    //System.out.println("tree level id is " + System.identityHashCode(level));

    Vectored v = level.getVectorParent(context);
    
    if (v != null)
    {
      TreeLevel nextLevel = level.getNextTreeLevel();
      if (nextLevel != TerminalTreeLevel.terminalLevel__)
      {
        
        int childCount = v.entries();
        int i          = 0;
        Any child      = null;
        while (i < childCount)
        {
          child = v.getByVector(i);
          
          // recurse in to sort any levels under the child (including
          // recursively defined levels)
          sortChildLevel(nextLevel, child, orderItems, isDescending);
          
          i++;
        }
      }
      
      // Sort the children of the given parent as we back out of the
      // recursion, thus sorting from the bottom level upwards.
      // Expansion nodes themselves do not have a vector, so just
      // skip them.
      //System.out.println("  at 1 ");
        
      if (v instanceof Orderable)
      {
        //System.out.println("  at 2 ");
        
        // OK, finally sort this level's dynamic children.
        sortVectorChildren(level,
                           (Orderable)v,
                           orderItems,
                           isDescending);
      }
    }
    
    // Traverse into any expansion nodes at this level
    int expansionCount = level.getExpansionCount();
    while (expansionCount-- > 0)
    {
      TreeNodeExpansion e = level.getExpansion(expansionCount);
      AnyTreeLevel expLevel = (AnyTreeLevel)e.getTreeLevel();
      
      TreeLevel expNextLevel = expLevel.getNextTreeLevel();

      if (expNextLevel != TerminalTreeLevel.terminalLevel__)
      {
        sortChildLevel(expNextLevel, context, orderItems, isDescending);
      }
    }
  }

  private void sortVectorChildren(TreeLevel   level,
                                  Orderable   o,
                                  int[]       orderItems,
                                  boolean     isDescending)
  {
		if (o == null)
		  return;
    
    if (orderItems == null)
    {
      level.setComparator(null);

      // TODO: Should we check if there is a scripted comparator_ and
      // apply that if so?
      
      return;
    }
		
    OrderComparator oc = level.getComparator();
    if (oc == null)
      level.setComparator(oc = new AnyComparator());

    oc.setDescending(isDescending);
    
    // Interactive sorting ignores case
    oc.setIgnoreCase(true);

    // Set up the functions that resolve the sort data at this level
    // for the comparator.
    Array orderFuncs = AbstractComposite.array();
    for (int i = 0; i < orderItems.length; i++)
    {
      int col = orderItems[i];
      if (col < 0)
        break;
      RenderInfo r = getColumnRenderInfo(level, col);
      orderFuncs.add(new AnyTableModel.ResolveSortData(r));
    }

    oc.setOrderBy(orderFuncs);
    
    //System.out.println("Sorting " + level.getName());
    //System.out.println("with Vector " + System.identityHashCode(o));

		oc.setToOrder((Map)o);
		AbstractComposite.sortOrderable(o, oc);
		// Remove any comparator that may be in the orderable node to
		// force insertions to their designated index after a manual sort
		o.sort((OrderComparator)null);
  }
  
  private RenderInfo getColumnRenderInfo(AnyTreeNode node, int col)
  {
    RenderInfo r = null;
    
    // Check if there's an override at the tree level
    if (!node.isExpansion())
    {
      if (col == 0)
        r = node.getTreeLevel().getRenderInfo();
      else
        r = node.getTreeLevel().getColumnRenderInfo(col);
      
      // If there's no rendering information defined at the tree
      // level in question then default to that defined at the table
      // itself. (Should only happen for col > 0 anyway and in any
      // case I think its never null because table-level objects
      // are inherited into tree levels).
      if (r == null)
        r = getRenderInfo(col);
    }
    else
    {
      TreeNodeExpansion e = node.getExpansion();
      r = e.getColumnRenderInfo(col);
    }
    
    return r;
  }
  
  // Return the RenderInfo for the specified column.  Because this
  // method takes a TreeLevel it cannot destinguish between the
  // dynamic and static (that is "expansion") node types, so
  // dynamic is assumed.  This is OK because this method is only
  // used in the context of sorting.
  private RenderInfo getColumnRenderInfo(TreeLevel l, int col)
  {
    RenderInfo r = null;
    
    if (col == 0)
      r = l.getRenderInfo();
    else
    {
      r = l.getColumnRenderInfo(col);
      
      if (r == null)
        r = getRenderInfo(col);
    }
      
    return r;
  }
}
