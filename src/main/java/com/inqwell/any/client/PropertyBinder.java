/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/PropertyBinder.java $
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
public class PropertyBinder extends    AbstractFunc
                            implements Cloneable
{
	private Any          bindTo_;
	private String       property_;
	private Any          r_;
	private boolean      immediate_;
	
	public PropertyBinder (Any        bindTo,
		                     String     property,
		                     Any        r,
		                     boolean    immediate)
	{
		bindTo_         = bindTo;
		property_       = property;
		r_              = r;
		immediate_      = immediate;
	}

	/**
	 * 
	 */
	public Any exec(Any a) throws AnyException
	{
    AnyView bindTo = (AnyView)EvalExpr.evalFunc(getTransaction(),
                                                a,
                                                bindTo_);
    if (bindTo == null)
      nullOperand(bindTo_);

  	RenderInfo r = (RenderInfo)EvalExpr.evalFunc(getTransaction(),
                                                 a,
                                                 r_,
                                                 RenderInfo.class);
  	bindTo.bindProperty(property_,
  	                    r,
  	                    immediate_);

    return bindTo;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	PropertyBinder g = (PropertyBinder)super.clone();
  	
  	g.bindTo_   = (Func)bindTo_.cloneAny();
  	g.r_        = r_.cloneAny();
  	
    return g;
  }
}
