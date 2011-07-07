/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Choose.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

import java.util.NoSuchElementException;

public class Choose extends    AbstractFunc
										 implements Cloneable
{
	public static final Any test__ = new ConstString("__test");
	public static final Any expr__ = new ConstString("__expr");
	
  private Array whens_;
  private Any   otherwise_;

	/**
	 * 
	 */
  public Choose(Array whens, Any otherwise)
  {
    whens_      = whens;
    otherwise_  = otherwise;
  }

  public Choose(Array whens)
  {
		this (whens, null);
  }

  public Any exec(Any a) throws AnyException
  {
		BooleanI   b    = new AnyBoolean(false);
		TestExpr   when = null;
		Any res;
		
		Iter i = whens_.createIterator();
		while (!b.getValue() && i.hasNext())
		{
			when = (TestExpr)i.next();
			
			res = EvalExpr.evalFunc(getTransaction(),
															a,
															when.test_.cloneAny());
															
			// whatever the result is, convert it to a boolean
			b.copyFrom(res);
		}
		
		Any ret = null;
		
		if (b.getValue())
		{
			ret = EvalExpr.evalFunc(getTransaction(),
															a,
															when.expr_.cloneAny());
			
		}
		else
		{
			if (otherwise_ != null)
				ret = EvalExpr.evalFunc(getTransaction(),
																a,
																otherwise_.cloneAny());
		}
		
		return ret;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(whens_);
		if (otherwise_ != null)
			a.add(otherwise_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Choose c = (Choose)super.clone();
    
    // Curiously, everything is lazily cloned and the whens_ array
    // itself is not mutated or anything...
    
    //c.whens_     = whens_.cloneAny();
    //c.otherwise_ = AbstractAny.cloneOrNull(otherwise_);
    
    return c;
  }
  
  static public class TestExpr extends AbstractAny
  {
    private Any test_;
    private Any expr_;
    
    private static final short init__ = 0;
    private static final short test__ = 1;
    private static final short expr__ = 2;
      
    public TestExpr(Any test, Any expr)
    {
      test_ = test;
      expr_ = expr;
    }

    public Iter createIterator ()
    {
      return new TestExprIter();
    }
    
    private class TestExprIter extends AbstractIter implements Iter
    {
      private short state_ = init__;
              
      public boolean hasNext()
      {
        return (state_ != expr__);
      }
  
      public Any next()
      {
        if (state_ == init__)
        {
          state_ = test__;
          return test_;
        }
        else if (state_ == test__)
        {
          state_ = expr__;
          return expr_;
        }
        else
        {
          throw new NoSuchElementException();
        }
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    }
  }
}
