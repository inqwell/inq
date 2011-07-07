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
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Map;
import com.inqwell.any.Transaction;

/**
 * Return a Map of instances being created in the current transaction.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Modifying extends    AbstractFunc
                      implements Cloneable
{
  private Any type_;
  
  public Modifying(Any type)
  {
    type_ = type;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
                                              (getTransaction(),
                                               a,
                                               type_,
                                               Descriptor.class);
    
    if (type_ != null && descriptor == null)
      nullOperand(type_);
    
    Transaction t = getTransaction();
    Map ret = t.getModifyList(descriptor);
    
    return ret;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Modifying m = (Modifying)super.clone();
    
    return m;
  }
}
