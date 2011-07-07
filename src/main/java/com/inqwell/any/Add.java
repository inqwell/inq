/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Add.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Add two Anys and return the result
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class Add extends OperatorVisitor
{
  private boolean nullOk_;
  
  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = new AnyInt (b.getValue() + op2.getValue());
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = new AnyInt (c.getValue() + op2.getValue());
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = new AnyInt (i.getValue() + op2.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = new AnyInt (s.getValue() + op2.getValue());
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = new AnyLong (l.getValue() + op2.getValue());
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = new AnyFloat (f.getValue() + op2.getValue());
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = new AnyDouble (d.getValue() + op2.getValue());
  }
  
  public void visitDecimal (Decimal d)
  {
    Decimal op2 = (Decimal)op2_;
    result_ = new AnyBigDecimal (d.getValue().add(op2.getValue()));
  }
  
  public void visitAnyDate (DateI d)
  {
    DateI op2 = (DateI)op2_;
    result_ = new AnyDate (d.getTime() +
                           op2.getTime());
  }

  public void visitAnyString (StringI s)
  {
    // CONST see ranking operation.  Commented out because we may not
    // convert unknown types of op2_ to string
    //StringI op2 = (StringI)op2_;
    // CONST should we propagate from either operand, always be const ?
    //result_ = new AnyString (s.getValue() + op2.getValue());
    result_ = new AnyString (s.getValue() + op2_.toString());
  }
  
  public void visitArray (Array a)
  {
    a.add(op2_);
    nullOk_ = true;
    result_ = a;
  }
  
  public void visitSet (Set s)
  {
    if (op2_ instanceof Array || op2_ instanceof Set)
      s.addAll((Composite)op2_, true);
    else
      if (!s.contains(op2_))
        s.add(op2_);
    
    nullOk_ = true;
    result_ = s;
  }
  
  protected Any handleAnyNull(Any op1, Any op2)
  {
    if (nullOk_)
      return null;
    
    return super.handleAnyNull(op1, op2);
  }
  
  /*
  protected Any handleAnyNull(Any op1, Any op2)
  {
    Any iNull = AnyNull.instance();
    // Handle unary and binary cases
    if ((op1 != null && op1 == iNull) ||
        (op2 != null && op2 == iNull))
    {
      return AnyNull.instance();
    }
    
    return null;
  }
  */
  
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
