/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EvalExpr.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Contains a binary or unary mathematical operator and applies that operator
 * to the given structure.  Instances can be chained to build complex
 * expressions as operands can themselves be functions.  Here's an example
 * <p>
 * <code>
 * <pre>
 * // Establish some LocateNode instances to track down the items of interest
 * // in the structure we wish to apply the expression to...
 * LocateNode qty       = new LocateNode("qty");
 * LocateNode loanValue = new LocateNode("loanValue");
 * LocateNode mid       = new LocateNode("mid");
 *
 * // ...then create an expression whose logic is:
 *   (loanValue > (qty * mid * 1.25)) || (loanValue < (qty * mid * 0.75))
 * EvalExpr priceError =
 *  new EvalExpr (new EvalExpr (loanValue,
 *                              new EvalExpr (qty
 *                                            new EvalExpr (mid,
 *                                                          new AnyFloat(1.25),
 *                                                          new Multiply()),
 *                                            new Multiply()),
 *                              new GreaterThan()),
 *                new EvalExpr (loanValue,
 *                              new EvalExpr (qty
 *                                            new EvalExpr (mid,
 *                                                          new AnyFloat(0.75),
 *                                                          new Multiply()),
 *                                            new Multiply()),
 *                              new LessThan()),
 *                new LogicalOr());
 *
 * // To execute the expression on a given structure 's' containing the required
 * // elements:
 * AnyBoolean b = (AnyBoolen)priceError.exec(s)
 * if (b.getValue())
 * {
 *   // handle the loan value outside 25% of market value
 * }
 * </pre>
 * </code>
 * <p>
 * In the above example the expression <code>priceError</code> can be applied
 * to any structure containing the required items.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.OperatorVisitor
 */
