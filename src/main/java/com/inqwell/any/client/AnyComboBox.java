/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyComboBox.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-05-02 20:08:39 $
 */

package com.inqwell.any.client;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractEvent;
import com.inqwell.any.Any;
import com.inqwell.any.AnyComparator;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.EventGenerator;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.Value;
import com.inqwell.any.beans.SelectionF;
import com.inqwell.any.beans.Setter;
import com.inqwell.any.client.AnySelection.UserSelectionListener;
import com.inqwell.any.client.AnyView.EventBinding;
import com.inqwell.any.client.swing.AutoCompletion;
import com.inqwell.any.client.swing.JComboBox;
import com.inqwell.any.client.swing.JPanel;

/**
 * Inq wrapper for a combo box.  The combo box component can have
 * a number of configurations.  Combo boxes always have a list model
 * of some sort.  This can either be <i>external</i>, that is the
 * list model is a node reference for the model root and further
 * node references for the displayable and corresponding value items
 * contained therein, or <i>internal</i>, where the list items are
 * the names and values of a BOT enumerated field.
 * <p>
 * There can then be an optional rendered data item, which the
 * combo box will update when items are selected, and which will
 * select the corresponding item in the list when updated by Inq
 * script assignments.
 * <p>
 * Whether a rendered data item is specified or not, the combo box
 * will be updated when the node at <i>name</i>.model.selection is
 * updated, and vice-versa.
 * 
 * TODO: Because this component overrides setRenderInfo etc and does
 * this functionality itself consider rebasing away from AnyComponent.
 * We still require the initUpdateModel call of that class, though,
 * currently provided on the call path through super.setObject(o)
 * 
 * PENDING: This class to be rewritten, in particular new event
 * handling will make it much tidier plus new rendering science should be used,
 * not the internal/external stuff, which is poor.
 */
public class AnyComboBox extends AnySortView
{
	private JComboBox     c_;
	private JComponent    borderee_;

	private AnyListModel  model_;

  // Must be a simple map or we can get duplicate parent errors.
  // Unlike tables/lists, combos can only select one item at a time
  // so unlike them, we put the selected node-set child in here
  // directly.
	private Map           modelVars_ = AbstractComposite.simpleMap();

	// Set if we are rendering a specified data item
	private EventDispatcher renderDispatcher_;
	private RenderInfo      renderInfo_;
  
  // Temporaries if we can't use them because of no context node
  // when originally supplied
  private Any modelRoot_;
	
	private AnyInt          indexSelection_ = new AnyInt(-1);
  
  // True if we select the first item after list replacement
  private boolean         selectFirst_;
  
  // True if the combo box should become enabled and not editable
  // after a list reload.  Allows a combo box to be set as
  // editable/disbaled to temporarily take on a value not in the
  // list and then return to normal when the list reloads
  private boolean         selectAfterReload_;

  // This is set up when the combo is editable to listen for
  // document events from the underlying editor.
	private ModelUpdateListener docListener_;

	private static Array  itemEventType__ = AbstractComposite.array();

	private static Set    comboProperties__;
  private static Any    selectFirst__           = new ConstString("selectFirst");
  private static Any    prototypeDisplayValue__ = new ConstString("prototypeDisplayValue");
  private static Any    selectAfterReload__     = new ConstString("selectAfterReload");
  private static Any    autoComplete__          = new ConstString("autoComplete");

	private Setter        setter_;
  
  private int           lastRender_;

  static
  {
		itemEventType__.add(EventConstants.E_ITEM);

    comboProperties__ = AbstractComposite.set();
    comboProperties__.add(modelKey__);
    comboProperties__.add(AnySelection.modelRoot__);
    comboProperties__.add(AnySelection.modelSort__);
    comboProperties__.add(AnySelection.showNull__);
    comboProperties__.add(AnySelection.nullText__);
    comboProperties__.add(selectFirst__);
    comboProperties__.add(prototypeDisplayValue__);
    comboProperties__.add(selectAfterReload__);
    comboProperties__.add(autoComplete__);
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

		//System.out.println ("..........AnyComboBox.setObject " + o);
		if (!(o instanceof JComboBox))
			throw new IllegalArgumentException
									("AnyComboBox wraps com.inqwell.any.client.swing.JComboBox and sub-classes");

		c_     = (JComboBox)o;
		model_ = (AnyListModel)c_.getModel();

    setter_ = SetterFactory.getSetter(c_);

    if (borderee_ == null)
    {
      borderee_ = new JPanel();
      borderee_.add(c_);
	  }

		super.setObject(c_);
		model_.setContext(getContextNode());
	}

