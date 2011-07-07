/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Find a named logger
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class GetLogger extends    AbstractFunc
                        implements Cloneable
{
  private Any   name_;

  public GetLogger(Any name)
  {
    name_    = name;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any name = EvalExpr.evalFunc(t,
                                 a,
                                 name_);
    
    if (name == null)
      nullOperand(name_);
    
    LogManager lm = LogManager.getLogManager();
    
    AnyLogger ret = null;
    
    // Can only return loggers when the Inq log manager is in effect
//    if (lm instanceof AnyLogManager)
//    {
//      AnyLogManager alm = (AnyLogManager)lm;
//      
//      ret = alm.getAnyLogger(name.toString());
//    }
    
    Logger l = lm.getLogger(name.toString());
    if (l instanceof AnyLogger)
      ret = (AnyLogger)l;
    
    return ret;
  }
  
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(name_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    GetLogger g = (GetLogger)super.clone();
    
    g.name_ = name_.cloneAny();
        
    return g;
  }
}
