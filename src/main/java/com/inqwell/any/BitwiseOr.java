/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/BitwiseOr.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;


/**
 * Perform bitwise or operation.  If the operands are booleans then
 * the effect is the same as logical or.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public class BitwiseOr extends OperatorVisitor
{
  public void visitAnyBoolean (BooleanI b)
  {
    BooleanI op2 = (BooleanI)op2_;
    result_ = new AnyBoolean ((b.getValue()) ||
                              (op2.getValue()));
  }

  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = new AnyInt (b.getValue() |
                          op2.getValue());
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = new AnyInt (c.getValue() |
                          op2.getValue());
  }

  public void visitAnyInt (IntI i)
  {
    AnyInt op2 = (AnyInt)op2_;
    result_ = new AnyInt (i.getValue() |
                          op2.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = new AnyInt (s.getValue() |
                          op2.getValue());
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = new AnyLong (l.getValue() |
                           op2.getValue());
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

    /*
    if (res1 == null)
      notResolved(op1);

    if (res2 == null)
      return res1.cloneAny();
    else
      return null;
    */
    return null;
  }
}
