/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyListModel.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-18 21:45:00 $
 */
package com.inqwell.any.client;

import java.util.Arrays;

import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
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
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;
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
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.LocateNode
 */
public class AnyNewListModel extends    AbstractListModel
                          implements MutableComboBoxModel
{
  // If the model data is being provided externally (for example
  // as a list chooser for a dynamic data set) then this will
  // be set.  If the model data is generated from configuration
  // data (for example an enum list in a BOT specification)
  // then this will be null initially.
  private Any        modelRootExpr_;

  private Map        modelRootM_; // not used directly - see resolveDataNode()

  // The node from which the modelRootExpr_ will be evaluated
  private Any        context_;

  // The rendering information for the list items
  private RenderInfo renderInfo_;

  // For the list model (awt)...
  private Object     selectedItem_;

  // Set to true if the list model includes a null item for
  // user selection.  This will always be the item at index 0.
  private boolean      allowsNull_;
  
  public static Any internal__ = new ConstString ("internal");
  public static Any external__ = new ConstString ("external");

  private int                lastSerialNumber_ = -1;

  private Map                selection_;
  private ListSelectionModel selectionModel_;
  
  //private Map       itemCache_ = AbstractComposite.simpleMap();

  private int       visibleRows_ = 10;
  private Any       protoListValue_;

  private Any       nullListValue_ = defaultNullValue__;
  
  private OrderComparator origComparator_; // for property sort

  static private Any defaultNullValue__;
  static private Any blankValue__;
  
  static
  {
    defaultNullValue__ = new ConstString("<any>");
    blankValue__ = AnyString.EMPTY;
  }
  
  public AnyNewListModel(Any modelRoot)
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
  public AnyNewListModel(RenderInfo r)
  {
    setRenderInfo(r);
  }

  public void setRenderInfo(RenderInfo r)
  {
    // The data values beneath the list node set
    renderInfo_ = r;

    // If the RenderInfo represents an enum then there is no
    // modelRoot (the root of a node-set in the node space). In
    // that case we create a suitable node structure containing the
    // internal and external enum values.
    if (modelRootExpr_ == null && r.isEnum())
      rebuildEnums(r);
    
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
  
  public Any getNullListValue()
  {
    return nullListValue_;
  }

  public void setSelectionModel(ListSelectionModel l)
  {
    selectionModel_ = l;
  }
  
  public void setModelRoot(Any newRoot) throws AnyException
  {
    if (context_ != null)
    {
      if (newRoot instanceof NodeSpecification)
      {
        NodeSpecification n = (NodeSpecification)newRoot;
        n = n.resolveIndirections(context_, Globals.process__.getTransaction());
        LocateNode l = new LocateNode(n);
        newRoot = l;
      }
    }
    
    // If there was no context then remember as-is for when it is
    // established
    modelRootExpr_ = newRoot;
    
    if (context_ != null)
      processReplace(null);
  }
  
  public Any getModelRoot()
  {
    return modelRootExpr_;
  }
  
  public void setShowNull(boolean showNull)
  {
    allowsNull_ = showNull;
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
      nullListValue_ = text;
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
  public void setItemSelection(Map selection)
  {
    // Easiest way to do this is to loop
    // over the list and check each item for
    // existence in the selection array
    
    int size = this.getRealSize();
    Vectored s = (Vectored)selection.shallowCopy();
    
    for (int i = 0; i < size; i++)
    {
      // Assume we are using a ListRenderInfo, which returns
      // a map.
      Map m = (Map)getElementAt(i);
      Any a = m.get(ListRenderInfo.internal__);
      int index = s.indexOf(a);
      if (index >= 0)
      {
        selectionModel_.addSelectionInterval(i, i);
        s.removeByVector(index);
      }
      // if there's anything left in the temporary
      // array copy then remove it from the supplied
      // selection, so that this selection accurately
      // reflects the list selection.
      selection.removeAll(s);
    }
    
    selection_ = selection.shallowCopy();
  }
  
  public Map getItemSelection()
  {
    return selection_;
  }
  
  // From ListModel
  public Object getElementAt(int index)
  {
    Any listValue = null;

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
    try
    {
      Vectored v = (Vectored)resolveDataNode(context_, false);

      if (v != null)
      {
        rowKey = v.getKeyOfVector(row);
//          Composite rowRoot = (Composite)v.getByVector(row);
//          rowKey = rowRoot.getNameInParent();
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
      if (m.hasKeys(AnyListModel.listKeys__))
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

  Any getAnySelectedItem()
  {
    return (Any)selectedItem_;
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
    Any selectedItem = null;
    
    // If the model supports a null item (incidentally, always the
    // first value in the model) then accept Java null as selecting
    // this item.
    if (allowsNull_ && anItem == null)
    {
      selectedItem_ = AnyNull.instance();
      return;
    }
    
    // Java null means no item is selected. Only possible when the
    // model does not allow null.
    
    if (anItem != null)
    {
      // The item is the result of the RenderInfo. We search the model
      // for it.
      
      Any iv = (Any)anItem;
      
      try
      {
        Vectored v = (Vectored)resolveDataNode(context_, false);
        if (v != null)
        {
          int entries = v.entries();
    
          for (int i = 0; i < entries; i++)
          {
            Any listItem = v.getByVector(i);
  
            // always re-evaluate the data node as the RenderInfo
            // object is used on a per-list-item basis.
            Any a = renderInfo_.resolveDataNode(listItem, true);
            
            if (iv.equals(a))
            {
              selectedItem = a;
              break;
            }
          }
        }
      }
      catch(AnyException e) { throw new RuntimeContainedException(e); }
    }

    
//    if (selectedItem == null)
//    {
      // If we still cannot determine the item then may be we are
      // TODO: autocompleting the null item...
//      if (isShowNull())
//        selectedItem = nullListValue_;
        
      //System.out.println("AnyListModel.setSelectedItem " + selectedItem);
      //System.out.println("AnyListModel  anItem " + anItem);
      //AbstractAny.stackTrace();
//    }
    
    // We can always select Java null - means there is no item selected.
    selectedItem_ = selectedItem;
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

    if (eventType.equals(EventConstants.BOT_UPDATE))
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

    fireContentsChanged(this, -1, -1);
  }

  private void processUpdate(Map id)
  {
    int row = findRowNumber(id, true, false);

    fireContentsChanged(this, row, row);
  }

  private void processRemove(Map id) throws AnyException
  {
    int row = findRowNumber(id, false, true);

    if (row < 0)
    {
      fireContentsChanged(this, -1, -1);
    }
    else
    {
      fireIntervalRemoved(this, row, row);
    }
  }

  private void processAdd(Map id) throws AnyException
  {
    int row = findRowNumber(id, false, true);

    if (row < 0)
    {
      sort(origComparator_);

      fireContentsChanged(this, -1, -1);
    }
    else
    {
      fireIntervalAdded(this, row, row);
    }
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
//      else
//      {
//        dataNode = rebuildEnums(renderInfo_);
//      }
      // TODO: Do we need this?
      
      modelRootM_ = dataNode;
    }

    return dataNode;
  }
  
  Map newSelection(ListSelectionModel lm,
                   Map                selection,
                   Array              keySelection,
                   IntI               selectCount)
  {
    int minI = lm.getMinSelectionIndex();
    int maxI = lm.getMaxSelectionIndex();
    
    int count = 0;

    selection.empty();
    
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
          Any k   = getRowKey(i);
          if (row != null)
          {
            Composite c = (Composite)row;
            
            selection.add(k, row);
            count++;
  
            if (keySelection != null)
            {
              keySelection.add(k);
            }
          }
        }
      }
    }
    
    selectCount.setValue(count);
    selection_ = selection;
    
    return selection;
  }

  void setContext(Any context)
  {
    Any oldContext = context_;
    context_ = context;

    if (oldContext != context)
    {
      try
      {
        setModelRoot(modelRootExpr_);
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
    // prevailing typedef/field

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
      enumSingle.add(internal__, intVal);
      enumSingle.add(external__, extVal);
      
      // Just use internal value as the node-set child key
      enumModel.add(intVal, enumSingle);
    }
    
    modelRootM_ = enumModel;

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
