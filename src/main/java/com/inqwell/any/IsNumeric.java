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
 * Returns <code>true</code> for numeric types of byte, char, short, int, long
 * and decimal:n, <code>false</code> otherwise.
 * <p>
 * Other operand types are unsupported.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */
public final class IsNumeric extends PassNull
{
  public void visitAnyBoolean (BooleanI b)
  {
    result_ = AnyBoolean.FALSE;
  }

  public void visitAnyByte (ByteI b)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitAnyChar (CharI c)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitAnyInt (IntI i)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitAnyLong (LongI l)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitDecimal (Decimal d)
  {
    result_ = AnyBoolean.TRUE;
  }

  public void visitAnyString (StringI s)
  {
    result_ = AnyBoolean.FALSE;
  }

  public void visitAnyDate (DateI d)
  {
    result_ = AnyBoolean.FALSE;
  }

  public void visitMap (Map m)
  {
    result_ = AnyBoolean.FALSE;
  }

  public void visitArray (Array a)
  {
    result_ = AnyBoolean.FALSE;
  }

  public void visitSet (Set s)
  {
    result_ = AnyBoolean.FALSE;
  }

  public void visitFunc (Func f)
  {
    result_ = AnyBoolean.FALSE;
  }

  public void visitAnyObject (ObjectI o)
  {
    result_ = AnyBoolean.FALSE;
  }
  
  public void visitUnknown(Any o)
  {
    result_ = AnyBoolean.FALSE;
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
