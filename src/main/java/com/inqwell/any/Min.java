/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Max.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.util.*;


/**
 * Compare two Anys and return which ever is the minimum.  The original
 * object is returned, not a clone.  If the operands are equal then
 * the first operand is returned.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public class Min extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = (b.getValue() > op2.getValue()) ? op2 : b;
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = (c.getValue() > op2.getValue()) ? op2 : c;
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = (i.getValue() > op2.getValue()) ? op2 : i;
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = (s.getValue() > op2.getValue()) ? op2 : s;
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = (l.getValue() > op2.getValue()) ? op2 : l;
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = (f.getValue() > op2.getValue()) ? op2 : f;
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = (d.getValue() > op2.getValue()) ? op2 : d;
  }

  public void visitDecimal (Decimal d)
  {
    Decimal op2 = (Decimal)op2_;
    result_ = (d.getValue().compareTo(op2.getValue()) > 0) ? op2 : d;
  }

  public void visitAnyString (StringI s)
  {
    StringI op2 = (StringI)op2_;
    result_ = (s.getValue().compareTo(op2.getValue()) > 0) ? op2 : s;
  }

  public void visitAnyDate (DateI d)
  {
    DateI op2 = (DateI)op2_;
    result_ = (d.compareTo(op2) > 0) ? op2 : d;
  }
  
  protected Any rippedField(Any a)
  {
    Transaction t = getTransaction();
    
    if (t.getResolving() == Transaction.R_FIELD)
    {
      a = t.getLastTField();
      if (a != null)
      {
        a = a.bestowConstness();
      }
      //System.out.println("2 rippedField " + System.identityHashCode(this) + " var is " + var);
      //System.out.println("2 rippedField " + t.getExecURL() + " at line " + t.getLineNumber());
    }
    return a;
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

    return null;
  }
}
