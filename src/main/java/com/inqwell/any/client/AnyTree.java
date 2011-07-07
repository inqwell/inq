/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTree.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.client.AnyTable.TreeExpansionListener;
import com.inqwell.any.client.AnyView.EventBinding;
import com.inqwell.any.client.swing.JPanel;
import com.inqwell.any.beans.SelectionF;
import com.inqwell.any.client.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import java.awt.Container;

public class AnyTree extends AnyComponent
{
	private JTree         t_;
	private JScrollPane   s_;

	private JComponent    borderee_;

	private Map           modelVars_ = AbstractComposite.managedMap();
	private AnyTreeModel  model_;
	private Array         selection_;
	private Array         keySelection_;
	private Array         pathSelection_;
	private Array         pathSet_;
  private IntI          selectCount_;

	static private Array  treeSelectedEventType__ = AbstractComposite.array();

  static public  Any    level__                 = new ConstString("levelRoot");
  static public  Any    levelArg__              = new ConstString("level");
  static public  Any    subLevel__              = new ConstString("subRoot");
	static public  Any    nextLevel__             = new ConstString("nextLevel");
	static public  Any    name__                  = new ConstString("name");
	static public  Any    expansion__             = new ConstString("expansion");
	static public  Any    recursive__             = new ConstString("recursive");
	static public  Any    branchOnly__            = new ConstString("branchOnly");
	static public  Any    isLeaf__                = new ConstString("isLeaf");
	static public  Any    openIcon__              = new ConstString("openIcon");
	static public  Any    closedIcon__            = new ConstString("closedIcon");
  static public  Any    leafIcon__              = new ConstString("leafIcon");
  static public  Any    expanded__              = new ConstString("expanded");

	private static Set     treeProperties__;
  public  static Any     levels__        = new ConstString("levels");
  public  static Any     expandToLevel__ = new ConstString("expandToLevel");
  public  static Any     expandAll__     = new ConstString("expandAll");
	private static Any     scrollable__    = new ConstString("scrollable");
	private static Any     pathSelection__ = new ConstString("pathSelection");
	private static Any     pathSet__       = new ConstString("pathSet");

  public  static Array treeExpansionEventTypes__;

  static
  {
		treeSelectedEventType__.add(EventConstants.E_TREESELECTION);

    treeProperties__ = AbstractComposite.set();
    treeProperties__.add(levels__);
    treeProperties__.add(scrollable__);
    treeProperties__.add(openIcon__);
    treeProperties__.add(closedIcon__);
    treeProperties__.add(AnyTree.expandToLevel__);
    treeProperties__.add(AnyTree.expandAll__);
    treeProperties__.add(AnyTable.selectionMode__);

    treeExpansionEventTypes__ = AbstractComposite.array();
    treeExpansionEventTypes__.add(EventConstants.E_TREEEXPANDED);
    treeExpansionEventTypes__.add(EventConstants.E_TREECOLLAPSED);

	}

  public AnyTree()
  {
	}

	public void setObject(Object o)
	{
		if (o instanceof JPanel)
		{
			JComponent borderee = (JComponent)o;
			borderee_ = borderee;
			setObject(borderee.getComponent(0));
			return;
		}

		if (borderee_ == null)
			borderee_ = (JComponent)o;

		if ((!(o instanceof JTree)) && (!(o instanceof JScrollPane)))
			throw new IllegalArgumentException
									("AnyTree wraps javax.swing.JTree/JScrollPane and sub-classes");


		if (o instanceof JTree)
		{
			t_ = (JTree)o;
		}
		else
		{
			s_ = (JScrollPane)o;
			t_ = (JTree)s_.getViewport().getView();
		}
    
    t_.setAnyTree(this);

		model_ = (AnyTreeModel)t_.getModel();

  	setScrollable(true);

		//System.out.println ("AnyTree.setObject1 ");
		super.setObject(t_);
		//System.out.println ("AnyTree.setObject2 ");
		//model_.setContext(getContextNode());
		//System.out.println ("AnyTree.setObject3 ");

		// The tree root is visible based on whether there is
		// rendering information in the tree model itself.
		// At this point there won't be so establish the
		// default state that the root is not visible.
		t_.setRootVisible(model_.isRootVisible());
    
    // Put a treeselectionlistener on to the tree. The tree nodes
    // maintain their expanded state allowing the GUI not to
    // collapse after sort or Inq node events
    addAdaptedEventListener(new TreeExpansionListener());

		// Trees must be rendering something so do the data
		// event listening stuff here.
		// The RenderInfo objects in the TreeLevel will already
		// be set up with their node specs.
		// Note - must handle the root node specification
		// when it is specified.  Not available at this point
		// (see above). To be fixed.
		//setupDataEvents();

	}

