/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Round.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Round operator. Returns the closest integer or long to the float
 * or double argument.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public final class Round extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    result_ = new AnyInt(b.getValue());
  }

  public void visitAnyChar (CharI c)
  {
    result_ = new AnyInt(c.getValue());
  }

  public void visitAnyInt (IntI i)
  {
    result_ = new AnyInt(i.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = new AnyInt(s.getValue());
  }

  public void visitAnyLong (LongI l)
  {
    result_ = new AnyLong(l.getValue());
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = new AnyInt(Math.round(f.getValue()));
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = new AnyLong(Math.round(d.getValue()));
  }

  public void visitDecimal (Decimal d)
  {
    result_ = new AnyLong(Math.round(d.doubleValue()));
  }

  public void visitAnyString (StringI s)
  {
    result_ = new AnyLong(Math.round(Double.valueOf(s.getValue()).doubleValue()));
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
