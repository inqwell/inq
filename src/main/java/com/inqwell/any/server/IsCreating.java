/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/IsCreating.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;

/**
 * IsCreating the given object in the current transaction context.  When
 * the transaction is committed the object will become transactional
 * and managed.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class IsCreating extends    AbstractFunc
                        implements Cloneable
{
	private Any toCreate_;
	
	public IsCreating(Any toCreate)
	{
		toCreate_  = toCreate;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Map toCreate    = (Map)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 toCreate_,
																					 Map.class);

    if (toCreate == null)
      nullOperand(toCreate);

    Map ret;
    
    if ((ret = getTransaction().isCreateMarked(toCreate)) != null)
    {
      Map m1 = AbstractComposite.simpleMap();
      Iter ii = ret.createKeysIterator();
      while (ii.hasNext())
      {
        Any kk = ii.next();
        Descriptor dd = ret.getDescriptor();
        if (dd.isKeyField(kk))
          m1.add(kk, ret.get(kk).bestowConstness());
        else
          m1.add(kk, ret.get(kk));
      }
      return ret;
    }
    else
      return AnyNull.instance();
      
		//return new AnyBoolean(getTransaction().isCreateMarked(toCreate) != null);
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    IsCreating c = (IsCreating)super.clone();
    
    c.toCreate_  = toCreate_.cloneAny();
    
    return c;
  }
}