  /**
   * Establish the renderinfo for the root.  Makes the root visible.
   */
	public void setRenderInfo(RenderInfo r)
	{
    if (r != null)
    {
  		model_.setRenderInfo(r);
  		t_.setRootVisible(model_.isRootVisible());
  		setupDataEvents();
    }
	}

	public Container getComponent()
	{
    /*
		if (s_ != null)
			return s_;
		else
			return t_;
    */
    
    return t_;
	}

	public JComponent getBorderee()
	{
		if (s_ != null)
			return s_;
		else
			return t_;
	}

  public Object getAddee()
  {
    return getBorderee();
  }

  /**
   * Sets the <i>scrollable</i> property.  This is a
   * <i>synthetic property</i> provided by <code>inq</code>
   * that can only be set prior to layout. Setting this
   * property once the tree has been placed in the
   * <code>awt</code> component hierarchy will have
   * undefined results.
   */
  public void setScrollable(boolean scrollable)
  {
  	if (s_ == null && scrollable)
  	{
  		if (borderee_ != null && borderee_ != t_)
  		  borderee_.remove(t_);

  	  s_ = new JScrollPane(t_);
  	  borderee_ = s_;
  	}
  	else if (s_ != null && !scrollable)
  	{
  		s_.setViewportView(null);
  	  s_ = null;
  	  borderee_ = new JPanel();
  	  borderee_.add(t_);
  	}
  }

	public void setScrollable(JScrollPane o)
	{
		//System.out.println("AnyTable.setScrollable: " + o);
		if (!(o instanceof JScrollPane))
			throw new IllegalArgumentException
									("setScrollable not a javax.swing.JScrollPane");


		s_ = (JScrollPane)o;

		if (t_ != null)
		{
			s_.setViewportView(t_);
		}
	}

  public void setExpandToLevel(Any expandToLevel)
  {
    model_.setExpandToLevel(expandToLevel);
  }

  public void setExpandAll(boolean expandAll)
  {
    model_.setExpandToLevelAll(expandAll);
  }

  public void setSelectionMode(IntI mode)
  {
    t_.getSelectionModel().setSelectionMode(mode.getValue());
  }
  
  public AnyTreeModel getModel()
  {
    return model_;
  }

	/**
	 * Override base functionality.  We adapt the inq event to a table
	 * event.
	 */
	protected void componentProcessEvent(Event e) throws AnyException
	{
		model_.translateEvent(e, null, t_);
//		System.out.println ("AnyTree.componentProcessEvent " + e);
	}

	protected Object getAttachee(Any eventType)
	{
		if (eventType.equals(ListenerConstants.TREESELECTION))
			return t_.getSelectionModel();
		else
			return super.getAttachee(eventType);
	}

//  EventDispatcher setupTreeLevelEvents(TreeLevel t, Map nodeSpecs)
//  {
//  	TreeLevelListener tl = new TreeLevelListener(t, nodeSpecs);
//  	return tl.getEventDispatcher();
//  }

	protected Object getPropertyOwner(Any property)
	{
		if (treeProperties__.contains(property))
		  return this;

		return super.getPropertyOwner(property);
	}

