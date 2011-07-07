/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/HierarchyTreeLevel.java $
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

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyAlwaysEquals;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Transaction;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.TreeTableModel;

/**
 * An implementation of <code>TreeLevel</code> that maps
 * all nodes below a given point in the Inq node space
 * to tree nodes.
 * <p>
 * As may be expected, composites are branch nodes and
 * non-composites leaf nodes. The implementation of
 * <code>Composite</code> at all levels within the structure
 * must support the <code>Composite.getNameInParent()</code>
 * method.
 * <p>
 * Please note this class is currently deprecated.  (May not
 * have kept up with all tree developments).  For a start, it
 * should return AnyTreeNode wrappers for the tree data to be
 * consistent with other implementations.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class HierarchyTreeLevel extends AbstractTreeLevel
{
	private Locate             levelRoot_;
	private Vectored           levelRootV_;

	private Any                context_;

  private AnyComponentEditor editor_;

	/**
	 * Construct a <code>HierarchyTreeLevel</code> object
	 * whose root node is given by the argument <i>l</i>.
	 * @param l the location of the node representing the
	 * parent of the nodes at this level.
	 */
	public HierarchyTreeLevel(Locate l)
	{
		levelRoot_ = l;
		renderer_  = new AnyCellRenderer(new AnyRenderInfo(new Render()),
                                     new DefaultTreeCellRenderer());
	}

	public Any getChild(Object parent, int index) throws AnyException
  {
  	Vectored v = (Vectored)parent;
  	return v.getByVector(index);
  }

  public int getChildCount(Object parent) throws AnyException
  {
  	Vectored v = (Vectored)parent;
  	return v.entries();
  }

  public int getIndexOfChild(Object parent, Object child) throws AnyException
  {
  	Vectored v = (Vectored)parent;
  	return getChildIndex(v, child);
  }

  public Any getRoot() throws AnyException
  {
  	Vectored v = resolveDataNode(context_, false);
  	//System.out.println("HierarchyTreeLevel.getRoot() " + v);
  	return v;
  }

  public boolean isLeaf(Object node) throws AnyException
  {
  	boolean ret = (!(node instanceof Composite));
  	//System.out.println("HierarchyTreeLevel.isLeaf() " + node + " " + ret);
  	return ret;
  }

  public boolean isLeaf(Any vectorChild, Any context)
  {
    return false;
  }
  
	public TreeLevel getTreeLevelForPath(TreePath p)
	{
		return this;
	}

  public TreeLevel getNextTreeLevel()
  {
    return this;
  }

	/**
   * Leaf nodes are considered editable whereas branch nodes are
   * not
   */
	public boolean isEditable(Any a)
	{
    return (!(a instanceof Composite));
	}

	public boolean isRecursive()
  {
    return true;
  }

	public Any getResponsibleFor(Any a)
	{
		return a;
	}

	public Any getValueFor(Any a)
	{
		return a;
	}

	public Any getSelectionFor(Any a)
	{
		return a;
	}

	public Any getKeySelectionFor(Any a)
	{
    if (a instanceof Composite)
    {
      Composite c = (Composite)a;
      return c.getNameInParent();
    }
    else
    {
      return null;
    }
	}

	public NodeSpecification getRootPath()
	{
		NodeSpecification root = levelRoot_.getNodePath();

		return root;
	}

	public void resolveNodeSpecs(NodeSpecification rootPath,
	                             Map               nodeSpecs,
                               TreeTableModel    treeTableModel,
	                             Any               contextNode)
	{
		// We are interested in any events from all fields
		// from the root downwards.  In practice, field
		// events will only happen in maps in the path are
		// transactional.  Node events may also happen.
		NodeSpecification contextRoot = (NodeSpecification)this.getRootPath().cloneAny();

		// Add the strict specifier followed by the any-trailing-path
		// token.
		contextRoot.add(NodeSpecification.strict__);
		contextRoot.add(AnyAlwaysEquals.instance());

		// all fields
		nodeSpecs.add(contextRoot, AnyAlwaysEquals.instance());

		// Add the model root (with no fields) to pick up events when
		// the model is replaced.
		nodeSpecs.add(this.getRootPath().cloneAny(), AbstractComposite.fieldSet());
	}

  public void markStale() {}

	public Any getContext()
	{
    return context_;
	}

	public void setContext(Any context)
	{
		context_ = context;
	}


  // Build the tree path in the Array path and return the
  // index of the affected node, or -1 if not applicable.
  public int translateEvent(Any               startAt,
                            Event             e,
                            Map               eventType,
                            Any               baseType,
                            boolean           force,
                            Array             path,
                            NodeSpecification eventPath) throws AnyException
  {
    Iter nodeSpecIter = eventPath.createPathItemsIter();

    Vectored v = resolveDataNode(context_, force);

  	// From startAt consume node specification elements
  	// until we reach v, which represents the Inq parent
  	// of the tree nodes at a given level.

  	Any current = startAt;

		while (current != v && nodeSpecIter.hasNext())
		{
			Any a = nodeSpecIter.next();
			current = ((Map)current).get(a);
		}

		if (!nodeSpecIter.hasNext() || current != v)
		{
			// getting here means we have exhausted the path
			// or the Inq parent node could not be found.
			// Either way, this level cannot be relevant to the
			// formation of the tree path to the TreeModelListsner
		  return -1;
		}

    // Now start consuming the node spec entries until they
    // are exhausted. All elements are eligible for the tree
    // path.
	  //path.add(current); // add the root.
	  //System.out.println("HierarchyTreeLevel.translateEvent: found root");
	  Vectored parent = (Vectored)current; // in case its the root.
	  Any currentKey = null;
		while (nodeSpecIter.hasNext())
		{
			currentKey = nodeSpecIter.next();
	    //System.out.println("HierarchyTreeLevel.translateEvent: found " + currentKey);
			parent  = (Vectored)current;
			Map mCurrent = (Map)current;
			if (!mCurrent.contains(currentKey))
			{
        // we've failed at some point in the path.  Assume
        // its a delete. The node is already gone from the
        // structure but (for a client) will be in the event.
        // put it in the tree path and quit out
        Any ec = e.getContext();
        if (ec != null)
          path.add(ec);
        return -1;
      }
			current = ((Map)current).get(currentKey);
	    path.add(current);
		}

		if (v == current)
		  return -1;

    //System.out.println("HierarchyTreeLevel.translateEvent: leaving at " + currentKey);
    //System.out.println("HierarchyTreeLevel.translateEvent: index " + parent.indexOf(currentKey));
	  // Work out the index of the child. If the event is of
	  // base type NODE_REMOVED then this is derived from the
	  // event id map and handled in the enclosing AnyTreeModel.
	  int childIndex = (baseType.equals(EventConstants.NODE_REMOVED) ||
                      baseType.equals(EventConstants.NODE_REMOVED_CHILD))
	                 ? -1
	                 : parent.indexOf(currentKey);

	  return childIndex;
  }

	public Component getTreeCellRendererComponent(JTree        tree,
	                                              AnyTreeNode  value,
	                                              boolean      selected,
	                                              boolean      expanded,
	                                              boolean      leaf,
	                                              int          row,
	                                              boolean      hasFocus)
  {
    return renderer_.renderTreeCell(tree,
                                    value,
                                    selected,
                                    expanded,
                                    leaf,
                                    row,
                                    hasFocus);
  }

