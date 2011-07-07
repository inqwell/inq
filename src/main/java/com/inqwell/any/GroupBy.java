/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GroupBy.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:19 $ 
 * $Author: sanderst $
 * @version $Revision: 1.5 $
 * @see 
 */

package com.inqwell.any;

/**
 * A combination of iteration and execution that can be used to effect
 * a <i>group by</i> function.
 * Iterate over the children of a given <i>node</i> and execute the
 * given <i>distinct</i>expression using each child as the context.
 * <p>
 * As each new distinct value is found, the <i>start</i> expression
 * is executed using the current child as the context.
 * The <code>@name</code> symbol yields the current distinct value at
 * this point.  The return value of the expression is retained for
 * use as the context node for the <i>end</i> expression, if there
 * is one.
 * <p>
 * If there is a <i>foreach</i> expression then this is executed using
 * the current child as the context.  The <code>@name</code> is the
 * distinct value (new or existing) as above.
 * <p>
 * When the iteration is complete, if there is an <i>end</i> expression
 * then this is executed for each distinct value found, using the return
 * value of the corresponding execution of the <i>start</i> expression
 * as the context and establishing the <code>@name</code> as the
 * distinct value.
 * <p>
 * This function returns ....
 * 
 */
public class GroupBy extends    AbstractFunc
										 implements Cloneable
{
	
  private Any root_;
  private Any distinct_;   // mandatory
  private Any start_;      // mandatory
  private Any foreach_;    // optional
  private Any end_;        // optional

	/**
	 * 
	 */
  public GroupBy(Any root,
                 Any distinct,
                 Any start,
                 Any foreach,
                 Any end)
  {
    root_       = root;
    distinct_   = distinct;
    start_      = start;
    foreach_    = foreach;
    end_        = end;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
		Any root = EvalExpr.evalFunc(getTransaction(),
																 a,
																 root_);

		Any res = AnyBoolean.FALSE;
		
    // If the root didn't resolve, return false.
		if (root == null || root == AnyNull.instance())
      return res;
    
    Map m = null;
    if (root instanceof Map)
      m = (Map)root;

    // The various functions executed during the iteration(s)
    // must be passed as FuncHolder instances.  This allows us
    // to parameterise the groupby expression itself.
    AnyFuncHolder.FuncHolder distinctF =
      (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(t,
                                                  a,
                                                  distinct_,
                                                  AnyFuncHolder.FuncHolder.class);
    if (distinctF == null)
      throw new AnyException("distinct function cannot be null");
    
    
    AnyFuncHolder.FuncHolder startF =
      (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(t,
                                                  a,
                                                  start_,
                                                  AnyFuncHolder.FuncHolder.class);
    
    if (startF == null)
      throw new AnyException("start function cannot be null");
    
    AnyFuncHolder.FuncHolder foreachF =
      (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(t,
                                                  a,
                                                  foreach_,
                                                  AnyFuncHolder.FuncHolder.class);
    
    AnyFuncHolder.FuncHolder endF =
      (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(t,
                                                  a,
                                                  end_,
                                                  AnyFuncHolder.FuncHolder.class);
    
		Iter   i         = (m == null) ? root.createIterator()
                                   : m.createKeysIterator();

    AnyInt iterCount = null;
    Map    stack     = null;
    
		Iter currentIter = t.getIter();
    Any  loop        = t.getLoop();
    
		try
		{
      AnyOrderedMap distinctValues = null;
			while (i.hasNext())
			{
				Any child = i.next();
        
        if (iterCount == null)
        {
          iterCount = new AnyInt();
          stack = t.getCurrentStackFrame();
          stack.replaceItem(NodeSpecification.atIterCount__, iterCount);
        }
				
        if (m != null)
        {
          child = m.get(child);
        }
          
        // Set $loop
        t.setLoop(child);
        
				// Always reset the iterator in case expression does
				// a commit, which is at liberty to clear the iterator
				t.setIter(i);

        if (distinctValues == null)
          distinctValues = new AnyOrderedMap();
        
        // Run the distinct value function for the current iteration
        Any distinctValue = distinctF.doFunc(t, null, a);
        if (distinctValue == null)
          throw new AnyException("distinct value cannot evaluate to null");
        
        // If the null constant is returned it means ignore this
        // iteration in the grouping. If the application requires
        // some sort of "catch-all" then it has to make up a value
        // itself that it knows is distinct from any of those possible
        // in the set being grouped.
        if (AnyNull.isNullInstance(distinctValue))
          continue;
        
        distinctValue = AbstractAny.ripSafe(distinctValue, t);
        

        // Put the distinct value on the stack as @name
        stack.replaceItem(NodeSpecification.name__, distinctValue);

        if (!distinctValues.contains(distinctValue))
        {
          // The distinct value hasn't been seen yet - run the start
          // expression and lodge its return value in the map
          Any startValue = startF.doFunc(t, null, a);
          startValue = AbstractAny.ripSafe(startValue, t);
          
          if (startValue == null)
            throw new AnyException("start value cannot evaluate to null");
          
          distinctValues.add(distinctValue, startValue);
				}
        
        // Run any foreach expression
        if (foreachF != null)
        {
          foreachF.doFunc(t, null, a);
        }
        
        // update the counter after each successful iteration.
        iterCount.setValue(iterCount.getValue()+1);

        // Loop completed at least once
        res = AnyBoolean.TRUE;
        
			} // iteration over root
      
      // If there is an end expression then execute it once for each distinct
      // value.
      if (endF != null && distinctValues != null)
      {
        iterCount.setValue(0);
        i = distinctValues.createKeysIterator();
        while (i.hasNext())
        {
          t.setIter(i);
          Any distinctValue = i.next();
          Any distinctContext = distinctValues.get(distinctValue);

          stack.replaceItem(NodeSpecification.name__, distinctValue);

          t.setLoop(distinctContext);
          endF.doFunc(getTransaction(), null, a);

          iterCount.setValue(iterCount.getValue()+1);
        }
      }
		}
		finally
		{
			t.setIter(currentIter);
      t.setLoop(loop);
      if (stack != null)
      {
        stack.remove(NodeSpecification.atIterCount__);
        if (stack.contains(NodeSpecification.name__))
          stack.remove(NodeSpecification.name__);
      }
		}
    
		return res;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(root_);
		a.add(distinct_);
		a.add(start_);
    if (foreach_ != null)
      a.add(foreach_);
    if (end_ != null)
      a.add(end_);
      
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    GroupBy g = (GroupBy)super.clone();
    
    g.root_       = root_.cloneAny();
    g.distinct_   = distinct_.cloneAny();
    g.start_      = start_.cloneAny();
    g.foreach_    = AbstractAny.cloneOrNull(foreach_);
    g.end_        = AbstractAny.cloneOrNull(end_);
    
    return g;
  }
}
