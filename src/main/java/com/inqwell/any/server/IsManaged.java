/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.server;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.Transaction;

/**
 * If the argument is a managed instance return <code>true</code>. Otherwise
 * return <code>false</code>. 
 */
public class IsManaged extends    AbstractFunc
                        implements Cloneable
{
	private Any isManaged_;
	
	public IsManaged(Any isManaged)
	{
		isManaged_  = isManaged;
	}
	
	public Any exec(Any a) throws AnyException
	{
    Transaction t = getTransaction();

    Map isManaged    = (Map)EvalExpr.evalFunc
																					(t,
																					 a,
																					 isManaged_,
																					 Map.class);

    if (isManaged == null)
      nullOperand(isManaged_);

    // When map is in the transaction get the public copy.
    if (t.getResolving() == Transaction.R_MAP)
    	isManaged = t.getLastTMap();
    
    Any ret = AnyBoolean.FALSE;
    
    if (isManaged.hasIdentity())
      ret = AnyBoolean.TRUE;
    
    return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    IsManaged m = (IsManaged)super.clone();
    
    m.isManaged_  = isManaged_.cloneAny();
    
    return m;
  }
}
