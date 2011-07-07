/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Scale.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Determine the scale of the operand. Returns -2 for double, -1 for float
 * or n for decimal:n n > 0.
 * <p>
 * Returns zero for integer types of byte, char, short, int, long
 * and decimal:0.
 * <p>
 * Other operand types are unsupported.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */
public final class Scale extends PassNull
{
  public void visitAnyByte (ByteI b)
  {
    result_ = new AnyInt(0);
  }

  public void visitAnyChar (CharI c)
  {
    result_ = new AnyInt(0);
  }

  public void visitAnyInt (IntI i)
  {
    result_ = new AnyInt(0);
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = new AnyInt(0);
  }

  public void visitAnyLong (LongI l)
  {
    result_ = new AnyInt(0);
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = new AnyInt(-1);
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = new AnyInt(-2);
  }

  public void visitDecimal (Decimal d)
  {
    result_ = new AnyInt(d.scale());
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
