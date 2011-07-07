/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;


/**
 * Overrides the ranking process so that value null is passed
 * and can be typed, for those operations that require it. 
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public abstract class PassNull extends OperatorVisitor
{
  protected void init()
  {
    rankV_ = new RankIfNull();
  }
  
  // Overrides the forcing of the operand to AnyNull if the value is
  // null. In these cases we still say the type is numeric, of course
  protected class RankIfNull extends RankVisitor
  {
    public void visitAnyChar (CharI c)
    {
      super.visitAnyChar(c);
      any_  = c;
    }

    public void visitAnyInt (IntI i)
    {
      super.visitAnyInt(i);
      any_  = i;
    }

    public void visitAnyShort (ShortI s)
    {
      super.visitAnyShort(s);
      any_  = s;
    }

    public void visitAnyLong (LongI l)
    {
      super.visitAnyLong(l);
      any_  = l;
    }

    public void visitAnyFloat (FloatI f)
    {
      super.visitAnyFloat(f);
      any_  = f;
    }

    public void visitAnyDouble (DoubleI d)
    {
      super.visitAnyDouble(d);
      any_  = d;
    }

    public void visitDecimal (Decimal d)
    {
      super.visitDecimal(d);
      any_  = d;
    }
  }
}
