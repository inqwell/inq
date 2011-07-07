/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstByte.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

/**
 * An immutable byte
 */
public class ConstByte extends    AbstractValue
                       implements ByteI,
                                  Numeric,
                                  Cloneable
{
  public static final ConstByte minVal__ = new ConstByte(Byte.MIN_VALUE+1);
  public static final ConstByte maxVal__ = new ConstByte(Byte.MAX_VALUE);
  
  private byte value_;

  public ConstByte() { }
  public ConstByte(byte b) { setToValue(b); }
  public ConstByte(String s) { fromString(s); }
  public ConstByte(Any a) { initialiseFrom(a); }
  public ConstByte(int i) { setToValue((byte)i); }

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
    return value_;
  }

  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == null && isNull())
      return true;
      
    return (a instanceof ByteI) &&
           (((ByteI)a).getValue() == getValue());
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
    if (this.getClass() == ConstByte.class) 
      return this;

    return super.clone();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyByte(this);
  }

  public Any copyFrom (Any a)
  {
		constViolation();
    return this;
  }

  public byte getValue() { return value_; }
  public void setValue(byte value) { constViolation(); }

  public boolean isNull()
  {
    return value_ == Byte.MIN_VALUE;
  }
  
  public void setNull()
  {
		constViolation();
  }
  
  protected void setToNull()
  {
    value_ = Byte.MIN_VALUE;
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
  
  protected void setToValue(byte value)
  {
    if (value == Byte.MIN_VALUE)
      throw new IllegalStateException("Cannot implicitly become null");
    
    value_ = value;
  }
  
  protected void setFromString(String s)
  {
    try
    {
      byte val = Byte.parseByte(s);
      if (val < minVal__.getValue() || val > maxVal__.getValue())
        AssignerVisitor.rangeError (AbstractAny.StringName,
                                    s,
                                    AbstractAny.ByteName);
      setToValue(val);
    }
    catch(NumberFormatException e)
    {
      setToNull();
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
        ConstByte.this.setToNull();
      else
      {
        // Check for data truncation
        int val = i.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.IntegerName, i,
                      AbstractAny.ByteName);
        }
        ConstByte.this.setToValue ((byte)i.getValue());
      }
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstByte.this.setToNull();
      else
      {
        // Check for data truncation
        float val = f.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.FloatName, f,
                      AbstractAny.ByteName);
        }
        ConstByte.this.setToValue ((byte)f.getValue());
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstByte.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DoubleName, d,
                      AbstractAny.ByteName);
        }
        ConstByte.this.setToValue ((byte)d.getValue());
      }
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstByte.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue().doubleValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DecimalName, d,
                      AbstractAny.ByteName);
        }
        ConstByte.this.setToValue ((byte)val);
      }
    }

    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstByte.this.setToValue ((byte)1);
      else
        ConstByte.this.setToValue ((byte)0);
    }

    public void visitAnyLong (LongI l)
    {
      // Check for data truncation
      if (l.isNull())
        ConstByte.this.setToNull();
      else
      {
        long val = l.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.LongName, l,
                      AbstractAny.ByteName);
        }
        ConstByte.this.setToValue ((byte)l.getValue());
      }
    }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstByte.this.setToNull();
      else
        ConstByte.this.setToValue (b.getValue());
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstByte.this.setToNull();
      else
      {
        int val = c.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.CharName, c,
                      AbstractAny.ByteName);
        }
        ConstByte.this.setToValue ((byte)c.getValue());
      }
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstByte.this.setToNull();
      else
        setFromString(s.getValue());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstByte.this.setToNull();
      else
        unsupportedOperation (o);
    }
  }
}

