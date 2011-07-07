/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ContainsValue.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return whether the first operand (a Map) contains
 * the second as a mapped value.  Result is an AnyBoolean.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class ContainsValue extends    AbstractFunc
                           implements Cloneable
{
	
	private Any container_;
	private Any contained_;
	
	public ContainsValue(Any container, Any contained)
	{
		container_ = container;
		contained_ = contained;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Map container = (Map)EvalExpr.evalFunc(getTransaction(),
                                           a,
                                           container_,
                                           Map.class);

		Any contained = EvalExpr.evalFunc(getTransaction(),
																		  a,
																		  contained_);

		AnyBoolean ret = new AnyBoolean();
		
		if (container != null)
		{
			ret.setValue(container.containsValue(contained));
		}

		return ret;
	}
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(container_);
		a.add(contained_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		ContainsValue c = (ContainsValue)super.clone();
		c.container_ = container_.cloneAny();
		c.contained_ = contained_.cloneAny();
		return c;
  }
}
