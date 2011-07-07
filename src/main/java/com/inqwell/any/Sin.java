/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Sin.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Trigonometric sine operator. Converts operand to a double and returns
 * a double result of the sine of the angle in radians.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public final class Sin extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    result_ = new AnyDouble(Math.sin(b.getValue()));
  }

  public void visitAnyChar (CharI c)
  {
    result_ = new AnyDouble(Math.sin(c.getValue()));
  }

  public void visitAnyInt (IntI i)
  {
    result_ = new AnyDouble(Math.sin(i.getValue()));
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = new AnyDouble(Math.sin(s.getValue()));
  }

  public void visitAnyLong (LongI l)
  {
    result_ = new AnyDouble(Math.sin(l.getValue()));
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = new AnyDouble(Math.sin(f.getValue()));
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = new AnyDouble(Math.sin(d.getValue()));
  }

  public void visitDecimal (Decimal d)
  {
    result_ = new AnyDouble(Math.sin(d.doubleValue()));
  }

  public void visitAnyString (StringI s)
  {
    result_ = new AnyDouble(Math.sin(Double.valueOf(s.getValue()).doubleValue()));
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
