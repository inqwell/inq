/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/DialogInstantiator.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.Facade;
import java.lang.reflect.Constructor;

/**
 *
 */
public class DialogInstantiator extends    GuiInstantiator
                                implements Cloneable
{
	private Any    parent_;
	private Object oParent_;

	public DialogInstantiator (String awtClass, Any parent)
	{
    super(awtClass, null);
		setParentComponent(parent);
	}

	public DialogInstantiator (String awtClass, String wrapperClass, Any parent)
	{
    super(awtClass, wrapperClass);
		setParentComponent(parent);
	}

	public Any exec(Any a) throws AnyException
	{
    Any p = EvalExpr.evalFunc(getTransaction(),
                              a,
                              parent_);

    if (p == null && parent_ != null)
      nullOperand(parent_);
    
    if (AnyNull.isNullInstance(p))
      p = null;
    
    if (p != null && (!(p instanceof AnyWindow)))
      throw new AnyException("parent is not a Window");
    
    AnyWindow parent = (AnyWindow)p;
    
    if (parent != null)
    {
      oParent_ = parent.getObject();
      // These casts (currently) always work because of the parser setup or because
      // we didn't get any reflection errors making the JDialog
      AnyDialog d = (AnyDialog)super.exec(a);
      d.setParentFrame(parent);
      return d;
    }
    else
      return super.exec(a);
	}

	/**
	 *
	 */
	protected Object makeAwtObject(String awtClass) throws Exception
	{
		if (oParent_ != null)
		{
			Class c = Class.forName(awtClass);
			Class[] args = new Class[1];
			args[0] = oParent_.getClass();

	    Constructor ctor = c.getConstructor(args);

	 		Object[] ctorArgs = new Object[1];
			ctorArgs[0] = oParent_;
	    Object o = ctor.newInstance(ctorArgs);
	    return o;
		}
		return super.makeAwtObject(awtClass);
	}

	public void setParentComponent(Any parent)
	{
    parent_ = parent;
	}

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
