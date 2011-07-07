/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Enum.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Yield the internal enumerated value for the given
 * typedef, field and value symbolic name.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Enum extends    AbstractFunc
									implements Cloneable
{
  private Any descriptor_;  // Managed object descriptor
  private Any field_;
  private Any symbol_;
  
  /**
   * Enum the given descriptor.
   */
  public Enum(Any descriptor, Any field, Any symbol)
  {
    descriptor_     = descriptor;
    field_          = field;
    symbol_         = symbol;
  }

  public Any exec(Any a) throws AnyException
  {
  	//System.out.println ("Enum.exec() : descriptor : " + descriptor_);
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
      throw new AnyException("Could not resolve typedef " + descriptor_);
    
    Map symbols = descriptor.getEnumSymbols();
    
    if (symbols == null)
      throw new AnyException("Descriptor " + descriptor_ + " has no symbols");
    
    Map m = (Map)symbols.getIfContains(field);
    
    if (m == null)
      throw new AnyException("Field " + field + " of " + descriptor_ + " is not an enum or does not exist");
    
    Any ret = m.getIfContains(symbol);
    
    if (ret == null)
      throw new AnyException("Symbol " + symbol + " is not valid");
    
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
    Enum e = (Enum)super.clone();
    
    e.descriptor_    = descriptor_.cloneAny();        
    e.field_         = field_.cloneAny();        
    e.symbol_        = symbol_.cloneAny();        
    
    return e;
  }
}
