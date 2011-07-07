/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JTree.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.inqwell.any.Any;
import com.inqwell.any.client.AnyTable;
import com.inqwell.any.client.AnyTree;
import com.inqwell.any.client.AnyTreeModel;
import com.inqwell.any.client.AnyTreeNode;

public class JTree extends javax.swing.JTree
{
  private AnyTree                   tree_;
  private MaintainExpansionListener expansionListener_;
  
	public JTree()
	{
    super(new AnyTreeModel());
    init((AnyTreeModel)getModel());
  }
  
	public JTree(AnyTreeModel m)
	{
		super(m);
		init(m);
	}

  public boolean isPathEditable(TreePath path)
  {
    // There is no AnyTree when used as a tree-table renderer
    if (tree_ == null)
      return ((AnyTreeModel)this.getModel()).isPathEditable(path);
    
  	return tree_.getModel().isPathEditable(path);
  }
  
  public boolean isTreeTableRenderer()
  {
    return false;
  }
  
  public void setAnyTree(AnyTree tree)
  {
    tree_ = tree;
  }
  
  // Means whether the table cell this JTree is rendering has the focus 
  public boolean hasCellFocus()
  {
    return false;
  }
  
  public boolean isCellSelected()
  {
    return false;
  }
  
  public JTable getJTable()
  {
    return null;
  }
  
  public AnyTree getAnyTree()
  {
    return tree_;
  }
  
  public AnyTable getAnyTable()
  {
    // The treetable renderer extension returns non-null
    return null;
  }
  
  public Any getRowStyle()
  {
    // As above
    return null;
  }
  
  public Any getContextNode()
  {
    // There is no AnyTree when used as a tree-table renderer
    return ((AnyTreeModel)this.getModel()).getContext();
  }
  
  /**
   * Overridden to return null.  The 
   */
  public String convertValueToText(Object value,
      boolean selected,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus)
  {
    // Only while the old renderer is in use.
    return value.toString();
  }
  
  public void setModel(TreeModel m)
  {
    if (expansionListener_ == null)
      expansionListener_ = new MaintainExpansionListener();
    
    TreeModel old = getModel();
    if (old != null)
      old.removeTreeModelListener(expansionListener_);
    
    super.setModel(m);
    
    m.addTreeModelListener(expansionListener_);
  }

  public void reinit(AnyTreeModel m)
  {
    setModel(m);
    init(m);
  }
  
//  protected TreeModelListener createTreeModelListener()
//  {
//    // Put our own functionality for maintaining tree path expansion state
//    // on top of Swing's own.
//    return new MaintainExpansionListener();
//  }
  
	private void init(AnyTreeModel m)
	{
		this.setEditable(true);
		// The model also serves as the renderer.
    this.setCellRenderer(m);
    this.setCellEditor(m);
	}
  
  public class MaintainExpansionListener extends TreeModelHandler
  {
    public void treeStructureChanged(TreeModelEvent e)
    {
      super.treeStructureChanged(e);

      
      AnyTreeModel.AnyTreeModelEvent ev = (AnyTreeModel.AnyTreeModelEvent)e;
      AnyTreeModel m = (AnyTreeModel)e.getSource();
      TreePath p = e.getTreePath();
      AnyTreeNode n = (AnyTreeNode)p.getLastPathComponent();
      if (ev.isExpandPaths())
      {
        // Expand to original state
        m.expandPaths(n, JTree.this, 1);
      }
      else
      {
        // Expand according to properties
        m.expandToDepth(n, JTree.this);
      }
    }
  }
}
