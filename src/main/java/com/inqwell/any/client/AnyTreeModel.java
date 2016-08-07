/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTreeModel.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import java.awt.Component;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractEvent;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.JTable;
import com.inqwell.any.client.swing.TreeTableModel;
import com.inqwell.any.client.swing.JTree.MaintainExpansionListener;

/**
 * A <code>TreeModel</code> implementation that maps the Any
 * framework's hierarchical structures to the TreeModel interface.
 * Each level in a tree view is described by a <code>TreeLevel</code>
 * implementation and most Tree Model functionality is delegated
 * there.
 * <p>
 * A given <code>JTree</code> can only have a single renderer and
 * editor, so this class fulfills those requirements also,
 * requesting the appropriate components from the relevant
 * <code>TreeLevel</code>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class AnyTreeModel implements TreeModel,
                                     TreeCellRenderer,
                                     TreeCellEditor,
                                     CellEditorListener
{
  // The root of the TreeLevel objects that resolve and render
  // the various levels of the tree.  A bit of a mis-nomer in
  // that the (single) root node of the tree is given by
  // rootRenderer_ below.
  private TreeLevel          rootTreeLevel_;

  // If levels are named then each is stored here by its name.
  private Map                levels_;
  
  // If not null, the level the tree is automatically expanded to
  // when a structure change occurs at the root...
  private Any                expandToLevel_;
  // ...and whether the first node only or all nodes are expanded
  private boolean            expandAll_;

  private Any                context_;

  private AnyTreeNode        cachedRoot_ = dummyRoot__;

  //private AnyRenderer        rootRenderer_;
  private AnyCellRenderer      rootRenderer_;
  private TreeCellEditor     lastEditor_;
  private Component          lastComponent_;

	private ArrayList          listeners_ = new ArrayList();
  private ArrayList          expansionListeners_ = new ArrayList();
	private ArrayList          editorListeners_;
  
	private int                lastSerialNumber_ = -1;

	private final static short CHANGED   = 0;
	private final static short REMOVED   = 1;
	private final static short INSERTED  = 2;
	private final static short STRUCTURE = 3;

	// Required by Swing when there the model cannot
	// locate the root node - contrary to the JDK docs,
	// it is illegal to return null from getRoot() when
	// the tree is empty.
	static private AnyTreeNode dummyRoot__     = new AnyTreeNode(TreeLevel.terminalLevel__);

	static private AnyCellRenderer dummyRenderer__ =
    new AnyCellRenderer(new AnyRenderInfo(new ConstString("Root")),
                        new DefaultTreeCellRenderer());

	public AnyTreeModel()
	{
		rootRenderer_ = dummyRenderer__;
	}

	public AnyTreeModel(TreeLevel rootTreeLevel)
	{
		rootTreeLevel_ = rootTreeLevel;
		rootRenderer_ = dummyRenderer__;
	}

  public void addTreeModelListener(TreeModelListener l)
  {
    if (l.getClass() == MaintainExpansionListener.class)
    {
      expansionListeners_.add(l);
    }
    else
    {
		  listeners_.add(l);
    }
  }

  public void removeTreeModelListener(TreeModelListener l)
  {
		int i = -1;
    if (l.getClass() == MaintainExpansionListener.class)
    {
      if ((i = expansionListeners_.indexOf(l)) >= 0)
        expansionListeners_.remove(i);
    }
    else
    {
  		if ((i = listeners_.indexOf(l)) >= 0)
  			listeners_.remove(i);
    }
  }

  public Object getChild(Object parent, int index)
  {
  	try
  	{
      // respect that the context of the root tree level
      // could change (example, recursion)
      rootTreeLevel_.setContext(context_);
  	  return rootTreeLevel_.getChild(parent, index);
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }

  public int getChildCount(Object parent)
  {
  	if (parent == dummyRoot__)
  	  return 0;

  	try
  	{
      rootTreeLevel_.setContext(context_);
  	  int i = rootTreeLevel_.getChildCount(parent);
      return i;
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }

  public int getIndexOfChild(Object parent, Object child)
  {
    if (parent == null || child == null)
      return -1;

  	try
  	{
      rootTreeLevel_.setContext(context_);
      int i = rootTreeLevel_.getIndexOfChild(parent, child);
      return i;
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }

  public Vectored getVectorParent(AnyTreeNode parent) throws AnyException
  {
  	if (parent == dummyRoot__)
  	  return null;

  	try
  	{
      rootTreeLevel_.setContext(context_);
  	  return rootTreeLevel_.getVectorParent(parent);
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }

  public Object getRoot()
  {
  	if (rootTreeLevel_ == null)
  	  return dummyRoot__;

  	try
  	{
      rootTreeLevel_.setContext(context_);
  	  Any root = rootTreeLevel_.getRoot();

      // Hmmm, do this for here for now to ensure that the root
      // is expanded in the model.  Is it always expanded in the
      // view? Not necessarily so may be revisit.
      if (root != null)
      {
        AnyTreeNode n = (AnyTreeNode)root;
        n.setExpanded(true);
      }

      // No guarantees are made that the root TreeLevel will return the
      // same object on successive calls, even if nothing has changed.
      // Thus, we remember it.
      if (cachedRoot_.equals(root))
      {
        return cachedRoot_;
      }

  	  cachedRoot_ = (root != null) ? (AnyTreeNode)root : dummyRoot__;
      return cachedRoot_;
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }

  public TreeLevel getRootTreeLevel()
  {
    return rootTreeLevel_;
  }

  public boolean isLeaf(Object node)
  {
  	if (node == dummyRoot__)
  	  return true;

  	try
  	{
      rootTreeLevel_.setContext(context_);
  	  return rootTreeLevel_.isLeaf(node);
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }

  public void valueForPathChanged(TreePath path, Object newValue)
  {
  	Any a  = getResponsibleValueForPath(path);
		Any sv = (Any)newValue;

    if (a != null)
      a.copyFrom(sv);

    // Must tell JTree that model has changed. Whether this
    // is actually necessary depends on the nature of the
    // TreeLevel implementation in use.  If the change would
    // alter the equality semantics of the node returned to the
    // tree ui then the event is required to that the ui can
    // update its cache of model objects.
    nodeChanged(path);
  }
  
  public void nodeChanged(TreePath path)
  {
    TreePath parentPath = path.getParentPath();
    Object child = path.getLastPathComponent();
    int    index = this.getIndexOfChild(parentPath.getLastPathComponent(),
        child);
    
    Object[] childArray = new Object[1];
    int[]    indexArray = new int[1];
    
    childArray[0] = child;
    indexArray[0] = index;
    
    TreeModelEvent tme = new AnyTreeModelEvent(this,
                                               parentPath,
                                               indexArray,
                                               childArray);

    fireChangedEvent(tme);
    
  }

	public Component getTreeCellRendererComponent(JTree   tree,
	                                              Object  value,
	                                              boolean selected,
	                                              boolean expanded,
	                                              boolean leaf,
	                                              int     row,
	                                              boolean hasFocus)
	{
    if (value == cachedRoot_)
    {
    	// presumably never called if the rootRenderer_ is
    	// null as this means root is not visible.
    	//if (!tree.isRootVisible())
    	//  return null;

      // Note that we don't actually use the passed value for the
      // root rendering.  Its not necessary except to know that we
      // are rendering the (single) root node.
      return rootRenderer_.renderTreeCell(tree,
                                          cachedRoot_,
                                          selected,
                                          expanded,
                                          leaf,
                                          row,
                                          hasFocus);
    }
    // For the recursive case, reset the context of the root(only) tree level
    rootTreeLevel_.setContext(context_);
    
    AnyTreeNode node = (AnyTreeNode)value;
		return rootTreeLevel_.getTreeCellRendererComponent(tree,
		                                                   node,
		                                                   selected,
		                                                   expanded,
		                                                   leaf,
		                                                   row,
		                                                   hasFocus);
	}

	public Component getTreeCellEditorComponent(JTree   tree,
																							Object  value,
																							boolean isSelected,
																							boolean expanded,
																							boolean leaf,
																							int     row)
	{
    Any            v  = (Any)value;
	  TreePath       tp = tree.getPathForRow(row);
    TreeLevel      l  = this.getTreeLevelForPath(tp);
    Any            a  = l.getResponsibleFor(v);

    lastEditor_ = l.getTreeCellEditor(v);

		Component ret = lastEditor_.getTreeCellEditorComponent(tree,
				                                                   a,
				                                                   isSelected,
				                                                   expanded,
				                                                   leaf,
				                                                   row);
  	lastEditor_.addCellEditorListener(this);
    lastComponent_ = ret;
  	return ret;
	}

	// CellEditor interface methods
	public void addCellEditorListener(CellEditorListener l)
	{
		if (editorListeners_ == null)
	    editorListeners_ = new ArrayList();

		editorListeners_.add(l);
	}

	public void cancelCellEditing()
	{
		lastEditor_.cancelCellEditing();
	}

	public Object getCellEditorValue()
	{
		return lastEditor_.getCellEditorValue();
	}

	public boolean isCellEditable(EventObject anEvent)
	{
		// should only get here if we previously returned true
		// from isPathEditable.  Just return true unless want
		// to do anything fancy with anEvent
		return true;
	}

	public void removeCellEditorListener(CellEditorListener l)
	{
		int i = -1;
		if ((i = editorListeners_.indexOf(l)) >= 0)
			editorListeners_.remove(i);
	}

	public boolean shouldSelectCell(EventObject anEvent)
	{
		//return lastEditor_.shouldSelectCell(anEvent);
		lastComponent_.requestFocus();
		return true;
	}

	public boolean stopCellEditing()
	{
		return lastEditor_.stopCellEditing();
	}

  /**
   * Establish the rendering information for the root the node.
   */
	public void setRenderInfo(RenderInfo r)
	{
		rootRenderer_ = new AnyCellRenderer(r, new DefaultTreeCellRenderer());
	}

	public RenderInfo getRenderInfo()
	{
	  RenderInfo ret = null;
	  
	  if (rootRenderer_ != dummyRenderer__)
	  {
	    ret = rootRenderer_.getRenderInfo();
	  }
	  
	  return ret;
	}
	
  /**
   * Set the tree level(s) this model should represent.
   */
	public void setLevels(TreeLevel l, Map namedLevels)
	{
    l.setModel(this);

    rootTreeLevel_ = l;

    // If there were any names discovered during the creation of
    // the TreeLevel objects then put them here.
    if (namedLevels != null && namedLevels.entries() != 0)
      levels_ = namedLevels;
    else
      levels_ = null;

    // Kick out a structure event on the root
    Object[] pathArray    = new Object[1];
    pathArray[0] = getRoot();
    // when the root is affected the children and their
    // indices are always null.
    TreePath treePath = new TreePath(pathArray);
    AnyTreeModelEvent tme = new AnyTreeModelEvent(this, treePath);

    // Clear down the root
    resetRoot();

    // Force a re-evaluation.  Bit crude but context never changes
    // at the root level otherwise (er, unless recursive from root!).
		rootTreeLevel_.setContext(null);
		rootTreeLevel_.setContext(context_);

    fireStructureEvent(tme);
  }

  /**
   * Only returns non-null if there were some named levels the last
   * time the levels were set.
   */
  public Map getLevels()
  {
    return levels_;
  }

  public void setClosedIcon(Any icon)
  {
    if (rootRenderer_ != dummyRenderer__)
      rootRenderer_.setClosedIcon(icon);
  }

  public void setOpenIcon(Any icon)
  {
    if (rootRenderer_ != dummyRenderer__)
      rootRenderer_.setOpenIcon(icon);
  }

  public boolean isPathEditable(TreePath path)
  {
  	if (path.getPathCount() == 1)
  	{
  		// the root
  		return rootRenderer_.isEditable();
  	}

    TreeLevel level = rootTreeLevel_.getTreeLevelForPath(path);
    if (level != null)
    {
      Any a = (Any)path.getLastPathComponent();
      boolean ret = level.isEditable(a);
      return ret;
    }
    return false;
  }

  public Any getResponsibleValueForPath(TreePath path)
  {
		Any cellValue = null;
    try
    {
    	if (path.getPathCount() == 1)
    	{
    		// the root
			  return rootRenderer_.getRenderInfo().resolveResponsibleData(context_);
    	}

      TreeLevel level = rootTreeLevel_.getTreeLevelForPath(path);
      if (level != null)
      {
        // Get the responsible value for the last component
        // in the path (to which the tree level we have just
        // for applies)
        Any a = (Any)path.getLastPathComponent();
        cellValue = level.getResponsibleFor(a);
      }
    }
    catch (AnyException e)
    {
      e.printStackTrace();
      cellValue = null;
    }
    return cellValue;
  }
  
  public Any getSelectionFor(Any node)
  {
    AnyTreeNode n = (AnyTreeNode)node;
    
    TreeLevel l = n.getTreeLevel();
    
    return l.getSelectionFor(n);
  }

  public Any getSelectionForPath(TreePath path)
  {
		Any selection = null;
  	if (path.getPathCount() == 1)
  	{
  		// the root
		  return cachedRoot_;
  	}

    TreeLevel level = rootTreeLevel_.getTreeLevelForPath(path);
    if (level != null)
    {
      // Get the responsible value for the last component
      // in the path (to which the tree level we have just
      // for applies)
      Any a = (Any)path.getLastPathComponent();
      selection = level.getSelectionFor(a);
    }
    return selection;
  }

  public Any getKeySelectionForPath(TreePath path)
  {
		Any selection = null;
    /*
  	if (path.getPathCount() == 1)
  	{
  		// the root
  		if (cachedRoot_ == dummyRoot__)
        return null;

  		Composite root = (Composite)cachedRoot_; //this.getRoot();
		  return root.getNameInParent();
  	} *********************************
  	*/
    TreeLevel level = null;
    if (rootTreeLevel_ != null)
      rootTreeLevel_.getTreeLevelForPath(path);

    if (level != null)
    {
      // Get the responsible value for the last component
      // in the path (to which the tree level we have just
      // for applies)
      Any a = (Any)path.getLastPathComponent();
      selection = level.getKeySelectionFor(a);
    }
    return selection;
  }

	public TreeLevel getTreeLevelForPath(TreePath p)
	{
    TreeLevel level = rootTreeLevel_.getTreeLevelForPath(p);

    return level;
	}

	// CellEditorListener
  public void editingStopped(ChangeEvent e)
  {
  	lastEditor_.removeCellEditorListener(this);
    lastEditor_ = null;
  	lastComponent_ = null;
  	fireEditingStopped(e);
  }

  public void editingCanceled(ChangeEvent e)
  {
  	lastEditor_.removeCellEditorListener(this);
    lastEditor_ = null;
  	lastComponent_ = null;
  	fireEditingCanceled(e);
  }

  public Any getContext()
  {
    return context_;
  }

  public AnyTable getTable()
  {
    throw new UnsupportedOperationException("Not a TreeTable");
  }

  /**
   * Translate the Inq structure event into a TreeModelEvent which may
   * then be dispatched to this model's registered TreeModelListeners.
   * @param e the Inq structure event
   * @param r an optional RenderInfo that, if supplied, must satisfy
   * the Inq event in the case of updates in order for the TreeModelEvent
   * to be dispatched.  Structural changes are always dispatched.  This
   * facility supports the TreeTable concept, whereby update events
   * may not be applicable to the rendering tree, rather to the other
   * columns in the view.
   */
  public AnyTreeModelEvent translateEvent(Event e, JTable table, JTree tree) throws AnyException
  {
  	int serialNumber = e.getSerialNumber();

  	// Inq NODE_ADDED/REMOVED/REPLACED can include a series
  	// of _CHILD event types for sub-structure notification.
  	// These are delivered from the top of the structure
  	// downwards and we are only interested in the first
  	// such event in a series. See also AnyTreeLevel.translateEvent
  	if (serialNumber >= 0 &&
        serialNumber == lastSerialNumber_)
  	  return null;

  	lastSerialNumber_ = serialNumber;

  	Any baseType  = AbstractEvent.getBasicType(e.getId());
    
    // If it's an update event we won't need to re-resolve the
    // context
  	boolean force =
  	  (baseType.equals(EventConstants.BOT_UPDATE) || baseType.equals(EventConstants.BOT_DELETE)) ? false
  	                                               : true;
  	Array path = AbstractComposite.array();
		Map   id   = (Map)e.getId();
    //Any   root = cachedRoot_; //(Any)getRoot();
    Any   root = (Any)getRoot();

		NodeSpecification nodePath = (NodeSpecification)id.get(EventConstants.EVENT_PATH);

  	int childIndex = rootTreeLevel_.translateEvent(cachedRoot_,
                                                   e,
                                                   id,
									                                 baseType,
									                                 force,
									                                 path,
									                                 nodePath);
    
    if (childIndex == -2)
      return null;
    
    AnyTreeModelEvent tme = null;
    short treeEvent       = CHANGED;

    // The path array will now contain the list
    // of children that we would return to the view.
    // If the event pertains to the root then this
    // array will be empty.  If there is a root then
    // add this to the front of the array. If there's
    // no root the array should be empty anyway.

    Object[] pathArray   = null;
    int[]    indices     = null;
    Object[] children    = null;
    TreePath treePath    = null;
    Any      child       = null;

    int      pathEntries = path.entries();

    if (pathEntries == 0)
    {
    	// The event relates to the root only
      pathArray    = new Object[1];
      pathArray[0] = root;

    	if (AbstractEvent.isBotEvent(baseType))
      {
      	// its a rendering change to the root
      	treeEvent = CHANGED;
      }
      else
      {
      	// its a node change of the root
      	treeEvent = STRUCTURE;
      }

    	// when the root is affected the children and their
    	// indices are always null.
      treePath = new TreePath(pathArray);
    	tme = new AnyTreeModelEvent(this, treePath);
    }
    else
    {
    	// To work out the index we use that returned by
    	// the path determination.
    	if (baseType.equals(EventConstants.NODE_REMOVED))
    	{
    		//IntI vector = (IntI)id.get(EventConstants.EVENT_VECTOR);
    		//childIndex    = vector.getValue();
    		treeEvent = REMOVED;
    	}
    	else if (baseType.equals(EventConstants.NODE_ADDED) ||
    	         baseType.equals(EventConstants.NODE_ADDED_CHILD))
    	{
    		treeEvent = INSERTED;
    	}
    	else if (AbstractEvent.isBotEvent(baseType))
    	{
    		treeEvent = CHANGED;
    	}
    	else
        treeEvent = STRUCTURE;

      // Build the array representing the tree path for
      // the AnyTreeModelEvent.


      // The last item in the path array is the child to
    	// which the event relates and we have to put the
    	// root on the front.  If the index could not be determined
      // then we will fire a structure event and the path
      // array returned by the model indicates the root.
      // If there is an index then the last element in the
      // path array is the (only) child.
      if (childIndex >= 0)
      {
        pathArray    = new Object[pathEntries];
        child = path.remove(pathEntries - 1);
      }
      else
        pathArray    = new Object[pathEntries+1];

      // The TreeLevel structure starts at level 1, so put the
      // root on the front now.
      pathArray[0] = root;
      for (int i = 0; i < path.entries(); i++)
        pathArray[i+1] = path.get(i);


      if (childIndex >= 0)
      {
        children     = new Object[1];
        children[0]  = child;
        indices    = new int[1];
        indices[0] = childIndex;

        treePath = new TreePath(pathArray);
				tme = new AnyTreeModelEvent(this,
                                    treePath,
                                    indices,
                                    children);
      }
      else
      {
      	// if we can't determine the index then assume
      	// structure change
        treePath = new TreePath(pathArray);
      	tme = new AnyTreeModelEvent(this, treePath);
      	treeEvent = STRUCTURE;
      }
    }

    if (treeEvent == REMOVED)
      fireRemovedEvent(tme);
    else if (treeEvent == INSERTED)
      fireInsertedEvent(tme);
    else if (treeEvent == CHANGED)
    {
      if (table == null)
        fireChangedEvent(tme);
      else
      {
        // In the tree table case we only dispatch the event if it relates
        // to the tree itself. Otherwise the (calling) AnyTreeTableModel
        // adapter further analyses the event to determine the table
        // column(s) appropriate.
        AnyTreeNode childNode = (AnyTreeNode)child;
        TreeLevel   level = childNode.getTreeLevel();
        RenderInfo r = level.getRenderInfo();
        
        Set fields = (Set)((Map)e.getId()).get(EventConstants.EVENT_FIELDS);
        Iter i = fields.createIterator();
        tme.setFields(fields);
        tme.setTable(table);
        boolean willDispatch = false;
        while (i.hasNext())
        {
          Any field = i.next();
          if (r.isDispatching(field))
          {
            willDispatch = true;
            break;
          }
        }

        if (willDispatch)
          fireChangedEvent(tme);
        else
          tme.setWasDispatched(false);
      }
    }
    else
    {
      // When its a structure event apply any sort currently in effect
      // from this node downward
      
      // Force the tree level to re-resolve its root. The sort goes
      // straight to the vector so without doing this we sort the old
      // structure then display the new!
      TreeLevel l = getTreeLevelForPath(treePath);
      l.markStale();
      
      // Perform the sort 
      AnyTreeNode n = this.sort(treePath, true);
      
      // Stale the node beneath which the sort was performed
      if (pathArray.length == 1)
        resetRoot();
      else
        n.setStale();
      
      // Tell our observers (not least the tree)
      //tme.setExpandPaths(true);
      fireStructureEvent(tme);
      
      // Reevaluate the expansion state of the sorted paths
      //expandPaths(n, tree, 1);

    }

    return tme;
  }

  public AnyTreeNode sort(TreePath p, boolean depthSort)
  {
    TreeLevel l = getTreeLevelForPath(p);
    AnyTreeNode n = (AnyTreeNode)p.getLastPathComponent();
    l.sort(n, depthSort);
    
    return n;
  }

	void setContext(Any context)
	{
		context_ = context;
		if (rootTreeLevel_ != null)
		  rootTreeLevel_.setContext(context);
	}

	boolean isRootVisible()
	{
		return rootRenderer_ != dummyRenderer__;
	}

  void resolveNodeSpecs(NodeSpecification rootPath,
                        Map               nodeSpecs,
                        TreeTableModel    treeTableModel,
                        Any               contextNode)
  {
  	if (rootTreeLevel_ != null)
  	  rootTreeLevel_.resolveNodeSpecs(rootPath, nodeSpecs, treeTableModel, contextNode);

  	// If there is a renderer for the root then evaluate its
  	// node specs
  	if (rootRenderer_ != null)
  	{
      rootRenderer_.getRenderInfo().resolveNodeSpecs(contextNode);
      Map ns = rootRenderer_.getRenderInfo().getNodeSpecs();
      Iter iter = ns.createKeysIterator();
      while (iter.hasNext())
      {
        Any thisNs    = iter.next();
        Any fieldList = ns.get(thisNs);
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
  }

  private void fireRemovedEvent(TreeModelEvent tme)
  {
  	for (int i = 0; i < listeners_.size(); i++)
  	{
  		TreeModelListener tml = (TreeModelListener)listeners_.get(i);
  		tml.treeNodesRemoved(tme);
  	}
  }

  private void fireInsertedEvent(TreeModelEvent tme)
  {
  	for (int i = 0; i < listeners_.size(); i++)
  	{
  		TreeModelListener tml = (TreeModelListener)listeners_.get(i);
  		tml.treeNodesInserted(tme);
  	}
  }

  private void fireChangedEvent(TreeModelEvent tme)
  {
  	for (int i = 0; i < listeners_.size(); i++)
  	{
  		TreeModelListener tml = (TreeModelListener)listeners_.get(i);
  		tml.treeNodesChanged(tme);
  	}
  }

  protected void resetRoot()
  {
    cachedRoot_ = dummyRoot__;

    //AnyTreeNode root = (AnyTreeNode)getRoot();
    //root.empty();
  }
  
  public boolean expandPaths(AnyTreeNode rootNode, JTree tree, int level)
  {
    // Find the deepest expanded nodes under the given node.  For
    // each such node, evaluate its path and expand it.
    boolean thisExpanded = false;
    boolean childExpanded = false;
    if (rootNode.isExpanded())
    {
      thisExpanded = true;
      int max = rootNode.entries();
      // Check if any of the paths underneath us are expanded.
      // If they are then we don't have to do anything at this level,
      // as expanding a lower level expands us, of course.
      for (int i = 0; i < max; i++)
      {
        AnyTreeNode child = rootNode.getChildAt(i);
        
        childExpanded |= expandPaths(child, tree, level+1);
      }
      if (!childExpanded)
      {
        // Nothing expanded underneath us.  We are expanded so
        // expand to this level.
        TreePath tp = rootNode.makeTreePath();
        tree.expandPath(tp);
      }
    }
    //return thisExpanded && (tree.isRootVisible() || level > 1) || (childExpanded && level == 1);
    return thisExpanded;
  }
  
  public void setExpandToLevel(Any expandToLevel)
  {
    if (AnyNull.isNullInstance(expandToLevel))
      expandToLevel = null;
    
    expandToLevel_ = expandToLevel;
  }
  
  public void setExpandToLevelAll(boolean expandAll)
  {
    expandAll_ = expandAll;
  }
  
  /**
   * Expand the tree to the given named level or numbered depth. 
   */
  public void expandToDepth(AnyTreeNode root, JTree t)
  {
    if (expandToLevel_ != null)
    {
      TreeLevel l = (TreeLevel)levels_.getIfContains(expandToLevel_);
      root = (AnyTreeNode)getRoot();  // Hmmm...
      //root.setExpanded(true);
      if (l == null)
      {
        // No named level, try number conversion. Zero means root,
        // one means root children, etc etc.
        AnyInt i = new AnyInt(expandToLevel_);
        
        // Check where the given node is in the hierarchy.
        // If it is at or below the specified depth then we
        // don't need to do anything. If it is above the
        // specified depth (count nodes back to the root)
        // then we have to do something. The tree's root
        // node is designated as depth zero. Expanding to
        // level zero is therefore a no-operation, indeed
        // if the root node is not visible (JTree.isRootVisible()
        // returns false) then the default state for the tree
        // when the root node is replaced is to show all
        // the root's child (that is level 1) nodes. In this
        // case expanding to level 1 is also degenerate.
        
        if (i.getValue() == 0)
          return;
        
        int depth = 0;
        AnyTreeNode node = root;
        while (node.getParent() != null)
        {
          depth++;
          node = node.getParent();
        }
        
        if (depth >= i.getValue())
          return;
        
        expandToNumberedDepth(root, depth, i.getValue(), expandAll_);
      }
      else
      {
        // Check where the given node is in the hierarchy.
        // If it is at or below the specified level we don't
        // need to do anything. If it is above the specified
        // level (we don't find the level as we traverse up)
        // then we have to do something. If it's the root node
        // we'll do something
        AnyTreeNode node = root;
        while (node.getParent() != null)
        {
          if (node.getTreeLevel().getName().equals(expandToLevel_))
            return;
          
          node = node.getParent();
        }
        expandToNamedLevel(root, l, expandAll_);
      }
      
      // Perform the expansion in the view
      expandPaths(root, t, 1);
    }
  }
  
  private void expandToNumberedDepth(AnyTreeNode node,
                                     int         current,
                                     int         toDepth,
                                     boolean     all)
  {
    if (current < toDepth)
    {
      int numChildren = node.entries();
      for (int i = 0; i < numChildren; i++)
      {
        AnyTreeNode child = node.getChildAt(i);
        child.setExpanded(true);
        expandToNumberedDepth(child, ++current, toDepth, all);
        if (i == 0 && !all)
          break;
      }
    }
  }
  
  private void expandToNamedLevel(AnyTreeNode node, TreeLevel level, boolean all)
  {
    // Expand the tree such that the first or all nodes represented at
    // the given level are visible.
    if (!node.getTreeLevel().equals(level))
    {
      //int numChildren = node.entries();
      int numChildren = this.getChildCount(node);
      for (int i = 0; i < numChildren; i++)
      {
        //AnyTreeNode child = node.getChildAt(i);
        AnyTreeNode child = (AnyTreeNode)getChild(node, i);
        child.setExpanded(true);
        expandToNamedLevel(child, level, all);
        if (i == 0 && !all)
          break;
      }
    }
  }

  public void fireStructureEvent(TreeModelEvent tme)
  {
    for (int i = 0; i < listeners_.size(); i++)
    {
      TreeModelListener tml = (TreeModelListener)listeners_.get(i);
      tml.treeStructureChanged(tme);
    }
    
    // The expansion listeners are fired after any other type of listener.
    // This is because its processing (to maintain expanded paths after
    // a structure event) can only be performed after all the other (at least
    // Java internal) listeners have run.
    for (int i = 0; i < expansionListeners_.size(); i++)
    {
      TreeModelListener tml = (TreeModelListener)expansionListeners_.get(i);
      tml.treeStructureChanged(tme);
    }
  }

  private void fireEditingStopped(ChangeEvent e)
  {
  	if (editorListeners_ != null)
  	{
	   	for (int i = 0; i < editorListeners_.size(); i++)
	  	{
	  		CellEditorListener cel = (CellEditorListener)editorListeners_.get(i);
	  		cel.editingStopped(e);
	  	}
  	}
  }

  private void fireEditingCanceled(ChangeEvent e)
  {
  	if (editorListeners_ != null)
  	{
	   	for (int i = 0; i < editorListeners_.size(); i++)
	  	{
	  		CellEditorListener cel = (CellEditorListener)editorListeners_.get(i);
	  		cel.editingCanceled(e);
	  	}
  	}
  }

  // A simple extension of TreeModelEvent so that we can determine
  // whether the event relates to a child index or not.  Inq generated
  // TreeModelEvents only ever have one index and this is more efficient
  // than calling getChildIndices(), which returns a newly allocated
  // array.
  // Where the event originates from the processing of a node event
  // it carries the affected fields. In the tree-table case, a node event
  // will relate to a particular level of data but may not be relevant to
  // the tree itself. In this case the wasDispatched_ flag is false and
  // further processing of the column rendering data against the event
  // fields is performed.
  static public class AnyTreeModelEvent extends TreeModelEvent
  {
    // The child index within the TreePath that the original
    // event translation identified
    private int index_ = -1;

    // Whether this event has already been dispatched to the
    // registered TreeModelListeners.  If it wasn't the event
    // cannot relate to the primary tree column (column zero)
    // and when dispatching TableModelEvents we can start
    // at column 1 when testing the event for eligibility.
    private boolean wasDispatched_ = true;

    // The fields contained within the original Inq event.  This
    // is as a convenience, since we have had to check whether
    // the event relates to the primary tree column already, and
    // we will go on to check it it relates to other columns as well.
    private Set fields_;
    
    // When deployed in a treetable, the table the tree is rendering within
    private JTable table_;
    
    private boolean expandPaths_;

    public AnyTreeModelEvent(Object   source,
                             Object[] path,
                             int[]    childIndices,
                             Object[] children)
    {
      super(source, path, childIndices, children);
      index_ = childIndices[0];
    }

    public AnyTreeModelEvent(Object   source,
                             TreePath path,
                             int[]    childIndices,
                             Object[] children)
    {
      super(source, path, childIndices, children);
      index_ = childIndices[0];
    }

    public AnyTreeModelEvent(Object source, Object[] path)
    {
      super(source, path);
    }

    public AnyTreeModelEvent(Object source, TreePath path)
    {
      super(source, path);
    }

    int getIndex()
    {
      return index_;
    }
    
    /**
     * Returns a tree path that corresponds to the originator of the
     * event that gave rise to <code>this</code>.
     * <p>
     * If an event is a structure change (that is there is no
     * affected child node) then the underlying tree path is returned.
     * Otherwise the path relating to the event originator is the
     * underlying path plus the child.
     * @return a TreePath
     */
    TreePath getEventPath()
    {
      if (index_ < 0)
        return getTreePath();
      else
      {
        return getTreePath().pathByAddingChild(getChildren()[0]);
      }
    }

    void setWasDispatched(boolean wasDispatched)
    {
      wasDispatched_ = wasDispatched;
    }

    boolean getWasDispatched()
    {
      return wasDispatched_;
    }

    void setFields(Set fields)
    {
      fields_ = fields;
    }

    Set getFields()
    {
      return fields_;
    }

    void setTable(JTable table)
    {
      table_ = table;
    }

    JTable getTable()
    {
      return table_;
    }
    
    void setExpandPaths(boolean expandPaths)
    {
      expandPaths_ = expandPaths;
    }
    
    public boolean isExpandPaths()
    {
      return expandPaths_;
    }
  }
}
