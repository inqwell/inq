/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/GuiPopup.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.ConstInt;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Locate;

/**
 * A function which attaches 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GuiPopup extends    AbstractFunc
                      implements Cloneable
{
	private Any     attachTo_;
	private Any     menu_;
	private Array   eventTypes_;
	//private boolean consume_;
	
	public GuiPopup (Any     attachTo,
                   Any     menu,
                   Array   eventTypes,
                   boolean consume)
	{
		attachTo_       = attachTo;
		menu_           = menu;
		eventTypes_     = eventTypes;
		//consume_        = consume;
	}

	/**
	 * 
	 */
	public Any exec(Any a) throws AnyException
	{
    AnyComponent attachTo = (AnyComponent)EvalExpr.evalFunc(getTransaction(),
					                                                  a,
					                                                  attachTo_,
					                                                  AnyComponent.class);

    AnyPopupMenu menu = (AnyPopupMenu)EvalExpr.evalFunc(getTransaction(),
					                                              a,
					                                              menu_,
					                                              AnyPopupMenu.class);
	  
    if (attachTo == null)
      throw new AnyException("Component not resolved: " + attachTo_);
      
    if (menu == null)
      throw new AnyException("Popup not resolved: " + menu_);
      
    // Used when menu is popped up
    menu.setUniqueKey(new ConstInt(System.identityHashCode(menu)));
    
	  for (int i = 0; i < eventTypes_.entries(); i++)
	  {
	  	Any eventType = eventTypes_.get(i);
	    attachTo.setPopupMenu(eventType, menu);
	  }
	  
    return attachTo;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	GuiPopup g = (GuiPopup)super.clone();
  	
  	g.attachTo_   = attachTo_.cloneAny();
  	g.menu_       = menu_.cloneAny();
  	
    return g;
  }
}
