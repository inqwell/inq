/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Stdout.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

/**
 * Cheesy access to Stdout for debugging inq scripts!
 */
public class Stdout extends    AbstractFunc
										implements Cloneable
{
	
  private Any any_;

	/**
	 * 
	 */
  public Stdout(Any any)
  {
    any_  = any;
  }

  public Any exec(Any a) throws AnyException
  {
		Any any = EvalExpr.evalFunc(getTransaction(),
																a,
																any_);
		
		System.out.println(any);
		return any;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(any_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Stdout s = (Stdout)super.clone();
    
    s.any_     = any_.cloneAny();
    
    return s;
  }
}
