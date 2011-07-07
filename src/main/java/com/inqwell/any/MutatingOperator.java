/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MutatingOperator.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-05-07 16:53:31 $
 */
package com.inqwell.any;

/**
 * Acts as a base class for any operators that mutate either
 * of their operands.  Provides the necessary transaction
 * handling.
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 * @see com.inqwell.any.Any
 */
public abstract class MutatingOperator extends OperatorVisitor
{
  protected Map    lastMap_;
  protected Locate lastFunc_;
  
  public void visitFunc (Func f)
  {
    try
    {
      lastMap_ = null;
      f.setTransaction(getTransaction());
      Any a     = f.execFunc(param_);
      lastFunc_ = (Locate)f;   // enforced by parser so should never throw
      if (a == null)
        notResolved(op1_);
      a.accept(this);
		}
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  protected void transactionalMap(Map m)
  {
    // Check write PRIVILEGE on m for pathItem_ in locate.
    //   NB - IS THIS NECESSARY? - TBI
    getTransaction().checkPrivilege(AbstractMap.P_WRITE, m, lastFunc_.getPath());
    
    Transaction t = getTransaction();
    Any a;
    try
    {
      t.copyOnWrite(m);
      a = lastFunc_.doTransactionHandling(param_, m);
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    lastMap_ = m;
    
    if (a == null)
      notResolved(op1_);
    
    a.accept(this);
    // By the time we get here the assignment to the field has
    // been done.  Tell the transaction something that allows
    // it to determine the field that has changed.  We wait
    // until now so that the assignee has the updated value
    // and that it is therefore legitimate to raise an event
    // now for those transactions that do so, such as
    // client-based ones might.
    if (lastMap_ != null)
      t.fieldChanging(m, lastFunc_, null);
    
    lastMap_ = null;
  }
  
  protected Any handleNullOperands(Any res1,
                                   Any res2,
                                   Any op1,
                                   Any op2) throws AnyException
  {
    if (res1 == null)
      notResolved(op1);

    return res1;
  }
}
