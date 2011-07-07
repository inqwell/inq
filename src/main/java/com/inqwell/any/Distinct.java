/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Distinct.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Applies the expression operand 2 to each of the children of the Map
 * operand 1.  The result of each execution of the expression is
 * checked as the iteration proceeds to verify it for distinctness
 * amongst the set. Non-distinct children of the Map are either removed
 * or the distinct ones transferred to the target, if specified.
 * Returns a set of the distinct values yielded by the expression.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Distinct extends    AbstractFunc
                      implements Cloneable
{
  private Any     distinctRoot_;
  private Func    distinctExpr_;
  private Any     target_;
  private boolean remove_ = false;

	/**
	 * Operand 1 must resolve to a <code>Map</code>.  Operand 2
	 * is applied to each of the children under operand 1 and is
	 * used to determine whether each child is distinct from
	 * those previously seen.  Non-distinct children are removed
   * from the Map (if remove_ is true).
	 */
  public Distinct(Any distinctRoot, Func distinctExpr)
  {
    this(distinctRoot, distinctExpr, null);
  }

	/**
	 * Operand 1 must resolve to a <code>Map</code>.  Operand 2
	 * is applied to each of the children under operand 1 and is
	 * used to determine whether each child is distinct from
	 * those previously seen.  Non-distinct children remain
   * in the Map.  The distinct children are moved to the Map
   * target.
	 */
  public Distinct(Any distinctRoot, Func distinctExpr, Any target)
  {
    distinctRoot_ = distinctRoot;
    distinctExpr_ = distinctExpr;
    target_       = target;
    
    // If we intend to move the distinct values to a target
    // then default remove_ to true.  Avoids any duplicate
    // parent violations that might happen otherwise.  If there's
    // no target then remove defaults to false and the structure
    // we iterate over will not be changed.
    if (target_ != null)
      remove_ = true;
  }

  public Any exec(Any a) throws AnyException
  {
		Map distinctRoot = (Map)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              distinctRoot_,
                                              Map.class);

    if (distinctRoot == null)
      throw new AnyException("Distinct root is null");

		Map target       = (Map)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              target_,
                                              Map.class);

    AnyFuncHolder.FuncHolder distinctF =
      (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(getTransaction(),
                                                  a,
                                                  distinctExpr_,
                                                  AnyFuncHolder.FuncHolder.class);
    if (distinctF == null)
      throw new AnyException("distinct function cannot be null");
    
    // Working set to check for distinctness and the return value
    Set distinct = AbstractComposite.set();
    
    Iter i = distinctRoot.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      Any v = distinctRoot.get(k);
      
      // Evaluate the distinct expression and place the
      // values in the working set
      Any dv = EvalExpr.evalFunc(getTransaction(),
                                 v,
                                 distinctExpr_);
      
      if (dv == null)
        throw new AnyException("Distinct value is null");
        
      if (!distinct.contains(dv))
      {
        distinct.add(dv);
        if (target != null && target != distinctRoot)
        {
          // specified target: maybe remove from source and add to
          // target
          if (remove_)
            i.remove();
          target.add(k, v);
        }
        // Otherwise just leave in the distinct root
      }
      else
      {
        if (target == null || target == distinctRoot)
        {
          // result left in root so remove anything already seen
          // if desired
          if (remove_)
            i.remove();
        }
      }
    }

	  return distinct;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(distinctRoot_);
  	a.add(distinctExpr_);
  	if (target_ != null)
      a.add(target_);
  	return a.createIterator();
  }

  public void setTarget(Any target)
  {
    target_ = target;
  }
  
  public void setRemove(boolean remove)
  {
    remove_ = remove;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Distinct d = (Distinct)super.clone();
    
    d.distinctRoot_ = distinctRoot_.cloneAny();        
    d.distinctExpr_ = (Func)distinctExpr_.cloneAny();
    d.target_       = AbstractAny.cloneOrNull(target_);
    
    return d;
  }
}