	protected void initUpdateModel()
	{
    // Tell base class about our tree selection model
    setupEventSet(t_.getSelectionModel());

    selection_      = AbstractComposite.array();
    keySelection_   = AbstractComposite.array();
    pathSelection_  = AbstractComposite.array();
    pathSet_        = AbstractComposite.array();
		modelVars_.add(SelectionF.selection__, selection_);
		modelVars_.add(SelectionF.keySelection__, keySelection_);
		modelVars_.add(pathSelection__, pathSelection_);
		modelVars_.add(pathSet__, pathSet_);
    modelVars_.add(SelectionF.selectCount__, selectCount_ = new AnyInt());
		modelVars_.setTransactional(true);
		this.add(AnyComponent.modelKey__, modelVars_);
		//System.out.println ("AnyTree.initUpdateModel ");
		addAdaptedEventListener(new TreeSelectionListener(treeSelectedEventType__));

    //addAdaptedEventListener(new TreeExpansionListener());
	}

	public void updateModel() throws AnyException
	{
    selection_.empty();
    keySelection_.empty();
    pathSelection_.empty();
    pathSet_.empty();

		// TreeSelectionModel tsm = t_.getSelectionModel();
		TreePath[] tp = t_.getSelectionPaths();
		if (tp == null || tp.length == 0)
    {
      selectCount_.setValue(0);
		  return;
    }

    selectCount_.setValue(tp.length);
    
    for (int i = 0; i < tp.length; i++)
    {
    	TreePath thisTp = tp[i];
      //System.out.println("TP " + i + " length " + thisTp.getPathCount());

      Any keySelection = model_.getKeySelectionForPath(thisTp);
    	if (keySelection != null)
        keySelection_.add(keySelection);

    	Any selection = model_.getSelectionForPath(thisTp);
    	if (selection != null)
        selection_.add(selection);
      else
        continue;

      Object[] o = thisTp.getPath();

      // This complete selection path...
      Array path    = AbstractComposite.array(o.length);

      // ...create the distinct selection paths as well.
      // Start from 1 because we are not interested in the
      // root node ( It is always the context )
      for (int j = 0; j < o.length; j++)
      {
        Any a = (Any)o[j];

        // respect getSelectionForPath if we on the last element
        if (j == o.length-1)
          a = selection;
        else
          a = model_.getSelectionFor(a);  // always gets selections now

        if (j != 0)
          path.add(a);

        if (pathSet_.entries() == j)
        {
          // The deepest we have so far yet been down any
          // particular selection path.  Make a new array and
          // add the current path object to it.
          Array pathSet = AbstractComposite.array();
          pathSet.add(a);
          pathSet_.add(pathSet);
        }
        else
        {
          // Get the existing object array at this level
          // and add the selection to it only if not already there
          Array pathSet = (Array)pathSet_.get(j);
          if (!pathSet.contains(a))
            pathSet.add(a);
        }
      }
      pathSelection_.add(path);
    }

		//System.out.println("**** Tree Selection Path " + tp.getPathCount());
//		System.out.println("**** Tree Selection ****");
//		System.out.println(tsm.getMinSelectionRow());
//		System.out.println(tsm.getMaxSelectionRow());
//		System.out.println("TP** " + tp);
//		System.out.println("PATH " + pathSelection_);
//		System.out.println("SET " + pathSet_);
	}

  public Any getLevels()
  {
    return model_.getLevels();
  }

  public void setLevels(Any levels)
  {
    // Receive level or a vector of levels.  If the latter then
    // we have a model that represents explicit level/render/expansion
    // information and we build the appropriate set of AnyTreeLevel
    // objects.  If a single map (containing a path) then build
    // a single HierarchyTreeLevel object.

    //
    //System.out.println("Setting levels to: " + levels);
    if (levels instanceof Map)
    {
      Map namedLevels = AbstractComposite.simpleMap();
      model_.setLevels(processTreeLevels((Map)levels,
                                         getContextNode(),
                                         null,
                                         namedLevels),
                       namedLevels);
    }
    else
      throw new AnyRuntimeException("Invalid model " + levels);

		t_.reinit((AnyTreeModel)t_.getModel());
    setupDataEvents();
  }

  public boolean getScrollable()
  {
    return s_ != null;
  }

