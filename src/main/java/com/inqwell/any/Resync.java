/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Resync.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Resync the instance of the given BOT/unique key value.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Resync extends    AbstractFunc
                    implements Cloneable
{
  private Any descriptor_;  // Managed object descriptor
  private Any uniqueKey_;
  
  /**
   * Resync the given descriptor.
   */
  public Resync(Any descriptor, Any uniqueKey)
  {
    descriptor_     = descriptor;
    uniqueKey_      = uniqueKey;
  }

  public Any exec(Any a) throws AnyException
  {
  	//System.out.println ("Resync.exec() : descriptor : " + descriptor_);
		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   descriptor_,
		                                   Descriptor.class);

		Map        uniqueKey  = (Map)EvalExpr.evalFunc
                         							(getTransaction(),
		                                   a,
		                                   uniqueKey_,
		                                   Map.class);

    descriptor.resync(getTransaction().getProcess(), uniqueKey);
    
	  return descriptor;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(descriptor_);
  	a.add(uniqueKey_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Resync r = (Resync)super.clone();
    
    r.descriptor_    = descriptor_.cloneAny();        
    r.uniqueKey_     = uniqueKey_.cloneAny();        
    
    return r;
  }
}
