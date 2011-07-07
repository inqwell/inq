/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MakeFormatter.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

/**
 * Make an AnyFormat evaluating the argument expressions that it
 * requires.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class MakeFormatter extends    AbstractFunc
                            implements Cloneable
{
  private Any    formatStr_;
  private Any    anyType_;

  public MakeFormatter(Any    formatStr,
                       Any    anyType)
  {
    formatStr_  = formatStr;
    anyType_    = anyType;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Any formatStr = EvalExpr.evalFunc(getTransaction(),
                                      a,
                                      formatStr_);
    
    if (formatStr == null)
      nullOperand(formatStr_);
    
    Any anyType = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    anyType_);
    
    if (anyType == null)
      nullOperand(anyType_);
    
    AnyFormat formatter = AnyFormat.makeFormat(anyType, formatStr.toString());
    
    return formatter;
  }

  public Object clone () throws CloneNotSupportedException
  {
    MakeFormatter m = (MakeFormatter)super.clone();
    
    m.formatStr_ = formatStr_.cloneAny();
    m.anyType_   = anyType_.cloneAny();
  
    return m;
  }
}
