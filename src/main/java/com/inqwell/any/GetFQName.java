/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetFQName.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the fully qualified name of the specified map.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetFQName extends    AbstractFunc
                       implements Cloneable
{
  private Any expr_;  // Managed object descriptor
  
  /**
   * GetFQName the given descriptor.
   */
  public GetFQName(Any expr)
  {
    expr_ = expr;
  }

  public Any exec(Any a) throws AnyException
  {
  	//System.out.println ("GetFQName.exec() : descriptor : " + descriptor_);
		Map m = (Map)EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   expr_,
                                   Map.class);

    return m.getDescriptor().getFQName();
  }

  public Iter createIterator()
  {
  	Array a = AbstractComposite.array();
  	a.add(expr_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    GetFQName g = (GetFQName)super.clone();
    
    g.expr_    = expr_.cloneAny();        
    
    return g;
  }
}
