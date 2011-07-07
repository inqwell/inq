/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/In.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $ 
 * $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

/**
 * Returns <code>true</code> if the first operand is contained
 * within the given list of items.
 */
public class In extends    AbstractFunc
								implements Cloneable
{
	
  private Any op1_;
  private Any inList_;

	/**
	 * 
	 */
  public In(Any op1, Any inList)
  {
    op1_    = op1;
    inList_ = inList;
  }

  public Any exec(Any a) throws AnyException
  {
		Any op1 = EvalExpr.evalFunc(getTransaction(),
																a,
																op1_);

		// Resolve to the list - it might be an expression!
		Any inList = EvalExpr.evalFunc(getTransaction(),
																	 a,
																	 inList_);

		Any ret = null;
		Set  s = AbstractComposite.set();
		Iter i = inList.createIterator();

		while (i.hasNext())
		{
			Any child = i.next();
			
			// Evaluate each child in case its an expression
			Any res = EvalExpr.evalFunc(getTransaction(),
																	a,
																	child);
			s.add(res);
		}
		
		return new AnyBoolean(s.contains(op1));
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(op1_);
		a.add(inList_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    In i = (In)super.clone();
    
    i.op1_    = op1_.cloneAny();
    i.inList_ = inList_.cloneAny();
    
    return i;
  }
}
