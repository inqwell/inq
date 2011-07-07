/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/TreeLevel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.Event;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.TableModel;
import com.inqwell.any.client.swing.TreeTableModel;

/**
 * A level of representation in a Tree view.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public interface TreeLevel extends Any
{
	public static TreeLevel terminalLevel__ = new TerminalTreeLevel();

	/**
   * Returns the child of <i>parent</i> at index <i>index</i> in
   * the parent's child array. <i>parent</i> must be a node
   * previously obtained from this data source.
   */
	public Any getChild(Object parent, int index) throws AnyException;

	/**
   * Returns the number of children of <i>parent</i>. Returns 0 if the
   * node is a leaf or if it has no children. <i>parent</i> must be a
   * node previously obtained from this data source.
   */
  public int getChildCount(Object parent) throws AnyException;
  public int getIndexOfChild(Object parent, Object child) throws AnyException;
  public Any getRoot() throws AnyException;

  public TreeLevel getNextTreeLevel();

  /**
   * Returns true if <i>node</i> is a leaf. It is possible for this
   * method to return false even if node has no children. A directory
   * in a filesystem, for example, may contain no files; the node
   * representing the directory is not a leaf, but it also has no
   * children.
   */
  public boolean isLeaf(Object node) throws AnyException;
  
  /**
   * Determine whether a candidate dynamic node is a leaf
   * @param vectorChild
   * @param context
   * @return true if leaf, false otherwise
   */
  public boolean isLeaf(Any vectorChild, Any context);

  /**
   * Return the <code>TreeLevel</code> that corresponds to the
   * given path <i>p</i>.
   */
	public TreeLevel getTreeLevelForPath(TreePath p);

	/**
   * Return <code>true</code> if <i>a</a> represents an
   * editable node at this level, <code>false</code> otherwise.
   */
	public boolean isEditable(Any a);

	public boolean isRecursive();

  /**
   * Return the rendering value at this level represented by <i>a</i>.
   */
	public Any getValueFor(Any a);

  /**
   * Return the responsible data item represented at
   * this level by <i>a</a>.
   */
	public Any getResponsibleFor(Any a);
  /**
   * Return the item to be held as the selection value
   * for data item represented at this level by <i>a</a>.
   */
	public Any getSelectionFor(Any a);

  /**
   * Return the key to be held as the key selection value
   * for data item represented at this level by <i>a</a>.
   * @return key value or <code>null</code> if there is no
   * appropriate or determinable key.
   */
	public Any getKeySelectionFor(Any a);

	/**
   * Return the <code>NodeSpecification</code> that identifies the
   * node defining the number of children at this level.
   */
  public NodeSpecification getRootPath();

  /**
   * Resolve the node specifications for event interest at
   * this level.
   * @param rootPath a path that resolves to the node from which
   * the the expressions representing the root of this level
   * will be applied.
   * @param nodeSpecs an output parameter - the list of specifications
   * built so far, having processed ancestor levels.
   * @param treeTableModel Non-null when deployed in a treetable context.
   * generally a tree level renders only the
   * result of its RenderInfo, however if being used as a tree table
   * there can be an additional set of rendered items.
   * @param contextNode the prevailing context node, typically used to
   * resolve any expression indirections.
   */
	public void resolveNodeSpecs(NodeSpecification rootPath,
                               Map               nodeSpecs,
                               TreeTableModel    treeTableModel,
                               Any               contextNode);

	/**
   * Return the context against which the location of the root
   * at this level is executed.
   */
	public Any getContext();

	/**
   * Set the context against which the location of the root
   * at this level is executed.
   */
	public void setContext(Any context);

	public void setRenderInfo(RenderInfo r);

  /**
   * Icon to be displayed for an expanded branch node
   */
  public void setOpenIcon(Any icon);

  /**
   * Icon to be displayed for a collapsed branch node
   */
  public void setClosedIcon(Any icon);

  /**
   * Icon to be displayed for a leaf node
   */
  public void setLeafIcon(Any icon);

  /**
   * An expression that can be supplied as an explicit test for
   * whether a node is branch or leaf.  This is optional
   * operation that may be implemented to override any explicit
   * branch or leaf status implied by the implementation.
   */
  public void setLeafExpression(Any isLeaf);

  /**
   * Provide additional rendering information at this tree level
   * when used as a TreeTable.  By default, the table columns
   * will be used at all levels
   */
  public void setColumns(Array columns, TableModel tableModel);

  /**
   * Return any rendering information for the specified column
   * or null is none has been configured.
   */
  public RenderInfo getColumnRenderInfo(int col);

  /**
   * Return any rendering information for this tree level.
   */
  public RenderInfo getRenderInfo();

  /**
   * Set a hint that the implementation should only return child
   * nodes that are, themselves, branch nodes.
   */
  public void setBranchOnly(Any branchOnly);

	public TreeNodeExpansion addExpansion(RenderInfo r, Locate node, TreeLevel l);

  public int getExpansionCount();

  public TreeNodeExpansion getExpansion(int index);
  
  public int getExpansionIndex(NodeSpecification path);
  
	/**
   * Returns the child index of the node that the event
   * relates to.
   */
  public int  translateEvent(Any               startAt,
                             Event             e,
                             Map               eventType,
                             Any               baseType,
                             boolean           force,
                             Array             path,
                             NodeSpecification eventPath) throws AnyException;

  /**
   * This tree level is marked as stale, meaning that it will
   * re-evaluate its internal state when next required
   */
  public void markStale();

  /**
   * Render the specified tree node
   */
	public Component getTreeCellRendererComponent(JTree        tree,
	                                              AnyTreeNode  value,
	                                              boolean      selected,
	                                              boolean      expanded,
	                                              boolean      leaf,
	                                              int          row,
	                                              boolean      hasFocus);

  /**
   * Returns the internal <code>DefaultTreeCellRenderer</code>
   * used to render the tree node represented by <i>a</i>.
   * Provided to assist with tree editing
  public DefaultTreeCellRenderer getDefaultTreeCellRenderer(Any a);
   */

  /**
   * Returns the <code>TreeCellEditor</code>
   * used to edit the tree node represented by <i>a</i>.
   */
  public TreeCellEditor getTreeCellEditor(Any a);

  public void setModel(AnyTreeModel m);

  public AnyTreeModel getModel();

  // The tree editor at this level
  public AnyComponentEditor getEditor();
  
  // The tree renderer at this level
  public AnyCellRenderer getRenderer();

  // The editor for the specified column.  Only valid for TreeTables.
  public AnyComponentEditor getEditor(int column);

  // The renderer for the specified column.  Only valid for TreeTables.
  public AnyCellRenderer getRenderer(int column);

  public void setEditor(AnyComponentEditor ace, int column);

  public void setEditor(AnyComponentEditor ace);

  public Vectored getVectorParent(AnyTreeNode parent) throws AnyException;

  public Vectored getVectorParent(Any context) throws AnyException;
  
  public Vectored getChildVector(AnyTreeNode n) throws AnyException;

  public OrderComparator getComparator();

  public void setComparator(OrderComparator oc);

  public void sort(AnyTreeNode n, boolean depthSort);

  public void setName(Any name);

  public Any getName();

  public void setEquals(Any equals);

  public Any getEquals();
}
