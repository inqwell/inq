/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Join.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.Any;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AnyException;
import com.inqwell.any.Map;
import com.inqwell.any.EvalExpr;

/**
 * Join an instance in the current transaction. Not currently used.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Join extends    AbstractFunc
									implements Cloneable
{
	
	private Any any_;
	
	public Join(Any any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Map toJoin = (Map)EvalExpr.evalFunc(getTransaction(),
																			  a,
																			  any_,
																			  Map.class);

    getTransaction().join(toJoin);

		return toJoin;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Join j = (Join)super.clone();
		j.any_ = AbstractAny.cloneOrNull(any_);
		return j;
  }
	
}
