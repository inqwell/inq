/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Merge.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Merge the second <code>Map</code> operand into the first leaving
 * the original intersection of the two in the second.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Merge extends    AbstractFunc
									    implements Cloneable
{
	
	private Any mergeTo_;
	private Any mergeFrom_;
	
	public Merge(Any mergeTo, Any mergeFrom)
	{
		mergeTo_   = mergeTo;
		mergeFrom_ = mergeFrom;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Map mergeTo   = (Map)EvalExpr.evalFunc(getTransaction(),
                                           a,
                                           mergeTo_,
                                           Map.class);

		Map mergeFrom = (Map)EvalExpr.evalFunc(getTransaction(),
                                           a,
                                           mergeFrom_,
                                           Map.class);

		if (mergeTo == null)
      nullOperand(mergeTo_);

		if (mergeFrom == null)
      nullOperand(mergeFrom_);
    
    Iter i = mergeFrom.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      
      if (!mergeTo.contains(k))
      {
        Any v = mergeFrom.get(k);
        i.remove();
        mergeTo.add(k, v);
      }
    }

    // Propagate the nodeSet data to the target set so
    // that any subsequent aggregates will work.
    mergeTo.setNodeSet(mergeFrom.getNodeSet());
    
		return mergeTo;
	}
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
		a.add(mergeFrom_);
  	a.add(mergeTo_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		Merge m = (Merge)super.clone();
		m.mergeFrom_ = mergeFrom_.cloneAny();
		m.mergeTo_   = mergeTo_.cloneAny();
		return m;
  }
}