  /**
   * Sets the ckised and open icons to <code>icon</code>.  If
   * a different open icon is required then the open icon
   * should be set afterwards.
   */
  public void setClosedIcon(Any icon)
  {
    model_.setClosedIcon(icon);
    model_.setOpenIcon(icon);
  }

  public void setOpenIcon(Any icon)
  {
    model_.setOpenIcon(icon);
  }

  public void evaluateContext()
	{
    super.evaluateContext();
		model_.setContext(getContextNode());
	}

  protected boolean handleBoundEvent(Event e)
  {
    //System.out.println(e.getUnderlyingEvent());
    // Hmmm, TODO
    return true;
  }
  
	private void setupDataEvents()
	{
		Map nodeSpecs = AbstractComposite.simpleMap();

		model_.resolveNodeSpecs(NodeSpecification.NULLNS,
                            nodeSpecs,
                            null,
                            getContextNode());
    
		//model_.setupDataEvents(this);

		// Now we've aggregated the node specifications and added the root
		// specification, if any.  Use base class method to route any
		// appropriate model to our componentProcessEvent method!
		//System.out.println ("AnyTree.setupDataEvents " + nodeSpecs);
		setupDataListener(nodeSpecs);
	}

  // Recursively process a map to return a linked set of TreeLevels
  // representing our model.  Process depth-first to leave the
  // top-level last.
  static TreeLevel processTreeLevels(Map      levels,
                                     Any      contextNode,
                                     AnyTable table,
                                     Map      namedLevels)
  {
    if (levels == null)
      throw new AnyRuntimeException("Found null tree level");

    AnyBoolean recursive = new AnyBoolean();
    if (levels.contains(recursive__))
      recursive.copyFrom(levels.get(recursive__));
    if (levels.contains(subLevel__))
      recursive.setValue(true);

    TreeLevel nextLevel = null;
    if (levels.contains(nextLevel__))
    {
      if (recursive.getValue())
        throw new AnyRuntimeException("A recursive level cannot have a sub-level. You can use explicit expansions");

      nextLevel = processTreeLevels((Map)levels.get(nextLevel__),
                                    contextNode,
                                    table,
                                    namedLevels);
    }

    Any n = null;
    Any r = null;

    if (levels.contains(AnyComponent.renderInfo__))
      r = levels.get(AnyComponent.renderInfo__);

    if (levels.contains(level__))
    {
      n = levels.get(level__);
    }

    Vectored exp = null;
    if (levels.contains(expansion__))
    {
      if (recursive.getValue())
        throw new AnyRuntimeException("A recursive level cannot have explicit expansions.");

      exp = (Vectored)levels.get(expansion__);
    }
    
    if ((n == null && r != null) ||
        (r == null && n != null))
      throw new AnyRuntimeException("If there is a level root then there must also be rendering info");

    if (n == null && (exp == null || exp.entries() == 0))
      throw new AnyRuntimeException("If there is no level root there must be at least one expansion");

    if (n != null)
    {
      if (n != AnyNull.instance() && !(n instanceof NodeSpecification))
        throw new AnyRuntimeException("Tree level root must be a node reference or null");
      if (!(r instanceof RenderInfo))
        throw new AnyRuntimeException("Tree level rendering must be a RenderInfo");
    }

    Locate l = null;
    if (n != null)
      l = new LocateNode(((NodeSpecification)n).resolveIndirections(contextNode,
                                                                    Globals.process__.getTransaction()));

    TreeLevel thisLevel = null;

    if (recursive.getValue())
    {
      Locate lsub = null;
      Any nsub = levels.getIfContains(subLevel__);

      if (nsub != null && !(nsub instanceof NodeSpecification))
        throw new AnyRuntimeException("Recursive level sub-root must be a node reference");

      if (nsub != null)
        lsub = new LocateNode(((NodeSpecification)nsub).resolveIndirections(contextNode,
                                                                      Globals.process__.getTransaction()));

      thisLevel = new AnyTreeLevel(l, lsub);
    }
    else if (nextLevel == null)
      thisLevel = new AnyTreeLevel(l);
    else
      thisLevel = new AnyTreeLevel(l, nextLevel);

    if (r != null)
      thisLevel.setRenderInfo((RenderInfo)r);

    // Once the renderInfo has been set into the TreeLevel we
    // can do any icons
    if (levels.contains(closedIcon__))
    {
      thisLevel.setClosedIcon(levels.get(closedIcon__));
      // When a closed icon is specified and there is no open
      // icon reuse the closed icon.
      if (!levels.contains(openIcon__))
        thisLevel.setOpenIcon(levels.get(closedIcon__));
    }

    if (levels.contains(openIcon__))
      thisLevel.setOpenIcon(levels.get(openIcon__));

    if (levels.contains(leafIcon__))
      thisLevel.setLeafIcon(levels.get(leafIcon__));

    if (levels.contains(isLeaf__))
      thisLevel.setLeafExpression(levels.get(isLeaf__));

    if (levels.contains(AnyAlwaysEquals.equals__))
      thisLevel.setEquals(levels.get(AnyAlwaysEquals.equals__));

    // Does this level have a name?
    if (levels.contains(name__))
    {
      Any name = levels.get(name__);
      if (namedLevels.contains(name))
        throw new AnyRuntimeException("There is already a level called " + name);
      namedLevels.add(name, thisLevel);
      thisLevel.setName(new ConstString(name.toString()));
    }

    if (levels.contains(AnySelection.modelSort__))
    {
      // TODO: Are levels-specified sorts applicable if we are a TreeTable?
      Any paths = levels.get(AnySelection.modelSort__);
      thisLevel.setComparator(AnySortView.makeComparator(paths, contextNode));
    }

    if (levels.contains(columns__))
    {
      Map colOverride = (Map)levels.get(columns__);
      Array overrides = processColumnOverrides(colOverride, contextNode, table);
      thisLevel.setColumns(overrides, table.getModel());
    }

    if (exp != null)
    {
      for (int i = 0; i < exp.entries(); i++)
      {
        Map exmap = (Map)exp.getByVector(i);
        processExpansion(thisLevel, exmap, contextNode, table, namedLevels);
      }
    }

    return thisLevel;
  }

