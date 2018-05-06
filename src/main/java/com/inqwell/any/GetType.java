/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetType.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the type of the argument.
 * If the argument is a typedef instance then its typedef is
 * returned. Maps that are not typedef instances will return the
 * degenerate typedef.
 * <p>
 * If the argument is one of the built-in fundamental types then
 * the appropriate type literal is returned.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class GetType extends    AbstractFunc
                     implements Cloneable
{
	private Any any_;

  public GetType(Any any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any am = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   any_);

    if (am == null)
      nullOperand(any_);
    
    TypeResolver tr = new TypeResolver();
    am.accept(tr);
        
    return tr.getResult();
	}

  public Object clone () throws CloneNotSupportedException
  {
    GetType g = (GetType)super.clone();
    
    g.any_   = any_.cloneAny();
    
    return g;
  }
  
  static private class TypeResolver extends AbstractVisitor
  {
    private Any result_;
    
    private Any getResult()
    {
      Any r = result_;
      result_ = null;
      return r;
    }
  
    public void visitMap (Map m)
    {
      result_ = m.getDescriptor();
    }
    
    public void visitAnyDate (DateI d)
    {
      result_ = AnyDate.class__;
    }
  
    public void visitAnyByte (ByteI b)
    {
      result_ = AnyByte.class__;
    }

    public void visitAnyBoolean (BooleanI b) { result_ = AnyBoolean.class__; }

    public void visitAnyChar (CharI c)
    {
      result_ = AnyChar.class__;
    }
  
    public void visitAnyInt (IntI i)
    {
      result_ = AnyInt.class__;
    }
  
    public void visitAnyShort (ShortI s)
    {
      result_ = AnyShort.class__;
    }
  
    public void visitAnyLong (LongI l)
    {
      result_ = AnyLong.class__;
    }
  
    public void visitAnyFloat (FloatI f)
    {
      result_ = AnyFloat.class__;
    }
  
    public void visitAnyDouble (DoubleI d)
    {
      result_ = AnyDouble.class__;
    }
  
    public void visitDecimal (Decimal d)
    {
      result_ = AnyBigDecimal.class__;
    }
  
    public void visitAnyString (StringI s)
    {
      result_ = AnyString.class__;
    }
  }
}
