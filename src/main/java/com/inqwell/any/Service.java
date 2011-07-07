/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Service.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.AbstractTransaction.RaiseCompletedEvent;


/**
 * Simply executes its single operand.  Exists to allow any function to be
 * executed with provision for input parameters provided by our base class.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Service extends    AbstractInputFunc
									   implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Func    op1_;
  private Map     paramLocations_;

  public Service(Func op1)
  {
    op1_  = op1;
  }

  public Service()
  {
  }

  public Any exec(Any a, Transaction t, Map callArgs) throws AnyException
  {
		// We clone our operand to ensure thread safety while executing all
		// our sub-functions
		//System.out.println ("Inside Service.exec()");

		Func f = (Func)op1_.cloneAny();
		
    if (raisesEvents())
    {
      Event e = makeEvent(EventConstants.EXEC_START);
      fireEvent(e);
      t.setGatherEvents(true);
    }
    
		Any ret = Call.call(f, callArgs, a, t, this);

    if (raisesEvents())
    {
      AbstractTransaction at = (AbstractTransaction)t;
      t.addAction(at.new RaiseCompletedEvent(this, getEventTypes()), Transaction.AFTER_EVENTS);
//      Event e = makeEvent(EventConstants.EXEC_COMPLETE);
//      fireEvent(e);
    }
    
    return ret;
  }
  
  public Any exec(Any a) throws AnyException
  {
		throw new ServiceArgumentException("Service.exec() without a Transaction");
  }
  
	public Map getParams() throws AnyException
	{
		buildParams();
		return (Map)paramLocations_.cloneAny();
	}

  public void setExpr(Any a)
  {
  	op1_ = (Func)a;
  }
  
  public boolean isSyncGraphics()
  {
    return false;
  }
  
  public void setSyncGraphics(boolean sync)
  {
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Service f = (Service)super.clone();
    
    f.op1_ = (Func)op1_.cloneAny();        
    return f;
  }

  private synchronized void buildParams() throws AnyException
  {
    if (paramLocations_ == null)
    {
      paramLocations_ = AbstractComposite.simpleMap();
      buildArgs(paramLocations_);
    }
	}
}
