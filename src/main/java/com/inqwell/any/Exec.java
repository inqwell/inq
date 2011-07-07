/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Exec.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Simply executes its single operand.  Exists to allow any function to be
 * executed with provision for input parameters provided by our base class.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Exec extends    AbstractInputFunc
									implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Func op1_;

  public Exec(Func op1)
  {
    op1_  = op1;
  }

  public Exec()
  {
  }

  public Any exec(Any a) throws AnyException
  {
		op1_.setTransaction(getTransaction());
    
    if (raisesEvents())
    {
      Event e = makeEvent(EventConstants.EXEC_START);
      fireEvent(e);
      
      // Once Transaction.setGatherEvents(true) has been called the
      // transaction will deliver all events it raises in a single
      // bundle carried within a EXEC_COMPLETE event raised when
      // the 
      getTransaction().setGatherEvents(true);
    }
    
  	Any ret = op1_.execFunc(a);
    
    if (raisesEvents())
    {
      Transaction t = getTransaction();
      AbstractTransaction at = (AbstractTransaction)t;
      t.addAction(at.new RaiseCompletedEvent(this, getEventTypes()), Transaction.AFTER_EVENTS);
    }
    
    return ret;
  }
  
  public void setExpr(Any a)
  {
  	op1_ = (Func)a;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(op1_);
  	return a.createIterator();
  }

  protected Any afterExecute(Any ret, Any a)
  {
    // Leave anything left in the transaction by the operand for the
    // Call class to pick up.
    return ret;
  }
  
  protected void setDefunct()
  {
    super.setDefunct();
    // Make this function's exec tree null now, so it can
    // be gc'd sooner.  We don't know how long it will take
    // for cached references in referring Call instances to
    // be cleared.
    op1_ = null;
  }
	
  public Object clone () throws CloneNotSupportedException
  {
    Exec f = (Exec)super.clone();
    
    f.op1_ = (Func)op1_.cloneAny();        
    return f;
  }
}
