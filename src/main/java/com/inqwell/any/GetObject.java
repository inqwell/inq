/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Fetch the item out of an AnyObject. The item within must be
 * an <code>Any</code>.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetObject extends    AbstractFunc
                   implements Cloneable
{
  private Any any_;
  
  public GetObject(Any any)
  {
    any_ = any;
  }
  
  public Any exec(Any a) throws AnyException
  {
    ObjectI any = (ObjectI)EvalExpr.evalFunc(getTransaction(),
                                             a,
                                             any_,
                                             ObjectI.class);

    if (any == null)
      nullOperand(any_);
    
    Object o = any.getValue();
    if (!(o instanceof Any))
      throw new AnyException("Not an Any: " + o.getClass());
    
    return (Any)o;
  }
  
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(any_);
    return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    GetObject c = (GetObject)super.clone();
    c.any_ = any_.cloneAny();
    return c;
  }
  
}
