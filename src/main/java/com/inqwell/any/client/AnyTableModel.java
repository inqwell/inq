/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTableModel.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-05-02 20:18:18 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.SelectionF;
import com.inqwell.any.client.swing.JTable;
import com.inqwell.any.client.swing.TableModel;
import com.inqwell.any.client.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreeModel;
import java.util.ArrayList;

/**
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.8 $
 * @see com.inqwell.any.LocateNode
 */
public class AnyTableModel extends    CommonTableModel
                           implements TableModel
{
	private Any	      modelRootExpr_;
	private Map	      modelRootM_;  // not used directly - see resolveDataNode()

	// Whether new rows can be added to this model
	//private boolean   expandable_ = false;

  private Any       context_;

  // Vector of RenderInfo objects describing columns
	private Array     colInfo_;

  private int       visibleRows_ = 10;

	private ArrayList editors_; // because it doesn't have to hold Anys

  // Although it wouldn't normally be the case that a reference to
  // the view (AnyTable) would be held within the model (this)
  // because the relationship is normally one way (and possibly
  // many to one) in the Inq case the model is only ever used by
  // one AnyTable.  The model's configuration, and possibly the
  // nodes that are resolved, are the true model in the conventional
  // sense.
  //private AnyTable  table_;

	private int             lastSerialNumber_ = -1;

	private OrderComparator sortComparator_; // for column click sort
	private OrderComparator origComparator_; // for property sort

	/**
	 *
	 */
	public AnyTableModel(Any modelRoot)
	{
		modelRootExpr_  = modelRoot;
		colInfo_        = AbstractComposite.array();
	}

	/**
	 *
	 * @return the number of rows
	 */
	public int getRowCount()
	{
		try
		{
			Map dataNode = resolveDataNode(context_, false);
			return (dataNode != null) ? dataNode.entries() : visibleRows_;
		}
		catch (AnyException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	public int getRealRowCount()
	{
		try
		{
			Map dataNode = resolveDataNode(context_, false);
			return (dataNode != null) ? dataNode.entries() : 0;
		}
		catch (AnyException e)
		{
			throw new RuntimeContainedException(e);
		}
	}

	/**
	 * Get the number of columns which is in fact the number that have
	 * been added.
	 * @see AnyTableModel#addColInfo()
	 * @return the number of cols
	 */
	public int getColumnCount()
	{
		int count = colInfo_.entries();
//		System.out.println("AnyTableModel.getColumnCount: " + count);
    return count;
	}

  public void setModelRoot(Any newRoot, JTable t) throws AnyException
  {
    modelRootExpr_ = newRoot;
		processReplace(null, t);
  }

  public Any getModelRoot()
  {
    Any ret = null;
    if (modelRootExpr_ != null)
    {
      Locate l = (Locate)modelRootExpr_;
      ret = l.getNodePath();
    }
    return ret;
  }

  public void setColumns(Vectored columns)
  {
  	colInfo_.empty();

    // chuck away the editors as well
    // Hmmm may be columns config should really be in the AnyTable
    // to make it easier to manage in the face of dynamic
    // switching between flat/tree cases.  At the moment, doing this
    // calls setColumns here and we don't really want to chuck
    // away the editors.  We went to so much trouble to store them
    // separately from the JTable's TableColumn instances, which
    // it chucks away when the model changes.... TBD
    editors_ = null;

  	for (int i = 0; i < columns.entries(); i++)
  	{
  	  RenderInfo r = (RenderInfo)columns.getByVector(i);
  	  colInfo_.add(new AnyCellRenderer(r));
  	  r.resolveNodeSpecs(getContext());
    }

  	fireTableStructureChanged();
  }

  public Vectored getColumns()
  {
    return (Vectored)colInfo_;
  }

  public Map getLevels()
  {
    throw new UnsupportedOperationException("Not a TreeTable");
  }

	/**
	 * Get the column name as stored in the AnyTableColumn Vector
	 */
	public String getColumnName(int c)
	{
		RenderInfo r = getRenderInfo(c);
//		System.out.println("AnyTableModel.getColumnName:" + c + ": " + r.getLabel());
		return r.getLabel();
	}

	/**
	 * Return the column class.  From <code>TableModel</code>.  This one's
	 * a blast - always returns <code>Any.class</code>!!
	 */
	public Class getColumnClass(int columnIndex)
	{
		return Any.class;
	}

	/**
	 * Get the value of the <code>Object</code> at
	 * <code>row</code>'th row and <code>col</code>'th column.
	 * <b>Note</b> - the <code>JTable</code> uses the <code>DefaultTableCellRenderer</code>
	 * which just calls <code>Object.toString()</code> on the value returned.
	 * @return the Object value else null
	 */
	public Object getValueAt(int row, int column)
	{
    // One of a number of rather unfortunate entry points into
    // the Inq code from the Java graphics subsystem.  This method
    // can be called from the repaint manager and we would like
    // to have the context set to support $context in rendering
    // expressions. A stack dump vindicates that this is the earliest
    // place where the context is known and can be established.
    // It will be undone in the EventQueue.
    if (Globals.process__.getContext() == null)
      Globals.process__.setContext((Map)context_);

		Any cellValue = AnyCellRenderer.null__;
		if (row < getRowCount() && column < getColumnCount())
		{
			try
			{
				RenderInfo r = getRenderInfo(column);
				Vectored v = (Vectored)resolveDataNode(context_, false);

				if (v != null)
				{
					Any rowRoot = v.getByVector(row);

					// always re-evaluate the data node as the RenderInfo
					// object is used on a per-column basis.
					cellValue = r.resolveDataNode(rowRoot, true);
				}
			}
			catch (AnyException e)
			{
        throw new RuntimeContainedException(e);
				//System.out.println("AnyTableModel.getValueAt: caught exception");
				//e.printStackTrace();
				//cellValue = AnyRenderer.null__;
			}
		}

		//System.out.println("AnyTableModel.getValueAt:" + row + ":" + column + ": " + cellValue);

    return cellValue;
  }

	public Any getRowAt(int row)
	{
		Any rowRoot = null;
		if (row < getRowCount())
		{
			try
			{
				Vectored v = (Vectored)resolveDataNode(context_, false);

				if (v != null)
				{
					rowRoot = v.getByVector(row);
				}
			}
			catch (AnyException e)
			{
				//System.out.println("AnyTableModel.getRowAt: caught exception " + row);
				e.printStackTrace();
			}
		}
		return rowRoot;
	}

	public Any getRowKey(int row)
	{
		Any rowKey = null;
		if (row < getRealRowCount())
		{
			try
			{
				Vectored v = (Vectored)resolveDataNode(context_, false);

				if (v != null)
				{
          rowKey = v.getKeyOfVector(row);
//					Composite rowRoot = (Composite)v.getByVector(row);
//					rowKey = rowRoot.getNameInParent();
				}
			}
			catch (AnyException e)
			{
				throw new RuntimeContainedException(e);
			}
		}
		return rowKey;
	}

	public void setValueAt(Object value, int row, int column)
	{
		Any sv = (Any)value;

		//System.out.println ("AnyTableModel.setValueAt() " + value + " " + ((value != null) ? value.getClass().toString() : null) + " " + row + ", " + column);

		Any vs = null;
		//Any rk = null;

		if (row < getRowCount() && column < getColumnCount())
		{
			try
			{
				RenderInfo r = getRenderInfo(column);
				Vectored v = (Vectored)resolveDataNode(context_, false);

				if (v != null)
				{
					Composite rowRoot = (Composite)v.getByVector(row);

					//rk = rowRoot.getNameInParent();

					// always re-evaluate the data node as the RenderInfo
					// object is used on a per-column basis.
		      vs = r.resolveResponsibleData(rowRoot);
				}
			}
			catch (AnyException e)
			{
				//System.out.println("AnyTableModel.setValueAt: caught exception");
				e.printStackTrace();
				vs = null;
			}
		}

		if (vs == null)
			return;


		vs.copyFrom(sv);
		//addToEdited(rk);
		//fireTableCellUpdated(row, column);
	}

  /**
   * If we have a designated item of data that we are
   * responsible for then return the cell value for it.
   * Otherwise just return the cell value of the
   * rendering expression
   */
	public Any getResponsibleValueAt(int row, int column)
	{
		Any cellValue = AnyCellRenderer.null__;
		if (row < getRowCount() && column < getColumnCount())
		{
			try
			{
				RenderInfo r = getRenderInfo(column);
				Vectored v = (Vectored)resolveDataNode(context_, false);

				if (v != null)
				{
					Any rowRoot = v.getByVector(row);

					cellValue = r.resolveResponsibleData(rowRoot);
				}
			}
			catch (AnyException e)
			{
				//System.out.println("AnyTableModel.getResponsibleValueAt: caught exception");
				e.printStackTrace();
				cellValue = AnyCellRenderer.null__;
			}
		}

    return cellValue;
	}

	public boolean isCellEditable(int rowIndex,
																int columnIndex)
	{
    // For the flat table, just use the renderinfo's editable
    // flag for the column.  It should only be set to true when
    // the AnyComponentEditor has been set on the column and
    // contains a component to handle the editing.
		RenderInfo r = getRenderInfo(columnIndex);
    boolean ret = r.isEditable();
    AnyComponentEditor ace = (AnyComponentEditor)this.getCellEditor
                                            (rowIndex, columnIndex);

		return ret && (ace != null) && (ace.getComponent() != null);
	}

	/**
	 * Returns <code>true</code> if this model can build cells on demand,
	 * rather than just being able to render a given node structure.
	 * <p>
	 * Whether this table model can build cells depends on whether the
	 * underlying rendering information has both a path and a data type.
	 */
	public boolean isBuildable()
	{
		for (int i = 0; i < getColumnCount(); i++)
		{
			if (!isBuildable(i))
				return false;
		}
		return true;
	}

	public boolean isBuildable(int column)
	{
		if (!(modelRootExpr_ instanceof Locate))
			return false;

		RenderInfo r = getRenderInfo(column);
		if (r.isBuildable())
			return true;
		else
			return false;
	}

	public Map insertRow(int row) throws AnyException
	{
		Vectored modelRoot = (Vectored)resolveDataNode(context_, false);
		if (modelRoot == null)
		{
			// There's no model at all so create its root node
      Transaction t = Globals.process__.getTransaction();
			Locate l = (Locate)modelRootExpr_;
			modelRoot = (Vectored)AbstractComposite.managedMap();
			BuildNodeMap b = new BuildNodeMap(l.getNodePath(), modelRoot, new InstanceHierarchyMap());
      b.setTransaction(t);
			b.exec(context_);
		}

		Map rowRoot = AbstractComposite.managedMap();

		modelRoot.addByVector(row, rowRoot);

		for (int i = 0; i < getColumnCount(); i++)
		{
			RenderInfo r = getRenderInfo(i);
			r.buildData(rowRoot);
		}

		return rowRoot;
	}

	public void removeRow(int row) throws AnyException
	{
		Vectored modelRoot = (Vectored)resolveDataNode(context_, false);
		modelRoot.removeByVector(row);
	}

	public void pasteCell(int     row,
												int     column,
												Any     toPaste) throws AnyException
	{
		Map        rowRoot   = (Map)getRowAt(row);
		RenderInfo r         = getRenderInfo(column);
		Any        cellValue = r.resolveResponsibleData(rowRoot);
		cellValue.copyFrom(toPaste);
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

  /*
	public void setModelVars(Map modelVars)
	{
    modelVars_ = modelVars;
	}

	public Map getModelVars()
	{
    return modelVars_;
	}
*/

  public Map getEventPaths()
  {
    Map nodeSpecs = AbstractComposite.simpleMap();

		NodeSpecification root = getRootPath();

		for (int i = 0; i < getColumnCount(); i++)
		{
			RenderInfo r = getRenderInfo(i);

			Map ns = r.getNodeSpecs();
      
      addNodeSpecs(nodeSpecs, ns, root);
		}
    
    NodeFunction nf = getRowNodeFunc();
    if (nf != null)
      addNodeSpecs(nodeSpecs, nf.getNodeSpecs(), root);

		if (root != null)
		{
  		// Add the model root (with no fields) to pick up events when
  		// the model is replaced.
  		nodeSpecs.add(root, AbstractComposite.fieldSet());
  
  		// Add the model root children (with no fields) to pick up events when
  		// node-set children are added/removed/replaced. We need to do this
      // because raising of "_CHILD" events is pruned at node-set roots.
  		root = (NodeSpecification)root.cloneAny();
  		root.add(NodeSpecification.strict__);
  		root.add(NodeSpecification.thisEquals__);
  		nodeSpecs.add(root, AbstractComposite.fieldSet());
  	}

    return nodeSpecs;
  }
  
  private void addNodeSpecs(Map nodeSpecs, Map these, NodeSpecification root)
  {
    Iter iter = these.createKeysIterator();
    while (iter.hasNext())
    {
      Any thisNs    = iter.next();
      Any fieldList = these.get(thisNs);

      if (root != null)
      {
        NodeSpecification fromRoot = (NodeSpecification)thisNs.cloneAny();
        // Rendering expression paths are applied to node-set children.
        fromRoot.addFirst(NodeSpecification.thisEquals__);
        for (int j = 0; j < root.entries(); j++)
        {
          fromRoot.addFirst(root.get(root.entries() - j - 1));
        }
        thisNs = fromRoot;
      }

      // If the node spec yielded no paths it will be
      // the same as the root.  Ignore as we add root
      // explicitly below
      //if (thisNs.equals(root))
      //  continue;

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

  // Determine the currently selected rows and represent these
  // in the supplied collections.
	public Map newSelection(ListSelectionModel lm,
                          Map                selection,
                          Array              keySelection,
                          Array              indexSelection,
                          IntI               selectCount)
	{
	  selection.empty();
	  selectCount.setValue(0);

	  if (keySelection != null)
	    keySelection.empty();
	  
	  if (indexSelection != null)
	    indexSelection.empty();
	  
	  if (getRealRowCount() == 0)
	    return null;
	  
	  int minI = lm.getMinSelectionIndex();
		int maxI = lm.getMaxSelectionIndex();

    int count = 0;

		if (minI >= 0)
		{
			for (int i = minI; i <= maxI; i++)
			{
				if (lm.isSelectedIndex(i))
				{
					Any row = getRowAt(i);
					if (row != null)
					{
					  count++;
            Any k = getRowKey(i);
						selection.add(k, row);

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
    
    return null;
	}

  /**
   * Establish the necessary selection intervals to
   * select the given items in the list model.  The
   * <code>Array</code> is assumed to contain the
   * unique keys of the child elements of the model
   * root.
   */
  public void setItemSelection(ListSelectionModel l,
                               Array selection,
                               Map   newSelection)
  {
 		// Easiest way to do this is to loop
 		// over the list and check each item for
 		// existence in the selection array

 		int size = this.getRealRowCount();
 		Array s = selection.shallowCopy();
    newSelection.empty();

 		for (int i = 0; i < size; i++)
 		{
      Any a = getRowKey(i);
 			int index = s.indexOf(a);
 			if (index >= 0)
 			{
 				l.addSelectionInterval(i, i);
 				s.remove(index);
 				//newSelection.add(a, m);  // If selection events go out then this not reqd?
        if (s.entries() == 0)
          break;
 			}
 		}
  }

  public void setIndexSelection(ListSelectionModel l,
                                Array selection,
                                Map   newSelection)
  {
    int size = selection.entries();
    AnyInt ii = new AnyInt();
    for (int i = 0; i < size; i++)
    {
      ii.copyFrom(selection.get(i));
      l.addSelectionInterval(ii.getValue(), ii.getValue());
    }
  }
  
	private NodeSpecification getRootPath()
	{
		NodeSpecification root = null;
		if (modelRootExpr_ instanceof Locate)
		{
			Locate l = (Locate)modelRootExpr_;
			root = l.getNodePath();
		}
		return root;
	}

  public boolean isTreeTable()
  {
    return false;
  }

  public TreeModel getTreeModel()
  {
    throw new UnsupportedOperationException("getTreeModel");
  }

  public TreeLevel getTreeLevel(int row)
  {
    throw new UnsupportedOperationException("getTreeRenderer");
  }

  public JTree getTreeRenderer()
  {
    throw new UnsupportedOperationException("getTreeRenderer");
  }

	// Package access - used by the Table component wrapper
  private Map resolveDataNode(Any root, boolean force) throws AnyException
	{
		//System.out.println ("AnyTableModel.resolveDataNode : " + root);
		Map dataNode = modelRootM_;

		if (dataNode == null || force)
		{
	    dataNode = (Map)EvalExpr.evalFunc(Globals.process__.getTransaction(),
																	      root,
																	      modelRootExpr_);
			modelRootM_ = dataNode;
		}

		return dataNode;
	}

	public RenderInfo getRenderInfo(int c)
	{
    AnyCellRenderer r = (AnyCellRenderer)colInfo_.get(c);
		return r.getRenderInfo();
	}

	/**
	 * Process a received event turning it into a JTable event
	 */
  public boolean translateEvent(Event e, JTable table, JTree tree) throws AnyException
  {
    // Special case to force table refresh. TODO: TreeTable
    if (e == null)
    {
      processReplace(null, table);
      return true;
    }
    
  	int serialNumber = e.getSerialNumber();
    boolean ret = false;

    //System.out.println("AnyTableModel.translateEvent " + e);
    
		Map id = (Map)e.getId();

		//System.out.println ("AnyTableModel translating " + id);
		Any eventType = id.get(EventConstants.EVENT_TYPE);

		if (eventType.equals(EventConstants.BOT_UPDATE) ||
		    eventType.equals(EventConstants.BOT_DELETE))
		{
			processUpdate(id, table);
		}
		else if (eventType.equals(EventConstants.NODE_ADDED) ||
						 eventType.equals(EventConstants.NODE_ADDED_CHILD))
		{
      // Inq NODE_ADDED/REMOVED/REPLACED can include a series
      // of _CHILD event types for sub-structure notification.
      // These are delivered from the top of the structure
      // downwards and we are only interested in the first
      // such event in a series.
      if (serialNumber >= 0 &&
          serialNumber == lastSerialNumber_)
        return ret;

      lastSerialNumber_ = serialNumber;

			ret = processAdd(id, table);
		}
		else if (eventType.equals(EventConstants.NODE_REMOVED) ||
						 eventType.equals(EventConstants.NODE_REMOVED_CHILD))
		{
      if (serialNumber >= 0 &&
          serialNumber == lastSerialNumber_)
        return ret;

      lastSerialNumber_ = serialNumber;

      processRemove(id, eventType, table);
		}
		else if (eventType.equals(EventConstants.NODE_REPLACED) ||
						 eventType.equals(EventConstants.NODE_REPLACED_CHILD))
		{
      if (serialNumber >= 0 &&
          serialNumber == lastSerialNumber_)
        return ret;

      lastSerialNumber_ = serialNumber;

      ret = processReplace(id, table);
		}
    
    return ret;
	}

	public void setContext(Any context)
	{
		context_ = context;
	}

  public Any getContext()
	{
		return context_;
	}

	public boolean sort(int[] orderItems, boolean isDescending, JTree tree) throws AnyException
	{
    if (orderItems == null)
    {
      sortComparator_ = null;
      
      // TODO: Should we check if there is an origComparator_ and
      // apply that if so?
      
      return false;
    }

    if (sortComparator_ == null)
    {
      sortComparator_ = new AnyComparator();
      sortComparator_.setTransaction(Globals.process__.getTransaction());
      // Interactive sorting ignores case
      sortComparator_.setIgnoreCase(true);
    }

		sortComparator_.setDescending(isDescending);
		sort(orderItems);
    fireTableDataChanged();
    
    return true;
	}

	private boolean sort(int[] orderItems) throws AnyException
	{
		Orderable o = (Orderable)resolveDataNode(context_, false);
		if (o == null)
		  return false;

    if (orderItems != null)
    {
      // Set up the functions that resolve the sort data
      // for the comparator.
      Array orderFuncs = AbstractComposite.array();
      for (int i = 0; i < orderItems.length; i++)
      {
        int col = orderItems[i];
        if (col < 0)
          break;
        RenderInfo r = getRenderInfo(col);
        orderFuncs.add(new ResolveSortData(r));
      }
      if (sortComparator_ == null)
      {
        sortComparator_ = new AnyComparator();
        sortComparator_.setTransaction(Globals.process__.getTransaction());
        sortComparator_.setIgnoreCase(true);
      }
      sortComparator_.setOrderBy(orderFuncs);
    }

		sortComparator_.setToOrder(resolveDataNode(context_, false));
		AbstractComposite.sortOrderable(o, sortComparator_);
		// Remove any comparator that may be in the orderable node to
		// force insertions to their designated index after a manual sort
		o.sort((OrderComparator)null);
    return true;
	}

  public boolean sort (OrderComparator oc)
  {
    try
    {
      origComparator_ = oc;

      if (oc == null)
        return false;

      Orderable o = (Orderable)resolveDataNode(context_, false);
      if (o == null)
        return false;


      oc.setToOrder(resolveDataNode(context_, false));
      oc.setTransaction(Globals.process__.getTransaction());
      o.sort(oc);
      // Leave the comparator in the collection to maintain
      // sorting after insertion
      fireTableDataChanged();
      
      return true;
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setTable(AnyTable table)
  {
    //table_ = table;
  }

  /**
   * Fetch the cell editor.  In this implementation the row
   * index is not used and editors are only configured by column.
   * @return the cell editor for the given column
   * or <code>null</code> if no editor has been configured.
   */
  public TableCellEditor getCellEditor(int row, int column)
  {
    if (editors_ == null)
      return null;

    TableCellEditor tce = (TableCellEditor)editors_.get(column);

    return tce;
  }

  /**
   * Set the cell editor.  In this implementation the row
   * index is not used and editors are only configured by column.
   */
  public void setCellEditor(TableCellEditor tce, int row, int column)
  {
    if (editors_ == null)
    {
      int cc = this.getColumnCount();
      editors_ = new ArrayList(cc);
      for (int i = 0; i < cc; i++)
        editors_.add(null);
    }
    editors_.set(column, tce);
  }

  /**
   * Fetch the cell renderer.  In this implementation the row
   * index is not used and renderers are only configured by column.
   * @return the cell renderer for the given column
   * or <code>null</code> if no renderer has been configured.
   */
  public TableCellRenderer getCellRenderer(int row, int column)
  {
    TableCellRenderer tcr = (TableCellRenderer)colInfo_.get(column);

    return tcr;
  }

//  public void setCellRenderer(TableCellRenderer r, int row, int column)
//  {
//    if (renderers_ == null)
//    {
//      int cc = this.getColumnCount();
//      renderers_ = new ArrayList(cc);
//      for (int i = 0; i < cc; i++)
//        renderers_.add(null);
//    }
//    renderers_.set(column, r);
//  }
  
	private boolean processReplace(Map id, JTable t) throws AnyException
	{
		int row = -1;
    
    if (id != null)
      row = findRowNumber(id, false, true);
    
    boolean ret = false;

		if (row < 0)
		{
		  if (t.isEditing())
		    t.resetEditor();
		  
      // apply any current sorting
      if (sortComparator_ != null)
      {
        sort((int[])null);  // apply current click sort
        //fireTableDataChanged();
      }
      else
        sort(origComparator_);
      
      if (getRowFunc() != null)
      {
        // Execute the row function for each row
        int rows = getRealRowCount();
        for (int i = 0; i < rows; i++)
        {
          Any rowRoot = getRowAt(i);
          AnyCellRenderer.callCellFunc(getRowFunc(),
                                       t.getAnyTable(),
                                       null,          // cellComponent
                                       null,          // renderinfo
                                       rowRoot,       // contextNode (NB $this)
                                       rowRoot,       // rowRoot
                                       null,          // oldValue
                                       null,          // newValue
                                       i,             // row
                                       getRowKey(i),
                                       -2,            // col
                                       null,          // colName
                                       false,         // mouseCell
                                       null,          // isUser
                                       null,          // after
                                       null,          // level
                                       null,          // isLeaf
                                       null);         // expanded
        }
      }
      
      //System.out.println ("AnyTableModel sort after replace");
      fireTableDataChanged();
      
      ret = true;
    }
	  else
    {
      if (getRowFunc() != null)
      {
        // Execute row function for this row
        Any rowRoot = getRowAt(row);
        AnyCellRenderer.callCellFunc(getRowFunc(),
                                     t.getAnyTable(),
                                     null,          // cellComponent
                                     null,          // renderinfo
                                     rowRoot,       // contextNode (NB $this)
                                     rowRoot,       // rowRoot
                                     null,          // oldValue
                                     null,          // newValue
                                     row,
                                     getRowKey(row),
                                     -2,            // col
                                     null,          // colName
                                     false,         // mouseCell
                                     null,          // isUser
                                     null,          // after
                                     null,          // level
                                     null,          // isLeaf
                                     null);         // expanded
      }

      fireTableRowsUpdated(row, row);
      
      ret = true;

      if (t.isEditing() && t.getEditingRow() == row)
        t.resetEditor();
    }

    return ret;
	}

	private boolean processAdd(Map id, JTable t) throws AnyException
	{
		int row = findRowNumber(id, false, true);
    boolean ret = false;
    
    //System.out.println ("AnyTableModel processAdd " + row);

		if (row < 0)
		{
      // apply any current sorting
      if (sortComparator_ != null)
      {
        sort((int[])null);  // apply current click sort
        //fireTableDataChanged();
      }
      else
        sort(origComparator_);

      if (getRowFunc() != null)
      {
        // Execute the row function for each row
        int rows = getRealRowCount();
        for (int i = 0; i < rows; i++)
        {
          Any rowRoot = getRowAt(i);
          AnyCellRenderer.callCellFunc(getRowFunc(),
                                       t.getAnyTable(),
                                       null,          // cellComponent
                                       null,          // renderinfo
                                       rowRoot,       // contextNode (NB $this)
                                       rowRoot,       // rowRoot
                                       null,          // oldValue
                                       null,          // newValue
                                       i,             // row
                                       getRowKey(i),
                                       -2,            // col
                                       null,          // colName
                                       false,         // mouseCell
                                       null,          // isUser
                                       null,          // after
                                       null,          // level
                                       null,          // isLeaf
                                       null);         // expanded
        }
      }
      
      //System.out.println ("AnyTableModel sort after add");
      fireTableDataChanged();

      ret = true;
    }
	  else
    {
      if (getRowFunc() != null)
      {
        // Execute row function for this row
        Any rowRoot = getRowAt(row);
        AnyCellRenderer.callCellFunc(getRowFunc(),
                                     t.getAnyTable(),
                                     null,          // cellComponent
                                     null,          // renderinfo
                                     rowRoot,       // contextNode (NB $this)
                                     rowRoot,       // rowRoot
                                     null,          // oldValue
                                     null,          // newValue
                                     row,
                                     getRowKey(row),
                                     -2,            // col
                                     null,          // colName
                                     false,         // mouseCell
                                     null,          // isUser
                                     null,          // after
                                     null,          // level
                                     null,          // isLeaf
                                     null);         // expanded
      }
      
	    fireTableRowsInserted(row, row);
    }
    return ret;
	}

	private void processRemove(Map id, Any eventType, JTable t) throws AnyException
	{
    int row = -1;

    if (eventType.equals(EventConstants.NODE_REMOVED))
    {
      Any vector = id.get(EventConstants.EVENT_VECTOR);
      if (vector instanceof IntI)
      {
        row = ((IntI)vector).getValue();
      }
    }
    else
    {
      row = findRowNumber(id, false, true);
    }

		if (row < 0)
		{
      resolveDataNode(context_, true);
		  fireTableDataChanged();
      if (t.isEditing())
        t.resetEditor();
    }
	  else
    {
      // Check if there are no rows left.  If so, fire a TableDataChanged
      // event.  Otherwise, if there is a selection Java doesn't clear it.
      if (getRowCount() == 0)
      {
        t.clearSelection();  // d'oh!
        fireTableDataChanged();
        //System.out.println ("AnyTableModel.processRemove() changed");
        if (t.isEditing())
          t.resetEditor();
      }
      else
      {
        fireTableRowsDeleted(row, row);
        //System.out.println ("AnyTableModel.processRemove() deleted");

        if (t.isEditing() && t.getEditingRow() == row)
          t.resetEditor();
      }
    }
	}

	private void processUpdate(Map id, JTable t)
	{
		// First find the row number.
		int row = findRowNumber(id, true, false);

		//System.out.println ("AnyTableModel.processUpdate() row is " + row);
		if (row < 0)
		  return;
    
    boolean fireRow     = t.isRowRefresh();
    boolean rowWillFire = fireRow;
    boolean rowFuncExec = false;

		// A single event will relate to one descriptor only
		Descriptor d = (Descriptor)id.get(Descriptor.descriptor__);
    //System.out.println("DDDDD " + d);

		// Process each field that has changed and determine the relevant
		// column.  Note that although an event delivered to us must,
		// at this stage, require some table processing, we might not
		// be rendering all the fields it indicates are changing.  This is
		// either because they are not displayed or because they relate
		// to a different descriptor.
	  Set fields = (Set)id.get(EventConstants.EVENT_FIELDS);
    //System.out.println("FFFFF " + fields);
	  Iter i = fields.createIterator();

	  while (i.hasNext())
	  {
	  	Any field = i.next();
      
      // If not yet done so, check if this field is being dispatched by (any)
      // row function.
      if (!rowFuncExec && isRowFuncDispatching(field))
      {
        fireRow     = true;
        rowWillFire = true;
        rowFuncExec = true;
        Any rowRoot = getRowAt(row);
        AnyCellRenderer.callCellFunc(getRowFunc(),
                                     t.getAnyTable(),
                                     null,          // cellComponent
                                     null,          // renderinfo
                                     rowRoot,       // contextNode (NB $this)
                                     rowRoot,       // rowRoot
                                     null,          // oldValue
                                     null,          // newValue
                                     row,
                                     getRowKey(row),
                                     -2,            // col
                                     null,          // colName
                                     false,         // mouseCell
                                     null,          // isUser
                                     null,          // after
                                     null,          // level
                                     null,          // isLeaf
                                     null);         // expanded
      }

	  	for (int col = 0; !rowWillFire && col < getColumnCount(); col++)
	  	{
	  		RenderInfo r = getRenderInfo(col);
        // The descriptor in the renderinfo may only be there to
        // infer width and formatting, and doesn't have to relate
        // to the item raising the event, [ er, so doesn't this apply
        // to the renderinfo's field as well?]
	  		//if (field.equals(r.getField()) && d.equals(r.getDescriptor()))
	  		//if (field.equals(r.getField()))
	  		if (r.isDispatching(field))
        {
          if (t.isEditing() &&
              t.getEditingRow() == row &&
              t.getEditingColumn() == col)
            t.resetEditor();

          //System.out.println("fireTableCellUpdated " + row + " " + col);
          fireTableCellUpdated(row, col);
          
          /*
          if (fireRow)
          {
            if (t.isEditing() &&
                t.getEditingRow() == row)
              t.resetEditor();
            
            fireTableRowsUpdated(row, row);
            break;
          }
          else
          {
            if (t.isEditing() &&
                t.getEditingRow() == row &&
                t.getEditingColumn() == col)
              t.resetEditor();
  
            //System.out.println("fireTableCellUpdated " + row + " " + col);
  	  		  fireTableCellUpdated(row, col);
          }
          */
        }
	  	}
	  }
    
    if (rowWillFire)
      fireTableRowsUpdated(row, row);
	}

	// Work out the row number the event originator corresponds to
	// i.e. perform map to index translation for the purposes of
	// interfacing with the table model.  If the isRow argument is
	// true then the originator must be a row root (rather than
	// anywhere further below)
	private int findRowNumber(Map id, boolean isRow, boolean force)
	{
		Map modelRoot = null;
		try
		{
		  modelRoot = resolveDataNode(context_, force);
		  if (modelRoot == null)
      {
        //System.out.println("AnyTableModel.findRowNumber 1");
		    return -1;
      }
		}
		catch(AnyException e)
		{
      //System.out.println("AnyTableModel.findRowNumber 2");
			return -1;
		}

    if (id == null)
    {
      //System.out.println("AnyTableModel.findRowNumber 3");
      return -1;
    }

		NodeSpecification path = (NodeSpecification)id.get(EventConstants.EVENT_PATH);

		// The event was delivered to context_ so we should be able
		// to navigate back down the path it contains until we reach
		// modelRoot_, which we treat as a Vectored.  Then the next
		// element of the path specification can be used to determine
		// the row number.

		Iter i     = path.createPathItemsIter();
		Map m      = (Map)context_;
		Vectored v = null;

		while (v == null && i.hasNext())
		{
			Any a = i.next();
			m = (Map)m.get(a);
			if (m == modelRoot)
				v = (Vectored)m;
		}

		if (v == null || !i.hasNext())
		{
			// something went wrong - we didn't find the model root
			// or we don't have a next member in the path to look up
			// the row number of
      //System.out.println("AnyTableModel.findRowNumber 4");
			return -1;
	  }

    Any k = i.next();
	  int ret = v.indexOf(k);

    // While we are about it, slip the next path spec as the
    // unique key of the row in the model root.  When new
    // nodes are added this is not guaranteed by the server.
    // TODO: is this still required?
    if (ret >= 0)
    {
      m = (Map)m.get(k);
      m.setUniqueKey(k);
    }

    //System.out.println("AnyTableModel.findRowNumber 5");
	  return ret;
	  //return (i.hasNext() && isRow) ? -1 : ret;
	}

  /*
	private void addToEdited(Any rk)
	{
		if (rk != null)
    {
      if (modelVars_.contains(SelectionF.editedKeySelection__))
      {
        Array editSel = (Array)modelVars_.get(SelectionF.editedKeySelection__);
        if (!editSel.contains(rk))
          editSel.add(rk);
      }
      else
      {
        Array editSel = AbstractComposite.array();
        editSel.add(rk);
        modelVars_.add(SelectionF.editedKeySelection__, editSel);
      }
    }
	}

	private void removeFromEdited(Any rk)
	{
		if (rk != null)
    {
      if (modelVars_.contains(SelectionF.editedKeySelection__))
      {
        Array editSel = (Array)modelVars_.get(SelectionF.editedKeySelection__);
        int indx = -1;
        if ((indx = editSel.indexOf(rk)) >= 0)
          editSel.remove(indx);
      }
    }
	}
	*/

	static class ResolveSortData extends AbstractFunc
	{
		private RenderInfo r_;

		ResolveSortData(RenderInfo r) { r_ = r; }

		public Any exec(Any a) throws AnyException
		{
			Any ret = r_.resolveDataNode(getTransaction().getLoop(), true);
      
      // Protect against data not resolving as AnyComparator now
      // croaks in this case.
      if (ret == null)
        ret = AnyNull.instance();
      
      return ret;
			//return r_.resolveResponsibleData(a);
		}
	}
}

