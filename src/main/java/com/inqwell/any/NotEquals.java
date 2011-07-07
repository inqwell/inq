/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NotEquals.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;



/**
 * Compare two Anys and return true if op1 is not equal to op2; false
 * otherwise
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class NotEquals extends RelationalOperator
{
  public void visitAnyByte (ByteI b)
  {
    ByteI op2 = (ByteI)op2_;
    result_ = getBooleanResult(b.getValue() != op2.getValue());
  }

  public void visitAnyBoolean (BooleanI b)
  {
    result_ = getBooleanResult(!b.equals(op2_));
  }

  public void visitAnyChar (CharI c)
  {
    CharI op2 = (CharI)op2_;
    result_ = getBooleanResult(c.getValue() != op2.getValue());
  }

  public void visitAnyInt (IntI i)
  {
    IntI op2 = (IntI)op2_;
    result_ = getBooleanResult(i.getValue() != op2.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    ShortI op2 = (ShortI)op2_;
    result_ = getBooleanResult(s.getValue() != op2.getValue());
  }

  public void visitAnyLong (LongI l)
  {
    LongI op2 = (LongI)op2_;
    result_ = getBooleanResult(l.getValue() != op2.getValue());
  }

  public void visitAnyFloat (FloatI f)
  {
    FloatI op2 = (FloatI)op2_;
    result_ = getBooleanResult(f.getValue() != op2.getValue());
  }

  public void visitAnyDouble (DoubleI d)
  {
    DoubleI op2 = (DoubleI)op2_;
    result_ = getBooleanResult(d.getValue() != op2.getValue());
  }

  public void visitDecimal (Decimal d)
  {
    result_ = getBooleanResult(!d.equals(op2_));
  }

  public void visitAnyString (StringI s)
  {
    //StringI op2 = (StringI)op2_;
    //result_ = getBooleanResult(!s.equals(op2));
    result_ = getBooleanResult(!s.equals(op2_));
  }
  
  public void visitAnyDate (DateI d)
  {
    result_ = getBooleanResult(!d.equals(op2_));
  }
  
  public void visitMap (Map m)
  {
    // See Equals.visitMap
    Map m2 = (Map)op2_;
		if (Globals.isServer() && m.hasIdentity() && m2.hasIdentity())
      result_ = getBooleanResult(m != m2);
    else
      result_ = getBooleanResult(!(m.getMap().equals(m2.getMap())));
  }
  
  public void visitSet(Set s)
  {
    result_ = getBooleanResult(!s.equals(op2_));
  }
  
  public void visitArray (Array a)
  {
    result_ = getBooleanResult(!a.equals(op2_));
  }
  
  public void visitAnyObject (ObjectI o)
  {
    result_ = getBooleanResult(!o.equals(op2_));
  }
  
  public void visitUnknown(Any o)
  {
    result_ = getBooleanResult(!o.equals(op2_));
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

  protected Any bumhandleAnyNull(Any op1, Any op2)
  {
    Any iNull = AnyNull.instance();

    if ((op1 != null && op1 == iNull) ||
        (op2 != null && op2 == iNull))
    {
      return AnyBoolean.FALSE;
    }
    
    return null;
  }

  protected Any handleAnyNull(Any op1, Any op2)
  {
    Any iNull   = AnyNull.instance();

    if (((op1 != null && op1 == iNull) &&
         (op2 != null && op2 != iNull))   || // null != non-null is TRUE
        ((op2 != null && op2 == iNull) &&
         (op1 != null && op1 != iNull)))
    {
      return AnyBoolean.TRUE;
    }
    
    return null;
  }
}
