/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ContainsAny.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Return whether the first operand contains
 * all of the children of the second as an AnyBoolean.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class ContainsAny extends    AbstractFunc
                         implements Cloneable
{
	
	private Any container_;
	private Any contains_;
	
	public ContainsAny(Any container, Any contains)
	{
		container_ = container;
		contains_  = contains;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Composite container = (Composite)EvalExpr.evalFunc(getTransaction(),
																											 a,
																											 container_,
																											 Composite.class);
    
    if (container == null)
      throw new AnyException("Did not resolve container");

		Composite contains  = (Composite)EvalExpr.evalFunc(getTransaction(),
                                                       a,
                                                       contains_,
                                                       Composite.class);

    if (contains == null)
      throw new AnyException("Did not resolve contains");

		AnyBoolean ret = new AnyBoolean();
		
    ret.setValue(container.containsAny(contains));

    return ret;
	}
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(container_);
		a.add(contains_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		ContainsAny c = (ContainsAny)super.clone();
		c.container_  = container_.cloneAny();
		c.contains_   = contains_.cloneAny();
		return c;
  }
}
