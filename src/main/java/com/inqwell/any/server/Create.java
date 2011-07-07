/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Create.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.ConstInt;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.EventIdMap;
import com.inqwell.any.IntI;
import com.inqwell.any.KeyDef;
import com.inqwell.any.Map;
import com.inqwell.any.Transaction;

/**
 * Create the given object in the current transaction context.  When
 * the transaction is committed the object will become transactional
 * and managed.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Create extends    AbstractFunc
										implements Cloneable
{
  public static IntI CREATE_ERROR   = new ConstInt(0);
  public static IntI CREATE_REPLACE = new ConstInt(1);
  public static IntI CREATE_LEAVE   = new ConstInt(2);
  
	private Any toCreate_;
	private Any eventData_;
  private Any action_;
	
	public Create(Any toCreate, Any eventData, Any action)
	{
		toCreate_  = toCreate;
		eventData_ = eventData;
		action_    = action;
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
                                        
		Any eventData   = EvalExpr.evalFunc(getTransaction(),
                                        a,
                                        eventData_);
    
		Any action      = EvalExpr.evalFunc(getTransaction(),
                                        a,
                                        action_);
    if (action == null)
      action = CREATE_ERROR;
    
    if (eventData == null && eventData_ != null)
      nullOperand(eventData_);
    
    if (AnyNull.isNullInstance(eventData))
      eventData = null;
                                        
    if (eventData instanceof Map)
    {
      // Must make an EventIdMap or dispatching won't work
      Map m = (Map)eventData;
      Map e = new EventIdMap();
      e.copyFrom(m);
      
      // Remove any "special" fields present in (for example) key instances
      // otherwise the equality check against new instances will fail
      e.remove(Descriptor.descriptor__);
      e.remove(KeyDef.key__);

      eventData = e;
    }
    
    Transaction t = getTransaction();
    
    if (action == CREATE_ERROR)
    {
      t.createIntent(toCreate, eventData);
      return toCreate;
    }
    else if (action == CREATE_REPLACE)
    {
      Map inCreation = t.isCreateMarked(toCreate);
      if (inCreation != null)
        t.deleteIntent(inCreation);
      t.createIntent(toCreate, eventData);
      return toCreate;
    }
    else
    {
      // CREATE_LEAVE
      Map inCreation = t.isCreateMarked(toCreate);
      if (inCreation == null)
      {
        t.createIntent(toCreate, eventData);
        return toCreate;
      }
      else
      {
        return inCreation.cloneAny(); // protect version left in txn
      }
    }
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    Create c = (Create)super.clone();
    
    c.toCreate_   = toCreate_.cloneAny();
    c.eventData_  = AbstractAny.cloneOrNull(eventData_);
    
    return c;
  }
	
}
