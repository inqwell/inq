/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Contains.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Return whether the first operand contains
 * the second as an AnyBoolean.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Contains extends    AbstractFunc
									    implements Cloneable
{
	
	private Any container_;
	private Any contained_;
	
	public Contains(Any container, Any contained)
	{
		container_ = container;
		contained_ = contained;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Composite container = (Composite)EvalExpr.evalFunc(getTransaction(),
																											 a,
																											 container_,
																											 Composite.class);

		Any contained = EvalExpr.evalFunc(getTransaction(),
																		  a,
																		  contained_);

		AnyBoolean ret = new AnyBoolean();
		
		if (container != null)
		{
			ret.setValue(container.contains(contained));
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
		Contains c = (Contains)super.clone();
		c.container_ = container_.cloneAny();
		c.contained_ = contained_.cloneAny();
		return c;
  }
}
