/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/PreDecrement.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Unary pre-increment operator.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */
public final class PreDecrement extends MutatingOperator
{
  public Any doOperation (Any op1, Any op2) throws AnyException
  {
    op1_ = op1;
    op2_ = op2;  // will be null

    op1_.accept(this);

    // pre-decrement can return the original operand, rather than
    // a temporary, since we want the result to be the decremented
    // value.
    return op1;
  }

  public void visitAnyByte (ByteI b)
  {
    b.setValue((byte)(b.getValue() - (byte)1));
  }

  public void visitAnyChar (CharI c)
  {
    c.setValue((char)(c.getValue() - (char)1));
  }

  public void visitAnyInt (IntI i)
  {
    i.setValue(i.getValue() - 1);
  }

  public void visitAnyShort (ShortI s)
  {
    s.setValue((short)(s.getValue() - (short)1));
  }

  public void visitAnyLong (LongI l)
  {
    l.setValue(l.getValue() - 1);
  }

  public void visitAnyFloat (FloatI f)
  {
    f.setValue(f.getValue() - 1);
  }

  public void visitAnyDouble (DoubleI d)
  {
    d.setValue(d.getValue() - 1);
  }

  public void visitAnyDate (DateI d)
  {
    // For dates this is defined as retarding one day (FWIW)
    d.setTime(d.getTime() - AnyDate.DAY_MILLI);
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
