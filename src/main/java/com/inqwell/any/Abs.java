/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Abs.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;


/**
 * Gennerate absolute value of single Any and return
 * the result as a new Any
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class Abs extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    byte br = b.getValue();
    result_ = new AnyByte ((byte)((br > 0) ? br : 0 - br));
  }

  public void visitAnyChar (CharI c)
  {
    char cr = c.getValue();
    result_ = new AnyChar ((char)((cr > 0) ? cr : 0 - cr));
  }

  public void visitAnyInt (IntI i)
  {
    int ir = i.getValue();
    result_ = new AnyInt ((ir > 0) ? ir : 0 - ir);
  }

  public void visitAnyShort (ShortI s)
  {
    short sr = s.getValue();
    result_ = new AnyShort ((short)((sr > 0) ? sr : 0 - sr));
  }

  public void visitAnyLong (LongI l)
  {
    long lr = l.getValue();
    result_ = new AnyLong ((lr > 0) ? lr : 0l - lr);
  }

  public void visitAnyFloat (FloatI f)
  {
    float fr = f.getValue();
    result_ = new AnyFloat ((fr > 0) ? fr : 0f - fr);
  }

  public void visitAnyDouble (DoubleI d)
  {
    double dr = d.getValue();
    result_ = new AnyDouble ((dr > 0) ? dr : 0d - dr);
  }
  
  public void visitDecimal (Decimal d)
  {
    result_ = new AnyBigDecimal (d.getValue().abs());
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
