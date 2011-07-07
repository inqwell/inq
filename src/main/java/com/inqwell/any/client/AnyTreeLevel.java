/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTreeLevel.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractEvent;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.Any;
import com.inqwell.any.AnyAlwaysEquals;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.BuildNodeMap;
import com.inqwell.any.Call;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Orderable;
import com.inqwell.any.RunInq;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.TableModel;
import com.inqwell.any.client.swing.TreeTableModel;

/**
 * A TreeLevel implementation that maps a container node
 * in an Inq structure to a tree level for display.
 * <H3>Inq-to-Tree Node Mapping</H3>
 * An <code>AnyTreeLevel</code> references a node, in the Inq sense,
 * the immediate children of which determine the
 * number of tree nodes at the next level. There are two ways in which
 * the nodes a given level can be considered branches or leaves:
 * <ol>
 * <li>
 * If this <code>AnyTreeLevel</code> itself contains a
 * child <code>AnyTreeLevel</code> then this level is represented
 * as branch tree nodes. Otherwise, in the absence of explicit
 * expansion declarations (see below) the level is represented as
 * leaf nodes.</li>
 * <li>
 * If an <code>AnyTreeLevel</code> has explicit expansion
 * declarations then the current level is deemed a branch
 * node irrespective of whether there is a
 * child <code>AnyTreeLevel</code>.
 * </li>
 * </ol>
 * <p>
 * When expansion declarations are used, the number of
 * tree nodes at this level is determined by the number of
 * expansion declarations and is therefore part of the GUI
 * configuration. If a tree level has a child tree level, the
 * number of tree nodes at this level is the number of Inq
 * children at the referenced node.
 * <p>
 * It is permissable to have a child tree level and expansion
 * declarations in the same <code>AnyTreeLevel</code> instance.
 * By default, the expansion declarations are returned first,
 * followed by the Inq children.
 * <H3>Rendering</H3>
 * For tree nodes determined by Inq children, a single rendering
 * specification is applied to each child node, using that child
 * as the context for node resolution within any rendering
 * expression. Expansion declarations are represented by one
 * rendering specification per tree child applied to the
 * referenced node.
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class AnyTreeLevel extends AbstractTreeLevel
{
  private static final long serialVersionUID = 1L;
  
  // A dummy node that is carried in the root AnyTreeNode for the
  // case where the root only contains expansions. This is not
  // often the case so only create it lazily.
  private static Any dummyNode__;

  // The levelRoot_ is the node, relative
	// to the context, that is the Inq parent of
	// the tree nodes at this level.
	private Locate      levelRoot_;
	private Vectored    levelRootV_;

  // If we are recursive then level2 and below will have
  // a different resolver for the subtree root.  This is
  // hard coded to the last path component of the levelRoot_.
  // If we are not recursive then this is always null.
  // So that we know whether to use subRoot_ have a flag that
  // says when we are recursing.
  private Locate      subRoot_;

  //private boolean     recursing_;

  // See above.  We remember the context so that we can optimise
  // the resolution of sub-tree children.
	private Any         context_;

	// When asked to return a tree node, if we are
	// not the correct level delegate to the next
	// one.
	private TreeLevel   nextLevel_;

  // An optional expression to specify whether nodes at
  // this level are leaf nodes.
	private Call        isLeaf_;
  private BooleanI    leafRes_ = new AnyBoolean();

  //...and optional explicitly declared children specified by
  // individual renderers.
  private Array       expansionNodes_;

  // If the Inq children are editable then this will
  // be set.  For the expansion nodes the editor is held
  // within the Expansion class.
  private AnyComponentEditor editor_;

	// If we must resolve the root data node.
	private boolean     mustResolve_ = true;

  // Each level has an event dispatcher to determine
  // whether an Inq event should be processed at this
  // level.
  //private EventDispatcher  levelDispatcher_;

  // Used to help isLeaf in top level only
  //private Queue       leafPath_;

  // A comparator that can sort the vector-based nodes at this
  // level. This is typically used to effect long term ordering
  // that is maintained as new nodes are inserted.
  private OrderComparator sortComparator_;
  
  // As above but provided by interactive means, for example by
  // selecting columns in a treetable display.
  //private OrderComparator interactiveComparator_;

	static public Any  node__ = new ConstString("node");

  /**
	 * Construct an <code>AnyTreeLevel</code> object which
	 * represents a leaf level in the tree representation.
	 * @param l the location of the node representing the
	 * parent of the nodes at this level.
	 */
	public AnyTreeLevel(Locate l)
	{
		this(l, TreeLevel.terminalLevel__);
	}

	/**
	 * Construct a <code>TreeLevel</code> object which
	 * represents a non-leaf level in the tree representation.
	 * @param l the location of the node representing the
	 * parent of the nodes at this level.
	 * @param nextLevel the next level in the tree.
	 */
	public AnyTreeLevel(Locate l, TreeLevel nextLevel)
	{
		levelRoot_ = l;

    if (nextLevel != null)
      nextLevel_ = nextLevel;
    else
    {
      nextLevel_ = this; // recursive structure
      subRoot_ = new LocateNode(l.getNodePath().getLast().toString());
    }
	}
  
  /**
   * Construct a <code>TreeLevel</code> that defines a recursive
   * structure whose first-level nodes are at <code>root</code>
   * and subsequent levels at <code>sub</code> with respect to
   * the node-set child.
   * @param root
   * @param sub
   */
  public AnyTreeLevel(Locate root, Locate sub)
  {
    levelRoot_ = root;
    nextLevel_ = this; // recursive structure
    subRoot_ = (sub != null) ? sub
                             : new LocateNode(root.getNodePath().getLast().toString());
  }

  /**
   * Set the context from which this level's vector or Inq children
   * and rendering information will be resolved.  A TreeLevel is
   * shared amongst all the children at a given level, so the context
   * cannot be guaranteed between method calls.  An AnyTreeNode
   * instance carries the context that should be used at a particular
   * level for a particular child and allows us to set this value into
   * the TreeLevel as required.
   */
	public void setContext(Any context)
	{
		if (context_ != context)
		  mustResolve_ = true;

		context_ = context;
	}

  public void markStale()
  {
    mustResolve_ = true;
  }

	public Any getContext()
	{
    return context_;
	}

	public Any getChild(Object parent, int index) throws AnyException
  {
    // Entry point from model is the first level definition under the
    // root.
    // This method either operates on this or acts as a dispatcher to
    // a sub-level

    AnyTreeNode pNode = (AnyTreeNode)parent;

    if (pNode.getParent() == null)
    {
      // Requesting child of root (that is, a first-level child).
      // Use this level directly.
      Vectored v = resolveDataNode(context_, false, false);

      // getChildNode handles whether vector child or explicit expansion.
      // NB at the first level the context does not vary
  		Any any = getChildNode(v, index, pNode, context_);

      return any;
    }
    else if (!pNode.isExpansion())
    {
      // Requesting a child of a sub-level where parent is not an
      // explicit expansion.  We know that the TreeLevel
      // to use is the next level from that contained within the given
      // parent carrier.  Even if we are recursive this is so, because
      // the next level is this level, just pass the recursive flag.
      AnyTreeLevel l = (AnyTreeLevel)pNode.getTreeLevel().getNextTreeLevel();

      // Put in the context - for level 2 and below it will be
      // different for each subtree (that is index) beneath the
      // current parent.  Inq structure convention requires this
      // to be the any_ in the parent carrier (except in the
      // first level case).
      Any context = (pNode.getParent() == null) ? pNode.getContext()
                                                : pNode.getAny();
      l.setContext(context);
      Vectored v = l.resolveDataNode(context, false, l.isRecursive());

      Any any = l.getChildNode(v, index, pNode, context);

      return any;
    }
    else
    {
      // Requesting child of an explicit expansion.  The tree level to
      // use for the children is the one inside the expansion definition
      // See above for significance of context.
      AnyTreeLevel l = (AnyTreeLevel)pNode.getExpansion().getTreeLevel();
      
//      Any context = (pNode.getParent() == null) ? pNode.getContext()
//          : pNode.getAny();
      // Surely for expansion children its always getContext()?
      // TODO: to be tested.
      Any context = pNode.getContext();
      
      l.setContext(context);
      // This is the top level of any recursion that might be taking
      // place below us
      Vectored v = l.resolveDataNode(context, false, false);
      Any any = l.getChildNode(v, index, pNode, context);
      return any;
    }
  }

  public int getChildCount(Object parent) throws AnyException
  {
    // May be this can be called before all the children are in (makes
    // sense) so we check the structure.
    AnyTreeNode pNode = (AnyTreeNode)parent;

    int count = 0;

    if (pNode.getParent() == null)
    {
      Vectored v = resolveDataNode(context_, false, false);

      if (v != null)
        return v.entries() + expansionNodesOffset();

      count = expansionNodesOffset();
    }
    else if (!pNode.isExpansion())
    {
      TreeLevel l = pNode.getTreeLevel().getNextTreeLevel();
      if (l == TerminalTreeLevel.terminalLevel__)
      {
        //System.out.println("TERMINAL");
        return 0;
      }

      AnyTreeLevel atl = (AnyTreeLevel)l;

      // Put in the context - for level 2 and below it will be
      // different for each subtree (that is index) beneath the
      // current parent.  Inq structure convention requires this
      // to be the any_ in the parent carrier (except in the
      // first level case)
      Any context = (pNode.getParent() == null)
                                              ? pNode.getContext()
                                              : pNode.getAny();
      atl.setContext(context);
      Vectored v = atl.resolveDataNode(context, false, atl.isRecursive());

      count = levelCount(v, atl);
    }
    else
    {
      // Requesting count of an explicit expansion.
      // See above for significance of context.
      AnyTreeLevel l = (AnyTreeLevel)pNode.getExpansion().getTreeLevel();
      // If there is no tree level in the expansion then there are
      // no children.
      if (l != null)
      {
        Any context = pNode.getContext();
        l.setContext(context);
        // This is the top level of any recursion that might be taking
        // place below us
        Vectored v = l.resolveDataNode(context, false, false);
        count = l.levelCount(v, l);
      }
    }
    pNode.sizeTo(count);
    return count;
  }

  public int getIndexOfChild(Object parent, Object child) throws AnyException
  {
    AnyTreeNode pNode = (AnyTreeNode)parent;
    AnyTreeNode cNode = (AnyTreeNode)child;

    return pNode.indexOf(cNode);
  }

  public Any getRoot() throws AnyException
  {
    // Note - only called on the top level instance (or once
    // at root node context when in recursive mode)
    
    // If the root has no vector specified (meaning it only holds
    // explicit expansions) then put the context in as the any
    // we carry. The context will always be present, does not change
    // and (fwiw) is in the event path to the tree data proper
    if (levelRoot_ == AnyNull.instance())
    {
      return new AnyTreeNode(null, context_, context_, this);
    }
    
  	Vectored v = resolveDataNode(context_, false, false);
  	//System.out.println("AnyTreeLevel.getRoot " + v);
    // If we resolve the root then we put this TreeLevel
    // into the returned AnyTreeNode.  This is OK and serves
    // for editing functions.
    if (v != null)
    {
      return new AnyTreeNode(null, v, context_, this);
    }
    else
      return null;
  }

  /**
   * Returns the vector that is the Inq node parent of which
   * the given tree node is a child. NB: Not actually used.
   * @return the vector parent or null if given parent is the lowest
   * level.
   */
  public Vectored getVectorParent(AnyTreeNode node) throws AnyException
  {
    if (node.getParent() == null)
    {
      // The given parent is the root. Use this level directly.
      
      // Actually, by the definition of what this function does we
      // might return null, because for the root there is no vector parent.
      Vectored v = resolveDataNode(context_, false, false);

      //System.out.println("getVectorParent 2 " + ((v == null) ? 0 : System.identityHashCode(v)));

      return v;
    }
    else if (!node.isExpansion())
    {
      TreeLevel l = node.getTreeLevel();
      //System.out.println("getVectorParent 3 " + l.getName());
      //TreeLevel l = parent.getTreeLevel().getNextTreeLevel();
      //if (l == TerminalTreeLevel.terminalLevel__)
      //{
        //System.out.println("TERMINAL");
        //return null;
      //}

      // Requesting a child of a sub-level where parent is not an
      // explicit expansion.  We know that the TreeLevel
      // to use is the next level from that contained within the given
      // parent carrier.  Even if we are recursive this is so, because
      // the next level is this level, just pass the recursive flag.
      AnyTreeLevel atl = (AnyTreeLevel)l;

      // Put in the context - for level 2 and below it will be
      // different for each subtree (that is index) beneath the
      // current parent.  Inq structure convention requires this
      // to be the any_ in the parent carrier (except in the
      // first level case).
      Any context = node.getParent().getAny();

      atl.setContext(context);
      Vectored v = atl.resolveDataNode(context, false, l.isRecursive());

      //System.out.println("getVectorParent 4 " + ((v == null) ? 0 : System.identityHashCode(v)));
      return v;
    }
    else
    {
      throw new IllegalArgumentException("No vector parent of explicit expansions");
    }
  }

  public Vectored getVectorParent(Any context) throws AnyException
  {
    setContext(context);

    Vectored v = resolveDataNode(context, false, isRecursive());

    return v;
  }
  
  // Return the vector containing the Inq children of the given node
  // or null if the vector cannot be resolved or the node is at the
  // bottom level
  public Vectored getChildVector(AnyTreeNode n) throws AnyException
  {
    Vectored v = null;
    
    if (n.getParent() == null)
    {
      // n is the root node
      v = resolveDataNode(context_, false, false);
    }
    else if (!n.isExpansion())
    {
      TreeLevel l = n.getTreeLevel().getNextTreeLevel();
      if (l == TerminalTreeLevel.terminalLevel__)
      {
        return null;
      }

      AnyTreeLevel atl = (AnyTreeLevel)l;

      // Put in the context - for level 2 and below it will be
      // different for each subtree (that is index) beneath the
      // current parent.  Inq structure convention requires this
      // to be the any_ in the parent carrier (except in the
      // first level case)
      Any context = (n.getParent() == null) ? n.getContext()
                                            : n.getAny();
      
      atl.setContext(context);
      v = atl.resolveDataNode(context, false, atl.isRecursive());
    }
    else
    {
      AnyTreeLevel l = (AnyTreeLevel)n.getExpansion().getTreeLevel();

      Any context = (n.getParent() == null) ? n.getContext()
                                            : n.getAny();
      
      l.setContext(context);
      // This is the top level of any recursion that might be taking
      // place below us
      v = l.resolveDataNode(context, false, false);
    }

    return v;
  }

	public NodeSpecification getRootPath()
	{
    // Note - only called once when in recursive mode and
    // for the root context path in that case.
		NodeSpecification root = levelRoot_.getNodePath();

		return root;
	}

	public TreeLevel getTreeLevelForPath(TreePath p)
	{
	  // 0 is root and that is handled in AnyTreeModel

    //if (isRecursive())
      //return this;  // In this case there's only one anyway

  	AnyTreeNode treeNode = (AnyTreeNode)p.getLastPathComponent();

    return treeNode.getTreeLevel();
	}

  public TreeLevel getNextTreeLevel()
  {
    return nextLevel_;
  }


  // Resolve the node specifications that should dispatch to the tree
  // overall.  [Note to readers - the context parameter is the prevailing
  // GUI context and is only used to resolve indirections in the node
  // paths.  It doesn't have magical properties at descendent levels!!]
	public void resolveNodeSpecs(NodeSpecification rootPath,
                               Map               nodeSpecs,
                               TreeTableModel    treeTableModel,
                               Any               contextNode)
	{
    //  EXPANSION NODE SPECIFICATIONS ARE TODO !!

		// Suffix outer specification with ours
		NodeSpecification contextRoot = (NodeSpecification)rootPath.cloneAny();
		NodeSpecification myRoot = this.getRootPath();
		for (int i = 0; i < myRoot.entries(); i++)
		  contextRoot.add(myRoot.get(i));

		// Add the thisEquals (and veryLazy if we are recursive) specifier as the
    // rendering node specifications are applied to the children of the
    // level root.
    NodeSpecification childRoot = (NodeSpecification)contextRoot.cloneAny();
    if (isRecursive())
    {
      childRoot.add(NodeSpecification.thisEquals__);
      childRoot.add(NodeSpecification.veryLazy__);
    }
    else
      childRoot.add(NodeSpecification.thisEquals__);

		// Where we will collect the node specifications generated
		// by this level.
		Map thisNodeSpecs = AbstractComposite.simpleMap();
		
    // Perform path evaluation on the tree renderer 
		RenderInfo r = renderer_.getRenderInfo();
    addEventPaths(r, childRoot, thisNodeSpecs, contextNode);

    // If there are columns then we are employed in a tree-table
    // context. Use the table column or tree-level override as
    // appropriate and evaluare its paths.
    if (treeTableModel != null)
    {
      // Column zero is always the tree renderer
      for (int i = 1; i < treeTableModel.getColumnCount(); i++)
      {
        r = this.getColumnRenderInfo(i);
        if (r == null)
          r = treeTableModel.getRenderInfo(i);

        addEventPaths(r, childRoot, thisNodeSpecs, contextNode);
      }
    }
    
		// Add the roots (with no fields) to pick up events when
		// the node-set or node-set children are replaced.
    thisNodeSpecs.add(contextRoot, AbstractComposite.fieldSet());
    if (isRecursive())
    {
      // In the recursive case we still want the immediate child
      // of our root but with "thisEquals" only and
      // not "thisEquals"+"veryLazy" as we set up above for childRoot.
      // As well, we want the subRoot and its children, reaching the
      // subRoot with the "veryLazy" specifier. Got that?
      NodeSpecification nodeSetChild = (NodeSpecification)contextRoot.cloneAny();
      nodeSetChild.add(NodeSpecification.thisEquals__);
      NodeSpecification subRootNs = (NodeSpecification)contextRoot.cloneAny();
      subRootNs.add(NodeSpecification.thisEquals__);
      subRootNs.add(NodeSpecification.veryLazy__);
      subRootNs.add(subRoot_.getNodePath().getLast());
      NodeSpecification subRootChildNs = (NodeSpecification)subRootNs.cloneAny();
      subRootChildNs.add(NodeSpecification.thisEquals__);
      thisNodeSpecs.add(nodeSetChild, AbstractComposite.fieldSet());
      thisNodeSpecs.add(subRootNs, AbstractComposite.fieldSet());
      thisNodeSpecs.add(subRootChildNs, AbstractComposite.fieldSet());
    }
    else
      thisNodeSpecs.add(childRoot, AbstractComposite.fieldSet());

		// When we've done this level add the node specs to the
		// list for the context node as a whole.
		Iter iter = thisNodeSpecs.createKeysIterator();
		while (iter.hasNext())
		{
			Any thisNs    = iter.next();
			Any fieldList = thisNodeSpecs.get(thisNs);
			if (!nodeSpecs.contains(thisNs))
			{
				nodeSpecs.add(thisNs, fieldList);
			}
			else
			{
				Set fieldSet = (Set)nodeSpecs.get(thisNs);
				fieldSet.addAll((Set)fieldList, true);
			}
		}

    // Do the next level unless we are it for evermore
    if (!isRecursive())
      nextLevel_.resolveNodeSpecs(childRoot, nodeSpecs, treeTableModel, contextNode);
	}

  public boolean isLeaf(Object node) throws AnyException
  {
    AnyTreeNode tNode = (AnyTreeNode)node;

    return tNode.isLeaf();
  }

  /**
   * Supply the rendering information for the Inq child nodes
   * represented at this level
   */
	public void setRenderInfo(RenderInfo r)
	{
		renderer_ = new AnyCellRenderer(r, new DefaultTreeCellRenderer());
	}

	public boolean isEditable(Any a)
	{
    AnyTreeNode node = (AnyTreeNode)a;
    // Model gives us the appropriate tree node in the path
		if (!node.isExpansion())
		  return renderer_.isEditable() &&
             editor_ != null
             && editor_.getComponent() != null;
		else
		{
			TreeNodeExpansion e = node.getExpansion();
			return e.isEditable();
		}
	}

	public Any getResponsibleFor(Any a)
	{
    AnyTreeNode node = (AnyTreeNode)a;
    // Model gives us the appropriate tree node in the path
    try
		{
      if (!node.isExpansion())
			  return renderer_.getRenderInfo().resolveResponsibleData(node.getAny());
			else
			{
        TreeNodeExpansion e = node.getExpansion();
				return e.getResponsibleFor(node.getContext());
			}
		}
		catch (AnyException e)
		{
			throw new RuntimeContainedException(e);
		}
	}

	public Any getValueFor(Any a)
	{
    // Model gives us the appropriate tree node in the path.  Do this
    // here even though there is a symbiotic relationship between
    // AnyTreeLevel and AnyTreeNode, because we need the RenderInfo
    // contained within the renderer.
    AnyTreeNode node = (AnyTreeNode)a;
    try
		{
      if (!node.isExpansion())
			  return renderer_.getRenderInfo().resolveDataNode(node.getAny(), true);
			else
			{
        TreeNodeExpansion e = node.getExpansion();
				return e.getValueFor(node.getContext());
			}
		}
		catch (AnyException e)
		{
			throw new RuntimeContainedException(e);
		}
	}

	public Any getSelectionFor(Any a)
	{
    AnyTreeNode node = (AnyTreeNode)a;
    // Model gives us the appropriate tree node in the path
		//try
		//{
    if (!node.isExpansion())
      return node.getAny();
    else
    {
      //TreeNodeExpansion e = (TreeNodeExpansion)a;
      //return e.getResponsibleFor(a);
      return node.getAny();
    }
		//}
		//catch (AnyException e)
		//{
		//	throw new RuntimeContainedException(e);
		//}
	}

	public Any getKeySelectionFor(Any a)
	{
    AnyTreeNode node = (AnyTreeNode)a;
    //if (!node.isExpansion())
    //{
    Composite c = (Composite)node.getAny();
    if (c == null)
      return null;  // when the expansion node des not resolve yet

    return c.getNameInParent();
    //}
    //else
    //{
    //  return null;
    //}
	}

  public void setLeafExpression(Any isLeaf)
  {
    if (isLeaf instanceof BooleanI)
      leafRes_.copyFrom(isLeaf);
    else
      isLeaf_ = AnyComponent.verifyCall(isLeaf);
  }

	/**
	 * Supply rendering information for successive explicit
	 * expansion declarations.
	 */
	public TreeNodeExpansion addExpansion(RenderInfo r, Locate node, TreeLevel l)
	{
    if (expansionNodes_ == null)
      expansionNodes_ = AbstractComposite.array();

    TreeNodeExpansion ret = new LevelExpansion(r, node, l);
    expansionNodes_.add(ret);
    return ret;
  }
  
  public int getExpansionCount()
  {
    if (expansionNodes_ == null)
      return 0;
    
    return expansionNodes_.entries();
  }
  
  /**
   * Return the index of the first expansion whose root node
   * could be located by the given path. Required by event dispaching
   * to determine the index for the TreeModelEvent.
   * @param path
   * @return index of matching expandsion or -1 if there is no match (possibly
   * because there are no expansions configured at this level).
   */
  public int getExpansionIndex(NodeSpecification path)
  {
    int ret = -1;
    int expCount = getExpansionCount();
    for (int i = 0; i < expCount && ret < 0; i++)
    {
      TreeNodeExpansion e = getExpansion(i);
      if (e.matchesPath(path))
        ret = i;
    }
    return ret;
  }

  public boolean isLeaf(Any vectorChild, Any context)
  {
    setContext(context);
    
    return doLeafExpression(vectorChild);
  }
  
  // Build the tree path in the Array path and return the
  // index of the affected node, or -1 if not applicable.
  // Called on the first tree level with:
  //   1) parent: the root tree node originally returned to the
  //      JTree.  Given that the this will have been returned (together
  //      with any children that the event could possibly relate to)
  //      this node represents the root of all possible tree paths.
  //   2) node: an AnyTreeNode that would compare equals with the root
  //      tree node.  This AnyTreeNode
  //      is mutable and can be used by successive levels as a
  //      comparison value with child nodes from the model.
  public int translateEvent(Any               startAt,
                            Event             e,
                            Map               eventType,
                            Any               baseType,
                            boolean           force,
                            Array             path,
                            NodeSpecification eventPath) throws AnyException
  {
    // If the event is one of the _CHILD types then stop now and return -1.
    // Has the effect of refreshing the model from the root.
    // A _CHILD event necessarily means that some ancestor of the root
    // of the tree is structurally changed. We only need (and get) one
    // of these and the effect must be to the entire tree.
    if (baseType.equals(EventConstants.NODE_REPLACED_CHILD) ||
        baseType.equals(EventConstants.NODE_ADDED_CHILD)    ||
        baseType.equals(EventConstants.NODE_REMOVED_CHILD))
      return -1;
    
    // There may not even a root to start from
    if (startAt == null ||
       (!(startAt instanceof AnyTreeNode)))
      return -1;

    Iter nodeSpecIter = eventPath.createPathItemsIter();

    // This is the root AnyTreeNode
    AnyTreeNode parent = (AnyTreeNode)startAt;

    // The context node in the root is the prevailing GUI context.
    // This does not change and is the dispatch point for the
    // event we are translating.
    Any startAny   = parent.getContext();

    int childIndex = -1;  // index within Java tree node structure
    int inqIndex   = -1;  // index within Inq node structure

    // Working object for contained tests within the TreeNode hierarchy.
    AnyTreeNode node = new AnyTreeNode(startAny);

    while (nodeSpecIter.hasNext())
    {
      boolean found   = false;
      Map     current = (Map)startAny;
      node.setAny(startAny);

      // From startAny consume node specification elements
      // stepping down the structure until we find a child
      // node or exhaust the path specification.

      // To build the array of AnyTreeNode objects we
      // check the their hierarchy as we walk down
      // the Inq node structure.

      // So as not to make any assumptions about the use of
      // AnyTreeNode other than that AnyTreeNode.getAny()
      // should be found as the Inq structure is walked, we
      // make the following compromises:
      //   - insert:  there will be no AnyTreeNode so go as
      //              far as we can to generate the path to
      //              the point where the insertion has taken
      //              place.  Return an index of -1 indicating
      //              structure change below this point.
      //   - replace: there will be no AnyTreeNode containing
      //              the new Inq child, although there will be
      //              one containing that which has just been
      //              replaced.  Same as for insert.
      //   - remove:  there will be an AnyTreeNode containing
      //              the removed Inq child, if it is the same
      //              as that placed in it by the model.  The
      //              index of this node should be the same as
      //              the vector number contained in the event.
      //              If this is so we can identify the
      //              AnyTreeNode to be removed and return its
      //              index.  Otherwise as for insert.
      while (!parent.contains(node) && nodeSpecIter.hasNext())
      {
        Any a = nodeSpecIter.next();
        
        // For UPDATE/DELETE events (CREATE does not eminanate from the
        // node structure) the path's ultimate element will be the
        // emitting typedef instance. These are not relevant to
        // determining the tree path so if we've just consumed the
        // last element in this case return what we've got.
        if (!nodeSpecIter.hasNext() && AbstractEvent.isBotEvent(baseType))
          return childIndex;
        
        if (current.contains(a))
        {
          // Successfully stepped down an inq level in the event path
          // spec. Place the new inq node in the working tree node in
          // preparation for checking whether the inq node constitutes
          // a child tree node.
          // Note that this works even for (tree) expansion nodes,
          // whose any_ is the (inq) structure node at which the expansion
          // children are found.
          current = (Map)current.get(a);
          node.setAny(current);
          found = true;
        }
        else
        {
          // Inq structure traversal has broken.  Insert, update and
          // replace should never break in the Inq sense. For
          // removals the event carries the node removed. We may
          // find it in the tree so set it into the working node (see
          // below also)
          // In any case, there's no point consuming any more of the
          // event path (note, in fact, that even for removals
          // we should just have consumed the last path element anyway). 
          // 
          Any ec = e.getContext();
          if (ec != null)
          {
            node.setAny(ec);
          }
          found = false;
          break;
        }
      }

      // We've left the loop above because
      //   1) the event path is exhausted;
      //   2) we found a node in the AnyTeeNode carrying the
      //      current Inq node;
      //   3) the Inq traversal failed at or before the event
      //      path was exhausted.

      if (!parent.contains(node))
      {
        // If we don't have a AnyTreeNode then the possibilities
        // are:
        //   1) An insertion
        //        TreeNode: insert/index = vector
        //        Expansion: structure on expansion node
        //   2) replace
        //         TreeNode: structure/index = vector
        //         Expansion: structure on expansion node
        // Note: with the condition of the inner while-loop
        // being (parent not contains) && (hasNext) we know in this
        // block that the event path is exhausted.
        
        if (baseType.equals(EventConstants.NODE_ADDED) ||
            baseType.equals(EventConstants.NODE_REPLACED))
        {
          // If there is a vector then the event must relate to
          // a node-set child (that is dynamic) tree node
          Any vec = eventType.get(EventConstants.EVENT_VECTOR);
          if (vec != AnyAlwaysEquals.instance())
          {
            Map vectorChild = (Map)e.getContext();
            
            // It is possible that the event has originated at a point
            // below which we currently have tree nodes for, because they
            // haven't yet been requested by the view, even though all
            // the Inq nodes are there. If the level is correct, then
            // 1) parent is a dynamic node (not root):
            //    parent of the parent of the node we are adding will be
            //    the Any of the parent tree node (got that!!).
            // 2) parent is an expansion node:
            //    parent of the node we are adding will be the Any of
            //    the parent tree node
            // 3) parent is root node: root node holds the root node-set
            //    when there are dynamic first-level children, so level
            //    check is only one node away.
            // I reckon we
            // can just ignore the event in this case as the tree view
            // doesn't even know about it.
            if ((parent.isExpansion() && vectorChild.getParentAny() != parent.getAny()) ||
                (!parent.isExpansion() && parent.getParent() != null && vectorChild.getParentAny().getParentAny() != parent.getAny()) ||
                (!parent.isExpansion() && parent.getParent() == null && vectorChild.getParentAny() != parent.getAny()))
              return -2; // means ignore
            
            IntI vector = (IntI)vec;
            childIndex = vector.getValue();
            
            if (baseType.equals(EventConstants.NODE_ADDED))
            {
              // Have vector, node ADDED
              
              // Prepare to add the node
              // 1) Determine the new node's context
              Any context;
              if (parent.isExpansion())
                context = parent.getContext();
              else
                context = (parent.getParent() == null) ? parent.getContext()
                                                       : parent.getAny();
              
              // 2) its TreeLevel
              TreeLevel l = parent.isExpansion() ? parent.getExpansion().getTreeLevel()
                                                 : parent.getTreeLevel().getNextTreeLevel();
              
              // 3) whether it is a leaf
              boolean isLeaf = l.isLeaf(vectorChild, context);
              
              // We know we are creating a dynamic node (see comment above).
              // As well, we *should* never get an insertion event for the
              // expansion structure node as these are created on-the-fly
              // if not present when the nodes are originally fetched from
              // the model.
              
              // We add the node at a specific index, so no parent on construction
              AnyTreeNode newNode = new AnyTreeNode(null,
                                                    vectorChild,
                                                    context,
                                                    l,
                                                    isLeaf);
              
              // Make allowances for any expansion nodes in the returned
              // index
              childIndex += l.getExpansionCount();
              
              parent.addChildAt(newNode, childIndex);
              
              // Put the new node on the path we are building
              path.add(newNode);
            }
            else
            {
              // Have vector, node REPLACED
              
              // We don't find the tree node because the Any it is
              // carrying is no longer in the Inq structure.
              
              // With a vector we know the node will be a dynamic one.
              
              // 1) Determine the tree level from the parent node. Suppose
              //    there should be at least a parent[0] we could have used
              //    but may be this is cleaner.
              TreeLevel l = parent.isExpansion() ? parent.getExpansion().getTreeLevel()
                                                 : parent.getTreeLevel().getNextTreeLevel();
              
              // 2) Make allowances for any expansion nodes in the returned
              //    index
              childIndex += l.getExpansionCount();
              
              // 3) Fetch the node as it is now from the tree structure.
              //    Remove it too.Just optimises the processing in
              //    setContents, since we already know the index.
              //    Add the node as it is now to the path
              AnyTreeNode changedNode = parent.remove(childIndex);
              path.add(changedNode);
              
              // 4) The context held in the node is still valid
              Any context = changedNode.getContext();

              // 5) Check if its still a leaf, well you never know
              boolean isLeaf = l.isLeaf(vectorChild, context);
              
              // 6) Make a new node
              AnyTreeNode newNode = new AnyTreeNode(null, vectorChild, context, l, isLeaf);
              
              // 7) Add it to the parent
              parent.addChildAt(newNode, childIndex);

              // 8) Signal a structure change
              childIndex = -1;
            }
          }
          else
          {
            // Insert/Replace without a vector. Processing in the
            // same for both event types in this case.
            
            // Check if the event in fact relates to an expansion.
            // There would be no vector if an expansion's structure node
            // was being manipulated and the event path would satisfy
            // the expansion's structure path.
            // Because there is no vector in this case we use the
            // event path to determine the child index.
            
            // If the current tree path is just the root node then that
            // node's tree level is used, otherwise it is the
            // next tree level
            TreeLevel l = parent.getTreeLevel();
            if (parent.getParent() != null)
              l = l.getNextTreeLevel();
            
            childIndex = l.getExpansionIndex(eventPath);
            if (childIndex >= 0)
            {
              // There's a path match. Check the level is OK as for
              // dynamic nodes, but in this case it is the parent
              // of the inserted node that is relevant.
              // Note: it's important to check the path before
              // calling getParent to check the hierarchy, to avoid
              // hitting a typedef instance, say, that does not
              // support getParent().
              // If there are expansion nodes that are children of the root
              // then the Inq hierarchy check will fail. In this case the
              // Inq node is located by the expansion's path somewhere
              // beneath the context. If the node is correct it will be
              // navigated by the path from the context.
              Map structureNode = (Map)e.getContext();
              if ((parent.getParent() != null && structureNode.getParentAny() == parent.getAny()) ||
                  (parent.getParent() == null && l.getExpansion(childIndex).navigableBetween((Map)getModel().getContext(), structureNode)))
              {
                AnyTreeNode changedNode = parent.remove(childIndex);
                
                // Put the old node on the path we are building
                path.add(changedNode);
                
                Any context;
                if (parent.isExpansion())
                  context = parent.getContext();
                else
                  context = (parent.getParent() == null) ? parent.getContext()
                                                           : parent.getAny();
                  
                // Leaf status is fixed by the expansion definition
                TreeNodeExpansion exp = changedNode.getExpansion();
                AnyTreeNode newChild = new AnyTreeNode.ExpansionTreeNode(parent,
                                                                         exp,
                                                                         context,
                                                                         l);
                // Add it to the parent
                parent.addChildAt(newChild, childIndex);
                
                childIndex = -1;   // Causes a structure change
              }
              else
              {
                // Likely a node-set root that would affect all the parent's
                // children. Issue a structure change on current path 
                return -1;
              }
            }
            else
            {
              // Not a vector and not identifiable as an expansion.
              // This would happen if the event related to (say) the
              // insertion of a typedef instance under a node-set child.
              // Such things do not map to tree insertions, instead
              // they would really be a rendering change at the current
              // tree level.
              // TODO: Issue a change. (by changing the baseType?)
              // Is this safe??
              //baseType.setValue(EventConstants.BOT_UPDATE);
              
              // TODO: What happens if the operation is to add a typedef
              // instance at {k}.<alias> ?? That creates a new node
              // in an illegal way... 
              return -1;
            }
          }
        }
        else
        {
          // Don't have a tree node but event type is not ADDED or REPLACED.
          // If it was REMOVED or UPDATED then we should have found the tree node
          // Don't know. Just return with whatever path we've built and
          // issue a structure change.
          // This case also covers the REMOVE case when we don't find
          // the tree node, such as removing a typedef instance from
          // the structure, if we were to do that.
          if (baseType.equals(EventConstants.NODE_REMOVED))
          {
            Map removedNode = (Map)e.getContext();
            if (removedNode.getDescriptor() != Descriptor.degenerateDescriptor__)
            {
              // Removed node is a typedef instance. Just a change
              //TODO: baseType.setValue(EventConstants.BOT_UPDATE);
            }
          }
          return -1;
        }
      }
      else
      {
        // Found the node in the tree hierarchy. This should be the case
        // for removals of structural nodes, given that we set the
        // removal context into the working node.
        
        // If the Inq traversal has broken then we are at the point
        // in the tree structure that the removal pertains to
        if (!found && baseType.equals(EventConstants.NODE_REMOVED))
        {
          // If there is a vector then the event must relate to
          // a node-set child (that is dynamic) tree node
          Any vec = eventType.get(EventConstants.EVENT_VECTOR);
          if (vec != AnyAlwaysEquals.instance())
          {
            // We could just do indexOf on the parent node to find the
            // index of the one whose Inq node we've just removed, however
            // we already have the vector number so this seems a waste.
            // Instead, we use the vector and make any allowances for
            // expansion nodes.
            IntI vector = (IntI)vec;
            
            // 1) Get the appropriate TreeLevel
            TreeLevel l = parent.isExpansion() ? parent.getExpansion().getTreeLevel()
                                               : parent.getTreeLevel().getNextTreeLevel();
            
            // 2) Correct for any expansions
            childIndex = vector.getValue() + l.getExpansionCount();
            
            // 3) The real node is still in the tree, so we can put it
            //    in the path
            path.add(parent.getChildAt(childIndex));
            
            // 4) Remove the node from the tree hierarchy
            parent.remove(childIndex);
            
            return childIndex;
            
          }
          else
          {
            // There's no vector. Use AnyTreeNode.indexOf instead. No need
            // to cater for expansions because this is the tree structure,
            // not the Inq one.
            childIndex = parent.indexOf(node);
            if (childIndex >= 0)
            {
              // Put the real node in the path
              path.add(parent.getChildAt(childIndex));

              // remove it from the tree hierarchy
              parent.remove(childIndex);
              
              return childIndex;
            }
            
            // Expansion children are always present
            // in the tree so we wouldn't end up removing one of them.
            // Instead we signal a structure change.
            // Check with path matching whether this is an expansion node.
            // If the current tree path is just the root node then that
            // node's tree level is used, otherwise it is the
            // next tree level
            TreeLevel l = parent.getTreeLevel();
            if (parent.getParent() != null)
              l = l.getNextTreeLevel();
            
            childIndex = l.getExpansionIndex(eventPath); // -1 if no match
            if (childIndex >= 0)
            {
              // Matched an expansion path.
              // Hmmm, when issuing a structure change it must be the
              // case that we *don't* use the same AnyTreeNode object
              // to hold the new data, or JTree will get confused.
              // Always make a new node.
              // TODO: Check other cases eg plain node replacements
              
              // 1) Get the original node and put it in the path.
              AnyTreeNode expansionNode = parent.getChildAt(childIndex);
              path.add(expansionNode);
              
              // 2) Make a new node and replace the current one
              Any context;
              if (parent.isExpansion())
                context = parent.getContext();
              else
                context = (parent.getParent() == null) ? parent.getContext()
                                                         : parent.getAny();
                
              TreeNodeExpansion exp = expansionNode.getExpansion();
              AnyTreeNode newChild = new AnyTreeNode.ExpansionTreeNode(parent,
                                                                       exp,
                                                                       context,
                                                                       l);
              parent.remove(childIndex);
              parent.addChildAt(newChild, childIndex);
              
              // Signal a structure change
              return -1;
            }
            else
            {
              // No vector, no expansion. Given that the node was found and the
              // Inq travseral has broken, can this ever happen?
              throw new IllegalStateException("Illegal Tree State 1");
            }
          }
        }
        else
        {
          // Just walking down the tree hierarchy, I reckon
          
          childIndex = parent.indexOf(node);
    
          AnyTreeNode child = null;
          if (childIndex >= 0)
          {
            // Put the real node in the path, not the spoof working one.
            child = (AnyTreeNode)parent.getChildAt(childIndex);
            path.add(child);
          }
    
          // At this point we have consumed node specification elements to
          // the point of the node that is contained as the child of
          // the given parent.
          // Its interesting to note that the function of translating
          // the Inq node events into a tree does not rely on state contained
          // within the various TreeLevel objects that configure the tree.
          startAny = child.getAny();
          parent   = child;
        }
      }
    }
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
		if (value.isExpansion())
    {
      TreeNodeExpansion e = value.getExpansion();
      return e.getTreeCellRendererComponent(tree,
                                            value,
                                            selected,
                                            expanded,
                                            leaf,
                                            row,
                                            hasFocus);
    }
    else
    {
      // Just need to guard against infinite recursion
      if (value.getTreeLevel() == this)
      {
        return renderer_.renderTreeCell(tree,
                                        value,
                                        selected,
                                        expanded,
                                        leaf,
                                        row,
                                        hasFocus);
      }
      else
      {
        TreeLevel l = value.getTreeLevel();
        //System.out.println("RENDERING " + node.getAny());
        //System.out.println("CONTEXT "   + node.getContext());
        //System.out.println("TREELEVEL "   + node.getTreeLevel());
        //System.out.println("PARENT "   + node.getParent());
        return l.getTreeCellRendererComponent(tree,
	                                            value,
	                                            selected,
	                                            expanded,
	                                            leaf,
	                                            row,
	                                            hasFocus);
      }
    }
  }

