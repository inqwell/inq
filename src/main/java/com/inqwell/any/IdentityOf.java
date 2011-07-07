/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/IdentityOf.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Return the identity hash code of the given object.  Useful when
 * generating keys for maps
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class IdentityOf extends    AbstractFunc
												implements Cloneable
{
	private Any obj_;
	
	public static Any identityOf(Any obj)
	{
		return new AnyInt(System.identityHashCode(obj));
	}
	
	public IdentityOf(Any obj)
	{
		obj_ = obj;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any obj    = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   obj_);
    if (obj == null)
      nullOperand(obj_);
    
		return IdentityOf.identityOf(obj);
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    IdentityOf i = (IdentityOf)super.clone();
    
    i.obj_   = obj_.cloneAny();
    
    return i;
  }
	
}
