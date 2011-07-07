/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Divide.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.math.BigDecimal;


/**
 * Divide two Anys and return the result
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class Divide extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = new AnyInt (b.getValue() / op2.getValue());
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = new AnyInt (c.getValue() / op2.getValue());
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = new AnyInt (i.getValue() / op2.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = new AnyInt (s.getValue() / op2.getValue());
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = new AnyLong (l.getValue() / op2.getValue());
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = new AnyFloat (f.getValue() / op2.getValue());
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = new AnyDouble (d.getValue() / op2.getValue());
  }
  
  public void visitDecimal (Decimal d)
  {
    // May be we need to carry the rounding mode in the Decimal, or
    // soecify it some other way?
    Decimal op2 = (Decimal)op2_;
    result_ = new AnyBigDecimal (d.getValue().divide(op2.getValue(),
                                                     BigDecimal.ROUND_HALF_UP));
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

    return null; // can't happen
  }
}
