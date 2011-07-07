/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstFloat.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

/**
 * An immutable float
 */
public class ConstFloat extends    AbstractValue
                        implements FloatI,
                                   Numeric,
                                   Cloneable
{	
  public static final ConstFloat minVal__ = new ConstFloat(-Float.MAX_VALUE);
  public static final ConstFloat maxVal__ = new ConstFloat(Float.MAX_VALUE);
  
  private float value_;

  public ConstFloat() { }
  public ConstFloat(float f) { setToValue(f); }
  public ConstFloat(String s) { setFromString(s); }
  public ConstFloat(Any a) { initialiseFrom(a); }

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
      
    return (a instanceof FloatI) &&
           (((FloatI)a).getValue() == getValue());
  }

  public boolean isConst()
  {
    return true;
  }

  public Any bestowConstness()
  {
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    if (this.getClass() == ConstFloat.class) 
      return this;

    return super.clone();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyFloat(this);
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this;
  }

  public float  getValue() { return value_; }
  public void setValue(float value) { constViolation(); }

  public boolean isNull()
  {
    return value_ == Float.MIN_VALUE;
  }
  
  public void setNull()
  {
    constViolation();
  }
  
  protected void setToNull()
  {
    value_ = Float.MIN_VALUE;
  }
  
  protected AssignerVisitor makeCopier()
  {
		return new CopyFrom();
  }
  
  protected void setToValue(float value)
  {
    if (value == Float.MIN_VALUE)
      throw new IllegalStateException("Cannot implicitly become null");
    
    value_ = value;
  }

  protected void setToNull(float value)
  {
    value_ = Float.MIN_VALUE;
  }

  protected void setFromString(String s)
  {
    try
    {
      float val = Float.parseFloat(s);
      if (val < minVal__.getValue() || val > maxVal__.getValue())
        AssignerVisitor.rangeError (AbstractAny.StringName,
                                    s,
                                    AbstractAny.FloatName);
      ConstFloat.this.setToValue (val);
    }
    catch(NumberFormatException e)
    {
      ConstFloat.this.setToNull();
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
        ConstFloat.this.setToNull();
      else
        ConstFloat.this.setToValue (i.getValue());
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstFloat.this.setToNull();
      else
        ConstFloat.this.setToValue (l.getValue());
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstFloat.this.setToNull();
      else
        ConstFloat.this.setToValue (f.getValue());
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstFloat.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DoubleName, d,
                      AbstractAny.FloatName);
        }
        ConstFloat.this.setToValue ((float)val);
      }
    }

    public void visitAnyDate (DateI d)
    {
      if (d.isNull())
        ConstFloat.this.setToNull();
      else
        ConstFloat.this.setToValue (d.getTime());
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstFloat.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue().doubleValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DecimalName, d,
                      AbstractAny.FloatName);
        }
        ConstFloat.this.setToValue ((float)val);
      }
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstFloat.this.setToNull();
      else
        ConstFloat.this.setFromString(s.getValue());
    }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstFloat.this.setToNull();
      else
        ConstFloat.this.setToValue (b.getValue());
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstFloat.this.setToNull();
      else
        ConstFloat.this.setToValue (c.getValue());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstFloat.this.setToNull();
      else
        unsupportedOperation (o);
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstFloat.this.setToValue (1);
      else
        ConstFloat.this.setToValue (0);
    }
  }
}