  static private void processExpansion(TreeLevel outer,
                                       Map       exSpec,
                                       Any       contextNode,
                                       AnyTable  table,
                                       Map       namedLevels)
  {
    if (!exSpec.contains(AnyComponent.renderInfo__))
      throw new AnyRuntimeException("An expansion must have rendering information");

    if (!exSpec.contains(level__))
      throw new AnyRuntimeException("An expansion must have a level root path");

    Any n = exSpec.get(level__);
    Any r = exSpec.get(AnyComponent.renderInfo__);

    if (!(n instanceof NodeSpecification))
      throw new AnyRuntimeException("Tree level root must be a node reference");
    if (!(r instanceof RenderInfo))
      throw new AnyRuntimeException("Tree level rendering must be a RenderInfo");

    TreeLevel nextLevel = null;
    if (exSpec.contains(nextLevel__))
    {
      Map m = (Map)exSpec.get(nextLevel__);
      nextLevel = processTreeLevels(m, contextNode, table, namedLevels);
    }

    Locate l = new LocateNode(((NodeSpecification)n).resolveIndirections
                                                      (contextNode,
                                                       Globals.process__.getTransaction()));

    TreeNodeExpansion e = outer.addExpansion((RenderInfo)r, l, nextLevel);

    if (exSpec.contains(columns__))
    {
      Map colOverride = (Map)exSpec.get(columns__);
      Array overrides = processColumnOverrides(colOverride, contextNode, table);
      e.setColumns(overrides, table.getModel());
    }
    
    if (exSpec.contains(closedIcon__))
    {
      e.setClosedIcon(exSpec.get(closedIcon__));
      // When a closed icon is specified and there is no open
      // icon reuse the closed icon.
      if (!exSpec.contains(openIcon__))
        e.setOpenIcon(exSpec.get(closedIcon__));
    }

    if (exSpec.contains(openIcon__))
      e.setOpenIcon(exSpec.get(openIcon__));

    if (exSpec.contains(leafIcon__))
      e.setLeafIcon(exSpec.get(leafIcon__));

    // Does this level have a name?
    if (exSpec.contains(name__))
    {
      Any name = exSpec.get(name__);
      namedLevels.add(name, e);
      //thisLevel.setName(name);
    }
  }
  
