/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DepthFirstIter.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.util.NoSuchElementException;

/**
 * Performs a depth-first iteration of the given Any structure, that
 * is the deepest descendent node at the current level is visited;
 * the next child is visited and so on.  The diagram below indicates
 * the order of visiting more clearly:
 * <pre>
 *
 *
 *                             Any(root)
 *                              |
 *                ----------------------------
 *               |                            |
 *              Any(4)                       Any(7)
 *               |                            |
 *       -------------------         --------------------
 *       |                  |        |                   |
 *      Any(2)             Any(3)   Any(5)              Any(6)
 *       | 
 *      Any(1) 
 *
 * </pre>
 * Note that the order of nodes returned at a given level is
 * dependent on the implementation and may be undefined.
 * <p>
 * Whether the root node is returned is optional.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class DepthFirstIter extends AbstractIter implements Iter
{
  private Iter      iter_;
  private Queue     q_;
  private Set       seen_;
  private IntI      i_;
  private IterLevel current_;
  private boolean   skipNodeSetChildren_ = false;
  private boolean   visitRoot_           = false;
	
  /**
   * Creates a depth first iterator to traverse the supplied Any
   */
  public DepthFirstIter(Any a)
  {
  	this(a, false);
  }

  /**
   * Creates a depth first iterator to traverse the supplied Any.
   * If <code>skipNodeSetChildren</code> is <code>false</code>
   * then node set maps will be visited, but their children will
   * not.
   */
  public DepthFirstIter(Any a, boolean skipNodeSetChildren, boolean cyclicSafe)
  {
    iter_ = a.createIterator();
    q_    = AbstractComposite.queue();
    q_.addLast(current_ = new IterLevel(visitRoot_ ? a : null,
                                        iter_));  // we don't return the origin node
    setSkipNodeSetChildren(skipNodeSetChildren);
    setCyclicSafe(cyclicSafe);
  }
  
  public DepthFirstIter(Any a, boolean skipNodeSetChildren)
  {
    this(a, skipNodeSetChildren, false);
  }

  public DepthFirstIter()
  {
    q_    = AbstractComposite.queue();
  }

  public boolean hasNext()
  {
  	if (current_.node_ != null)
  	  return true;
 
  	while ((!iter_.hasNext() && q_.entries() != 0))
  	{
  		IterLevel i = (IterLevel)q_.removeLast();
  		iter_    = i.iter_;
  		current_ = i;
  	}

    return (iter_.hasNext() || current_.node_ != null);
  }

  public Any next()
  {
  	if (iter_.hasNext())
  	  return doNext(iter_.next());

    Any ret = current_.node_;
    current_.node_ = null;
    
    if (q_.entries() != 0)
    {
  		IterLevel i = (IterLevel)q_.removeLast();
  		iter_    = i.iter_;
  		current_ = i;
    }
    
    if (ret == null)
      throw new NoSuchElementException();

    return ret;
  }
  
  private Any doNext(Any a)
  {
  	if (a == null)
  	  return a;

  	if (skipNodeSetChildren_ && (a instanceof Map))
  	{
  		Map m = (Map)a;
			if (m.getNodeSet() != null)
			  return a;
  	}
  	
    if (!visited(a))
    {
    	Iter i = a.createIterator();
    	if (i.hasNext())
    	{
    		q_.addLast(current_);
    		iter_ = i;
    	  current_ = new IterLevel(a, iter_);
    		return doNext(iter_.next());
    	}
    }
    return a;
  }
  
  
  public void remove()
  {
    throw (new java.lang.UnsupportedOperationException(getClass().toString()));
  }
  
 /**
  * Resets the iterator to the root of a new structure.
  * @param root the new structure to iterate over.  Can be null
  * to ensure dangling references are cleared.
  */
  public void reset (Any root)
  {
    q_.empty();
    iter_    = null;
    current_ = null;
    if (root != null)
    {
			iter_ = root.createIterator();
			q_.addLast(current_ = new IterLevel(visitRoot_ ? root : null, iter_));
    }

    if (seen_ != null)
      seen_.empty();
  }
  
	public void setSkipNodeSetChildren(boolean skip)
	{
		skipNodeSetChildren_ = skip;
	}
	
	public void setVisitRoot(boolean visitRoot)
	{
		visitRoot_ = visitRoot;
	}
	
	/**
	 *  Returns the parent of the current iteration depth
	 * @return
	 */
	public Any getParent()
	{
	  if (current_ == null)
	    throw new IllegalStateException();
	  
	  return current_.node_;
	}
	
  public void setCyclicSafe(boolean cyclicSafe)
  {
    if (cyclicSafe)
    {
      seen_ = AbstractComposite.set();
      i_    = new AnyInt();
    }
    else
    {
      seen_ = null;
      i_    = null;
    }
  }

  private boolean visited(Any a)
  {
    if (seen_ != null)
    {
      i_.setValue(System.identityHashCode(a));
      if (seen_.contains(i_))
        return true;

      seen_.add(new ConstInt(System.identityHashCode(a)));
    }
    return false;
  }

  private static class IterLevel extends AbstractAny
	{
		private Any  node_;
		private Iter iter_;
		
		IterLevel(Any node, Iter iter)
		{
			node_ = node;
			iter_ = iter;
		}
	}
}  
