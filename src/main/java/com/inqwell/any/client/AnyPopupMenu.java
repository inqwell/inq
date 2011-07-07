/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyPopupMenu.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Container;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Func;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.JPopupMenu;

public class AnyPopupMenu extends    AnyComponent
{
	private JPopupMenu     p_;
	//private Any            eventType_;
  
  // The component we were last popped up on
  private AnyView   lastPoppedUpOn_; 

	static Any     popupComponent__ = new ConstString("popupVia");
  
  private static Array poppedDownEventTypes__;
  
  static
  {
    poppedDownEventTypes__ = AbstractComposite.array();
    poppedDownEventTypes__.add(EventConstants.POPUP_INVISIBLE);
    poppedDownEventTypes__.add(EventConstants.POPUP_CANCELED);
  }

	public void setObject(Object t)
	{
		if (!(t instanceof JPopupMenu))
			throw new IllegalArgumentException
									("AnyPopupMenu wraps com.inqwell.any.client.swing.JPopupMenu and sub-classes");
		
		p_ = (JPopupMenu)t;
		super.setObject(t);
    addAdaptedEventListener(new PoppedDownListener());
	}
	
	public JPopupMenu getPopupMenu()
	{
    return p_;
	}
	
  public Container getComponent()
  {
    return p_;
  }

//	public void setEventType(Any eventType)
//	{
//    eventType_ = eventType;
//	}
//	
//	public Any getEventType()
//	{
//    return eventType_;
//	}
  
  public AnyView getLastPoppedUpOn()
  {
    AnyView ret = lastPoppedUpOn_;
    lastPoppedUpOn_  = null;
    return ret;
  }
  
  public void setLastPoppedUpOn(AnyView v)
  {
//    if ((lastPoppedUpOn_ == null) || (v == null) || (lastPoppedUpOn_ == v))
//      lastPoppedUpOn_ = v;
//    else
//      throw new IllegalStateException("There is already a popup parent");
    lastPoppedUpOn_ = v;
  }
  
  public boolean somethingVisible()
  {
    return p_.somethingVisible();
  }
  
  protected boolean handleBoundEvent(Event e)
  {
    // By the time we get here our Inq parent is the component we
    // were popped up on.  Put it in the event being raised
    // on the menu itself so Inq script event handlers can get at it
    e.add(popupComponent__, getParentAny());
    return true;
  }

  protected class PoppedDownListener extends EventBinding
  {
    public PoppedDownListener()
    {
      super(poppedDownEventTypes__, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      //System.out.println("POPPING DOWN " + e.getId());
      //AnyPopupMenu.this.setLastPoppedUpOn(null); No because popupVia is set up afterwards
      //AnyPopupMenu.this.removeInParent(); No because we get cancel and invisible one after the other
      return null;
		}
  }
}
