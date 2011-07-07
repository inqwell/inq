/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Subtract.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

//import java.util.*;


/**
 * Subtract two Anys and return the result
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class Subtract extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = new AnyInt (b.getValue() - op2.getValue());
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = new AnyInt (c.getValue() - op2.getValue());
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = new AnyInt (i.getValue() - op2.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = new AnyInt (s.getValue() - op2.getValue());
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = new AnyLong (l.getValue() - op2.getValue());
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = new AnyFloat (f.getValue() - op2.getValue());
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = new AnyDouble (d.getValue() - op2.getValue());
  }
  
  public void visitDecimal (Decimal d)
  {
    Decimal op2 = (Decimal)op2_;
    result_ = new AnyBigDecimal (d.getValue().subtract(op2.getValue()));
  }
  
  public void visitAnyDate (DateI d)
  {
    DateI op2 = (DateI)op2_;
    result_ = new AnyLong (d.getTime() -
                           op2.getTime());
  }

  public void visitArray (Array a)
  {
    int i = a.indexOf(op2_);
    if (i >= 0)
      a.remove(i);
      
    result_ = a;
  }
  
  public void visitSet (Set s)
  {
    if (s.contains(op2_))
      s.remove(op2_);
      
    result_ = s;
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