public class EvalExpr extends    AbstractFunc
                      implements Cloneable
{
  private Any op1_;
  private Any op2_;
  private OperatorVisitor oper_;
  
  // Maps function/service FQ names to line numbers. If expressions
  // are evaluated in a listed context their result will be logged.
  private static final java.util.Map<Any, ArrayList<LineNumberRange>> loggedLines__
                          = new java.util.HashMap<Any, ArrayList<LineNumberRange>>();
  
  private static     LogManager lm = LogManager.getLogManager();
  private static     Logger l = lm.getLogger("inq");

  private static boolean find(int line, ArrayList<LineNumberRange> al)
  {
  	for (LineNumberRange l : al)
  	{
  		if (line < l.start_)
  			break;
  		
  		if (line > l.end_)
  		  continue;
  		
  		if (line >= l.start_)
  			return true;
  	}
  	return false;
  }
  
  public static boolean isLogged(Any execFQName, int line)
  {
  	ArrayList<LineNumberRange> al = null;
  	
  	synchronized(loggedLines__)
  	{
  		al = loggedLines__.get(execFQName);
  	}
  	
  	return (al != null && find(line, al));
  }
  
  public static void clearLineLogging()
  {
  	synchronized(loggedLines__)
  	{
  		loggedLines__.clear();
  	}
  }
  
  public static void setLineLogging(Any execFQName, int start, int end)
  {
  	synchronized(loggedLines__)
  	{
  		if (start == 0 || end == 0)
  			loggedLines__.remove(execFQName);
  		else
  		{
    		ArrayList<LineNumberRange> al = loggedLines__.get(execFQName);
    		
    		if (al == null)
    		{
    			al = new ArrayList<LineNumberRange>();
    			al.add(new LineNumberRange(start, end));
    			loggedLines__.put(execFQName, al);
    		}
    		else
    		{
    			al.add(new LineNumberRange(start, end));
    			Collections.sort(al);
    		}
  		}
  	}
  }

  /**
	 * Evaluate a <code>Func</code> or derived until an instance
	 * other than that given by <code>execWhile</code> is returned.  If
	 * argument <code>a</code> implements <code>execWhile</code> then it
	 * is evaluated on the structure given by <code>root</code>.
	 * This process is repeated until the result is not a <code>execWhile</code>
	 * implementation.
	 * <P>
	 * If ultimate target requires transaction arbitration then this is performed.
	 * Thus, if the target is a transactional <code>Map</code> any transaction private
	 * instance will actually be returned.  If the target is a leaf within a t-Map then
	 * any private instance of that will be returned also.
	 * @param t the desired transaction context to propagate
	 * @param root the structure passed to <code>exec</code> if <code>a</code>
	 * is a <code>Func</code>
	 * @param a the operand
	 * @param execWhile the class that the operand must satisfy while
	 * evaluation continues.  Must be a <code>Func</code> or derived.
	 * @param c the class <code>a</code> (or its evaluation) must be an
	 * instance of.  May be null in which case this condition is not imposed.
	 * @return <code>a</code> if <code>a</code> is not a <code>execWhile</code>
	 * or a.exec(root) until not a <code>execWhile</code>
	 * @throws <code>IllegalArgumentException</code> is the return
	 * value is not an instance of class <code>c</code>
	 */
  public static Any evalFunc(Transaction t,
														 Any         root,
														 Any         a,
														 Class       c,
														 Class       execWhile) throws AnyException
  {
		if (!(Func.class.isAssignableFrom(execWhile)))
		{
			throw (new IllegalArgumentException
								 ("Specified execution class is not a Func" + execWhile));
		}
		
    Any orig = a;
    
    boolean nonFunc = true;
    
		if (a != null)
		{
			Func f = null;
			while (execWhile.isInstance(a))
			{
        nonFunc = false;
				f = (Func)a;
				f.setTransaction(t);
				a = f.execFunc(root);
			}
			
			if (a != null && a.isTransactional())
			{
				if (f != null)
				{
					// Give the func a chance to do transaction handling
					a = f.doTransactionHandling(root, a);
				}
				else
				{
					// otherwise do it ourselves
					a = t.getTransInstance((Map)a);
				}
			}
			if (a != null && c != null && !c.isInstance(a))
				throw (new IllegalArgumentException("Operand of class " + a.getClass() + " did not evaluate to " + c));
			
		}
    
    // If we never executed a func then reset the transaction resolving
    // data, in case some is left around otherwise.
    if (nonFunc)
      t.resetResolving();
      
    // Are we logging at this FQName/line number?
    if (isLogged(t.getExecFQName(), t.getLineNumber()))
		{
    	l.log(Level.INFO, "Eval: {0}", orig);
    	l.log(Level.INFO, "Expr: {0}", a);
		}
    
    return a;
  }
  
  public static Any evalFunc(Transaction t, Any root, Any a, Class c) throws AnyException
  {
		return evalFunc(t, root, a, c, Func.class);
	}
	
  public static Any evalFunc(Transaction t, Any root, Any a) throws AnyException
  {
		return evalFunc(t, root, a, null, Func.class);
	}
	
	/**
	 * Resolve an operand reference to the operand itself.  Operand references
	 * are instances of the <code>Locate</code> interface which are
	 * executed until an operand (which may be another <code>Func</code>)
	 * is reached.
	 * <p>
	 * If the operand resolution yields an <code>Expression</code> then
	 * this, in turn, will be requested to resolve its operands.  Hence
	 * an expression network can be <i>prepared</i> for execution, as opposed
	 * to actually executing it, which is done through the
	 * normal <code>Func.exec</code> method.  With a pre-constructed
	 * function network, there is no need to separate operand resolution
	 * and function execution
	 */
  public static Any resolveOperand(Transaction t, Any root, Any a) throws AnyException
  {
		Any ret = evalFunc(t, root, a, null, Locate.class);
		if (ret instanceof Expression)
		{
			Expression e = (Expression)ret;
			e.resolveOperands(root);
		}
		return ret;
  }

 /**
  * Create an expression to perform the specified binary operator on the
  * given operands
  */
  public EvalExpr(Any op1, Any op2, OperatorVisitor oper)
  {
    op1_  = op1;
    op2_  = op2;
    oper_ = oper;
    if (op1_ instanceof Func)
      this.setLineNumber(((Func)op1_).getLineNumber());
  }

 /**
  * Create an expression to perform the specified unary operator on the
  * given operand
  */
  public EvalExpr(Any op1, OperatorVisitor oper)
  {
    this (op1, null, oper);
  }
  
 /**
  * Create an expression which performs the Null operator on the
  * given operand.  The Null operator simply returns the given operand
  * although if the operand is a <code>Func</code> it will be executed
  * until a scalar is arrived at.
  * <p>
  * This construction is useful for evaluating other expression types
  * such as <code>Ternary</code> which can return <code>Func</code>s.
  */
  public EvalExpr(Any op1)
  {
    this (op1, null, new NullOperator());
  }
  
  public EvalExpr()
  {
  	this(null, null, new NullOperator());
  }

  public Any exec(Any a) throws AnyException
  {
    // Set the given Any into the operator in case either of the operands
    // are funcs.  If so, the argument will be supplied to those funcs.
    oper_.setParam(a);
    oper_.setTransaction(getTransaction());
    return oper_.doOperation(op1_, op2_);
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(op1_);
  	a.add(op2_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    EvalExpr e = (EvalExpr)super.clone();
    
//    // Its OK to share the operands.  Indeed this might even be desirable
//    // if the operands are instances of LocateNode as said class can cache
//    // located nodes.  Also, operands are generally read only.
        
    // oper_ must be cloned as it contains inner class references
    e.oper_ = (OperatorVisitor)oper_.clone();
    
    if (op1_ != null)
	    e.op1_ = op1_.cloneAny();

		if (op2_ != null)
	    e.op2_ = op2_.cloneAny();
    
    return e;
  }
  
  public void setOp1(Any op1)
  {
  	op1_ = op1;
  }

  public void setOp2(Any op2)
  {
  	op2_ = op2;
  }
  
	/**
	 * The null operator.  Requires only one operand which it simply returns.
	 * @author $Author: sanderst $
	 * @version $Revision: 1.2 $
	 * @see com.inqwell.any.Any
	 */ 
  static private class NullOperator extends OperatorVisitor
	{
		public void visitAnyByte (ByteI b)
		{
			result_ = b;
		}

		public void visitAnyChar (CharI c)
		{
			result_ = c;
		}

		public void visitAnyInt (IntI i)
		{
			result_ = i;
		}

		public void visitAnyShort (ShortI s)
		{
			result_ = s;
		}

		public void visitAnyLong (LongI l)
		{
			result_ = l;
		}

		public void visitAnyFloat (FloatI f)
		{
			result_ = f;
		}

		public void visitAnyDouble (DoubleI d)
		{
			result_ = d;
		}

		public void visitDecimal (Decimal d)
		{
			result_ = d;
		}

		public void visitAnyDate (DateI d)
		{
			result_ = d;
		}

		public void visitAnyString (StringI s)
		{
			result_ = s;
		}

		public void visitAnyBoolean (BooleanI b)
		{
			result_ = b;
		}
	
    protected Any handleNullOperands(Any res1,
                                     Any res2,
                                     Any op1,
                                     Any op2) throws AnyException
    {
      return res1;
    }
	}
  
  public static class LineNumberRange implements Comparable<LineNumberRange>
  {
    public final int start_;
    public final int end_;
    
    public LineNumberRange(int s, int e)
    {
    	start_ = s;
    	end_   = e;
    }

		@Override
		public int compareTo(LineNumberRange other)
		{
			if (start_ < other.start_) return -1;
			if (start_ > other.start_) return 1;
			
			if (end_ < other.end_) return -1;
			if (end_ > other.end_) return 1;

			return 0;
		}
		
		public int hashCode()
		{
			return start_ * end_;
		}
		
		public boolean equals(Object other)
		{
			LineNumberRange l = (LineNumberRange)other;
			
			return start_ == l.start_ && end_ == l.end_;
		}
  }
}
