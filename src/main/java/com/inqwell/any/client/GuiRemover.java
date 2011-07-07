/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/GuiRemover.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * A function which attaches 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GuiRemover extends    AbstractFunc
                         implements Cloneable
{
	private Any     removeListener_;
	
	public GuiRemover(Any removeListener)
	{
    removeListener_ = removeListener;
	}

	/**
	 * 
	 */
	public Any exec(Any a) throws AnyException
	{
    AnyView.EventBinding removeListener = (AnyView.EventBinding)
                                EvalExpr.evalFunc(getTransaction(),
                                                  a,
                                                  removeListener_,
                                                  AnyView.EventBinding.class);

    removeListener.removeBinding();
    
    return null;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	GuiRemover g = (GuiRemover)super.clone();
  	
  	g.removeListener_   = removeListener_.cloneAny();
  	
    return g;
  }
}
