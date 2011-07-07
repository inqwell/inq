/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstDouble.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

/**
 * An immutable double
 */
public class ConstDouble extends    AbstractValue
                         implements DoubleI,
                                    Numeric,
                                    Cloneable
{	
  public static final ConstDouble minVal__ = new ConstDouble(-Double.MAX_VALUE);
  public static final ConstDouble maxVal__ = new ConstDouble(Double.MAX_VALUE);
  
  private double value_;

  public ConstDouble() { }
  public ConstDouble(double d) { setToValue(d); }
  public ConstDouble(String s) { setFromString(s); }
  public ConstDouble(Any a) { initialiseFrom(a); }

  public String toString()
  {
    if (isNull())
      return AnyString.EMPTY.toString();
    else
      return (String.valueOf(getValue()));
  }

	public void fromString(String s)
	{
    constViolation();
	}

  public int hashCode()
  {
    return (int)value_;
  }

  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == null && isNull())
      return true;
      
    return (a instanceof DoubleI) &&
           (((DoubleI)a).getValue() == getValue());
  }

  public boolean isConst()
  {
    return true;
  }

  public Object clone() throws CloneNotSupportedException
  {
    if (this.getClass() == ConstDouble.class) 
      return this;

    return super.clone();
  }

  public Any bestowConstness()
  {
    return this;
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyDouble(this);
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this;
  }

  public double  getValue() { return value_; }
  public void setValue(double value) { constViolation(); }

  public boolean isNull()
  {
    return value_ == Double.MIN_VALUE;
  }
  
  public void setNull()
  {
    constViolation();
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
    try
    {
      double val = Double.parseDouble(s);
      if (val < minVal__.getValue() || val > maxVal__.getValue())
        AssignerVisitor.rangeError (AbstractAny.StringName,
                                    s,
                                    AbstractAny.FloatName);
      ConstDouble.this.setToValue (val);
    }
    catch(NumberFormatException e)
    {
      ConstDouble.this.setToNull();
    }
  }

  protected void setToNull()
  {
    value_ = Double.MIN_VALUE;
  }
  
  protected void setToValue(double value)
  {
    if (value == Double.MIN_VALUE)
      throw new IllegalStateException("Cannot implicitly become null");
    
    value_ = value;
  }

  protected AssignerVisitor makeCopier()
  {
		return new CopyFrom();
  }

  // Using an inner class for assignment has the benefits:
  //   1) The name space is not cluttered with the assignment classes
  //   2) The code for assignment is closely tied with the user class
  //   3) We have implicit access to the operand we are assigning to
  //      while the visitor is explicitly only passed the source

  private class CopyFrom extends AssignerVisitor
  {
    protected void copy (Any from)
    {
      from.accept (this);
    }

    public void visitAnyInt (IntI i)
    {
      if (i.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setToValue (i.getValue());
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setToValue (l.getValue());
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setToValue (d.getValue());
    }

    public void visitAnyDate (DateI d)
    {
      if (d.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setToValue (d.getTime());
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstDouble.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue().doubleValue();
        if (val < (-Double.MAX_VALUE) || val > Double.MAX_VALUE)
        {
          rangeError (AbstractAny.DecimalName, d,
                      AbstractAny.DoubleName);
        }
        ConstDouble.this.setToValue (val);
      }
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setToValue (f.getValue());
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setFromString(s.getValue());
    }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setToValue (b.getValue());
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstDouble.this.setToNull();
      else
        ConstDouble.this.setToValue (c.getValue());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstDouble.this.setToNull();
      else
        unsupportedOperation (o);
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstDouble.this.setToValue (1);
      else
        ConstDouble.this.setToValue (0);
    }
  }
}