//  public DefaultTreeCellRenderer getDefaultTreeCellRenderer(Any a)
//  {
//  	AnyTreeNode node = (AnyTreeNode)a;
//
//		if (!node.isExpansion())
//    {
//      if (node.getTreeLevel() == this)
//      {
//        return renderer_.getDefaultTreeCellRenderer();
//      }
//      else
//      {
//        TreeLevel l = node.getTreeLevel();
//        return l.getDefaultTreeCellRenderer(a);
//      }
//    }
//		else
//		{
//			TreeNodeExpansion e = node.getExpansion();
//			return e.getDefaultTreeCellRenderer();
//		}
//  }

	public TreeNodeExpansion getExpansion(int index)
	{
	  TreeNodeExpansion e = (TreeNodeExpansion)expansionNodes_.get(index);
	  //e.setContext((context != null) ? context : context_);
	  //e.setParentTreeLevel(this);
	  //return e.cloneAny();
	  return e;
	}
	
  public RenderInfo getRenderInfo()
  {
    return renderer_.getRenderInfo();
  }

  public TreeCellEditor getTreeCellEditor(Any a)
  {
  	AnyTreeNode node = (AnyTreeNode)a;

		if (!node.isExpansion())
	  {
      if (node.getTreeLevel() == this)
      {
        // Must be set by scripts and not created on demand here,
        // which is the path from swing.
        //if (editor_ == null)
          //editor_ = new AnyComponentEditor(renderer_.getRenderInfo());
        return editor_;
      }
      else
      {
        TreeLevel l = node.getTreeLevel();
        return l.getTreeCellEditor(a);
      }
	  }
		else
		{
			TreeNodeExpansion e = node.getExpansion();
			return e.getEditor();
		}
  }

  // If this instance is intended to work as a level specifier for the
  // a repeating structure that can nest to any level then this is true.
  public boolean isRecursive()
  {
    return this == nextLevel_;
  }

  public AnyComponentEditor getEditor()
  {
    return editor_;
  }

  public void setEditor(AnyComponentEditor ace)
  {
    editor_ = ace;
  }

  public OrderComparator getComparator()
  {
    return sortComparator_;
  }

  public void setComparator(OrderComparator oc)
  {
    sortComparator_ = oc;
  }

  public String toString()
  {
    return levelRoot_.toString();
  }
  
