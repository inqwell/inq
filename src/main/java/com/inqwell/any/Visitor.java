/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.io.Serializable;

/**
 * The Any visitor interface.
 */

public interface Visitor extends Serializable
{
  public static Any READ  = AbstractValue.flyweightConst(new ConstInt(0));
  public static Any WRITE = AbstractValue.flyweightConst(new ConstInt(1));
  
  public void visitAnyBoolean (BooleanI b);
  public void visitAnyByte (ByteI b);
  public void visitAnyChar (CharI c);
  public void visitAnyInt (IntI i);
  public void visitAnyShort (ShortI s);
  public void visitAnyLong (LongI l);
  public void visitAnyFloat (FloatI f);
  public void visitAnyDouble (DoubleI d);
  public void visitDecimal (Decimal d);
  public void visitAnyString (StringI s);
  public void visitAnyDate (DateI d);
  public void visitMap (Map m);
  public void visitArray (Array a);
  public void visitSet (Set s);
  public void visitFunc (Func f);
  public void visitAnyObject(ObjectI o);
  public void visitUnknown(Any o);
  
  public void setTransaction(Transaction t);
  public Transaction getTransaction();

}
