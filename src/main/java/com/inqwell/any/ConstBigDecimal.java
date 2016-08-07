/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstBigDecimal.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.decimal.NullBigDecimal;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

/**
 * Concrete class ConstBigDecimal.  BigDecimal data type of Any.
 * Neither this class or any operator classes support assignment
 * from the non-integral fundamental types, represented by
 * <code>DoubleI</code> or <code>FloatI</code>, so as to
 * prevent the loss of accuracy associated with these types
 * to creep in to calculations without realising.
 */
public class ConstBigDecimal extends    AbstractValue
                             implements Numeric,
                                        Decimal,
                                        Cloneable
{  
  private static final java.util.Map<Any, BigDecimal> nulls__ = new HashMap<Any, BigDecimal>();
  private static final AnyInt nullLookup__   = new AnyInt();

  // The default value for a Decimal.  Default scale is zero.
	private static final BigDecimal default__  = new BigDecimal("0");
	private static final BigInteger nullSeed__ = new BigInteger("0");

  private BigDecimal         value_;
  
  static
  {
    synchronized(nulls__)
    {
      nullLookup__.setValue(0);
      nulls__.put(nullLookup__.cloneAny(), new NullBigDecimal(nullSeed__, 0));
    }
  }

  public ConstBigDecimal()
  {
  	setToValue(default__);
  }

  public ConstBigDecimal(int value)
  {
  	setToValue(new BigDecimal(value));
  }
  
  public ConstBigDecimal(BigDecimal d)
  {
  	setToValue(d);
  }

  public ConstBigDecimal(String s)
  {
    if (s == null || s.equals(AnyString.null__))
      setToNull();
    else
      setFromString(s);
  }

  public ConstBigDecimal(String s, int scale)
  {
    if (s == null || s.equals(AnyString.null__))
    {
      setToScale(scale);
      setToNull();
    }
    else
    {
      setFromString(s);
      setToScale(scale);
    }
  }

  public String toString()
  {
  	if (isNull())
  	  return "";

    return value_.toString();
  }

	public void fromString(String s)
	{
    constViolation();
	}
  
  public int hashCode()
  {
  	//if (value_ == null)
  	  //return 0;

    return value_.hashCode();
  }

  /**
   * Compare two ConstBigDecimals for equality.  This method uses
   * the <code>equals</code> method of the underlying BigDecimal
   * value, due to its efficiency over compareTo.  This means that
   * the scale factor of the two values must be the same for
   * this method to return true.
   */
  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == null && isNull())
      return true;

    return (a instanceof Decimal) &&
           (((Decimal)a).getValue().equals(getValue()));
  }

  public Any bestowConstness()
  {
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    if (this.getClass() == ConstBigDecimal.class) 
      return this;

    // The underlying BigDecimal value_ is immutable, so there's
    // no need to clone it (well, so long as all its methods are
    // reentrant!)

    return super.clone();
  }

  public Any buildNew(Any a)
  {
    ConstBigDecimal ret = new ConstBigDecimal();
    ret.setToScale(this.scale());
    if (a != null)
      ret.initialiseFrom(a);
    return ret;
  }
  
  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitDecimal(this);
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this;
  }

  public boolean isConst()
  {
    return true;
  }

  public BigDecimal  getValue() { return value_; }
  
  public void setValue(BigDecimal value)
  {
    constViolation();
  }

  public boolean isNull()
  {
    synchronized(nulls__)
    {
      nullLookup__.setValue(value_.scale());
      BigDecimal value = (BigDecimal)nulls__.get(nullLookup__);
      //System.out.println("isNull " + value);
      return value_ == value;
    }
  }

  public void setNull()
  {
    constViolation();
  }

  public int scale()
  {
    if (value_ != null)
      return value_.scale();
    
    return 0;
  }
  
  public void setScale(int scale)
  {
    constViolation();
  }

  public double doubleValue()
  {
    if (isNull())
      return 0;
    
    return value_.doubleValue();
  }

  public long longValue()
  {
    if (isNull())
      return 0;
    
    return value_.longValue();
  }

  public int signum()
  {
    if (isNull())
      return 0;
      
    return value_.signum();
  }
  
  public void fromInt(int i)
  {
    constViolation();
  }

  public static BigDecimal nullForScale(BigDecimal d)
  {
    synchronized(nulls__)
    {
      nullLookup__.setValue(d.scale());
      BigDecimal value = (BigDecimal)nulls__.get(nullLookup__);
      if (value == null)
      {
        value = new NullBigDecimal(nullSeed__, d.scale());
        nulls__.put(nullLookup__.cloneAny(), value);
        //System.out.println("setNull " + value);
      }
      return value;
    }
  }

  protected void setToValue(BigDecimal value)
  {
    if (value_ != null && value_ != default__)
    {
      // Maintain the scale of this with sensible rounding
      //System.out.println("setValue " + value);
      if (value != null)
      {
        int thisScale = value_.scale();
        //int otherScale = value.scale();
        //if (thisScale > otherScale)
        //  value_ = new BigDecimal(value.unscaledValue(), thisScale);
        //else
          value_ = value.setScale(thisScale, BigDecimal.ROUND_HALF_UP);
      }
      else
        setToNull();
    }
    else
    {
      if (value == null)
        throw new IllegalStateException("Setting to null before scale is known");

      value_ = value;
    }
  }
  
  protected void setToScale(int scale)
  {
    if (isNull())
    {
      synchronized(nulls__)
      {
        nullLookup__.setValue(scale);
        BigDecimal value = (BigDecimal)nulls__.get(nullLookup__);
        if (value == null)
        {
          value = new NullBigDecimal(nullSeed__, scale);
          nulls__.put(nullLookup__.cloneAny(), value);
          //System.out.println("setNull " + value);
        }
        value_ = value;
      }
    }
    else
    {
      // Special case - default__ has zero scale but setting the
      // scale explicitly must fix it
      if (value_ == default__ && scale == 0)
        value_ = new BigDecimal(0);
      else
        value_ = value_.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
  }

  protected void setToNull()
  {
    synchronized(nulls__)
    {
      nullLookup__.setValue(value_.scale());
      BigDecimal value = (BigDecimal)nulls__.get(nullLookup__);
      if (value == null)
      {
        value = new NullBigDecimal(nullSeed__, value_.scale());
        nulls__.put(nullLookup__.cloneAny(), value);
        //System.out.println("setNull " + value);
      }
      value_ = value;
    }
  }

  protected void initialiseFrom(Any a)
  {
    if (a == null)
      setToNull();
    else
    {
      AssignerVisitor v = makeCopier();
      v.copy(a);
    }
  }
  
  protected void setFromString(String s)
  {
    this.setToValue (new BigDecimal(s.toString()));
  }

  protected void setFromInt(int i)
  {
    setToValue (new BigDecimal(String.valueOf(i)));
  }
  
  /**
   * Provide access to the underlying value without any concessions
   * to maintaining the current scale.  Use with caution.
  protected void setValueForce(BigDecimal value)
  {
    value_ = value;
  }
   */
  
  protected AssignerVisitor makeCopier()
  {
		return new CopyFrom();
  }

  // Only the integral types are supported.  Its not OK to copy from a float
  // or double.
  private class CopyFrom extends AssignerVisitor
  {
    protected void copy (Any from)
    {
      from.accept (this);
    }

    public void visitAnyInt (IntI i)
    {
      if (i.isNull())
        ConstBigDecimal.this.setToNull();
      else
        ConstBigDecimal.this.setToValue (new BigDecimal(String.valueOf(i.getValue())));
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstBigDecimal.this.setToNull();
      else
        ConstBigDecimal.this.setToValue (new BigDecimal(String.valueOf(l.getValue())));
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstBigDecimal.this.setToNull();
      else
        ConstBigDecimal.this.setToValue (new BigDecimal(d.toString()));
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstBigDecimal.this.setToNull();
      else
        ConstBigDecimal.this.setToValue (new BigDecimal(s.toString()));
    }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstBigDecimal.this.setToNull();
      else
      {
      	int i = (int)b.getValue();
        ConstBigDecimal.this.setToValue (new BigDecimal(String.valueOf(i)));
      }
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstBigDecimal.this.setToNull();
      else
        ConstBigDecimal.this.setToValue (new BigDecimal(String.valueOf(c.getValue())));
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstBigDecimal.this.setToNull();
      else
        unsupportedOperation (o);
    }
  }
}

