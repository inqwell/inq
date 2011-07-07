/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstChar.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

/**
 * An immutable char
 */
public class ConstChar extends    AbstractValue
                       implements CharI,
                                  Numeric,
                                  Cloneable
{
  public static final ConstChar minVal__ = new ConstChar(Character.MIN_VALUE);
  public static final ConstChar maxVal__ = new ConstChar(Character.MAX_VALUE-1); // because of null
  
  private char value_;

  public ConstChar() { }
  public ConstChar(char c) { setToValue(c); }
  public ConstChar(String s) { setFromString(s); }
  private ConstChar(int i) { setToValue((char)i); }

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
      
    return (a instanceof CharI) &&
           (((CharI)a).getValue() == getValue());
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
    if (this.getClass() == ConstChar.class) 
      return this;

    return super.clone();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyChar(this);
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this;
  }

  public char  getValue() { return value_; }
  public void setValue(char value) { constViolation(); }

  public boolean isNull()
  {
    return value_ == Character.MAX_VALUE;
  }
  
  public void setNull()
  {
    constViolation();
  }
  
  protected void setToValue(char value)
  {
    if (value == Character.MAX_VALUE)
      throw new IllegalStateException("Cannot implicitly become null");
    
    value_ = value;
  }
  
  protected void setToNull()
  {
    value_ = Character.MAX_VALUE;
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
    // Cannot assign String longer than 1 character to Char
    if (s.length() != 1)
      AssignerVisitor.rangeError (AbstractAny.StringName,
                                  s,
                                  AbstractAny.CharName);
    else
      setToValue(s.charAt(0));
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
        ConstChar.this.setToNull();
      else
      {
        // Check for data truncation
        int val = i.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.IntegerName, i,
                      AbstractAny.CharName);
        }
        ConstChar.this.setToValue ((char)i.getValue());
      }
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstChar.this.setToNull();
      else
      {
        // Check for data truncation
        float val = f.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.FloatName, f,
                      AbstractAny.ByteName);
        }
        ConstChar.this.setToValue ((char)f.getValue());
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstChar.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DoubleName, d,
                      AbstractAny.CharName);
        }
        ConstChar.this.setToValue ((char)d.getValue());
      }
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstChar.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue().doubleValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DecimalName, d,
                      AbstractAny.CharName);
        }
        ConstChar.this.setToValue ((char)val);
      }
    }

    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstChar.this.setToValue ((char)1);
      else
        ConstChar.this.setToValue ((char)0);
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstChar.this.setToNull();
      else
      {
        // Check for data truncation
        long val = l.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.IntegerName, l,
                      AbstractAny.CharName);
        }
        ConstChar.this.setToValue ((char)l.getValue());
      }
    }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstChar.this.setToNull();
      else
      {
        int val = b.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.ByteName, b,
                      AbstractAny.CharName);
        }
        ConstChar.this.setToValue ((char)b.getValue());
      }
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstChar.this.setToNull();
      else
        ConstChar.this.setToValue (c.getValue());
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull() || s.length() == 0)
        ConstChar.this.setToNull();
      else
      {
        ConstChar.this.setFromString(s.getValue());
      }
    }
    
    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstChar.this.setToNull();
      else
        unsupportedOperation (o);
    }
  }
}

