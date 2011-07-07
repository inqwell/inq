/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/IsType.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Return the typedef of the given managed object instance.
 * If the given object is not a typedef instance then an exception
 * is thrown
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class IsType extends    AbstractFunc
                     implements Cloneable
{
  private Any any_;
  private Any type_;

  public IsType(Any any, Any type)
  {
    any_  = any;
    type_ = type;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Any am = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   any_);

    if (am == null)
      nullOperand(any_);
    
    Any type = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 type_);

    if (type == null)
      nullOperand(type_);

    TypeResolver tr = new TypeResolver();
    am.accept(tr);
        
    Any resolvedType = tr.getResult();
    
    return new AnyBoolean(resolvedType == type);
  }

  public Object clone () throws CloneNotSupportedException
  {
    IsType g = (IsType)super.clone();
    
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
