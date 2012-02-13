/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Equals.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;



/**
 * Compare two Anys and return true if op1 is equal to op2; false otherwise
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public class Equals extends RelationalOperator
{
  private static final long serialVersionUID = 1L;

  public void visitAnyByte (ByteI b)
  {
    result_ = getBooleanResult(b.equals(op2_));
  }

  public void visitAnyBoolean (BooleanI b)
  {
    result_ = getBooleanResult(b.equals(op2_));
  }

  public void visitAnyChar (CharI c)
  {
    result_ = getBooleanResult(c.equals(op2_));
  }

  public void visitAnyInt (IntI i)
  {
    result_ = getBooleanResult(i.equals(op2_));
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = getBooleanResult(s.equals(op2_));
  }

  public void visitAnyLong (LongI l)
  {
    result_ = getBooleanResult(l.equals(op2_));
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = getBooleanResult(f.equals(op2_));
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = getBooleanResult(d.equals(op2_));
  }

  public void visitDecimal (Decimal d)
  {
    result_ = getBooleanResult(d.equals(op2_));
    //result_ = getBooleanResult(d.getValue().compareTo(op2_.getValue()) == 0);
  }

  public void visitAnyString (StringI s)
  {
    result_ = getBooleanResult(s.equals(op2_));
  }

  public void visitAnyDate (DateI d)
  {
    result_ = getBooleanResult(d.equals(op2_));
  }
  
  public void visitMap (Map m)
  {
    // Some maps implement identity equals
    // and some value equals at the Java level.
    // At the Inq level we want to be independent of this so
    // we check for it.
    // In a server environment
    // objects that are managed have identity semantics unless they
    // have been joined into a transaction, therefore two identity
    // objects are considered equal if they are the same object. If
    // the object has been entered into a transaction then the
    // non-identity private instance will be resolved by now and we
    // compare the underlying maps.  On a client we always compare
    // the underlying maps, as whatever the identity status as
    // there are no txn semantics.
    // Bit hacky but probably OK.  Of course, could be expensive
    // if the map is deep, but that was always the case.
    Map m2 = (Map)op2_;
		if (Globals.isServer() && m.hasIdentity() && m2.hasIdentity())
      result_ = getBooleanResult(m == m2);
    else
      result_ = getBooleanResult(m.valueEquals(m2));
  }
  
  public void visitArray (Array a)
  {
    result_ = getBooleanResult(a.equals(op2_));
  }
  
  public void visitSet(Set s)
  {
    result_ = getBooleanResult(s.equals(op2_));
  }
  
  public void visitAnyObject (ObjectI o)
  {
    result_ = getBooleanResult(o.equals(op2_));
  }
  
  public void visitUnknown (Any o)
  {
    result_ = getBooleanResult(o.equals(op2_));
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
    Any iNull   = AnyNull.instance();
    Any iEquals = AnyAlwaysEquals.instance();

    if (((op1 != null && op1 == iNull) &&
         (op2 != null && op2 == iNull))   || // null == null is TRUE
        ((op1 != null && op1 == iEquals) &&
         (op2 != null && op2 == iNull))   || // equals == null is TRUE
        ((op1 != null && op1 == iNull) &&
         (op2 != null && op2 == iEquals)))   // null == equals is TRUE
    {
      return AnyBoolean.TRUE;
    }
    
    if ((op1 != null && op1 == iNull) ||
        (op2 != null && op2 == iNull))
    {
      return AnyBoolean.FALSE;
    }
    
    return null;
  }
}