//  public DefaultTreeCellRenderer getDefaultTreeCellRenderer(Any a)
//  {
//    return renderer_.getDefaultTreeCellRenderer();
//  }

  public TreeCellEditor getTreeCellEditor(Any a)
  {
    if (editor_ == null)
      editor_ = new AnyComponentEditor(renderer_.getRenderInfo());
    return editor_;
  }

	public void setRenderInfo(RenderInfo r)
	{
    // Not applicable - see inner class Render below
	}

  public RenderInfo getRenderInfo()
  {
    // Hmmm. Just to satisfy interface.
    return renderer_.getRenderInfo();
  }

  public AnyComponentEditor getEditor()
  {
    return editor_;
  }

  public void setEditor(AnyComponentEditor ace)
  {
    editor_ = ace;
  }

  public Vectored getVectorParent(AnyTreeNode parent) throws AnyException
  {
    return null;
  }

  public Vectored getVectorParent(Any context) throws AnyException
  {
    return null;
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

  public void setLeafExpression(Any isLeaf)
  {
    throw new UnsupportedOperationException("setLeafExpression");
  }

	public TreeNodeExpansion addExpansion(RenderInfo r, Locate node, TreeLevel l) { return null; }

  private Vectored resolveDataNode(Any root, boolean force) throws AnyException
	{
		//System.out.println ("AnyTableModel.resolveDataNode : " + root);
		Vectored dataNode = levelRootV_;

		if (dataNode == null || force)
		{
	    dataNode = (Vectored)EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
																	      root,
																	      levelRoot_);
			levelRootV_ = dataNode;
		}

		return dataNode;
	}

	private int getChildIndex(Vectored v, Object child)
	{
		int entries =  v.entries();
		for (int i = 0; i < entries; i++)
		{
			Any a = v.getByVector(i);
			if (a.equals(child))
			  return i;
		}
		return -1;
	}

  // The function to resolve the data node for rendering is fixed
  // for a HierarchyTreeLevel.  If we are given a composite then
  // the resulting value is the name of that composite in its
  // parent.  Otherwise toString() is applied (may be enhance with
  // formatting?)  Note that, for composites, object must support
  // the getNameInParent() method.
	static private class Render extends    AbstractFunc
                              implements Cloneable
  {
    public Any exec(Any a) throws AnyException
    {
      if (a instanceof Composite)
      {
        Composite c = (Composite)a;
        return c.getNameInParent();
      }
      else
      {
        return new ConstString(a.toString());
      }
    }

    public Object clone () throws CloneNotSupportedException
    {
      return super.clone();
    }
  }
}
