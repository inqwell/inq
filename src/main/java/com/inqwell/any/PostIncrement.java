/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/PostIncrement.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Unary post-increment operator.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public final class PostIncrement extends MutatingOperator
{
  public Any doOperation (Any op1, Any op2) throws AnyException
  {
    op1_ = op1;
    op2_ = op2;  // will be null

    op1_.accept(this);

    // post-increment must return a temporary, rather than
    // the original operand, since we don't want the result to be
    // the incremented value.
    return result_;
  }

  public void visitAnyByte (ByteI b)
  {
    result_ = b.cloneAny();
    b.setValue((byte)(b.getValue() + (byte)1));
  }

  public void visitAnyChar (CharI c)
  {
    result_ = c.cloneAny();
    c.setValue((char)(c.getValue() + (char)1));
  }

  public void visitAnyInt (IntI i)
  {
    result_ = i.cloneAny();
    i.setValue(i.getValue() + 1);
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = s.cloneAny();
    s.setValue((short)(s.getValue() + (short)1));
  }

  public void visitAnyLong (LongI l)
  {
    result_ = l.cloneAny();
    l.setValue(l.getValue() + 1);
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = f.cloneAny();
    f.setValue(f.getValue() + 1);
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = d.cloneAny();
    d.setValue(d.getValue() + 1);
  }

  public void visitAnyDate (DateI d)
  {
    result_ = d.cloneAny();
    // For dates this is defined as retarding one day (FWIW)
    d.setTime(d.getTime() + AnyDate.DAY_MILLI);
  }

  public void visitMap (Map m)
  {
		if (m.isTransactional())
    {
      transactionalMap(m);
	  }
	  else
	  {
      unsupportedOperation(m);
		}
  }
}
