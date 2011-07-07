/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EnumStaticExternal.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Yield the external enumerated value for the given
 * typedef, field and value symbolic name.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class EnumStaticExternal extends    AbstractFunc
                                implements Cloneable
{
  private Any descriptor_;  // Managed object descriptor
  private Any field_;
  private Any symbol_;
  
  /**
   * EnumStaticExternal the given descriptor.
   */
  public EnumStaticExternal(Any descriptor, Any field, Any symbol)
  {
    descriptor_     = descriptor;
    field_          = field;
    symbol_         = symbol;
  }

  public Any exec(Any a) throws AnyException
  {
  	//System.out.println ("EnumStaticExternal.exec() : descriptor : " + descriptor_);
		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   descriptor_,
		                                   Descriptor.class);

		Any field  = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   field_);

		Any symbol = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   symbol_);

    if (descriptor == null)
      nullOperand(descriptor_);
    
    Map enums   = descriptor.getEnums();
    Map symbols = descriptor.getEnumSymbols();
    
    if (enums == null)
      operandError(descriptor_, "has no enumerations");
    
    //System.out.println("ENUMS " + enums);
    //System.out.println("SYMS " + symbols);
    
    Map e = (Map)enums.get(field);
    Map s = (Map)symbols.get(field);
    //System.out.println("E  " + e);
    //System.out.println("S  " + s);
    
    Any ret = e.getIfContains(s.getIfContains(symbol));
    
    if (ret == null)
    {
      // May be we've been given an internal value, not a symbol
      ret = e.getIfContains(symbol);
      
      // try the null symbol if the enums defined one
      //if (symbol == null)
      //  symbol = AnyNull.instance();
      
      //ret = e.getIfContains(s.getIfContains(symbol));
    }
    
	  return ret;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(descriptor_);
  	a.add(field_);
  	a.add(symbol_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    EnumStaticExternal e = (EnumStaticExternal)super.clone();
    
    e.descriptor_    = descriptor_.cloneAny();        
    // readonly e.field_         = field_.cloneAny();   static from parser        
    e.symbol_        = symbol_.cloneAny();        
    
    return e;
  }
}
