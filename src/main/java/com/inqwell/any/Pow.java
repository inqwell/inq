/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Pow.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Exponentiation operator.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public final class Pow extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = new AnyDouble(Math.pow(b.getValue(), op2.getValue()));
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = new AnyDouble(Math.pow(c.getValue(), op2.getValue()));
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = new AnyDouble(Math.pow(i.getValue(), op2.getValue()));
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = new AnyDouble(Math.pow(s.getValue(), op2.getValue()));
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = new AnyDouble(Math.pow(l.getValue(), op2.getValue()));
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = new AnyDouble(Math.pow(f.getValue(), op2.getValue()));
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = new AnyDouble(Math.pow(d.getValue(), op2.getValue()));
  }

  public void visitDecimal (Decimal d)
  {
    Decimal op2 = (Decimal)op2_;
    //result_ = new AnyDouble(Math.pow(d.doubleValue(), op2.doubleValue()));
    // As of Java 1.5, the BigDecimal class supports exponentiation to
    // integer powers. This is OK because Inq allows the mixing of decimals
    // and ints. If non-integral powers emerge then these would need to be
    // decimals also as things stand, as float/double are rejected during
    // the ranking exercise.
    result_ = new AnyBigDecimal (d.getValue().pow(op2.getValue().intValue()));
  }

  public void visitAnyString (StringI s)
  {
    result_ = new AnyDouble(Math.pow(Double.valueOf(s.getValue()).doubleValue(),
                                     Double.valueOf(op2_.toString()).doubleValue()));
  }

  protected Any handleNullOperands(Any res1,
                                   Any res2,
                                   Any op1,
                                   Any op2) throws AnyException
  {
    if (res1 == null)
      notResolved(op1);

    return null;
  }
}
