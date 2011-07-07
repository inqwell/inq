/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Negate.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

//import java.util.*;


/**
 * Negate single Any and return the result as a new Any
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class Negate extends OperatorVisitor
{
  public void visitAnyByte (ByteI b)
  {
    result_ = new AnyByte ((byte)(0 - b.getValue()));
  }

  public void visitAnyBoolean (BooleanI b)
  {
    result_ = new AnyBoolean (!b.getValue());
  }

  public void visitAnyChar (CharI c)
  {
    result_ = new AnyChar ((char)(0 - c.getValue()));
  }

  public void visitAnyInt (IntI i)
  {
    result_ = new AnyInt (0 - i.getValue());
  }

  public void visitAnyShort (ShortI s)
  {
    result_ = new AnyShort ((short)(0 - s.getValue()));
  }

  public void visitAnyLong (LongI l)
  {
    result_ = new AnyLong (0 - l.getValue());
  }

  public void visitAnyFloat (FloatI f)
  {
    result_ = new AnyFloat (0 - f.getValue());
  }

  public void visitAnyDouble (DoubleI d)
  {
    result_ = new AnyDouble (0 - d.getValue());
  }
  
  public void visitAnyString (StringI s)
  {
    // Negate is supported on strings.  Pointless other than supporting
    // expressions like -string for use in sorting constructs to get
    // string-descending. Can also set whole comparator to descending.
  	// Cannot be used in combination with a collator.
  	String str = s.getValue();
  	char[] c = new char[str.length()];
  	str.getChars(0,
				         str.length(),
				         c,
				         0);
  	for (int i = 0; i < c.length; i++)
  		c[i] = (char)-c[i];
    result_ = new AnyString (c);
  }
  
  public void visitDecimal (Decimal d)
  {
    result_ = new AnyBigDecimal (d.getValue().negate());
  }
  
  public void visitAnyDate (DateI d)
  {
    // Negate is supported on dates.  Meaningless in calendar terms
    // but means that expressions like -date can be used in
    // sorting constructs to get date-descending.
    result_ = new AnyDate (0 - d.getTime());
  }

  public void visitAnyObject (ObjectI o)
  {
  	// Breaks a number of rules:
  	// 1. Assumes an AnyCollationKey
  	// 2. Negating an AnyCollationKey that has been negated does not yield
  	//    the original function (it stays negated)
  	// 3. Returns the original object, not a new one
  	// So a bit dirty but convenient for descending string sorts.
  	AnyCollationKey k      = (AnyCollationKey)o;
  	k.negate();
    result_ = k;
  }

  protected Any handleNullOperands(Any res1,
                                   Any res2,
                                   Any op1,
                                   Any op2) throws AnyException
  {
    if (res1 == null)
      notResolved(op1);

    return null;
  }
}
