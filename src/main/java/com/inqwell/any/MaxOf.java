/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MaxOf.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $ 
 * $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any;

/**
 * Iterate over the children of a given node and execute the given
 * expression using each child as the context.  Determine the
 * maximum value of the result of the expression with
 * successive values to give the maximum for the set.
 * <p>
 * The first iteration is used to provide the initial value.
 * Subsequent iterations are compared as if
 * using the <code>max(x, y)</code> function to yield the
 * maximum value for the set.
 * <p>
 * The result is a new object of the type of those under comparison,
 * so expressions like
 * <code><pre>
 *    any maxValue = max(mySet, $this.MyType.MyValue);
 * </pre></code>
 * are safe in the face of transactional objects and field ripping.
 * <p>
 * Given that the same expresison is used throughout the
 * iteration, it would generally be the case that the set is
 * homogeneous.  However this is not a requirement and if the
 * precision of the expression result varies then that of the
 * return value will be the highest encountered.  Note that the
 * non-integral types <code>float</code> and <code>double</code>
 * cannot be combined with fixed precision decimals.
 * <p>
 * The return value is the maximum (or only) value in the set or
 * the nodeset child if the optional childMode argument is true,
 * or <code>null</code> if the set is empty.  If the set is
 * not resolved or if the expression cannot be resolved on
 * any given iteration then a <code>NullPointerException</code>
 * is thrown.
 */
public class MaxOf extends    AbstractFunc
                   implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any root_;
  private Any expression_;
  private Any childMode_;
  
//  private static     LogManager lm = LogManager.getLogManager();
//  private static     Logger l = lm.getLogger("inq");

	/**
	 * 
	 */
  public MaxOf(Any root, Any expression, Any childMode)
  {
    root_       = root;
    expression_ = expression;
    childMode_  = childMode;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
		Any root = EvalExpr.evalFunc(t,
																 a,
																 root_);

		if (root == null)
      throw new NullPointerException("Null set");
    
    BooleanI childMode = (BooleanI)EvalExpr.evalFunc(t,
                                                     a,
                                                     childMode_,
                                                     BooleanI.class);

    if (childMode == null && childMode_ != null)
      nullOperand(childMode_);

		Any res = null;
    
		Iter i = root.createIterator();

    Any      expression = null;
    Any      max        = null;
    Any      maxChild   = null;
    EvalExpr maxop      = null;
    
    Any  curLoop = t.getLoop();
    
    try
    {
      while (i.hasNext())
      {
        Any child = i.next();
        t.setLoop(child);
        
        // Lazy clone of expression
        if (expression == null)
        {
          expression = expression_.cloneAny();
          maxop      = new EvalExpr(null, null, new Max());
          maxop.setLineNumber(getLineNumber());
          maxop.setTransaction(t);
        }
        
        if (max == null)
        {
          // First iteration, establish max
          max = EvalExpr.evalFunc(t,
                                  a,
                                  expression);
          if (max == null)
            throw new NullPointerException("Null expression");
          
          maxChild = child;
        }
        else
        {
          Any next = EvalExpr.evalFunc(t,
                                       a,
                                       expression);
          if (next == null)
            throw new NullPointerException("Null expression");
          
          maxop.setOp1(max);
          maxop.setOp2(next);
          Any newMax = EvalExpr.evalFunc(t, a, maxop);
          
          // If there is a new maximum then set the candidate child to the
          // current child also.
          if (newMax.equals(next))
          {
            maxChild = child;
          }
  
          max = newMax;
        }
        res = max;
      }
    }
    finally
    {
      t.setLoop(curLoop);
    }

    if (childMode != null && childMode.getValue())
      res = maxChild;
    
    if (res != null && res != maxChild)
      res = res.cloneAny();
    
    if (res == null)
      res = AnyNull.instance();
      
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
    MaxOf m = (MaxOf)super.clone();
    
    m.root_       = root_.cloneAny();
    //expression cloned as late as possible
    
    m.childMode_  = AbstractAny.cloneOrNull(childMode_);

    return m;
  }
}
