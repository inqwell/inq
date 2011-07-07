/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/TxnBlock.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

public class TxnBlock extends    Try
                      implements Cloneable
{
  private Any create_;
  
  // Whether to raise creation events
  private boolean bCreate_ = true;

  public TxnBlock(Any create, Any eTry, Any eCatch, Any eFinally)
  {
    super(eTry, eCatch, eFinally);
    create_ = create;
  }

  public Any exec(Any a) throws AnyException
  {
    Any create = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   create_);
    
    if (create == null && create_ != null)
      nullOperand(create_);
    
    if (create != null)
      bCreate_ = (new AnyBoolean(create)).getValue();
    
    return super.exec(a);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    TxnBlock t = (TxnBlock)super.clone();

    t.create_  = AbstractAny.cloneOrNull(create_);

    return t;
  }

  protected boolean isNestedTransaction()
  {
    return true;
  }
  
  protected boolean isCreateEvents()
  {
    return bCreate_;
  }
}
