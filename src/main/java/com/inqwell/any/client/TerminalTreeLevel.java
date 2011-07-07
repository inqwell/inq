/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/TerminalTreeLevel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Queue;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.TableModel;
import com.inqwell.any.client.swing.TreeTableModel;

/**
 * A TreeLevel implementation that represents the end of
 * a chain.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class TerminalTreeLevel extends CommonTreeLevel
{
  private static final long serialVersionUID = 1L;

  private DefaultTreeCellRenderer dummy_ = new DefaultTreeCellRenderer();

  static private Any terminalName__ = new ConstString("TerminalLevel");

	public Any getChild(Object parent, int index) throws AnyException
	{
		return null;
	}

  public int getChildCount(Object parent) throws AnyException
  {
  	return 0;
  }

  public int getIndexOfChild(Object parent, Object child) throws AnyException
  {
  	return -1;
  }

  public Any getRoot() throws AnyException
  {
  	return null;
  }

	public NodeSpecification getRootPath()
	{
  	return null;
	}

	public void resolveNodeSpecs(NodeSpecification rootPath,
	                             Map               nodeSpecs,
                               TreeTableModel    treeTableModel,
	                             Any               contextNode)
	{
	}

  public void markStale() {}

  public AnyComponentEditor getEditor()
  {
    throw new UnsupportedOperationException();
  }

  public AnyComponentEditor getEditor(int column)
  {
    throw new UnsupportedOperationException();
  }

  public AnyCellRenderer getRenderer(int column)
  {
    throw new UnsupportedOperationException();
  }
  
  public AnyCellRenderer getRenderer()
  {
    throw new UnsupportedOperationException();
  }
  
  public OrderComparator getComparator()
  {
    return null;
  }

  public void setComparator(OrderComparator oc)
  {
  }

  public void sort(AnyTreeNode n)
  {
  }

  public Vectored getVectorParent(AnyTreeNode parent) throws AnyException
  {
    return null;
  }

  public Vectored getVectorParent(Any context) throws AnyException
  {
    return null;
  }

  public boolean isLeaf(Object node) throws AnyException
  {
  	System.out.println("TerminalTreeLevel.isLeaf " + node);
  	return true;
  }

  public boolean isLeaf(Any vectorChild, Any context)
  {
    return true;
  }
  
	public boolean isEditable(Any a)
	{
    return false;
  }

	public Any getValueFor(Any a)
	{
    return null;
	}

	public Any getResponsibleFor(Any a)
	{
    return null;
	}

	public Any getSelectionFor(Any a)
	{
		return null;
	}

	public Any getKeySelectionFor(Any a)
	{
		return null;
	}

	public TreeLevel getTreeLevelForPath(TreePath p)
	{
    return this;
	}

	public TreeLevel getTreeLevelForPath(TreePath p, int index) throws AnyException
	{
    return this;
	}

	public Any getContext() { return null; }

  public TreeLevel getNextTreeLevel() { return null; }

	public boolean isRecursive()
  {
    return false;
  }

  public void setContext(Any context) {}

	public void setRenderInfo(RenderInfo r) {}

  public RenderInfo getRenderInfo()
  {
    throw new UnsupportedOperationException("setLeafExpression");
  }

  public void setLeafExpression(Any isLeaf)
  {
    throw new UnsupportedOperationException("setLeafExpression");
  }

  public void setColumns(Array columns, TableModel tableModel)
  {
    throw new UnsupportedOperationException("setColumns");
  }

  public RenderInfo getColumnRenderInfo(int col)
  {
    throw new UnsupportedOperationException("getColumnRenderInfo");
  }

  public void setBranchOnly(Any branchOnly)
  {
  }

  public void setClosedIcon(Any icon)
  {
  }

  public void setLeafIcon(Any icon)
  {
  }

  public void setOpenIcon(Any icon)
  {
  }

	public TreeNodeExpansion addExpansion(RenderInfo r, Locate node, TreeLevel l) { return null;}

  public int translateEvent(Any               startAt,
                            Event             e,
                            Map               eventType,
                            Any               baseType,
                            boolean           force,
                            Array             path,
                            NodeSpecification eventPath) throws AnyException
  {
  	return -1;
  }


  /**
   * Shouldn't be called
   */
	public Component getTreeCellRendererComponent(JTree        tree,
	                                              AnyTreeNode  value,
	                                              boolean      selected,
	                                              boolean      expanded,
	                                              boolean      leaf,
	                                              int          row,
	                                              boolean      hasFocus)
  {
  	return dummy_.getTreeCellRendererComponent(tree,
  	                                           "Terminal Level!",
  	                                           selected,
  	                                           expanded,
  	                                           leaf,
  	                                           row,
  	                                           hasFocus);
  }

  public DefaultTreeCellRenderer getDefaultTreeCellRenderer(Any a)
  {
  	return null;
  }

  public TreeCellEditor getTreeCellEditor(Any a)
  {
  	return null;
  }

  public void setModel(AnyTreeModel m)
  {
    throw new UnsupportedOperationException();
  }

  public AnyTreeModel getModel()
  {
    throw new UnsupportedOperationException();
  }

  public void setEditor(AnyComponentEditor ace, int column)
  {
    throw new UnsupportedOperationException();
  }

  public void setEditor(AnyComponentEditor ace)
  {
    throw new UnsupportedOperationException();
  }

  public void setName(Any name)
  {
  }

  public Any getName()
  {
    return terminalName__;
  }
}
