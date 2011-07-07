/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstLong.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;


/**
 * Concrete class ConstLong.  Long integer data type of Any.
 */
public class ConstLong extends    AbstractValue
                        implements LongI,
                                   Numeric,
                                   Cloneable
{
  public static final ConstLong minVal__ = new ConstLong(Long.MIN_VALUE+1);
  public static final ConstLong maxVal__ = new ConstLong(Long.MAX_VALUE);
  
  private long value_;

  public ConstLong() {}
  public ConstLong(long i) { value_ = i; }
  public ConstLong(String s) { setFromString(s); }
  public ConstLong(Any a) { initialiseFrom(a); }

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
      
    return (a instanceof LongI) &&
           (((LongI)a).getValue() == getValue());
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
    if (this.getClass() == ConstLong.class) 
      return this;

    return super.clone();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyLong(this);
  }

  public Any copyFrom (Any a)
  {
		constViolation();
    return this;
  }

  public long  getValue() { return value_; }
  
  public void setValue(long value)
  {
		constViolation();
  }

  public boolean isNull()
  {
    return value_ == Long.MIN_VALUE;
  }
  
  public void setNull()
  {
		constViolation();
  }
  
  protected void setToValue(long value)
  {
    if (value == Long.MIN_VALUE)
      throw new IllegalStateException("Cannot implicitly become null");
    
		value_ = value;
  }

  protected void setToNull()
  {
    value_ = Long.MIN_VALUE;
  }
  
  protected void setFromString(String s)
  {
    try
    {
      long val = Long.parseLong(s);
      if (val < minVal__.getValue() || val > maxVal__.getValue())
        AssignerVisitor.rangeError (AbstractAny.StringName,
                                    s,
                                    AbstractAny.LongName);
      ConstLong.this.setToValue (val);
    }
    catch(NumberFormatException e)
    {
      ConstLong.this.setToNull();
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
        ConstLong.this.setToNull();
      else
        ConstLong.this.setToValue (i.getValue());
    }

    public void visitAnyShort (ShortI s)
    {
      if (s.isNull())
        ConstLong.this.setToNull();
      else
        ConstLong.this.setToValue (s.getValue());
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstLong.this.setToNull();
      else
      {
        // Check for data truncation
        float val = f.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.FloatName, f,
                      AbstractAny.LongName);
        }
        ConstLong.this.setToValue ((long)val);
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstLong.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DoubleName, d,
                      AbstractAny.LongName);
        }
        ConstLong.this.setToValue ((long)val);
      }
    }

    public void visitAnyDate (DateI d)
    {
      if (d.isNull())
        ConstLong.this.setToNull();
      else
        ConstLong.this.setToValue (d.getTime());
    }
    
    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstLong.this.setToNull();
      else
        ConstLong.this.setToValue (l.getValue());
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstLong.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue().doubleValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DecimalName, d,
                      AbstractAny.LongName);
        }
        ConstLong.this.setToValue ((long)val);
      }
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstLong.this.setToNull();
      else
        ConstLong.this.setFromString(s.toString());
    }
    
    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstLong.this.setToNull();
      else
        ConstLong.this.setToValue (b.getValue());
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstLong.this.setToNull();
      else
        ConstLong.this.setToValue (c.getValue());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstLong.this.setToNull();
      else
        unsupportedOperation (o);
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstLong.this.setToValue (1);
      else
        ConstLong.this.setToValue (0);
    }
  }
}

