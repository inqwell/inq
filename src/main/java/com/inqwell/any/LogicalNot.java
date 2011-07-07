/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LogicalNot.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;


/**
 * Return logical inverse of given single operand
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public class LogicalNot extends OperatorVisitor
{
  public void visitAnyBoolean (BooleanI b)
  {
    result_ = new AnyBoolean (!b.getValue());
  }

  public void visitAnyByte (ByteI b)
  {
    result_ = new AnyBoolean (!(b.getValue() != 0));
  }

  public void visitAnyChar (CharI c)
  {
    result_ = new AnyBoolean (!(c.getValue() != 0));
  }

  public void visitAnyInt (IntI i)
  {
    result_ = new AnyBoolean (!(i.getValue() != 0));
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = new AnyBoolean (!(s.getValue() != 0));
  }

  public void visitAnyLong (LongI l)
  {
    result_ = new AnyBoolean (!(l.getValue() != 0));
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = new AnyBoolean (!(f.getValue() != 0));
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = new AnyBoolean (!(d.getValue() != 0));
  }

  public void visitAnyString (StringI s)
  {
    //StringI op2 = (StringI)op2_;
    result_ = new AnyBoolean (!AnyBoolean.booleanValue(s.getValue()));
  }
  
  protected Any handleNullOperands(Any res1,
                                   Any res2,
                                   Any op1,
                                   Any op2) throws AnyException
  {
    if (res1 == null)
      return new AnyBoolean(true);
    else
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
