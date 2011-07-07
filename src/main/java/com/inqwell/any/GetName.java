/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetName.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Return the name of the typedef of the given managed object instance.
 * If the given object is not a typedef instance then an exception
 * is thrown
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetName extends    AbstractFunc
												 implements Cloneable
{
	private Any any_;

  public GetName(Any any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Map m = (Map)EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   any_,
                                   Map.class);

    if (m == null)
      throw new AnyException("Could not resolve " + any_);
    
    Descriptor d = m.getDescriptor();
    
    if (d == Descriptor.degenerateDescriptor__)
      throw new AnyException("Not a typedef instance");

    return d.getDefaultAlias();
	}

  public Object clone () throws CloneNotSupportedException
  {
    GetName g = (GetName)super.clone();
    
    g.any_   = any_.cloneAny();
    
    return g;
  }
}
