/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Expire.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Expire the given BOT.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Expire extends    AbstractFunc
									implements Cloneable
{
  private Any descriptor_;  // Managed object descriptor
  
  /**
   * Expire the given descriptor.
   */
  public Expire(Any descriptor)
  {
    descriptor_     = descriptor;
  }

  public Any exec(Any a) throws AnyException
  {
  	//System.out.println ("Expire.exec() : descriptor : " + descriptor_);
		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   descriptor_,
		                                   Descriptor.class);

    if (descriptor == null)
      nullOperand(descriptor_);
    
    descriptor.expire(getTransaction());
    
	  return descriptor;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(descriptor_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Expire e = (Expire)super.clone();
    
    e.descriptor_    = descriptor_.cloneAny();        
    
    return e;
  }
}
