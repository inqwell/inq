/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Sum.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.math.BigDecimal;

/**
 * Iterate over the children of a given node and execute the given
 * expression using each child as the context.  Returns the
 * sum of the values of the expression result or, if the takeAverage
 * property is set, their average.
 * <p>
 * The initial evaluation of the sum expression determines the precision
 * of the result, subject to the minimum precision of Integer.  The
 * set may still be heterogeneous but if greater precisions are
 * discovered then these will be narrowed to that of the result. The
 * sum operation will proceed unless the narrowing causes a loss of data,
 * that is the value won't fit into the result.
 * <p>
 * If the average is being calculated then the precision of the result
 * will still be as stated above, that is there is no automatic
 * conversion to ensure any fractional result from the division is
 * available when the sum is over a set of integers.  If this is required
 * then the caller should widen the sum result and perform the division
 * themselves.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Sum extends    AbstractFunc
                 implements Cloneable
{
  private Any     sumRoot_;
  private Any     expression_;
  private boolean takeAverage_;

	/**
	 * Operand 1 must resolve to a <code>Composite</code>; operand 2 must
	 * resolve to a scalar.  Operand 2 is applied
	 * to each of the children under operand 1.  Successive results
	 * are summed to give the result.
	 */
  public Sum(Any sumRoot, Any op2)
  {
    sumRoot_    = sumRoot;
    expression_ = op2;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
		Composite sumRoot = (Composite)EvalExpr.evalFunc(t,
                                                     a,
                                                     sumRoot_,
                                                     Composite.class);

		if (sumRoot == null)
      nullOperand(sumRoot_);
    
    Any      expression = null;
    Any      sum        = null;
    Any      tmp        = null;
    Adder    adder      = null;
    
    if (takeAverage_ && sumRoot.entries() == 0)
      return null;

    Iter i = sumRoot.createIterator();
    
    Any  loop        = t.getLoop();

    try
    {
      while (i.hasNext())
      {
        Any child = i.next();
        
        // Set $loop
        t.setLoop(child);
  
        // Lazy clone of expression
        if (expression == null)
        {
          expression = expression_.cloneAny();
        }
        
        if (sum == null)
        {
          sum = EvalExpr.evalFunc(t,
                                  a,
                                  expression);
  
          if (sum == null)
            throw new NullPointerException("Failed to resolve " + expression_ + "during summation");
          
          // To save on the creation of temporaries, the first sum
          // item is used as the prototype for the result.  The
          // remaining items, if not the same, must be convertible to it.
          adder = new Adder(sum);
          sum   = adder.add(sum);
          tmp   = sum.buildNew(null);
          adder.setTmp(tmp);
        }
        else
        {
          Any next = EvalExpr.evalFunc(t,
                                       a,
                                       expression);
          if (next == null)
            throw new NullPointerException("Failed to resolve " + expression_ + "during summation");
          
          adder.add(next);
        }
        
        // No point in continuing the iteration if we hit a null
        if (adder.isNull())
          break;
  	  }
    
      if (adder != null && adder.isNull())
        return AnyNull.instance();
        
      if (takeAverage_ && sum != null)
        return adder.doAvg(sum, sumRoot.entries());
  
  	  return sum == null ? AnyNull.instance() : sum;
    }
    finally
    {
      t.setLoop(loop);
    }
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(sumRoot_);
  	a.add(expression_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Sum s = (Sum)super.clone();
    
    s.sumRoot_    = sumRoot_.cloneAny();        
    
    return s;
  }

	public String toString()
	{
		return super.toString() + "(" + sumRoot_ + ", " + expression_ + ")";
	}
  
  public void setTakeAverage(boolean b)
  {
    takeAverage_ = b;
  }
  
  static private class Adder extends AbstractVisitor
  {
    // These are always the same type but tmp_ can be null to
    // indicate we are creating the result
    private Any sum_;
    private Any tmp_;
    private int count_ = -1;
    private boolean isNull_ = false;
    
    private Adder(Any sum)
    {
      sum_ = sum;
      tmp_ = null;
    }
    
    private void setTmp(Any tmp)
    {
      tmp_ = tmp;
    }
    
    private Any getSum()
    {
      return sum_;
    }
    
    private boolean isNull()
    {
      return isNull_;
    }
    
    private Any doAvg(Any sum, int count)
    {
      count_ = count;
      sum_ = sum;
      sum_.accept(this);
      // avg is left in sum_
      return sum_;
    }
    
    private Any add(Any toAdd)
    {
      if (tmp_ != null)
        tmp_.copyFrom(toAdd);  // here's where we get auto type conversion
        
      sum_.accept(this);
      return sum_;
    }
    
    public void visitAnyByte (ByteI b)
    {
      // Only called when we are setting up the result
      AnyInt i = new AnyInt();
      if (b.isNull())
      {
        i.setNull();
        isNull_ = true;
      }
      else
        i.setValue(b.getValue());
      sum_ = i;
    }
    
    public void visitAnyChar (CharI c)
    {
      // Only called when we are setting up the result
      AnyInt i = new AnyInt();
      if (c.isNull())
      {
        i.setNull();
        isNull_ = true;
      }
      else
        i.setValue(c.getValue());
      sum_ = i;
    }
    
    public void visitAnyShort (ShortI s)
    {
      // Only called when we are setting up the result
      AnyInt i = new AnyInt();
      if (s.isNull())
      {
        i.setNull();
        isNull_ = true;
      }
      else
        i.setValue(s.getValue());
      sum_ = i;
    }
    
    public void visitAnyInt (IntI i)
    {
      if (count_ > 0)
        i.setValue(i.getValue() / count_);
      else if (tmp_ == null)
      {
        sum_ = i.cloneAny();
        if (i.isNull())
          isNull_ = true;
      }
      else
      {
        IntI i2 = (IntI)tmp_;
        if (i2.isNull())
        {
          i.setNull();
          isNull_ = true;
        }
        else
          i.setValue(i.getValue() + i2.getValue());
      }
    }
    
    public void visitAnyLong (LongI l)
    {
      if (count_ > 0)
        l.setValue(l.getValue() / count_);
      else if (tmp_ == null)
      {
        sum_ = l.cloneAny();
        if (l.isNull())
          isNull_ = true;
      }
      else
      {
        LongI l2 = (LongI)tmp_;
        if (l2.isNull())
        {
          l.setNull();
          isNull_ = true;
        }
        else
          l.setValue(l.getValue() + l2.getValue());
      }
    }
    
    public void visitAnyFloat (FloatI f)
    {
      if (count_ > 0)
        f.setValue(f.getValue() / count_);
      else if (tmp_ == null)
      {
        sum_ = f.cloneAny();
        if (f.isNull())
          isNull_ = true;
      }
      else
      {
        FloatI f2 = (FloatI)tmp_;
        if (f2.isNull())
        {
          f.setNull();
          isNull_ = true;
        }
        else
          f.setValue(f.getValue() + f2.getValue());
      }
    }
    
    public void visitAnyDouble (DoubleI d)
    {
      if (count_ > 0)
        d.setValue(d.getValue() / count_);
      else if (tmp_ == null)
      {
        sum_ = d.cloneAny();
        if (d.isNull())
          isNull_ = true;
      }
      else
      {
        DoubleI d2 = (DoubleI)tmp_;
        if (d2.isNull())
        {
          d.setNull();
          isNull_ = true;
        }
        else
          d.setValue(d.getValue() + d2.getValue());
      }
    }
    
    public void visitDecimal(Decimal d)
    {
      if (count_ > 0)
      {
        Decimal dCount = new AnyBigDecimal(String.valueOf(count_));
        d.setValue(d.getValue().divide(dCount.getValue(),
                                       BigDecimal.ROUND_HALF_UP));
      }
      else if (tmp_ == null)
      {
        sum_ = d.cloneAny();
        if (d.isNull())
          isNull_ = true;
      }
      else
      {
        Decimal d2 = (Decimal)tmp_;
        if (d2.isNull())
        {
          d.setNull();
          isNull_ = true;
        }
        else
          d.setValue(d.getValue().add(d2.getValue()));
      }
    }
  }
}
