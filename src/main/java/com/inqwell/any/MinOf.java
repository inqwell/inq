/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MinOf.java $
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
 * minimum value of the result of the expression with
 * successive values to give the minimum for the set.
 * <p>
 * The first iteration is used to provide the initial value.
 * Subsequent iterations are compared as if
 * using the <code>min(x, y)</code> function to yield the
 * minimum value for the set.
 * <p>
 * The result is a new object of the type of those under comparison,
 * so expressions like
 * <code><pre>
 *    any minValue = min(mySet, $this.MyType.MyValue);
 * </pre></code>
 * are safe in the face of transactional objects and field ripping.
 * <p>
 * Given that the same expression is used throughout the
 * iteration, it would generally be the case that the set is
 * homogeneous.  However this is not a requirement and if the
 * precision of the expression result varies then that of the
 * return value will be the highest encountered.  Note that the
 * non-integral types <code>float</code> and <code>double</code>
 * cannot be combined with fixed precision decimals.
 * <p>
 * The return value is the minimum (or only) value in the set or
 * the nodeset child if the optional childMode argument is true,
 * or <code>null</code> if the set is empty.  If the set is
 * not resolved or if the expression cannot be resolved on
 * any given iteration then a <code>NullPointerException</code>
 * is thrown.
 */
public class MinOf extends    AbstractFunc
                   implements Cloneable
{
	
  private Any root_;
  private Any expression_;
  private Any childMode_;

//  private static     LogManager lm = LogManager.getLogManager();
//  private static     Logger l = lm.getLogger("inq");

	/**
	 * 
	 */
  public MinOf(Any root, Any expression, Any childMode)
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
    Any      min        = null;
    Any      minChild   = null;
    EvalExpr minop      = null;
    
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
          minop      = new EvalExpr(null, null, new Min());
          minop.setLineNumber(getLineNumber());
          minop.setTransaction(t);
        }
        
        if (min == null)
        {
          min = EvalExpr.evalFunc(t,
                                  a,
                                  expression);
          if (min == null)
            throw new NullPointerException("Null expression");
  
          minChild = child;
        }
        else
        {
          Any next = EvalExpr.evalFunc(t,
                                       a,
                                       expression);
          if (next == null)
            throw new NullPointerException("Null expression");
          
          minop.setOp1(min);
          minop.setOp2(next);
          Any newMin = EvalExpr.evalFunc(t, a, minop);
//          l.severe("min is " + min);
//          l.severe("next is " + next);
//          l.severe("newMin is " + newMin);
          
          // If there is a new minimum then set the candidate child to the
          // current child also.
          if (newMin.equals(next))
          {
            minChild = child;
//            l.severe("minChild now " + minChild);
          }
          
          min = newMin;
        }
        res = min;
      }
    }
    finally
    {
      t.setLoop(curLoop);
    }
    
//    l.severe("Final scalar " + res);
    
    if (childMode != null && childMode.getValue())
      res = minChild;
    
    if (res != null && res != minChild)
      res = res.cloneAny();
    
    if (res == null)
      res = AnyNull.instance();
      
//    l.severe("Final result " + res);
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
    MinOf m = (MinOf)super.clone();
    
    m.root_       = root_.cloneAny();
    //expression cloned as late as possible
    
    m.childMode_  = AbstractAny.cloneOrNull(childMode_);
    
    return m;
  }
}
