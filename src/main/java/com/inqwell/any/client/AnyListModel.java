/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyListModel.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-05-02 20:14:16 $
 */
package com.inqwell.any.client;

import java.util.Arrays;

import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.OrderComparator;
import com.inqwell.any.Orderable;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.SwingInvoker;

/**
 * A model for all swing list-based components.
 * <p>
 * This class implements all list models defined in swing, the
 * most complex being <code>javax.swing.MutableComboBoxModel</code>.
 * In this way this class can operate as the model for any
 * of the list component types.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 * @see com.inqwell.any.LocateNode
 */
public class AnyListModel extends    AbstractListModel
													implements MutableComboBoxModel
{
	// If the model data is being provided externally (for example
	// as a list chooser for a dynamic data set) then this will
	// be set.  If the model data is generated from configuration
	// data (for example an enum list in a BOT specification)
	// then this will be null initially.
	private Any	       modelRootExpr_;

	private Map	       modelRootM_; // not used directly - see resolveDataNode()

	// The node from which the modelRootExpr_ will be evaluated
  private Any        context_;

  private RenderInfo renderInfo_;
  private RenderInfo itemRenderInfo_;

  // For the list model (awt)...
  private Object     selectedItem_;

  // Set to true if the list model includes a null item for
  // user selection.  This will always be the item at index 0.
  private boolean    allowsNull_;
  
	private int        lastSerialNumber_ = -1;

	private int        visibleRows_ = 10;
	private Any        protoListValue_;

	private Map        nullListValue_ = defaultNullValue__;
  
	private OrderComparator origComparator_; // for property sort

  static private Map defaultNullValue__;
  static private Map blankValue__;
  
  static public Array     listKeys__;
  static
  {
    defaultNullValue__ = new ListRenderInfo.ListItemMap();
    defaultNullValue__.add(ListRenderInfo.internal__, null);
    defaultNullValue__.add(ListRenderInfo.external__, new ConstString("<any>"));

    blankValue__ = new ListRenderInfo.ListItemMap();
    blankValue__.add(ListRenderInfo.internal__, null);
    blankValue__.add(ListRenderInfo.external__, AnyString.EMPTY);
    
    listKeys__ = AbstractComposite.array();
    listKeys__.add(ListRenderInfo.internal__);
    listKeys__.add(ListRenderInfo.external__);
  }
  
	public AnyListModel(Any modelRoot)
	{
		modelRootExpr_ = modelRoot;
	}

	/**
	 * Create an <code>AnyListModel</code> from the given RenderInfo.
	 * The <code>RenderInfo</code> must refer to an enumerated value.
	 * <p>
	 * This constructor is used when setting up a ComboBox editor
	 * for table cells.
	 */
	public AnyListModel(RenderInfo r)
	{
		setRenderInfo(r);
	}

	public void setRenderInfo(RenderInfo r)
	{
		// This method (can be called from BML) gives us an opportunity
		// to set up the model root expression if there isn't one already.

		renderInfo_ = r;

		if (modelRootExpr_ == null && r.isEnum())
		{
			rebuildEnums(r);
		}
		
    if (protoListValue_ == null)
    {
      char[] nullListValue = new char[r.getWidth()];
      Arrays.fill(nullListValue, 'm');
      protoListValue_ = new ConstString(new String(nullListValue));
    }
	}
  
  public void setPrototypeDisplayValue(Any a)
  {
    protoListValue_ = a;
  }

  public Any getPrototypeDisplayValue()
  {
    return protoListValue_;
  }

	public RenderInfo getRenderInfo()
	{
		return renderInfo_;
	}
  
  public Map getNullListValue()
  {
    return nullListValue_;
  }

  public RenderInfo getItemRenderInfo()
  {
  	return itemRenderInfo_;
  }
  
  public void setItemRenderInfo(RenderInfo r)
  {
  	itemRenderInfo_ = r;
  }
  
  public void setModelRoot(Any newRoot) throws AnyException
  {
    modelRootExpr_ = newRoot;
		processReplace(null);
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
  
  public void setShowNull(boolean showNull)
  {
    allowsNull_ = showNull;
    fireContentsChanged(this, 0, 0);
  }
  
  public boolean isShowNull()
  {
    return allowsNull_;
  }
  
  public void setNullText(Any text)
  {
    // This is because the string representation of null is morphed
    // into AnyNull by the assignment operator, via which properties
    // are set in script.  If this is not good enough then revisit later.
    if (AnyNull.isNullInstance(text))
    {
      nullListValue_ = defaultNullValue__;
      setShowNull(false);
    }
    else
    {
      if (nullListValue_ == defaultNullValue__)
        nullListValue_ = (Map)nullListValue_.cloneAny();
      
      nullListValue_.replaceItem(ListRenderInfo.external__, text);
      setShowNull(true);
    }
  }

  /**
   * Establish the necessary selection intervals to
   * select the given items in the list model.  The
   * <code>Array</code> is assumed to contain
   * values which represent the internal list
   * model items.
   */
  public void setItemSelection(ListSelectionModel l,
                               Array              selection,
                               Map                newSelection)
  {
 		// Easiest way to do this is to loop
 		// over the list and check each item for
 		// existence in the selection array
 		
 		int size = this.getRealSize();
 		Array s = (Array)selection.shallowCopyOf();
 		
 		for (int i = 0; i < size; i++)
 		{
      Any a = getRowKey(i);
 			int index = s.indexOf(a);
 			if (index >= 0)
 			{
 				l.addSelectionInterval(i, i);
 				s.remove(index);
 				if (s.entries() == 0)
 				  break;
 			}
 		}
  }
  	
	// From ListModel
	public Object getElementAt(int index)
	{
		//System.out.println("AnyListModel.getElementAt: " + index);
    Any listValue = allowsNull_ ? nullListValue_ : blankValue__ ;

    if (allowsNull_ && index == 0)
      return listValue;

		try
		{
			Vectored v = (Vectored)resolveDataNode(context_, false);

			if (v != null)
			{
				Any listItem = v.getByVector((allowsNull_) ? index-1 : index);
				//System.out.println("AnyListModel.getElementAt listItem: " + listItem);

				// always re-evaluate the data node as the RenderInfo
				// object is used on a per-list-item basis.
				listValue = renderInfo_.resolveDataNode(listItem, true);
				//System.out.println("AnyListModel.getElementAt : " + index + " returning " + listValue);
			}
		}
		catch (AnyException e)
		{
			//System.out.println("AnyListModel.getElementAt: caught exception");
			e.printStackTrace();
		}

		//System.out.println("AnyListModel.getElementAt:" + index + ": " + listValue);

    return listValue;
	}
  
  public Any getExternalElementAt(int index)
  {
    Map m = (Map)getElementAt(index);
    Any ret = m.get(ListRenderInfo.external__);
    return (ret != null) ? ret
                         : m.get(ListRenderInfo.internal__);
  }

  // Different to getElementAt in that this method returns the row root
  // for the specified item and not the resolved data node from the
  // from the renderinfo (typically internal/external values)
	public Any getItemAt(int index)
	{
		Any listValue = allowsNull_ ? nullListValue_ : blankValue__ ;
    
    if (allowsNull_ && index == 0)
      return listValue;

		try
		{
			Vectored v = (Vectored)resolveDataNode(context_, false);

			if (v != null)
			{
				listValue = v.getByVector((allowsNull_) ? index-1 : index);
			}
		}
		catch (AnyException e)
		{
			//System.out.println("AnyListModel.getElementAt: caught exception");
			throw new RuntimeContainedException(e);
		}

		//System.out.println("AnyListModel.getElementAt:" + index + ": " + listValue);

    return listValue;
	}

  public Any getRowKey(int row)
  {
    Any rowKey = null;
    
    if (allowsNull_ && row == 0)
      return null;
    
    if (allowsNull_)
      row--;
    
    try
    {
      Vectored v = (Vectored)resolveDataNode(context_, false);

      if (v != null)
      {
        rowKey = v.getKeyOfVector(row);
      }
    }
    catch (AnyException e)
    {
      //System.out.println("AnyTableModel.getRowAt: caught exception " + row);
      e.printStackTrace();
    }
    return rowKey;
  }

	public int getSize()
	{
		try
		{
			Map dataNode = resolveDataNode(context_, false);
      int ret;
      if (dataNode != null)
        ret = dataNode.entries() + ((allowsNull_) ? 1 : 0);
      else
        ret = (allowsNull_) ? 1 : visibleRows_;
			return ret;
		}
		catch (AnyException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	public int getRealSize()
	{
		try
		{
			Map dataNode = resolveDataNode(context_, false);
			int ret = (dataNode != null) ? dataNode.entries() : 0;
      if (allowsNull_)
        ret++;
      return ret;
		}
		catch (AnyException e)
		{
			throw new RuntimeContainedException(e);
		}
	}
  
  /**
   * Return <code>true</code> if the model contains the specified
   * item <code>false</code>otherwise.
   */
  public boolean contains(Any v)
  {
    Any iv = v;
    
		if (v instanceof Map)
		{
			Map m = (Map)v;
			if (m.hasKeys(listKeys__))
				iv = m.get(ListRenderInfo.internal__);
		}
    
    if (iv == null)
    {
      return allowsNull_;
    }
    
    int s = this.getRealSize();
    for (int i = 0; i < s; i++)
    {
      Map m = (Map)this.getElementAt(i);
      if (iv.equals(m.get(ListRenderInfo.internal__)))
        return true;
    }
    return false;
  }

	// From ComboBoxModel
	public Object getSelectedItem()
	{
		//System.out.println ("--------AnyListModel.getSelectedItem " + selectedItem_ + " " + this +
												//" " + System.identityHashCode(selectedItem_));
		//System.out.println ("--------AnyListModel.getSelectedItem " + modelRootM_);

		//System.out.println ("--------AnyListModel.getSelectedItem " + selectedItem_);
		return selectedItem_;
	}

  public Any getExternalForItem(Object item)
  {
    Map m = (Map)item;
    Any ret = m.get(ListRenderInfo.external__);
    return (ret != null) ? ret
                         : m.get(ListRenderInfo.internal__);
  }
  
	public void setSelectedItem(Object anItem)
	{
    //System.out.println("AnyListModel.setSelectedItem " + anItem);
		//System.out.println ("--------AnyListModel.setSelectedItem " + anItem +  " " + anItem.getClass());
		int entries = getRealSize();

		Any selectedItem = null;
		Any iv = (Any)anItem;

		if (anItem instanceof Map)
		{
			Map m = (Map)anItem;
			if (m.hasKeys(listKeys__))
				iv = m.get(ListRenderInfo.internal__);
		}
    //System.out.println("AnyListModel.setSelectedItem iv " + iv);
    //System.out.println("AnyListModel.setSelectedItem entries " + entries);

		for (int i = 0; i < entries; i++)
		{
			Map m = (Map)getElementAt(i);
			Any a = m.get(ListRenderInfo.internal__);
      //System.out.println("AnyListModel.setSelectedItem a " + a);
      if (iv == null)
      {
        if (a == null)
        {
          selectedItem = m;
          break;
        }
        continue;
      }
      
			if (iv.equals(a))
			{
				selectedItem = m;
				//System.out.println ("--------AnyListModel.setSelectedItem selected" + m +
				//" " + System.identityHashCode(selectedItem));
				break;
			}
		}

    //System.out.println("AnyListModel.setSelectedItem " + selectedItem);
    if (selectedItem    == null &&
        itemRenderInfo_ != null &&
        itemRenderInfo_.isEditable())
    {
    	selectedItem = (Any)anItem;
    }
    
    if (selectedItem == null)
    {
      // If we still cannot determine the item then may be we are
      // autocompleting the null item...
      if (isShowNull())
        selectedItem = nullListValue_;
        
      //System.out.println("AnyListModel.setSelectedItem " + selectedItem);
      //System.out.println("AnyListModel  anItem " + anItem);
      //AbstractAny.stackTrace();
    }
    
		selectedItem_ = selectedItem;
    
		fireContentsChanged(this, -1, -1);
	}

	// From MutableComboBoxModel

	/**
	 * The object must be a <code>Map<code> whose unique key is
	 * suitable as the key to add the object to the model root.
	 * Standard terms and conditions for an <code>Any</code>
	 * structure.
	 */
	public void addElement(Object obj)
	{
		Map m = (Map)obj;

		try
		{
			Map modelRoot = resolveDataNode(context_, false);

			modelRoot.add(m.getUniqueKey(), m);
		}
		catch (AnyException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This is unsupported.  Better to just add the element and resort
	 */
	public void insertElementAt(Object obj, int index)
	{
		throw new UnsupportedOperationException("AnyListModel.insertElementAt()");
	}

	public void removeElement(Object obj)
	{
		Map m = (Map)obj;

		try
		{
			Map modelRoot = resolveDataNode(context_, false);
			modelRoot.remove(m.getUniqueKey());
		}
		catch (AnyException e)
		{
			e.printStackTrace();
		}

	}

	public void removeElementAt(int index)
	{
		try
		{
			Vectored modelRoot = (Vectored)resolveDataNode(context_, false);
			modelRoot.removeByVector(index);
		}
		catch (AnyException e)
		{
			e.printStackTrace();
		}
	}

	public void setVisibleRows(int visibleRows)
	{
		visibleRows_ = visibleRows;
		if (visibleRows_ == 0)
			visibleRows_ = 10;
	}

  public void translateEvent(Event e) throws AnyException
  {
  	int serialNumber = e.getSerialNumber();
  	
		Map id = (Map)e.getId();

		Any eventType = id.get(EventConstants.EVENT_TYPE);

    if (eventType.equals(EventConstants.BOT_UPDATE) ||
        eventType.equals(EventConstants.BOT_DELETE))
		{
			processUpdate(id);
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
        return;
      
      lastSerialNumber_ = serialNumber;
      
			processAdd(id);
		}
		else if (eventType.equals(EventConstants.NODE_REMOVED) ||
						 eventType.equals(EventConstants.NODE_REMOVED_CHILD))
		{
      if (serialNumber >= 0 &&
          serialNumber == lastSerialNumber_)
        return;
      
      lastSerialNumber_ = serialNumber;
      
			processRemove(id);
		}
		else if (eventType.equals(EventConstants.NODE_REPLACED) ||
						 eventType.equals(EventConstants.NODE_REPLACED_CHILD))
		{
      if (serialNumber >= 0 &&
          serialNumber == lastSerialNumber_)
        return;
      
      lastSerialNumber_ = serialNumber;
      
			processReplace(id);
		}
	}

  void sort (OrderComparator oc)
  {
    try
    {
      origComparator_ = oc;
      
      if (oc == null)
        return;
        
      Orderable o = (Orderable)resolveDataNode(context_, false);
      if (o == null)
        return;
      
      oc.setToOrder(resolveDataNode(context_, false));
      oc.setTransaction(Globals.process__.getTransaction());
      //AbstractComposite.sortOrderable(o, oc);
      o.sort(oc);
      // Leave the comparator in the collection to maintain
      // sorting after insertion
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
	private void processReplace(Map id) throws AnyException
	{
    resolveDataNode(context_, true);
    sort(origComparator_);
    fireContentsChanged(this,
                        -1,
                        -1);
	}

	private void processUpdate(Map id)
	{
		// First find the row number.
		int row = findRowNumber(id, true, false);

    fireContentsChanged(this, row, row);
	}

	private void processRemove(Map id) throws AnyException
	{
    int row = findRowNumber(id, false, true);

    fireContentsChanged(this, row, row);
	}

	private void processAdd(Map id) throws AnyException
	{
    int row = findRowNumber(id, false, true);

    if (row < 0)
      sort(origComparator_);

    fireContentsChanged(this, row, row);
	}

	NodeSpecification getRootPath()
	{
		NodeSpecification root = null;
		if (modelRootExpr_ instanceof Locate)
		{
			Locate l = (Locate)modelRootExpr_;
			root = l.getNodePath();
		}
		return root;
	}

  Map resolveDataNode(Any root, boolean force) throws AnyException
	{
		//System.out.println ("AnyListModel.resolveDataNode : " + root);
		Map dataNode = modelRootM_;

		if (dataNode == null || force)
		{
			if (modelRootExpr_ != null)
			{
				dataNode = (Map)EvalExpr.evalFunc(Globals.process__.getTransaction(),
																					root,
																					modelRootExpr_);
			}
			else
			{
				dataNode = rebuildEnums(renderInfo_);
			}
			modelRootM_ = dataNode;
		}

		return dataNode;
	}

	Map newSelection(ListSelectionModel lm,
                   Map                selection,
                   Array              keySelection,
                   Array              indexSelection,
                   IntI               selectCount)
	{
		int minI = lm.getMinSelectionIndex();
		int maxI = lm.getMaxSelectionIndex();
    
    int count = 0;

    selection.empty();
    selectCount.setValue(0);

		if (keySelection != null)
      keySelection.empty();

		if (minI >= 0)
		{
			for (int i = minI; i <= maxI; i++)
			{
				if (lm.isSelectedIndex(i))
				{
				  if (i == 0 && allowsNull_)
				    continue;
				  
          Any row = getItemAt(i);
          if (row != null)
          {
            Any k   = getRowKey(i);
            
  					selection.add(k, row);
            count++;
  
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
		
		return selection;
	}

	void setContext(Any context)
	{
    Any oldContext = context_;
		context_ = context;

    if (oldContext != null) // safeguard during primordial phase
    {
      try
      {
        processReplace(null);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);	
      }
    }
	}

  Any getContext()
	{
		return context_;
	}

	private Map rebuildEnums(RenderInfo r)
	{
		// Generate the enums allowed for the
		// prevailing BOT/field

		Descriptor d = r.getDescriptor();

		// Transform into a suitable model-style structure
		Map enumModel = AbstractComposite.orderedMap();
		Map enumVals = (Map)d.getEnums().get(r.getField());
		Iter i = enumVals.createKeysIterator();
		while (i.hasNext())
		{
			Any intVal = i.next();
			Any extVal = enumVals.get(intVal);
			Map enumSingle = AbstractComposite.simpleMap();
			enumSingle.add(ListRenderInfo.internal__, intVal);
			enumSingle.add(ListRenderInfo.external__, extVal);
			//System.out.println("AnyListModel.rebuildEnums() " + enumSingle + " " + System.identityHashCode(enumSingle));
			enumModel.add(intVal, enumSingle);
		}
		modelRootM_ = enumModel;
		//System.out.println("AnyListModel.rebuildEnums() " + enumModel);
		return enumModel;
	}

	private int findRowNumber(Map id, boolean isRow, boolean force)
	{
		Map modelRoot = null;
		try
		{
		  modelRoot = resolveDataNode(context_, force);
		  if (modelRoot == null)
		    return -1;
		}
		catch(AnyException e)
		{
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
			return -1;
	  }

	  int ret = v.indexOf(i.next());

	  return ret;
	}
}
