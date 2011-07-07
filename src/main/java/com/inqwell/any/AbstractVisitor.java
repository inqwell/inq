/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Abstract base class for visitor classes in RTF.
 * All operations by default throw IllegalArgumentException
 */
public abstract class AbstractVisitor implements Visitor
{

	private Transaction transaction_ = Transaction.NULL_TRANSACTION;
	
  public void setTransaction(Transaction t)
  {
    if (t == null)
      t = Transaction.NULL_TRANSACTION;
      
    // TODO: REMOVE DEBUG
    if (AbstractFunc.debug &&
        transaction_ != Transaction.NULL_TRANSACTION &&
        t != Transaction.NULL_TRANSACTION &&
        transaction_ != t)
      throw new AnyRuntimeException("Illegal visitor state");
    
		transaction_ = t;
	}
	
  public Transaction getTransaction()
  {
		return transaction_;
	}
	
  public void visitAnyBoolean (BooleanI b)
  {
    unsupportedOperation (b);
  }

  public void visitAnyByte (ByteI b)
  {
    unsupportedOperation (b);
  }

  public void visitAnyChar (CharI c)
  {
    unsupportedOperation (c);
  }

  public void visitAnyInt (IntI i)
  {
    unsupportedOperation (i);
  }

  public void visitAnyShort (ShortI s)
  {
    unsupportedOperation (s);
  }

  public void visitAnyLong (LongI l)
  {
    unsupportedOperation (l);
  }

  public void visitAnyFloat (FloatI f)
  {
    unsupportedOperation (f);
  }

  public void visitAnyDouble (DoubleI d)
  {
    unsupportedOperation (d);
  }

  public void visitDecimal (Decimal d)
  {
    unsupportedOperation (d);
  }

  public void visitAnyString (StringI s)
  {
    unsupportedOperation (s);
  }

  public void visitAnyDate (DateI d)
  {
    unsupportedOperation (d);
  }

  public void visitMap (Map m)
  {
    unsupportedOperation (m);
  }

  public void visitArray (Array a)
  {
    unsupportedOperation (a);
  }

  public void visitSet (Set s)
  {
    unsupportedOperation (s);
  }

  public void visitFunc (Func f)
  {
    unsupportedOperation (f);
  }

  public void visitAnyObject (ObjectI o)
  {
    unsupportedOperation (o);
  }
  
  public void visitUnknown(Any o)
  {
    unsupportedOperation (o);
  }
  
  protected void unsupportedOperation (Any o)
  {
    throw new IllegalArgumentException
      ("Operation not supported on " + o.getClass().getName());
  }
}
