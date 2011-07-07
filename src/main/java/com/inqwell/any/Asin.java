/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Asin.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Trigonometric arc sine operator. Converts operand to a double and
 * returns a double result of the arc sine of the angle -pi/2 through pi/2.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public final class Asin extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    result_ = new AnyDouble(Math.asin(b.getValue()));
  }

  public void visitAnyChar (CharI c)
  {
    result_ = new AnyDouble(Math.asin(c.getValue()));
  }

  public void visitAnyInt (IntI i)
  {
    result_ = new AnyDouble(Math.asin(i.getValue()));
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = new AnyDouble(Math.asin(s.getValue()));
  }

  public void visitAnyLong (LongI l)
  {
    result_ = new AnyDouble(Math.asin(l.getValue()));
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = new AnyDouble(Math.asin(f.getValue()));
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = new AnyDouble(Math.asin(d.getValue()));
  }

  public void visitDecimal (Decimal d)
  {
    result_ = new AnyDouble(Math.asin(d.doubleValue()));
  }

  public void visitAnyString (StringI s)
  {
    result_ = new AnyDouble(Math.asin(Double.valueOf(s.getValue()).doubleValue()));
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