  static private Array processColumnOverrides(Map      colOverride,
                                              Any      contextNode,
                                              AnyTable table)
  {
    if (table == null)
      throw new AnyRuntimeException("Columns not allowed when not a TreeTable");

    Vectored tableCols = table.getColumns();
    if (tableCols == null)
      throw new AnyRuntimeException("No columns were specified in table");

    // Make a simple array for the columns, copy in the renderinfos
    // from the table and replace any named overrides with those
    // in the given Map.  This map doesn't have to be ordered because
    // the ordering is defined in the parent table.
    Array overrides = AbstractComposite.array(tableCols.entries());
    for (int j = 0; j < tableCols.entries(); j++)
      overrides.add(table.getColumnRenderInfo(j));
    
    Iter i = colOverride.createKeysIterator();
    while (i.hasNext())
    {
      Any colName = i.next();
      RenderInfo or = (RenderInfo)colOverride.get(colName);
      or.resolveNodeSpecs(contextNode);
      int idx = table.getColumnIndex(colName);
      //System.out.println("OVERRIDING " + colName + " AT INDEX " + idx);
      overrides.replaceItem(idx, or);
    }
    return overrides;
  }

  class TreeSelectionListener extends EventBinding
  {
    public TreeSelectionListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
			updateModel();
			return null;
		}
  }

  protected class TreeExpansionListener extends EventBinding
  {
    public TreeExpansionListener()
    {
      super(treeExpansionEventTypes__, false);
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      //System.out.println("EXPANSION " + e.getId());

      // We keep track of the expansion state in the AnyTreeNode
      // hierarchy. See also AnyTable.TreeExpansionListener
      AnyTreeNode node = (AnyTreeNode)e.get(AnyTreeLevel.node__);
      Array       path = (Array)e.get(AnyComponent.path__);
      node.setExpanded(e.getId().equals(EventConstants.E_TREEEXPANDED));
      //System.out.println("TreeExpansionListener NODE " + node);
      //System.out.println("TreeExpansionListener PATH " + path);

      if (path.entries() > 1)
      {
        // Step down the path from the current root in case there are
        // a new set of tree node objects in the model.
        // TODO: is this necessary as AnyTreeNode.setExpanded sets the
        // expanded flag back to the root? Check.
        node = (AnyTreeNode)model_.getRoot();
        Any lookFor;
        int max = path.entries();
        for (int i = 1; i < max; i++)
        {
          lookFor = ((AnyTreeNode)path.get(i)).getAny();
          node = node.findNode(lookFor);
        }
        node.setExpanded(e.getId().equals(EventConstants.E_TREEEXPANDED));
      }

      return null;
    }
  }

  // A data event listener that will dispatch events pertinent
  // to a given tree level.
//	class TreeLevelListener extends DataListener
//	{
//		private TreeLevel       thisLevel_;
//		private EventDispatcher levelDispatcher_;
//
//		TreeLevelListener(TreeLevel t, Map nodeSpecs)
//		{
//			// Use base class to load ourselves with the desired
//			// event types at this tree level
//			super(nodeSpecs);
//
//			// remember the level
//			thisLevel_ = t;
//
//			// set up the dispatcher to filter the events for us
//			levelDispatcher_ = new EventDispatcher();
//			levelDispatcher_.addEventListener(this);
//		}
//
//		public void processEvent(Event e) throws AnyException
//		{
//			// When we get an event here we know its for us to
//			// process
//			//thisLevel_.translateEvent(e);
//		}
//
//		EventDispatcher getEventDispatcher()
//		{
//			return levelDispatcher_;
//		}
//	}
}
