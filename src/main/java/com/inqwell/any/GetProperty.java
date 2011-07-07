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
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Evaluates a property binding
 * 
 * an <code>Any</code>.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetProperty extends    AbstractFunc
                         implements Cloneable
{
  private Any any_;
  
  public GetProperty(Any any)
  {
    any_ = any;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    PropertyBinding any = (PropertyBinding)EvalExpr.evalFunc(t,
                                                             a,
                                                             any_,
                                                             PropertyBinding.class);

    if (any == null)
      nullOperand(any_);
    
    return t.readProperty(any);
  }
  
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(any_);
    return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    GetProperty c = (GetProperty)super.clone();
    c.any_ = any_.cloneAny();
    return c;
  }
  
}
