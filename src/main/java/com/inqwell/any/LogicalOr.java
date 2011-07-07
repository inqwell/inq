/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LogicalOr.java $
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
public class LogicalOr extends OperatorVisitor
{
	/**
	 * Evaluate first operand and if true, don't evaluate
	 * second
	 */
  public Any doOperation (Any op1, Any op2) throws AnyException
  {
    rank (op1);
    op1_ = rankV_.getAny();
    AnyBoolean result = new AnyBoolean();
    result.copyFrom(op1_);
    
    if (result.getValue())
      return result;   // op1 is true so don't evaluate op2
    
    rank(op2);
    op2_ = rankV_.getAny();
    return result.copyFrom(op2_);
  }
  
  // These now defunct
  public void visitAnyBoolean (BooleanI b)
  {
    BooleanI op2 = (BooleanI)op2_;
    result_ = new AnyBoolean ((b.getValue()) ||
                              (op2.getValue()));
  }

  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = new AnyBoolean ((b.getValue() != 0) ||
                              (op2.getValue() != 0));
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = new AnyBoolean ((c.getValue() != 0) ||
                              (op2.getValue() != 0));
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = new AnyBoolean ((i.getValue() != 0) ||
                              (op2.getValue() != 0));
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = new AnyBoolean ((s.getValue() != 0) ||
                              (op2.getValue() != 0));
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = new AnyBoolean ((l.getValue() != 0) ||
                              (op2.getValue() != 0));
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = new AnyBoolean ((f.getValue() != 0) ||
                              (op2.getValue() != 0));
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = new AnyBoolean ((d.getValue() != 0) ||
                              (op2.getValue() != 0));
  }

  public void visitAnyString (StringI s)
  {
    StringI op2 = (StringI)op2_;
    result_ = new AnyBoolean (AnyBoolean.booleanValue(s.getValue()) ||
                              AnyBoolean.booleanValue(op2.getValue()));
  }
  
  protected Any handleNullOperands(Any res1,
                                   Any res2,
                                   Any op1,
                                   Any op2) throws AnyException
  {
    // This is not called because we have overridden doOperation above
    Any ret = null;

    if (res1 == null && res2 == null)
      return new AnyBoolean(); // false
      
    if (res2 == null)
    {
      ret = new AnyBoolean();
      ret.copyFrom(res1);  // convert resolved op1 to boolean
    }
    
    return ret;
  }

  protected Any handleAnyNull(Any op1, Any op2)
  {
    Any iNull = AnyNull.instance();

    if ((op1 != null && op1 == iNull) ||
        (op2 != null && op2 == iNull))
    {
      return AnyBoolean.FALSE;
    }
    
    return null;
  }
}
