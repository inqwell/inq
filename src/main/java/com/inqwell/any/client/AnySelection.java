/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnySelection.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-07 16:57:52 $
 */

package com.inqwell.any.client;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Func;
import com.inqwell.any.Map;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.SelectionF;

public abstract class AnySelection extends    AnySortView
																	 implements SelectionF
{
  protected Map          modelVars_ = AbstractComposite.managedMap();
	
  protected static Array selectionChangedEventType__ = AbstractComposite.array();

  private static Set     selectionProperties__;
  
  static Any   modelRoot__      = new AnyString("modelRoot");
  static Any   modelSort__      = new AnyString("modelSort");
  static Any   showNull__       = new AnyString("showNull");
  static Any   nullText__       = new AnyString("nullText");
  //static Any   itemSelection__  = new AnyString("itemSelection");

  static
  {
    selectionChangedEventType__.add(EventConstants.E_SELECTIONCHANGED);
    selectionProperties__ = AbstractComposite.set();
    selectionProperties__.add(itemSelection__);
    selectionProperties__.add(indexSelection__);
    selectionProperties__.add(modelRoot__);
  }
  
  public EventBinding makeEventBinding(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
  {
    if (eventTypes.contains(EventConstants.E_SELECTIONCHANGED))
      return new UserSelectionListener(expr, eventTypes, consume, busy, modelFires);
    
    return super.makeEventBinding(expr, eventTypes, consume, busy, modelFires);
  }

	protected Object getPropertyOwner(Any property)
	{
		if (selectionProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
	  
  // Used internally to update the selection state
  protected class SelectionListener extends EventBinding
  {
    public SelectionListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
			newSelection(e);
			return null;
		}
  }

  // Used by scripted event handlers for selection events. Ensures
  // events are propagated from selection state
  protected class UserSelectionListener extends EventBinding
  {
    public UserSelectionListener(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
    {
      super(expr, eventTypes, consume, busy, modelFires);
    }

    protected boolean doFireModel(Transaction t, Event e) throws AnyException
    {
      if ((e.getId().equals(EventConstants.E_SELECTIONCHANGED)))
      {
        t.copyOnWrite(modelVars_);
        t.fieldChanging(modelVars_, modelVars_, null);
        return true;
      }
      return false;
    }
  }
}
