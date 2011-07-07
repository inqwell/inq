/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LessThan.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Compare two Anys and return true if op1 is less than op2; false otherwise
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */
public class LessThan extends OperatorVisitor
{
	private static final long serialVersionUID = 1L;

	public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = new AnyBoolean (b.getValue() < op2.getValue());
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = new AnyBoolean (c.getValue() < op2.getValue());
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = new AnyBoolean (i.getValue() < op2.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = new AnyBoolean (s.getValue() < op2.getValue());
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = new AnyBoolean (l.getValue() < op2.getValue());
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = new AnyBoolean (f.getValue() < op2.getValue());
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = new AnyBoolean (d.getValue() < op2.getValue());
  }

  public void visitDecimal (Decimal d)
  {
    Decimal op2 = (Decimal)op2_;
    result_ = new AnyBoolean (d.getValue().compareTo(op2.getValue()) < 0);
  }

  public void visitAnyString (StringI s)
  {
    StringI op2 = (StringI)op2_;
    result_ = new AnyBoolean (s.getValue().compareTo(op2.getValue()) < 0);
  }

  public void visitAnyDate (DateI d)
  {
    DateI op2 = (DateI)op2_;
    result_ = new AnyBoolean (d.compareTo(op2) < 0);
  }
  
  public void visitAnyBoolean (BooleanI b)
  {
    BooleanI op2 = (BooleanI)op2_;
    result_ = new AnyBoolean (!b.getValue() && op2.getValue());
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

  protected Any handleAnyNull(Any op1, Any op2)
  {
    Any iNull = AnyNull.instance();

    // null == null so not less-than
    if (op1 == iNull && op2 == iNull)
      return AnyBoolean.FALSE;
    
    if (op1 == iNull)
      return AnyBoolean.FALSE;
    
    if (op2 == iNull)
      return AnyBoolean.FALSE;
    
    return null;
  }
}
