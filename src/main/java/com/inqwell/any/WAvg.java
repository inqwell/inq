/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/WAvg.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.math.BigDecimal;

/**
 * Iterate over the children of a given node and execute the two
 * given expressions using each child as the context.  Returns the
 * weighted average by summing avg * wgt and dividing the result
 * by the sum of wgt.
 * <p>
 * The initial evaluation of the sum expression determines the precision
 * of the result, subject to the minimum precision of Integer.  This
 * will be the hightest precision of sum and wgt, including any
 * increase in scale if both operands are decimals.  The
 * set may still be heterogeneous but if greater precisions are
 * discovered then these will be narrowed to that of the result. The
 * sum operation will proceed unless the narrowing causes a loss of data,
 * that is the value won't fit into the result.  In the case of decimals,
 * any increase in scale is maintained as sum(avg * wgt) is accumulated.
 * <p>
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class WAvg extends    AbstractFunc
                 implements Cloneable
{
  private Any     sumRoot_;
  private Any     avg_;
  private Any     wgt_;

	/**
	 * Operand 1 must resolve to a <code>Composite</code>; operands 2
   * and 3 must resolve to a scalars.
	 */
  public WAvg(Any sumRoot, Any avg, Any wgt)
  {
    sumRoot_  = sumRoot;
    avg_      = avg;
    wgt_      = wgt;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();

    Composite sumRoot = (Composite)EvalExpr.evalFunc(t,
                                                     a,
                                                     sumRoot_,
                                                     Composite.class);

		if (sumRoot == null)
      throw new NullPointerException("Null set");
    
    Any      avg  = null;
    Any      wgt  = null;
    Any      sum  = null; // sum(avg * wgt)
    Any      swgt = null; // sum(wgt)
    Any      tmp        = null;
    Adder    adder      = null;
    
    if (sumRoot.entries() == 0)
      return null;

    Iter i = sumRoot.createIterator();
    
    Any  curLoop = t.getLoop();
    
    try
    {
      while (i.hasNext())
      {
        Any child = i.next();
        t.setLoop(child);
        
        // Lazy clone of expressions
        if (avg == null)
        {
          avg = avg_.cloneAny();
          wgt = wgt_.cloneAny();
        }
        
        if (sum == null)
        {
          sum  = EvalExpr.evalFunc(t,
                                   a,
                                   avg);
                                  
          swgt = EvalExpr.evalFunc(t,
                                   a,
                                   wgt);
  
          if (sum == null || swgt == null)
            throw new NullPointerException("Null expression");
          
          // To save on the creation of temporaries, the first sum
          // item is used as the prototype for the result.  The
          // remaining items ...
          adder = new Adder();
          sum   = adder.add(sum, swgt);
        }
        else
        {
          sum  = EvalExpr.evalFunc(t,
                                   a,
                                   avg);
                                  
          swgt = EvalExpr.evalFunc(t,
                                   a,
                                   wgt);
  
          if (sum == null || swgt == null)
            throw new NullPointerException("Null expression");
          while (i.hasNext())

          adder.add(sum, swgt);
        }
      }
	  }
    finally
    {
      t.setLoop(curLoop);
    }
    
	  return adder.doWAvg();
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(sumRoot_);
  	a.add(avg_);
  	a.add(wgt_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    WAvg s = (WAvg)super.clone();
    
    s.sumRoot_    = sumRoot_.cloneAny();        
    
    return s;
  }

	public String toString()
	{
		return super.toString() + "(" + sumRoot_ + ", " + avg_ + ", " + wgt_ + ")";
	}
  
  static private class Adder extends AbstractVisitor
  {
    // wgt_ and tmp_ are always the same type but tmp_ can be null to
    // indicate we are creating the wgt_.  Initially wgt_ is
    // used to create the prototype value in which the divisor
    // total is summed.  wgt_ and sum_ are initially multiplied
    // to get the protos for the weighted total.
    private Any sum_;
    private Any add_;
    private Any addWgt_;
    private Any wgt_;
    private Any tmp_;
    private Multiply mult_;
    
    private Any doWAvg() throws AnyException
    {
      Divide d = new Divide();
      Any wAvg = d.doOperation(sum_, wgt_);
      return wAvg;
    }
    
    private Any add(Any toAdd, Any toWgt) throws AnyException
    {
      if (tmp_ == null)
      {
        // Only the first time.  Accept on toWgt to get the weighting
        // total type and multiply to possibly align precisions and
        // get the weighted sum type
        wgt_ = toWgt;  
        wgt_.accept(this);
        // wgt_ now contains a new instance in which we can sum the
        // weighting divisor.
        tmp_ = wgt_.cloneAny();
        
        mult_ = new Multiply();
        sum_ = mult_.doOperation(toAdd, toWgt);
        // The operands will have been aligned if necessary.  If this
        // actually happened then one of them will be new, but it may
        // not have and we don't know which one anyway, so clone them
        // both. This creates working variables to continue with and
        // future multiplications will not create temporary operands.
        // The result will also be new, of the appropriate precision and,
        // in the case of decimals, this could be to a bigger scale
        // than the original operands.
        add_    = mult_.getOp1().cloneAny();
        addWgt_ = mult_.getOp2().cloneAny();
        return sum_;
      }
      else
      {
        tmp_.copyFrom(toWgt);  // here's where we get auto type conversion
        wgt_.accept(this);     // Add to the weight divisor
      
        add_.copyFrom(toAdd);
        addWgt_.copyFrom(toWgt);
        mult_.doOperation(add_, addWgt_).accept(this);
        return sum_;
      }
    }
    
    public void visitAnyByte (ByteI b)
    {
      // Only called when we are setting up the result
      AnyInt i = new AnyInt();
      i.setValue(b.getValue());
      wgt_ = i;
    }
    
    public void visitAnyChar (CharI c)
    {
      // Only called when we are setting up the result
      AnyInt i = new AnyInt();
      i.setValue(c.getValue());
      wgt_ = i;
    }
    
    public void visitAnyShort (ShortI s)
    {
      // Only called when we are setting up the result
      AnyInt i = new AnyInt();
      i.setValue(s.getValue());
      wgt_ = i;
    }
    
    public void visitAnyInt (IntI i)
    {
      if (tmp_ == null)
        wgt_ = i.cloneAny();
      else if (i == wgt_)
      {
        AnyInt i2 = (AnyInt)tmp_;
        i.setValue(i.getValue() + i2.getValue());
      }
      else
      {
        AnyInt is = (AnyInt)sum_;
        is.setValue(is.getValue() + i.getValue());
      }
    }
    
    public void visitAnyLong (LongI l)
    {
      if (tmp_ == null)
        wgt_ = l.cloneAny();
      else if (l == wgt_)
      {
        LongI l2 = (LongI)tmp_;
        l.setValue(l.getValue() + l2.getValue());
      }
      else
      {
        LongI ls = (LongI)sum_;
        ls.setValue(ls.getValue() + l.getValue());
      }
    }
    
    public void visitAnyFloat (FloatI f)
    {
      if (tmp_ == null)
        wgt_ = f.cloneAny();
      else if (f == wgt_)
      {
        FloatI f2 = (FloatI)tmp_;
        f.setValue(f.getValue() + f2.getValue());
      }
      else
      {
        FloatI fs = (FloatI)sum_;
        fs.setValue(fs.getValue() + f.getValue());
      }
    }
    
    public void visitAnyDouble (DoubleI d)
    {
      if (tmp_ == null)
        wgt_ = d.cloneAny();
      else if (d == wgt_)
      {
        DoubleI d2 = (DoubleI)tmp_;
        d.setValue(d.getValue() + d2.getValue());
      }
      else
      {
        DoubleI ds = (DoubleI)sum_;
        ds.setValue(ds.getValue() + d.getValue());
      }
    }
    
    public void visitDecimal(Decimal d)
    {
      if (tmp_ == null)
        wgt_ = d.cloneAny();
      else if (d == wgt_)
      {
        Decimal d2 = (Decimal)tmp_;
        d.setValue(d.getValue().add(d2.getValue()));
      }
      else
      {
        Decimal ds = (Decimal)sum_;
        ds.setValue(ds.getValue().add(d.getValue()));
      }
    }
  }
}
