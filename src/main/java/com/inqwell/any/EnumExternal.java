/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EnumExternal.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Yield the internal enumerated value for the given
 * typedef, field and value symbolic name.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class EnumExternal extends    AbstractFunc
                          implements Cloneable
{
  private Locate expr_;
  
  public EnumExternal(Locate expr)
  {
    expr_ = expr;
  }

  public Any exec(Any a) throws AnyException
  {
		Any expr = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 expr_);

    if (expr == null)
      return AnyString.EMPTY;
    
    Map parent = expr_.getMapParent();
    Any last   = expr_.getPath();
    
    Descriptor d = parent.getDescriptor();
    
    if (d == Descriptor.degenerateDescriptor__)
    {
      if (!(expr instanceof NativeDescriptor.NativeEnumProto))
        operandError(expr_, "Cannot determine operand's type");
      
      d = ((NativeDescriptor.NativeEnumProto)expr).getDescriptor();
      last = d.getName();
    }
    else
    {
      if (!d.isEnum(last))
        operandError(expr_, "is not an enumerated field");
    }
    
    if (expr instanceof Value)
    {
      Value v = (Value)expr;
      if (v.isNull())
        return AnyNull.instance();
    }
    
    Map enums = d.getEnums();
    enums = (Map)enums.get(last);
    
	  return enums.get(expr);
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(expr_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    EnumExternal e = (EnumExternal)super.clone();
    
    e.expr_    = (Locate)expr_.cloneAny();        

    return e;
  }
}