//  public void setInteractiveComparator(OrderComparator oc)
//  {
//    interactiveComparator_ = oc;
//  }
//  
//  public OrderComparator getInteractiveComparator()
//  {
//    return interactiveComparator_;
//  }
  
  // Entry point for sorting the tree below the given node. Uses
  // the sortComparator_ and leaves it inside each vector visited
  // during the sort.
  public void sort(AnyTreeNode n, boolean depthSort)
  {
    // Sort from the given node children downwards, processing from the bottom
    // upwards.
    
    // TODO: We can optimise this if we want by maintaining the expanded flag in
    // the AnyTreeNode instances (need a tree expansion listener for that) and not
    // bothering to sort children that are not expanded, until they are.
    
    
    try
    {
      // For the children, go straight to the vector. We treat any expansions
      // present separately.
      TreeLevel l = n.getTreeLevel();
      Vectored v = l.getChildVector(n);
      // v will be null if it didn't resolve for some reason or the given
      // node is at the lowest level.
      if (v != null)
      {
        // This is slightly opaque to understand: The vector v is the
        // Inq node containing the children of the given node argument n.
        // This vector is yielded by the level n.getTreeLevel().getNextTreeLevel()
        // used inside TreeLevel.getChildVector
        // Hence, we pass n.getTreeLevel().getNextTreeLevel() into this
        // method (unless we are dealing with the root node)
        
        // Fetch the context from which the initial vector was
        // resolved. We need this if we encounter any explicit
        // expansions.
        Any context;
        if (n.getParent() == null)
        {
          context = n.getContext();
        }
        else
        {
          context = n.getAny();
          l = l.getNextTreeLevel();
        }

        sortChildLevel(v, context, l, depthSort);
      }
      
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  
  private void sortChildLevel(Vectored  v,
                              Any       context,
                              TreeLevel level,
                              boolean   depthSort) throws AnyException
  {
    // Recurse to the next level (if any) before sorting this one
    if (depthSort)
    {
      TreeLevel nextLevel = level.getNextTreeLevel();
      if (nextLevel != TerminalTreeLevel.terminalLevel__)
      {
        int childCount = v.entries();
        
        for (int i = 0; i < childCount; i++)
        {
          Any child = v.getByVector(i);
          Vectored nextV = nextLevel.getVectorParent(child);
          if (nextV != null)
            sortChildLevel(nextV, child, nextLevel, depthSort);
        }
        
        // Check for any expansions at this level
        int expansionCount = level.getExpansionCount();
        while (expansionCount-- > 0)
        {
          TreeNodeExpansion e = level.getExpansion(expansionCount);
          AnyTreeLevel expLevel = (AnyTreeLevel)e.getTreeLevel();
          
          expLevel.setContext(context);
          // This is the top level of any recursion that might be taking
          // place below us
          Vectored expansionV = expLevel.resolveDataNode(context, false, false);
          
          TreeLevel expNextLevel = expLevel.getNextTreeLevel();

          if (expansionV != null && expNextLevel != TerminalTreeLevel.terminalLevel__)
          {
            int expChildCount = expansionV.entries();
            
            for (int i = 0; i < expChildCount; i++)
            {
              Any child = expansionV.getByVector(i);
              Vectored nextV = expNextLevel.getVectorParent(child);
              if (nextV != null)
                sortChildLevel(nextV, child, expNextLevel, depthSort);
            }
          }
          
          // Sort this expansion's children
          OrderComparator oc = expLevel.getComparator();
          if (oc != null)
          {
            Orderable o = (Orderable)expansionV;
            oc.setToOrder((Map)o);
            oc.setTransaction(Globals.process__.getTransaction());
            o.sort(oc);
          }
        }
      }
    }
    // sort this level
    OrderComparator oc = level.getComparator();
    if (oc != null)
    {
      Orderable o = (Orderable)v;
      oc.setToOrder((Map)o);
      oc.setTransaction(Globals.process__.getTransaction());
      o.sort(oc);
      // Leave the comparator in the collection to maintain
      // sorting after insertion.
      // Although there can be many orderables at a given level
      // they can all have the same comparator.
      // TODO: Check whether insertions can use the index that
      // can be determined when elements are added to an
      // Orderable containing a comparator.
    }
  }
  
  //void sortExpansions()

  private Vectored resolveDataNode(Any     root,
                                   boolean force,
                                   boolean recursing) throws AnyException
	{
		// If the expression is AnyNull then return hard-null. The root (for
    // which this is unacceptable) is already handled in getRoot() so this is
    // for the first level children (in the rootTreeLevel case).
    // For any level so configured, there are only expansion node children.
    if (levelRoot_ == AnyNull.instance())
      return null;
    
		Vectored dataNode = levelRootV_;

		if ((dataNode == null || force || mustResolve_) &&
        (levelRoot_ != null))
		{
	    dataNode = (Vectored)EvalExpr.evalFunc(Globals.process__.getTransaction(),
    																	       root,
    																	       recursing ? subRoot_
                                                       : levelRoot_);
      levelRootV_ = dataNode;
      if (dataNode != null)
        dataNode.initOrderBacking();

			mustResolve_ = false;
		}

		return dataNode;
	}

  private Any getChildNode(Vectored    v,
                           int         index,
                           AnyTreeNode pNode,
                           Any         context)
  {
    //System.out.println("getChildNode " + pNode);
    //System.out.println("getChildNode " + index);
    // Check if there's already a carrier that we can use.  Note
    // that there is a derived class of AnyTreeNode for expansions
    // so we cannot be sure that the use the any existing instance
    // is being put is compatible with the use that created it.  For
    // the moment, this problem could only arise when there are both Inq
    // structure children and explicit expansion nodes.  Because
    // we always return explicit expansion nodes as indices 0
    // to #expansions-1, before any Inq children, this cannot happen.
    AnyTreeNode cNode = null;
    if (pNode.entries() > index)
    {
      cNode = pNode.getChildAt(index);
    }

    // If there are any explicit expansion nodes then return them first
    if (expansionNodes_ != null &&
        index < expansionNodes_.entries())
    {
      // Can expansions ever be leaves?  Doesn't make sense?
      TreeNodeExpansion e = getExpansion(index);
      if (cNode != null)
        cNode.setContents(e, context, this); // isLeaf);
      else
        cNode = new AnyTreeNode.ExpansionTreeNode(pNode,
                                                  e,
                                                  context,
                                                  this); //, isLeaf)
    }
    else
    {
      index -= expansionNodesOffset();

      Any a = v.getByVector(index);

      boolean isLeaf = doLeafExpression(a);

      if (cNode != null)
        cNode.setContents(a, context, this, isLeaf);
      else
        cNode = new AnyTreeNode(pNode,
                                a,
                                context,
                                this,
                                isLeaf);
    }
    return cNode;
  }

  private int levelCount(Vectored     v,
                         AnyTreeLevel l)
  {
    if (v != null)
      return v.entries() + l.expansionNodesOffset();
    else
      return l.expansionNodesOffset();
  }

  // We should always be on the swing thread here, so no need
  // to take specific steps in this regard at this level.
  private boolean doLeafExpression(Any node)
  {
    if (isLeaf_ == null)
      return leafRes_.getValue();

    Map leafArgs = isLeaf_.getArgs();
    Map args = leafArgs; // put originals back afterwards.
    if (leafArgs == null)
      leafArgs = AbstractComposite.simpleMap();
    else
      leafArgs = (Map)leafArgs.cloneAny();

    leafArgs.add(node__, node);

    Transaction t = Globals.process__.getTransaction();
    isLeaf_.setTransaction(t);
    isLeaf_.setArgs(leafArgs);

    Any ret = null;
    try
    {
      ret = isLeaf_.exec(getContext());
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      //leafArgs_.empty();
      isLeaf_.setArgs(args);
      isLeaf_.setTransaction(Transaction.NULL_TRANSACTION);
    }

    leafRes_.copyFrom(ret);
    return leafRes_.getValue();
  }

	private int expansionNodesOffset()
	{
		return (expansionNodes_ != null) ? expansionNodes_.entries()
		                                 : 0;
	}

  /**
   * Add the node references within the given <code>RenderInfo</code>
   * to the map of paths-to-fields.
   * @param r the RenderInfo whose <code>$this</code> node references
   * will be added to the current set.
   * @param contextRoot the path with which to prefix the RenderInfo paths.
   * @param nodeSpecs the mapping of paths-to-fields currently found.
   * @param contextNode the context node (used to resolve any indirections
   * there may be in the node specs.
   */
  private static void addEventPaths(RenderInfo        r,
                                    NodeSpecification contextRoot,
                                    Map               nodeSpecs,
                                    Any               contextNode)
  {
    // Ask the RenderInfo to evaluate any $this-based paths
    // in its rendering expression and fetch them out.
    r.resolveNodeSpecs(contextNode);
    Map ns = r.getNodeSpecs();

    Iter iter = ns.createKeysIterator();
    while (iter.hasNext())
    {
      Any thisNs    = iter.next();
      Any fieldList = ns.get(thisNs);

      NodeSpecification fromRoot = (NodeSpecification)thisNs.cloneAny();
      for (int j = 0; j < contextRoot.entries(); j++)
      {
        fromRoot.addFirst(contextRoot.get(contextRoot.entries() - j - 1));
      }

      thisNs = fromRoot;

      if (!nodeSpecs.contains(thisNs))
      {
        nodeSpecs.add(thisNs, fieldList.cloneAny());
      }
      else
      {
        Set fieldSet = (Set)nodeSpecs.get(thisNs);
        fieldSet.addAll((Set)fieldList, true);
      }
    }
  }
  
  // A TreeNodeExpansion implementation that, itself, represents a
  // child at the current level and optionally has as its children
  // another tree.  The root of this sub-tree is represented by this
  // node and it is able to render itself using its own renderer.
  // Any child nodes below are handled by the tree levels rooted
  // within this node.
  // In order to make node processing easy this class holds a
  // referencing expression, and resultant node, that it represents
  // in the tree hierarchy.  That is, that a node event on the Inq
  // structure will identify this node in the tree path.
  // Unique instances of AnyTreeNode will be returned to the JTree
  // for each child node of the parent level, however each of
  // these AnyTreeNode instances
  static private class LevelExpansion extends    AbstractMap
                                      implements TreeNodeExpansion,
                                                 Cloneable
  {
    // our children - null if we don't have a sub-tree (makes us a leaf)
    private TreeLevel   level_;

    //private TreeLevel parent_;  // the TreeLevel that we are contained in

    // The renderer for the root of this subtree
    private AnyCellRenderer renderer_;

    // Likewise the editor
    private AnyComponentEditor editor_;

    // Only non-null if we are being used as a TreeTable and
    // specific columns have been configured at this level.
    // [Note that colInfo[0] will be the same as renderer_ above
    // unless overridden]
    private Array colInfo_;
    
    // Like the above, but the editors
    private Array colEditors_;
    
    // The tree model we are a part of
    AnyTreeModel m_;

    // Resolve the node from which children of the expansion will
    // be derived. 
    private Locate      levelRoot_;
    
		LevelExpansion(RenderInfo r, Locate levelRoot)
    {
      this(r, levelRoot, null);
    }

		LevelExpansion(RenderInfo r, Locate levelRoot, TreeLevel l)
    {
      renderer_       = new AnyCellRenderer(r, new DefaultTreeCellRenderer());
      levelRoot_      = levelRoot;
      level_          = l;
    }

    public boolean isLeaf()
    {
      return level_ == null;
    }

    public TreeLevel getTreeLevel()
    {
      return level_;
    }

    public Any resolveStructureNode(Any root) throws AnyException
    {
      Any ret =  EvalExpr.evalFunc(Globals.process__.getTransaction(),
                               root,
                               levelRoot_);
      
      if (ret == null)
      {
        // The expansion's structure node must exist so that the AnyTreeNode
        // can be entered into the tree model hierarchy.
        Any node = root.buildNew(root);
        BuildNodeMap b = new BuildNodeMap(levelRoot_.getNodePath(), node);
        ret = EvalExpr.evalFunc(Globals.process__.getTransaction(),
                                root,
                                b);
      }
      return ret;
    }

    public boolean isEditable()
    {
		  return renderer_.isEditable();
    }

    public Any getResponsibleFor(Any a) throws AnyException
    {
			return renderer_.getRenderInfo().resolveResponsibleData(a);
    }

    public Any getValueFor(Any a) throws AnyException
    {
			return renderer_.getRenderInfo().resolveDataNode(a, true);
    }

    public RenderInfo getColumnRenderInfo(int col)
    {
      RenderInfo r = null;

      if (col == 0)
        r = renderer_.getRenderInfo();
      else
      {
        AnyCellRenderer renderer = (AnyCellRenderer)colInfo_.get(col);
        r = renderer.getRenderInfo();
      }
      
      return r;
    }
    
    // The renderer for the specified column.  Only valid for TreeTables.
    public AnyCellRenderer getRenderer(int column)
    {
      AnyCellRenderer r = null;
      
      if (column == 0)
        r = renderer_;
      else
      {
        if (colInfo_ != null)
          r = (AnyCellRenderer)colInfo_.get(column);
      }
      
      return r;
    }
    
    public AnyCellRenderer getRenderer()
    {
      return renderer_;
    }

    public void setColumns(Array columns, TableModel tableModel)
    {
      // The tableModel offers up the renderinfos and corresponding
      // cell renderers. We are given an array of renderinfos the elements
      // of which are 1) the same as the tableModel when there is no
      // override or 2) are different when there is. Work out which and
      // create new cell renderers as appropriate
      // NOTE: this method is the same as AbstractTreeLevel.setColumns()
      //       :-/
      
      if (colInfo_ == null)
        colInfo_ = AbstractComposite.array();
      else
        colInfo_.empty();
      
      // Column zero is the tree
      colInfo_.add(columns.get(0));
      
      for (int i = 1; i < columns.entries(); i++)
      {
        RenderInfo override  = (RenderInfo)columns.get(i);
        RenderInfo fromTable = tableModel.getRenderInfo(i);
        if (override == fromTable)
        {
          // inherit the renderer from the table
          colInfo_.add((AnyCellRenderer)tableModel.getCellRenderer(-1, i));
        }
        {
          // Its an override so make a new one
          colInfo_.add(new AnyCellRenderer(override));
        }
      }
    }
    
    public void setModel(AnyTreeModel m)
    {
      m_ = m;
    }

    public AnyTreeModel getModel()
    {
      return m_;
    }
    
    // Do the expansion's rendering.  The supplied value is the any
    // contained within the AnyTreeNode.  for an expansion this is
    // the Inq node that represents the expansion and is the node
    // from which the renderinfo should be applied.
    public Component getTreeCellRendererComponent(JTree       tree,
                                                  AnyTreeNode value,
                                                  boolean     selected,
                                                  boolean     expanded,
                                                  boolean     leaf,
                                                  int         row,
                                                  boolean     hasFocus)
    {
      //System.out.println("EXPANSION RENDERING " + value);
      return renderer_.renderTreeCell(tree,
                                      value,
                                      selected,
                                      expanded,
                                      leaf,
                                      row,
                                      hasFocus);
    }

//    public DefaultTreeCellRenderer getDefaultTreeCellRenderer()
//    {
//      return renderer_.getDefaultTreeCellRenderer();
//    }

    public TreeCellEditor getEditor()
    {
      return editor_;
    }

    public void setClosedIcon(Any icon)
    {
      renderer_.setClosedIcon(icon);
    }

    public void setLeafIcon(Any icon)
    {
      renderer_.setLeafIcon(icon);
    }

    public void setOpenIcon(Any icon)
    {
      renderer_.setOpenIcon(icon);
    }
    
    public boolean matchesPath(NodeSpecification path)
    {
      // Step backwards along our path expecting its non-control
      // tokens to be the same as those in the given path. We expect
      // the given path to be longer than ours (that is we will exhaust
      // our path first).
      
      boolean ret = true;
      
      NodeSpecification thisNs = levelRoot_.getNodePath();
      int thisPos  = thisNs.entries() - 1;
      
      int otherPos = path.entries() - 1;
      
      while (thisPos >= 0 && ret)
      {
        thisPos = nextToken(thisPos, thisNs);
        otherPos = nextToken(otherPos, path);
        
        ret = thisNs.get(thisPos--).equals(path.get(otherPos--));
      }
      
      return ret;
    }
    
    public boolean navigableBetween(Map start, Any end)
    {
      // Return true if this expansion's path can navigate from start
      // and arrive at end. Used when verifying expansion nodes that
      // are children of the tree root node.
      boolean ret = false;
      
      try
      {
        Any a = levelRoot_.exec(start);
        if (a == end)
          ret = true;
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      
      return ret;
    }
    
    public NodeSpecification getRootPath()
    {
      NodeSpecification root = levelRoot_.getNodePath();

      return root;
    }

    // Resolve the node specifications that should dispatch to the tree
    // overall.  [Note to readers - the context parameter is the prevailing
    // GUI context and is only used to resolve indirections in the node
    // paths.  It doesn't have magical properties at descendent levels!!]
    public void resolveNodeSpecs(NodeSpecification rootPath,
                                 Map               nodeSpecs,
                                 TreeTableModel    treeTableModel,
                                 Any               contextNode)
    {
      // Suffix outer specification with ours
      NodeSpecification contextRoot = (NodeSpecification)rootPath.cloneAny();
//      NodeSpecification myRoot = this.getRootPath();
//      for (int i = 0; i < myRoot.entries(); i++)
//        contextRoot.add(myRoot.get(i));
      
      // myRoot is the root node of this level. This is relevant for
      // the evaluation of deeper paths for our children (if any)
      
      // Where we will collect the node specifications generated
      // by this level.
      Map thisNodeSpecs = AbstractComposite.simpleMap();
      
      // Rendering of this node (the expansion child) is w.r.t. the given
      // rootPath, not myRoot, which represents a vector of children.
      // Perform path evaluation on the tree renderer 
      RenderInfo r = renderer_.getRenderInfo();
      addEventPaths(r, rootPath, thisNodeSpecs, contextNode);

      // If there are columns then we are employed in a tree-table
      // context. Use the table column or tree-level override as
      // appropriate and evaluare its paths.
      if (treeTableModel != null)
      {
        // Column zero is always the tree renderer
        for (int i = 1; i < treeTableModel.getColumnCount(); i++)
        {
          r = this.getColumnRenderInfo(i);
          if (r == null)
            r = treeTableModel.getRenderInfo(i);

          addEventPaths(r, rootPath, thisNodeSpecs, contextNode);
        }
      }
      
      // We don't need to worry about the this.getRootPath as this is the
      // root node of the next level, really. (TODO: Not tested!!)
      
      // When we've done this level add the node specs to the
      // list for the context node as a whole.
      Iter iter = thisNodeSpecs.createKeysIterator();
      while (iter.hasNext())
      {
        Any thisNs    = iter.next();
        Any fieldList = thisNodeSpecs.get(thisNs);
        if (!nodeSpecs.contains(thisNs))
        {
          nodeSpecs.add(thisNs, fieldList);
        }
        else
        {
          Set fieldSet = (Set)nodeSpecs.get(thisNs);
          fieldSet.addAll((Set)fieldList, true);
        }
      }


      // If there are child nodes (defined by a TreeLevel) then
      // determine their paths
      if (level_ != null)
      {
        level_.resolveNodeSpecs(rootPath, nodeSpecs, treeTableModel, contextNode);
      }
    }
    
    public AnyComponentEditor getEditor(int column)
    {
      if (column == 0)
        return editor_;
      
      if (colEditors_ == null)
        return null;
      
      return (AnyComponentEditor)colEditors_.get(column);
    }
    
    public void setEditor(AnyComponentEditor ace, int column)
    {
      if (column == 0)
        editor_ = ace;
      else
      {
        // If no array then initialise to number of columns and clear
        // to null.
        if (colEditors_ == null)
        {
          int cc = this.getModel().getTable().getModel().getColumnCount();
          colEditors_ = AbstractComposite.array(cc);
          for (int i = 0; i < cc; i++)
            colEditors_.add(null);
        }
        
        colEditors_.replaceItem(column, ace);
      }
    }
    
    // AbstractMap
    public Any get(Any key)
    {
      Any ret = getWithKey(key);
      if (ret == null)
        handleNotExist(key); // throws
      
      return ret;
    }
    
    public Any getIfContains(Any key)
    {
      return getWithKey(key);
    }

    public boolean contains (Any key)
    {
      return (key.equals(AnyTable.editor__) ||
              key.equals(AnyTable.renderer__));
    }
    
    public boolean isEmpty() { return false; }

    protected boolean beforeAdd(Any key, Any value) { return true; }
    protected void afterAdd(Any key, Any value) {}
    protected void beforeRemove(Any key) {}
    protected void afterRemove(Any key, Any value) {}
    protected void emptying() {}
    public Iter createIterator () {return DegenerateIter.i__;}
    
    public boolean containsValue (Any value)
    {
      throw new UnsupportedOperationException();
    }
    
    private Any getWithKey(Any key)
    {
      // The editor for the tree itself (as opposed to any other
      // columns when a TreeTable).
      if (key.equals(AnyTable.editor__))
      {
        // When fetching through property access, if there's no
        // editor available, create one.  See also
        // the AnyTable.ColumnProperty class.
        if (editor_ == null)
        {
          RenderInfo r = renderer_.getRenderInfo();
          editor_ = new AnyComponentEditor(r);
          r.setEditable(true);
        }
        return editor_;
      }
      else if (key.equals(AnyTable.renderer__))
      {
        return renderer_;
      }
      
      return null;
    }
    
    private int nextToken(int pos, NodeSpecification n)
    {
      if (pos < 0)
        return pos; 
      
      while (pos >= 0 &&
          NodeSpecification.isControl(n.get(pos)))
      {
        pos--;
      }
      
      return pos;
    }
    
    // Support the Map interface to access the various features
    // of the configuration of the TreeNodeExpansions within a TreeTable.
    // Instances of these objects are used twice in a path like
    //  myTreeTable.properties.levels.SwapTrade.columns.Price.editor
    // once for "Price" and once for "editor"
    private class ColumnAccess extends AbstractMap
    {
      private static final long serialVersionUID = 1L;

      // Once we have been queried for a column name this will be
      // set.  -1 means column does not exist
      private int column_ = -2;
      
      public Any get(Any key)
      {
        Any ret = getWithKey(key);
        if (ret == null)
          handleNotExist(key); // throws

        return ret;
      }
      
      public Any getIfContains(Any key)
      {
        return getWithKey(key);
      }
      
      public boolean contains (Any key)
      {
        // If we are initialised as a known column then we can have
        // specific children.
        if (column_ >= 0 && (key.equals(AnyTable.editor__) ||
                             key.equals(AnyTable.renderer__)))
          return true;
        
        // Otherwise assume the child is a column key.  If its known
        // to the table (so must be a treetable) then associate us
        // with that column.
        AnyTable t = getModel().getTable();
        
        int i = t.getColumnIndex(key);
        
        if (i >= 0)
          return true;
        
        return false;
      }
      
      public boolean isEmpty() { return false; }
      
      protected boolean beforeAdd(Any key, Any value) { return true; }
      protected void afterAdd(Any key, Any value) {}
      protected void beforeRemove(Any key) {}
      protected void afterRemove(Any key, Any value) {}
      protected void emptying() {}
      public Iter createIterator () {return DegenerateIter.i__;}
      
      public boolean containsValue (Any value)
      {
        throw new UnsupportedOperationException();
      }
      
      private Any getWithKey(Any key)
      {
        if (column_ >= 0)
        {
          // returning child of specific column.  The only supported
          // children at present are "renderer" and "editor"
          if (key.equals(AnyTable.editor__))
          {
            // When fetching through property access, if there's no
            // editor available, create one.  See also
            // the AnyTable.ColumnProperty class.
            AnyComponentEditor ace = getEditor(column_);
            if (ace == null)
            {
              //RenderInfo r = getModel().getTable().getColumnRenderInfo(column_);
              RenderInfo r = LevelExpansion.this.getColumnRenderInfo(column_);
              ace = new AnyComponentEditor(r);
              r.setEditable(true);
              setEditor(ace, column_);
            }
            
            return ace;
          }
          else if (key.equals(AnyTable.renderer__))
          {
            AnyCellRenderer r = null;
            if (column_ == 0)
              r = getRenderer();
            else
              r = getRenderer(column_);
            
            return r;
          }
          return null;
        }
        else
        {
          // Identifying us as a specific column
          AnyTable t = getModel().getTable();
          
          int i = t.getColumnIndex(key);
          
          if (i >= 0)
          {
            column_ = i;
            return this;
          }
          
          return null;
        }
      }
    }
  }
}
