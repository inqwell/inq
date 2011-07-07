/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Match.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.util.regex.Pattern;

/**
 * Match string operand 1 against regex operand 2 and return
 * the boolean result.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class Match extends OperatorVisitor
{
  public void visitAnyString (StringI s)
  {
    StringI op2 = (StringI)op2_;
    result_ = new AnyBoolean (Pattern.matches(op2.getValue(), s.getValue()));
  }

  protected Any handleNullOperands(Any res1,
                                   Any res2,
                                   Any op1,
                                   Any op2) throws AnyException
  {
    if (res1 == null)
      notResolved(op1);

    if (res2 == null)
      notResolved(op2);

    return null;
  }

  protected Any handleAnyNull(Any op1, Any op2)
  {
    Any iNull = AnyNull.instance();

    if ((op1 != null && op1 == iNull) ||
        (op2 != null && op2 == iNull))
    {
      return AnyBoolean.FALSE;
    }
    
    return null;
  }
}
