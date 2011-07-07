/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/IndexOf.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Return index in first operand of the second as an AnyInt. Returns -1
 * if the second operand is not contained within the first.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class IndexOf extends    AbstractFunc
									   implements Cloneable
{

	private Any container_;
	private Any contained_;

	public IndexOf(Any container, Any contained)
	{
		container_ = container;
		contained_ = contained;
	}

	public Any exec(Any a) throws AnyException
	{
		Vectored container = (Vectored)EvalExpr.evalFunc(getTransaction(),
																											 a,
																											 container_,
																											 Vectored.class);

		Any contained = EvalExpr.evalFunc(getTransaction(),
																		  a,
																		  contained_);

    if (container == null)
      nullOperand(container_);
    
		AnyInt ret = new AnyInt(-1);

		ret.setValue(container.indexOf(contained));

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
		IndexOf c = (IndexOf)super.clone();
		c.container_ = container_.cloneAny();
		c.contained_ = contained_.cloneAny();
		return c;
  }
}
