/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ForEach.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:19 $ 
 * $Author: sanderst $
 * @version $Revision: 1.5 $
 * @see 
 */

package com.inqwell.any;

/**
 * Iterate over the children of a given node and execute the given
 * expression using each child as the context.
 * <p>
 * This function returns false if the loop is never entered,
 * true if the loop is executed at least once or the result
 * of an optional expression if a <code>break</code>
 * statement is executed.
 * 
 */
public class ForEach extends    AbstractFunc
										 implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any root_;
  private Any expression_;

	/**
	 * 
	 */
  public ForEach(Any root, Any expression)
  {
    root_       = root;
    expression_ = expression;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
		Any root = EvalExpr.evalFunc(t,
																 a,
																 root_);
    
    // In case the root is a property binding, force a read of it
    root = t.readProperty(root);

		Any res = AnyBoolean.FALSE;
		
		if (root == null)
      return res;
    
    Map m = null;
    if (root instanceof Map)
      m = (Map)root;

		Iter   i = makeIter(m, root);
    
    // Stack variables for this loop
    AnyInt     iterCount = null;
    BooleanI   iterFirst = null;
    BooleanI   iterLast  = null;
    Map        stack     = null;
    
    // Stack variables of any outer loop
    Any outerIterCount = null;
    Any outerIterFirst = null;
    Any outerIterLast  = null;
    Any outerIterName  = null;
    
		Iter currentIter = t.getIter();
    Any  currentLoop = t.getLoop();
		try
		{
      Any expression = null;
			while (i.hasNext())
			{
				Any child = i.next();
        
        if (iterCount == null)
        {
          // If there's an outer loop then get its stack variables, so
          // we can restore then when we're done
          stack = t.getCurrentStackFrame();
          if (stack.contains(NodeSpecification.atIterCount__))
          {
            outerIterCount = stack.get(NodeSpecification.atIterCount__);
            outerIterLast  = stack.get(NodeSpecification.atIterLast__);
            outerIterFirst = stack.get(NodeSpecification.atIterFirst__);
            
            // only if iterating over a map anyway
            if (stack.contains(NodeSpecification.name__))
              outerIterName  = stack.get(NodeSpecification.name__);
          }
          
          iterCount = new AnyInt();
          iterFirst = new AnyBoolean(true);
          iterLast  = new AnyBoolean();
          stack.replaceItem(NodeSpecification.atIterCount__, iterCount);
          stack.replaceItem(NodeSpecification.atIterFirst__, iterFirst);
          stack.replaceItem(NodeSpecification.atIterLast__,  iterLast);
        }
				
        if (m != null)
        {
          stack.replaceItem(NodeSpecification.name__, child);
          child = m.get(child);
        }
          
				// Always reset the iterator in case expression does
				// a commit, which is at liberty to clear the iterator
				t.setIter(i);
        
        t.setLoop(child);

        if (expression == null)
          expression = expression_.cloneAny();
          
				try
				{
          // Update the last flag before the iteration
          iterLast.setValue(!i.hasNext());
          
          EvalExpr.evalFunc(getTransaction(),
                            a,
                            expression);
          
          // update the counter after each successful iteration.
          iterCount.setValue(iterCount.getValue()+1);
          
          // Clear the first flag
          iterFirst.setValue(false);
				}
				catch (BreakException bex)
				{
          Any r = bex.getResult();
					if (r != null)
            res = r;
					break;
				}
				catch (ContinueException cex)
				{
          // update the first/counter after continue.
          iterCount.setValue(iterCount.getValue()+1);
          iterFirst.setValue(false);
					continue;
				}
        
        // Loop completed at least once
        res = AnyBoolean.TRUE;
			}
		}
		finally
		{
			t.setIter(currentIter);
      t.setLoop(currentLoop);
      if (stack != null)
      {
        // If there is an outer loop, put back its iter variables
        if (outerIterCount != null)
        {
          stack.replaceItem(NodeSpecification.atIterCount__, outerIterCount);
          stack.replaceItem(NodeSpecification.atIterFirst__, outerIterFirst);
          stack.replaceItem(NodeSpecification.atIterLast__,  outerIterLast);
          if (outerIterName != null)
            stack.replaceItem(NodeSpecification.name__, outerIterName);
        }
        else
        {
          stack.remove(NodeSpecification.atIterCount__);
          stack.remove(NodeSpecification.atIterFirst__);
          stack.remove(NodeSpecification.atIterLast__);
          if (m != null)
            stack.remove(NodeSpecification.name__);
        }        
      }
		}
		
		return res;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(root_);
		a.add(expression_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    ForEach f = (ForEach)super.clone();
    
    f.root_       = root_.cloneAny();
    //f.expression_ = expression_.cloneAny();  Clone as late as possible
    
    return f;
  }
  
  protected Iter makeIter(Map m, Any root)
  {
    Iter   i = (m == null) ? root.createIterator()
                           : m.createKeysIterator();
    
    return i;
  }
}
