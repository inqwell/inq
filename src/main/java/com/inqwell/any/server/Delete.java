/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Delete.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.Locate;
import com.inqwell.any.AnyException;
import com.inqwell.any.Map;
import com.inqwell.any.EvalExpr;

/**
 * Delete the given object in the current transaction context.  When
 * the transaction is committed the object will become non-transactional
 * and un-managed.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Delete extends    AbstractFunc
										implements Cloneable
{
	private Locate toDelete_;

	public Delete(Locate toDelete)
	{
		toDelete_ = toDelete;
	}

	public Any exec(Any a) throws AnyException
	{
		Map m = (Map)EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   toDelete_,
                                   Map.class);

    Map toDelete = toDelete_.getTMapFound();
    // This is required in case we are deleting an instance marked
    // for creation
    if (toDelete == null)
      toDelete = m;
    
    if (toDelete == null)
      nullOperand(toDelete_);

		//System.out.println ("Delete.exec() toDelete_ " + toDelete_);
		//System.out.println ("Delete.exec() deleting " + toDelete);

		getTransaction().deleteIntent(toDelete);

		// Important NOT to return the node we deleted as this node is
		// transactional and this causes the transaction code to enter
		// this function twice!
		return a;
	}

  public Object clone () throws CloneNotSupportedException
  {
    Delete c = (Delete)super.clone();

    c.toDelete_   = (Locate)toDelete_.cloneAny();

    return c;
  }

}
