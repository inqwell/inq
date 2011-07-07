/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstShort.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;


/**
 * Concrete class ConstShort.  Short integer data type of Any.
 */
public class ConstShort extends    AbstractValue
                        implements ShortI,
                                   Numeric,
                                   Cloneable
{
  public static final ConstShort minVal__ = new ConstShort(Short.MIN_VALUE+1); // because of null
  public static final ConstShort maxVal__ = new ConstShort(Short.MAX_VALUE);
  
  private short value_;

  public ConstShort() {}
  public ConstShort(short i) { value_ = i; }
  public ConstShort(String s) { setFromString(s); }
  public ConstShort(Any a) { initialiseFrom(a); }
  public ConstShort(int i) { value_ = (short)i; }

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
      
    return (a instanceof ShortI) &&
           (((ShortI)a).getValue() == getValue());
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
    if (this.getClass() == ConstShort.class) 
      return this;

    return super.clone();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyShort(this);
  }

  public Any copyFrom (Any a)
  {
		constViolation();
    return this;
  }

  public short  getValue() { return value_; }
  
  public void setValue(short value)
  {
		constViolation();
  }

  public boolean isNull()
  {
    return value_ == Short.MIN_VALUE;
  }
  
  public void setNull()
  {
		constViolation();
  }
  
  protected void setToValue(short value)
  {
    if (value == Short.MIN_VALUE)
      throw new IllegalStateException("Cannot implicitly become null");
    
		value_ = value;
  }

  protected void setToNull()
  {
    value_ = Short.MIN_VALUE;
  }
  
  protected void setFromString(String s)
  {
    try
    {
      short val = Short.parseShort(s);
      if (val < minVal__.getValue() || val > maxVal__.getValue())
        AssignerVisitor.rangeError (AbstractAny.StringName,
                                    s,
                                    AbstractAny.ShortName);
      ConstShort.this.setToValue (val);
    }
    catch(NumberFormatException e)
    {
      ConstShort.this.setToNull();
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
        ConstShort.this.setToNull();
      else
      {
        // Check for data truncation
        int val = i.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.IntegerName, i,
                      AbstractAny.ShortName);
        }
        ConstShort.this.setToValue ((short)i.getValue());
      }
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstShort.this.setToNull();
      else
      {
        // Check for data truncation
        long val = l.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.LongName, l,
                      AbstractAny.ShortName);
        }
        ConstShort.this.setToValue ((short)l.getValue());
      }
    }

    public void visitAnyShort (ShortI s)
    {
      if (s.isNull())
        ConstShort.this.setToNull();
      else
        ConstShort.this.setToValue (s.getValue());
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstShort.this.setToNull();
      else
      {
        // Check for data truncation
        float val = f.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.FloatName, f,
                      AbstractAny.ShortName);
        }
        ConstShort.this.setToValue ((short)val);
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstShort.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DoubleName, d,
                      AbstractAny.ShortName);
        }
        ConstShort.this.setToValue ((short)val);
      }
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstShort.this.setToNull();
      else
      {
        // Check for data truncation
        int val = d.getValue().intValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DecimalName, d,
                      AbstractAny.ShortName);
        }
        ConstShort.this.setToValue ((short)val);
      }
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstShort.this.setToNull();
      else
        ConstShort.this.setFromString(s.toString());
    }
    
    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstShort.this.setToNull();
      else
        ConstShort.this.setToValue ((short)b.getValue());
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstShort.this.setToNull();
      else
        ConstShort.this.setToValue ((short)c.getValue());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstShort.this.setToNull();
      else
        unsupportedOperation (o);
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstShort.this.setToValue ((short)1);
      else
        ConstShort.this.setToValue ((short)0);
    }
  }
}

