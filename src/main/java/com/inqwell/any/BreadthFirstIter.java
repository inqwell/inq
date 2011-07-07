/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/BreadthFirstIter.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Performs a breadth-first iteration of the given Any structure, that is
 * all the nodes at the current level are visited; the first node at the
 * current level is then descended and so on.  The diagram below indicates
 * the order of visiting more clearly:
 * <pre>
 *
 *
 *                             Any(root)
 *                              |
 *                ----------------------------
 *               |                            |
 *              Any(1)                       Any(2)
 *               |                            |
 *       -------------------         --------------------
 *       |                  |        |                   |
 *      Any(3)             Any(4)   Any(5)              Any(6)
 *       | 
 *      Any(7) 
 *
 * </pre>
 * This iterator is safe against cyclic paths in the structure
 * and will only visit a given node once.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class BreadthFirstIter extends AbstractIter implements Iter
{
  private Iter    iter_;
  private Queue   q_;
  private Set     seen_;
  private IntI    i_;
  private Func    descendInto_;
	
  /**
   * Creates a breadth first iterator to traverse the supplied Any
   */
  public BreadthFirstIter(Any a)
  {
    iter_ = a.createIterator();
    q_    = AbstractComposite.queue();
    seen_ = null;
  }

  /**
   * Creates a breadth first iterator to traverse the supplied Any.
   * If <code>skipNodeSetChildren</code> is <code>false</code>
   * then node set maps will be visited, but their children will
   * not.
   */
  public BreadthFirstIter(Any a, Func descendInto)
  {
    iter_ = a.createIterator();
    q_    = AbstractComposite.queue();
    seen_ = null;
    setDescendInto(descendInto);
  }

  public BreadthFirstIter()
  {
    q_    = AbstractComposite.queue();
    seen_ = null;
  }

  public boolean hasNext()
  {
    while (!iter_.hasNext())
    {
      // Current iterator level is exhausted - move on to the next one, if any
      if (q_.entries() == 0)
        return false;
        
      Any a = q_.removeFirst();
      
      if (a == null)
      {
				// null child, skip
				continue;
			}
			
      iter_ = a.createIterator();
    }
    return true;
  }

  public Any next()
  {
    Any a = iter_.next();
    if (descendInto_ != null)
    {
      try
      {
        if (descendInto_.exec(a) == null)
  			  return a;
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
		}
		
    if (!visited(a))  // Guard against infinite loop when circular reference path
      q_.addLast(a);
      
    return a;
  }
  
  /**
   * Do not descend from the current node.  Can be called at any
   * point during the iteration passing the current node. This node
   * will not then be descended when the current level is exhausted.
   * <p>
   * If the current node is a node set and
   * <code>setSkipNodeSetChildren(true)</code> was called then the
   * current node will not be descended anyway and this method has
   * no effect. If this method is called with a node that is not the
   * current node then this method also has no effect.
   */
  public void skipCurrent(Any a)
  {
    if ((q_.entries() != 0) && (q_.getLast() == a))
      q_.removeLast();
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
    if (seen_ != null)
      seen_.empty();
    iter_ = null;
    if (root != null)
			iter_ = root.createIterator();
  }
  
	public void setDescendInto(Func descendInto)
	{
	  descendInto_ = descendInto;
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
}
