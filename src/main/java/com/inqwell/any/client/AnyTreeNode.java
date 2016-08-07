/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTreeNode.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;

import javax.swing.JTree;
import java.awt.Component;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

/**
 * A carrier for information obtained from <code>AnyTreeModel</code>,
 * and <code>TreeLevel</code> implementations.  Instances are
 * returned to a <code>JTree</code> such that all necessary
 * information is available during model and rendering callbacks.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class AnyTreeNode extends AbstractAny
{
  // The data that this instance is carrying
  private Any         any_;
  
  // The context from which the data was resolved
  private Any         context_;
  
  // The TreeLevel implementation that configured the above
  private TreeLevel   treeLevel_;
  
  // Our parent, or null if we are the root node
  private AnyTreeNode parent_;
  
  // Lazily created collection to hold our children 
  private VectoredSet children_;
  
  // If set to true then we never create children_
  private boolean     isLeaf_;
  
  // Maintained by view listeners so that view state can be
  // preserved in the face of sorting etc.
  private boolean     expanded_;
  
  AnyTreeNode(Any       any,
              Any       context,
              TreeLevel treeLevel)
  {
    this(null, any, context, treeLevel, false);
  }
  
  AnyTreeNode(AnyTreeNode parent,
              Any         any,
              Any         context,
              TreeLevel   treeLevel)
  {
    this(parent, any, context, treeLevel, false);
  }
  
  AnyTreeNode(AnyTreeNode parent,
              Any         any,
              Any         context,
              TreeLevel   treeLevel,
              boolean     isLeaf)
  {
    if (any == null)
      throw new IllegalArgumentException("User data cannot be null");
      
    setAny(any);
    parent_    = parent;
    setLevel(treeLevel);
    //treeLevel_ = treeLevel;
    isLeaf_    = isLeaf;
    setContext(context);
    
    if (parent_ != null && treeLevel_ == null)
      throw new AnyRuntimeException("No TreeLevel for " + any);
      
    if (parent_ != null)
    {
      parent_.addChild(this);
    }
  }
  
  // Just for findNode()
  AnyTreeNode(Any any)
  {
    any_ = any;
  }
  
  // Just for dummy root
  AnyTreeNode(TreeLevel treeLevel)
  {
    isLeaf_ = true;
    setLevel(treeLevel);
  }
  
  /**
   * Add the given node as a child to this
   */
  private void addChild(AnyTreeNode node)
  {
    if (isLeaf_)
      throw new AnyRuntimeException("Leaf nodes cannot contain children");
    
    if (children_ == null)
      children_ = new VectoredSet();
    
    children_.add(node);
  }
  
  void addChildAt(AnyTreeNode node, int at)
  {
    if (isLeaf_)
      throw new AnyRuntimeException("Leaf nodes cannot contain children");
    
    if (children_ == null)
      children_ = new VectoredSet();
    
    node.setParent(this);
    children_.addByVector(at, node);
  }
  
  Any getAny()
  {
    return any_;
  }
  
	Any getValueFor()
  {
    return treeLevel_.getValueFor(this);
  }
  
	Any getResponsibleFor()
  {
    return treeLevel_.getResponsibleFor(this);
  }
  
  TreeLevel getTreeLevel()
  {
    return treeLevel_;
  }
  
  void setAny(Any any)
  {
    // The any_ member is what we hash/equals by, so if it is
    // changed we need to inform our parent so that the integrity
    // of its children_ collection is maintained.
    boolean changing = (parent_ != null) &&
                       ((any == null && any_ != null) ||
                        (any_ == null && any != null) ||
                        (any != null && !any.equals(any_)));
    
    int i = -1;
    AnyTreeNode parent = parent_;
    
    if (changing)
    {
      i = parent_.indexOf(this);
      if (i >= 0)
        parent_.remove(i);
    }

    any_ = any;
    
    if (changing && i >= 0)
    {
      if (parent.indexOf(this) >= 0)
      {
        int xx = parent.indexOf(this);
      }
      parent.addChildAt(this, i);
      parent_ = parent;
    }
  }
  
  AnyTreeNode getChildAt(int i)
  {
    if (children_ == null)
      throw new IllegalStateException("No children yet added");
    
    return (AnyTreeNode)children_.getByVector(i);
  }
  
  int indexOf(AnyTreeNode child)
  {
    if (children_ == null)
      return -1;
    
    return children_.indexOf(child);
  }
  
  boolean contains(AnyTreeNode child)
  {
    if (children_ == null)
      return false;
    
    return children_.contains(child);
  }
  
  void sizeTo(int size)
  {
    // If the requested size is same or larger than we already are
    // then ignore.  Otherwise downsize to requested size.
    if (children_ == null || children_.entries() <= size)
      return;
    
    int entries = children_.entries();
    int j = entries - size;
    for (int i = 0; i < j; i++)
      children_.removeByVector(--entries);
  }
  
  AnyTreeNode getParent()
  {
    return parent_;
  }
  
  boolean isRoot()
  {
    return parent_ == null;
  }
  
  Any getContext()
  {
    return context_;
  }
  
  void setContext(Any context)
  {
    context_ = context;
  }
  
  void setParent(AnyTreeNode node)
  {
    if (parent_ != null && node != null)
      throw new IllegalStateException("Parent already set");
    
    parent_ = node;
  }
  
  void setContents(Any         any,
                   Any         context,
                   TreeLevel   treeLevel)
  {
    this.setContents(any, context, treeLevel, false);
  }
  
  void setContents(Any         any,
                   Any         context,
                   TreeLevel   treeLevel,
                   boolean     isLeaf)
  {
    setAny(any);
    setLevel(treeLevel);
    //treeLevel_ = treeLevel;
    isLeaf_    = isLeaf;
    setContext(context);
  }
  
  /**
   * Replaces this node in its parent with a new one that has no children
   * at the same index position.
   * Children will be recreated when the tree model (which is messaged with the
   * appropriate TreeModelEvent) requests them. Although this node's parent
   * no longer has a reference to us, we keep the reference to our parent
   * so that the expansion state of <code>this</code>, the children of
   * of <code>this</code> and the TreePath back to the root can all be
   * evaluated. This allows the expansion state of the tree view to be maintained
   * even though there may be new AnyTreeNodes in the model, for example
   * after sorting the children of an AnyTreeNode.
   */
  void setStale()
  {
    if (parent_ == null)
      throw new IllegalStateException("root or orphan tree node");
    
    // Keep the current parent as it is cleared by the removal
    AnyTreeNode parent = parent_;
    
    int i = parent_.indexOf(this);
    
    // Since we must have a parent the index should be valid. Allow
    // to throw if somehow it is not.
    parent_.remove(i);

    // Make a new AnyTreeNode that looks like us and place in parent
    AnyTreeNode n = new AnyTreeNode(null,
                                    any_,
                                    context_,
                                    treeLevel_,
                                    isLeaf_);
    n.setExpanded(this.isExpanded());
    
    parent.addChildAt(n, i);
    //n.setParent(parent);

    // Restore our parent link
    parent_ = parent;
  }
  
  public boolean isLeaf()
  {
    return isLeaf_;
  }
  
  public void setExpanded(boolean expanded)
  {
    expanded_ = expanded;
    
    // If we are marked as expanded then all nodes back to the
    // root are expanded as well.  If we are collapsed then all
    // possible paths below us are collapsed as well.
    if (expanded && parent_ != null)
    {
      parent_.setExpanded(true);
    }
    else if (!expanded && children_ != null)
    {
      int max = children_.entries();
      for (int i = 0; i < max; i++)
      {
        AnyTreeNode child = (AnyTreeNode)children_.getByVector(i);
        child.setExpanded(false);
      }
    }
  }
  
  public boolean isExpanded()
  {
    return expanded_;
  }
  
  /**
   * Convert the path represented by this node back to the root into
   * a TreePath
   */
  TreePath makeTreePath()
  {
    // First see how many objects there back to the root
    AnyTreeNode n = this;
    int         i = 1;
    while ((n = n.getParent()) != null)
      i++;
    
    TreePath ret = null;
    
    // If there is only one object (we are the root) then no need to
    // create an array
    if (i == 1)
      ret = new TreePath(this);
    else
    {
      Object[] path = new Object[i];
      n = this;
      do
        path[--i] = n;
      while ((n = n.getParent()) != null);
      ret = new TreePath(path);
    }
    
    return ret;
  }
  
  int entries()
  {
    if (isLeaf_)
      return 0;
    
    if (children_ == null)
      return 0;
    
    return children_.entries();
  }
  
  void empty()
  {
    if (children_ != null)
      children_.empty();
  }
  
  /**
   * Remove the child node at the index position. After removal the child
   * node will have no parent.
   * @param i index position.
   * @return the node removed.
   */
  AnyTreeNode remove(int i)
  {
    if (children_ == null)
      throw new IllegalStateException("No children yet added");
    
    AnyTreeNode node = (AnyTreeNode)children_.getByVector(i);
    children_.removeByVector(i);
    node.setParent(null);
    
    return node;
  }
  
  boolean isExpansion()
  {
    return false;
  }
  
  TreeNodeExpansion getExpansion()
  {
    throw new UnsupportedOperationException("Not an expansion node");
  }
    
  public boolean equals(Any a)
  {
    if (a == null)
      return false;
    
    if (a == this)
      return true;
          
    if (a == null && any_ != null)
      return false;
      
    if (a != null && any_ == null)
      return false;
      
    if (!(a instanceof AnyTreeNode))
      return false;
    
    AnyTreeNode node = (AnyTreeNode)a;
    
    if (any_ == null)
    {
      if (node.any_ == null)
        return true;
      else
        return false;
    }
    
    if (node.any_ == null)
    {
      if (any_ == null)
        return true;
      else
        return false;
    }
    
    // Need the null test on treeLevel_ for model event translation
    if (treeLevel_ == null || treeLevel_.getEquals() == null || node.treeLevel_ == null)
      return this.any_.equals(node.any_);
    else if (treeLevel_ != node.treeLevel_)
      return false;
    else
    {
      // By the time we get here we should be dealing with two
      // fully-fledged tree nodes. Have established they have the
      // same tree level. If their parents are both null then
      // we are dealing with two root nodes.
      if (parent_ == null && node.parent_ == null)
        return true;
      
      // Two nodes at the same level and with an equals expression
      Transaction t = Globals.process__.getTransaction();
      AnyFuncHolder.FuncHolder f = (AnyFuncHolder.FuncHolder)treeLevel_.getEquals();
      Any equals = f.getFunc();
      try
      {
        Any a1 = EvalExpr.evalFunc(t,
                                   any_,
                                   equals);

        Any a2 = EvalExpr.evalFunc(t,
                                   node.any_,
                                   equals);
        
        if (a1 != null && a2 != null)
          return a1.equals(a2);
        else
          return false;
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
  
  public int hashCode()
  {
    if (any_ == null)
      return 0;
      
    return any_.hashCode();
  }
  
  public String toString()
  {
    //String s = (any_ != null) ? ("" + System.identityHashCode(any_))
    //                          : "null";
    //s += " " + ((treeLevel_ != null) ? treeLevel_.toString() : "null");
    
    return super.toString() +
           " carrying: " + System.identityHashCode(any_) +
           " context:  " + System.identityHashCode(context_) +
           " expanded: " + expanded_ +
           " leaf: " + isLeaf_ +
           " #child " + entries();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    AnyTreeNode n = (AnyTreeNode)super.clone();
    n.children_ = null;
    n.parent_   = null;
    
    return n;
  }
  
  /**
   * Performs a depth-first scan of the tree rooted at this
   * to search for the given node containing the argument as
   * its any. Returns null if no matching node can be found
   */
  AnyTreeNode findNode(Any data)
  {
    if (any_.equals(data))
      return this;
    
    AnyTreeNode n = new AnyTreeNode(data);
    
    return scanForNode(n);
  }
  
  private AnyTreeNode scanForNode(AnyTreeNode node)
  {
    if (children_ == null)
      return null;
      
    int j = children_.entries();
    
    for (int i = 0; i < j; i++)
    {
      Any n = children_.getByVector(i);
      if (n.equals(node))
        return (AnyTreeNode)n;
      
      AnyTreeNode next = ((AnyTreeNode)n).scanForNode(node);
      if (next != null)
        return next;
    }
    return null;
  }
  
  private void setLevel(TreeLevel level)
  {
    if (level == null)
    {
      throw new IllegalArgumentException("level cannot be null");
    }
    treeLevel_ = level;
  }
  
  /**
   * Provides additional storage and recognition of the fact that
   * this tree node is an explicit expansion.
   */
  static class ExpansionTreeNode extends AnyTreeNode
  {
    private TreeNodeExpansion e_;
    
    public ExpansionTreeNode(Any       any,
                             Any       context,
                             TreeLevel treeLevel)
    {
      this(null, any, context, treeLevel);
    }
    
    public ExpansionTreeNode(AnyTreeNode parent,
                             Any         any,
                             Any         context,
                             TreeLevel   treeLevel)
    {
      // Leaf status in base class is not used
      super(parent, any, context, treeLevel, false);
    }
    
    boolean isExpansion()
    {
      return true;
    }
    
    public boolean isLeaf()
    {
      return e_.isLeaf();
    }
    
    private void setExpansion (TreeNodeExpansion e)
    {
      if (e == null)
        throw new IllegalArgumentException("TreeNodeExpansion cannot be null");

      // See this.setAny()
      if (e_ == null)
      {
        try
        {
          super.setAny(e.resolveStructureNode(getContext()));
        }
        catch (AnyException ex)
        {
          throw new RuntimeContainedException(ex);
        }
      }
      e_ = e;
    }

    TreeNodeExpansion getExpansion()
    {
      return e_;
    }
    
    void setContext(Any context)
    {
      Any oldContext = getContext();
      boolean mustResolve = (context != oldContext);
      
      super.setContext(context);
      
      if (mustResolve && e_ != null)
      {
        try
        {
          super.setAny(e_.resolveStructureNode(context)); // could be null
        }
        catch (AnyException ex)
        {
          throw new RuntimeContainedException(ex);
        }
      }
    }
    
    // Any tree level calls us with passing the TreeNodeExpansion.
    // Store this as such and put as the base class any_ the
    // structure node that expansion is related to.
    void setAny(Any any)
    {
      if (any instanceof TreeNodeExpansion)
      {
        setExpansion((TreeNodeExpansion)any);
      }
      else
      {
        // Assume its the data we carry
        super.setAny(any);
      }
      
    }
  }
}