  public void setModelRoot(Any newRoot) throws AnyException
  {
    if (getContextNode() != null)
    {
      Any v = setter_.get(c_);
      // Pass on to model
      if (newRoot instanceof NodeSpecification)
      {
        NodeSpecification n = (NodeSpecification)newRoot;
        n = n.resolveIndirections(getContextNode(), Globals.process__.getTransaction());
        LocateNode l = new LocateNode(n);
        newRoot = l;
      }
      else
        throw new AnyRuntimeException("Not a path");
      
      c_.setSelectedIndex(-1);
      model_.setModelRoot(newRoot);
      setupDataEvents();
      setter_.set(v, c_);
      modelRoot_ = null;
    }
    else
      modelRoot_ = newRoot;
  }

  public Any getModelRoot()
  {
    return model_.getModelRoot();
  }
  
	public Map getModel()
	{
    return null;
  }

  public void setModel(Map model)
  {
    // receive a map containing internal and optional
    // external RenderInfo objects
    if (!model.contains(ListRenderInfo.internal__))
      throw new AnyRuntimeException("List model does not contain " + ListRenderInfo.internal__);

    RenderInfo ri = (RenderInfo)model.get(ListRenderInfo.internal__);
    if (!ri.isEnum())
    {
      RenderInfo re = null;
      if (model.contains(ListRenderInfo.external__))
        re = (RenderInfo)model.get(ListRenderInfo.external__);

      ListRenderInfo r = new ListRenderInfo(ri, re);
      r.resolveNodeSpecs(getContextNode());
      model_.setRenderInfo(r);
      c_.reinit((AnyListModel)c_.getModel());
    }
    else
    {
      try
      {
        model_.setRenderInfo(new ListRenderInfo(ri.getDescriptor(),
                                                ri.getField()));
        model_.setModelRoot(null);
        c_.reinit(model_);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    
    if (getContextNode() != null)
      setupDataEvents();
  }

  protected void applySort(AnyComparator c)
  {
    model_.sort(c);
  }
  
  public void setShowNull(boolean showNull)
  {
    model_.setShowNull(showNull);
  }
  
  public boolean isShowNull()
  {
    return model_.isShowNull();
  }

  public void setSelectFirst(boolean selectFirst)
  {
    selectFirst_ = selectFirst;
  }
  
  public boolean isSelectFirst()
  {
    return selectFirst_;
  }
  
  public void setPrototypeDisplayValue(Any a)
  {
    c_.setPrototypeDisplayValue(a);
    model_.setPrototypeDisplayValue(a);
  }
  
  public Any getPrototypeDisplayValue()
  {
    return model_.getPrototypeDisplayValue();
  }
  
  public void setSelectAfterReload(boolean selectAfterReload)
  {
    selectAfterReload_ = selectAfterReload;
  }
  
  public boolean getSelectAfterReload()
  {
    return selectAfterReload_;
  }
  
  public void setNullText(Any text)
  {
    model_.setNullText(text);
  }
  
  public void setAutoComplete(boolean autoComplete)
  {
    if (autoComplete)
      AutoCompletion.enable(c_);
      
    // disable tbd
  }
  
  public void setEditable(boolean editable)
	{
    // TODO: remove super.setEditable(editable);
    c_.setEditable(editable);
    if (renderInfo_ != null)
      renderInfo_.setEditable(editable);

		if (docListener_ != null)
		{
      removeAdaptedEventListener(docListener_);
      docListener_ = null;
    }
    
		
		if (editable)
		{
      docListener_ = new ModelUpdateListener(AnyText.documentEventTypes__);
      addAdaptedEventListener(docListener_);
    }
	}

  public boolean isEditable()
  {
    if (renderInfo_ == null)
      return c_.isEditable();

    return renderInfo_.isEditable();
  }

	public JComponent getBorderee()
	{
		return borderee_;
	}

  public Container getComponent()
  {
    return c_;
  }

  public void initAsCellEditor()
  {
    Boolean inTable = (Boolean)c_.getClientProperty("JComboBox.isTableCellEditor");
    if (inTable != null &&
        inTable.equals(Boolean.TRUE))
      return;

    final JComboBox cb = c_;
    if (cb.isEditable())
    {
      ((JComponent) cb.getEditor().getEditorComponent()).setBorder(null);
    }
    cb.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    cb.addPopupMenuListener(new PopupMenuListener()
                            {
                              public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                              {
                                // Force table editor to wake up
                                String oldCommand = cb.getActionCommand();
                                cb.setActionCommand("comboBoxEdited");
                                cb.fireActionEvent();
                                cb.setActionCommand(oldCommand);
                              }

                              public void popupMenuCanceled(PopupMenuEvent e)
                              {
                              }

                              public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                              {
                              }
                              
                            }
                            );
    //getBorderee().setBorder(null);
  }

	protected Object getAttachee(Any eventType)
	{
		if (AnyText.documentEvents__.contains(eventType))
		{
			if (c_.isEditable())
			{
        return c_.getEditor();
			}
			else
			{
				return null;
			}
		}
	  else
	  {
			return super.getAttachee(eventType);
	  }
	}

	public Object getAddee()
	{
		return getBorderee();
	}

	public void evaluateContext()
	{
    super.evaluateContext();
		model_.setContext(getContextNode());
	}

  // Set the rendering item to the new combo selection
	public void updateModel(boolean selecting, Event e) throws AnyException
	{
    int i = c_.getSelectedIndex();
    if (i >= 0)
    {
      Any row = model_.getItemAt(i);
      if (row != model_.getNullListValue())
      {
        Any k = model_.getRowKey(i);
        Composite c = (Composite)row;
        modelVars_.replaceItem(SelectionF.keySelection__, k);
        modelVars_.replaceItem(SelectionF.selection__, c);
        indexSelection_.setValue(i);
      }
      else
      {
        modelVars_.replaceItem(SelectionF.keySelection__, null);
        modelVars_.replaceItem(SelectionF.selection__, null);
        indexSelection_.setValue(-1);
      }
    }
    else
    {
      modelVars_.replaceItem(SelectionF.keySelection__, null);
      modelVars_.replaceItem(SelectionF.selection__, null);
      indexSelection_.setValue(-1);
    }

    if (renderInfo_ != null && (i >= 0 || c_.isEditable()))
    {
      Any v = setter_.get(c_);
      // Update the rendered data item also.  The user cannot
      // make no selection so if the combo fires an item
      // event with null for any reason don't update the model.
			Any dataItem = renderInfo_.resolveDataNode(getContextNode(), false);

      
      // This is a bit messy and relates to the avoidance of problems
      // of race events when lists are set up on the server...
			if (dataItem != null &&
          selecting &&
          e.getSequence() > lastRender_ &&
          (c_.isEditable() || model_.contains(v)))
      {
        dataItem.copyFrom(v);
      }
    }
	}

  /**
   * Override base functionality.  Informs the combo box that it
   * is rendering a data item and sets up a node listener to
   * listen for changes on it.
   */
	public void setRenderInfo(RenderInfo r)
	{
    renderInfo_ = r;
    
    if (getContextNode() != null && r != null)
    {
      c_.setEditable(r.isEditable());
      r.resolveNodeSpecs(getContextNode());
  
      // Try to resolve the data node given by the RenderInfo
      // in case it causes nodes to be created.  Then we can set
      // them during script initialisation.
      Any dataNode = null;
      try
      {
        dataNode = r.resolveDataNode(getContextNode(), true);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
  
      EventGenerator contextEg = (EventGenerator)getContextNode();
  
      // We must listen to the context node for events which will cause
      // us to render our data.
      if (renderDispatcher_ != null)
      {
        contextEg.removeEventListener(renderDispatcher_);
      }
  
      renderDispatcher_ = new EventDispatcher();
  
      DataListener d = new ComboRenderingListener(r.getNodeSpecs());
  
      listenForUpdates(contextEg, renderDispatcher_, d);
      model_.setItemRenderInfo(r);
      c_.reinit((AnyListModel)c_.getModel());
      if (dataNode == null)
        setter_.set(dataNode, c_);
      else
        setter_.set(dataNode.cloneAny(), c_);

      setupEventSet(c_.getEditor());
      
      if (docListener_ != null)
      {
        removeAdaptedEventListener(docListener_);
        docListener_ = null;
      }
      
      if (r.isEditable())
      {
        docListener_ = new ModelUpdateListener(AnyText.documentEventTypes__);
        addAdaptedEventListener(docListener_);
      }
    }
	}

	public String getLabel()
	{
    // Use the renderinfo of the data item we are updating when available
    if (renderInfo_ != null)
      return renderInfo_.getLabel();
    
    // Try the list model
    RenderInfo r = model_.getRenderInfo();
    if (r != null)
      return r.getLabel();
    
    return null;
	}

  public Any getRenderedValue()
  {
    try
    {
      // Get the current rendered value
  		Any a = renderInfo_.resolveResponsibleData(getContextNode());
      return a;
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public void setRenderedValue(Any v) throws AnyException
  {
    Any a = renderInfo_.resolveResponsibleData(getContextNode());
    a.copyFrom(v);
    setter_.set(a.cloneAny(), c_);
  }
  
	public RenderInfo getRenderInfo()
	{
		return renderInfo_;
	}

  public boolean forwardKeyBinding(KeyStroke ks,
                                   KeyEvent  e,
                                   int       condition,
                                   boolean   pressed)
  {
    return c_.processKeyBinding(ks, e, condition, pressed);
  }
  
  public void setBounds(Rectangle r)
  {
    getComponent().setPreferredSize(new Dimension((int)r.getWidth(), (int)r.getHeight()));
  }
  
  public EventBinding makeEventBinding(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
  {
    if (eventTypes.equals(EventConstants.DEFAULT_TYPE) && modelFires)
      return new ActionListener(expr, eventTypes, consume, busy, modelFires);
    
    return super.makeEventBinding(expr, eventTypes, consume, busy, modelFires);
  }

	protected Object getPropertyOwner(Any property)
	{
		if (comboProperties__.contains(property))
		  return this;

		return super.getPropertyOwner(property);
	}

	protected void initUpdateModel()
	{
		modelVars_.setTransactional(true);
		this.add(AnyComponent.modelKey__, modelVars_);
		modelVars_.add(SelectionF.indexSelection__, indexSelection_);

		addAdaptedEventListener(new ComboModelUpdateListener(itemEventType__));
	}

	/**
	 * Override base functionality.  We adapt the inq event to a list
	 * event.  Note that this method is called by the Inq runtime
	 * only to signal updates that affect the list items, not the
	 * selection model and not any data item the combo box is viewing.
	 */
	protected void componentProcessEvent(Event e) throws AnyException
	{
    // Check if we are deleting the selected item and if so
    // set selection to null.
    Map id = (Map)e.getId();
    Any et = AbstractEvent.getBasicType(id);
    int row = -1;
    
    // Clear out any event memory
    c_.didFireItemStateChanged();
    
    //System.out.println ("AnyComboBox.componentProcessEvent 1" + e);
    if (et.equals(EventConstants.NODE_REMOVED))
    {
      Any vector = id.get(EventConstants.EVENT_VECTOR);
      if (vector instanceof IntI)
      {
        // Removing an individual list item
        row = ((IntI)vector).getValue();
        // Are we removing the currently selected item?
        if (row == indexSelection_.getValue())
        {
          // do this now 
          modelVars_.replaceItem(SelectionF.keySelection__, null);
          if (selectFirst_)
          {
            c_.setSelectedIndex(0);
            // This should kick out an event that will update the
            // rendered value for the current selection
          }
          else
          {
            indexSelection_.setValue(-1);
            setter_.set(null, c_);
            // Code in updateModel vetos the writing of null to
            // the model (may be need to look at that ?). So
            // do it here.
            setRenderedNull();
          }
        }
      }
      else
      {
        // Whole list has gone
          indexSelection_.setValue(-1);
          setter_.set(null, c_);
          setRenderedNull();
      }
    }
    
		Any v = null;
    if (renderInfo_ != null)
      v = renderInfo_.resolveDataNode(getContextNode(), false);

		model_.translateEvent(e);

    // If there is a new model, deselect and reselect with the
    // current combo value.  If its no longer there then
    // selection will be rejected and combo is blank (or
    // null text when there is some).
    if (et.equals(EventConstants.NODE_ADDED) ||
				et.equals(EventConstants.NODE_ADDED_CHILD) ||
        et.equals(EventConstants.NODE_REPLACED) ||
        et.equals(EventConstants.NODE_REPLACED_CHILD))
    {
      boolean isNull = (v == null) || (v == AnyNull.instance()) || ((v instanceof Value) && ((Value)v).isNull());
      if (isNull || !model_.contains(v))
      {
        if (selectFirst_ && model_.getRealSize() > 0)
          c_.setSelectedIndex(0);
        else
        {
          setter_.set(null, c_);
          setRenderedNull();
        }
      }
      else
      {
        setter_.set(v, c_);
      }
      
      if (selectAfterReload_ && !c_.isEnabled())
      {
        c_.setEnabled(true);
        setEditable(false);
      }
      
      // Did we fire anything?  If not refire.
      if (!c_.didFireItemStateChanged())
      {
        Any a = (Any)c_.getSelectedItem();
        c_.fireReselection(a);
      }

    }
	}

  protected void contextEstablished()
  {
    try
    {
      if (modelRoot_ != null)
        setModelRoot(modelRoot_);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    if (renderInfo_ != null)
      setRenderInfo(renderInfo_);
  }
  
  // The rendered value was updated by script code.
  // Try to set the combo selection to whatever the rendering
  // item value is.
  private void renderingProcessEvent(Event e) throws AnyException
  {
//    if (modelIsFiring())
//      return;

    lastRender_ = e.getSequence();
    
		Map id = (Map)e.getId();
    Any dataItem = null;

		Any eventType = id.get(EventConstants.EVENT_TYPE);

		if (eventType.equals(EventConstants.BOT_UPDATE))
		{
			dataItem = renderInfo_.resolveDataNode(getContextNode(), false);
		}
		else
		{
			dataItem = renderInfo_.resolveDataNode(getContextNode(), true);
		}

    setter_.set(dataItem.cloneAny(), c_);
  }

	private void setupDataEvents()
	{
		Map nodeSpecs = AbstractComposite.simpleMap();

		NodeSpecification root = model_.getRootPath();
		RenderInfo r = model_.getRenderInfo();

		Map ns = r.getNodeSpecs();

		Iter iter = ns.createKeysIterator();
		while (iter.hasNext())
		{
			Any thisNs    = iter.next();
			Any fieldList = ns.get(thisNs);

			if (root != null)
			{
				NodeSpecification fromRoot = (NodeSpecification)thisNs.cloneAny();
				fromRoot.addFirst(NodeSpecification.thisEquals__);
				for (int j = 0; j < root.entries(); j++)
				{
					fromRoot.addFirst(root.get(root.entries() - j - 1));
				}
				thisNs = fromRoot;
			}

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

		if (root != null)
		{
  		// Add the model root (with no fields) to pick up events when
  		// the model is replaced.
  		nodeSpecs.add(root, AbstractComposite.fieldSet());

  		// Add the model root children (with no fields) to pick up events when
  		// items are added/removed/replaced.
      root = (NodeSpecification)root.cloneAny();
      root.add(NodeSpecification.strict__);
      root.add(NodeSpecification.thisEquals__);
      nodeSpecs.add(root, AbstractComposite.fieldSet());
    }
    
		setupDataListener(nodeSpecs);
	}
  
  private void setRenderedNull() throws AnyException
  {
    if (renderInfo_ != null)
    {
      Any dataItem = renderInfo_.resolveDataNode(getContextNode(), false);

      if ((dataItem != null) && dataItem instanceof Value)
      {
        ((Value)dataItem).setNull();
      }
    }
  }

  class ComboRenderingListener extends DataListener
  {
		ComboRenderingListener(Map nodeSpecs)
		{
			super(nodeSpecs);
		}

    protected void dispatchToGraphics(Event e) throws AnyException
		{
			renderingProcessEvent(e);
		}
	}

  class ComboModelUpdateListener extends EventBinding
  {
    public ComboModelUpdateListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      if (e.get(ListenerAdapterFactory.stateChange__).equals(ListenerAdapterFactory.selected__))
        updateModel(true, e);
      else
        updateModel(false, e);

			return null;
		}
  }

  private class ModelUpdateListener extends EventBinding
  {
    public ModelUpdateListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      if (renderInfo_ != null)
      {
        AnyComboBoxEditor ce = (AnyComboBoxEditor)c_.getEditor();
        Any a = (Any)ce.getItem();
        Any dataItem = renderInfo_.resolveDataNode(getContextNode(), false);
        dataItem.copyFrom(a);
      }
			return null;
		}
  }

  // The combo box will fire the default event as a consequence
  // of specifying firemodel=true, so avoid the event loop
  protected class ActionListener extends EventBinding
  {
    private boolean modelFiring_ = false;
    
    public ActionListener(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
    {
      super(expr, eventTypes, consume, busy, modelFires);
    }

    protected boolean doFireModel(Transaction t, Event e) throws AnyException
    {
      if (!modelFiring_ && super.doFireModel(t, e))
      {
        modelFiring_ = true;
        return modelFiring_;
      }
      modelFiring_ = false;
      return false;
    }
  }
}
