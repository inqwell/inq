/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyList.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-05-07 21:54:44 $
 */

package com.inqwell.any.client;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyComparator;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.ListF;
import com.inqwell.any.client.swing.JList;
import com.inqwell.any.client.swing.JPanel;

public class AnyList extends    AnySelection
										 implements ListF
{
	private JList         l_;
	private JScrollPane   s_;
	private JComponent    borderee_;

	private AnyListModel model_;
	private Map          selection_;
	private Array        keySelection_;
  private Array        indexSelection_;
  private IntI         selectCount_;
  private IntI         rowCount_;

  // Temporaries if we can't use them because of no context node
  // when originally supplied
  private Any modelRoot_;
	
	private static Set     listProperties__;
	private static Any     scrollable__ = new ConstString("scrollable");

  static
  {
    listProperties__ = AbstractComposite.set();
    listProperties__.add(modelKey__);
    listProperties__.add(scrollable__);
    listProperties__.add(modelRoot__);
    listProperties__.add(modelSort__);
    listProperties__.add(AnySelection.showNull__);
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

		if ((!(o instanceof JList)) && (!(o instanceof JScrollPane)))
			throw new IllegalArgumentException
									("AnyList wraps javax.swing.JList/JScrollPane and sub-classes");


		if (o instanceof JList)
		{
			l_ = (JList)o;
		}
		else
		{
			s_ = (JScrollPane)o;
			l_ = (JList)s_.getViewport().getView();
		}

    l_.setAnyList(this);
    
		model_ = (AnyListModel)l_.getModel();

  	setScrollable(true);
		super.setObject(l_);
		model_.setContext(getContextNode());
	}

  public void setModelRoot(Any newRoot) throws AnyException
  {
    if (getContextNode() != null)
    {
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
  
      model_.setModelRoot(newRoot);
      setupDataEvents();
      modelRoot_ = null;
    }
    else
      modelRoot_ = newRoot;
  }

  public Any getModelRoot()
  {
    return model_.getModelRoot();
  }
  
  
  public AnyListModel getListModel()
  {
    return model_;
  }
  
  public void setShowNull(boolean showNull)
  {
    model_.setShowNull(showNull);
  }
  
  public boolean isShowNull()
  {
    return model_.isShowNull();
  }

	public void evaluateContext()
	{
    super.evaluateContext();
		model_.setContext(getContextNode());
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
			l_.reinit((AnyListModel)l_.getModel());
    }
    else
    {
    	try
    	{
    	  model_.setModelRoot(null);
    	}
    	catch(AnyException e)
    	{
    		throw new RuntimeContainedException(e);
    	}
    	model_.setRenderInfo(new ListRenderInfo(ri.getDescriptor(),
    	                                        ri.getField()));
    }
    if (getContextNode() != null)
      setupDataEvents();
  }
  
	/**
	 * Override.  Ensures that the correct component is returned regardless of
	 * whether we are scrolled.  Important for correct construction of
	 * swing component hierarchy.
	 */
	public Container getComponent()
	{
    /*
		if (s_ != null)
			return s_;
		else
			return l_;
    */
    
    return l_;
	}

	public JComponent getBorderee()
	{
		return borderee_;
	}

	public Object getAddee()
	{
		return getBorderee();
	}

  // Called via GUI events (and by programmatic selection
  // if these propagate through)
	public void newSelection(Event e)
	{
		model_.newSelection(l_.getSelectionModel(),
                        selection_,
                        keySelection_,
                        indexSelection_,
                        selectCount_);
	}

	public Any getItemSelection()
	{
    return keySelection_.shallowCopy();
	}

	public void setItemSelection(Any selection)
	{
		l_.clearSelection();
		
		Array a;
		if (!(selection instanceof Array))
		{
		  a = AbstractComposite.array();
		  a.add(selection);
		}
		else
		  a = (Array)selection;
		
    if (a.entries() != 0)
    {
      model_.setItemSelection(l_.getSelectionModel(),
                              a,
                              selection_);
    }
	}

	public void setSelectionMode(int selectionInterval)
	{
		l_.getSelectionModel().setSelectionMode(selectionInterval);
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
  }
  
  protected void applySort(AnyComparator c)
  {
    model_.sort(c);
  }
  
  /**
   * Sets the <i>scrollable</i> property.  This is a
   * <i>synthetic property</i> provided by <code>inq</code>
   * that can only be set prior to layout. Setting this
   * property once the table has been placed in the
   * <code>awt</code> component hierarchy will have
   * undefined results.
   */
  public void setScrollable(boolean scrollable)
  {
  	if (s_ == null && scrollable)
  	{
  		if (borderee_ != null && borderee_ != l_)
  		  borderee_.remove(l_);
  		  
  	  s_ = new JScrollPane(l_);
  	  borderee_ = s_;
  	}
  	else if (s_ != null && !scrollable)
  	{
  		s_.setViewportView(null);
  	  s_ = null;
  	  borderee_ = new JPanel();
  	  borderee_.add(l_);
  	}
  }
  
  public boolean getScrollable()
  {
  	return s_ != null;
  }
  
	protected void initUpdateModel()
	{
		//model_.setModelVars(modelVars_);
    modelVars_.setTransactional(true);
		this.add(AnyComponent.modelKey__, modelVars_);
    selection_      = AbstractComposite.orderedMap();
    keySelection_   = AbstractComposite.array();
    indexSelection_ = AbstractComposite.array();
		modelVars_.add(selection__, selection_);
		modelVars_.add(keySelection__, keySelection_);
    modelVars_.add(indexSelection__, indexSelection_);
    modelVars_.add(selectCount__, selectCount_ = new AnyInt());
    modelVars_.add(rowCount__, rowCount_ = new AnyInt());

		addAdaptedEventListener(new SelectionListener(selectionChangedEventType__));
		PaintValidate pv;
		if (s_ != null)
		  pv = new PaintValidate(s_);
		else
		  pv = new PaintValidate(l_);
		pv.maybeSync();
	}

	/**
	 * Override base functionality.  We adapt the inq event to a list
	 * event.
	 */
	protected void componentProcessEvent(Event e) throws AnyException
	{
		model_.translateEvent(e);

    if (model_.getRealSize() != rowCount_.getValue())
    {
      rowCount_.setValue(model_.getRealSize());
      Transaction t = Globals.getProcessForCurrentThread().getTransaction();
      boolean active = t.isActive();
      t.copyOnWrite(modelVars_);
      t.fieldChanging(modelVars_, rowCount__, null);
      
      if (!active)
        t.commit();
    }
	}

	protected Object getPropertyOwner(Any property)
	{
		if (listProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
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
  		// the model is replaced and children of model added/removed
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
}
