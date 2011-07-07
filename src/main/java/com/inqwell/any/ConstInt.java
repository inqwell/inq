/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstInt.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

/**
 * An immutable integer
 */
public class ConstInt extends    AbstractValue
                      implements IntI,
                                 Numeric,
                                 Cloneable
{
	public static final ConstInt ZERO     = new ConstInt(0);
	
  public static final ConstInt minVal__ = new ConstInt(Integer.MIN_VALUE+1);
  public static final ConstInt maxVal__ = new ConstInt(Integer.MAX_VALUE);
  
  private int value_;

  public ConstInt() { }
  public ConstInt(int i) { value_ = i; }
  public ConstInt(Any a) { initialiseFrom(a); }
  public ConstInt(String s) { setFromString(s); }

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
    if (this.getClass() == ConstInt.class) 
      return this;

    return super.clone();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyInt(this);
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this;
  }
  
  public Any roundFrom(Any a)
  {
    constViolation();
    return this;
  }


  public int  getValue() { return value_; }
  public void setValue(int value) { constViolation(); }

  public void increment()
  {
    constViolation();
  }
  
  public void decrement()
  {
    constViolation();
  }
  
  public boolean isNull()
  {
    return value_ == Integer.MIN_VALUE;
  }
  
  public void setNull()
  {
    constViolation();
  }
  
  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == null && isNull())
      return true;
      
    return (a instanceof IntI) &&
           (((IntI)a).getValue() == getValue());
  }

  // Protected section.  Mutation of underlying value for non-const
  // derived classes. Also called from this class but for the purposes
  // of initialising const instances.
  
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
  
  protected void setToNull()
  {
    value_ = Integer.MIN_VALUE;
  }
  
  protected void setToValue(int value)
  {
    if (value == Integer.MIN_VALUE)
      throw new IllegalStateException("Cannot implicitly become null");
    
    value_ = value;
  }
  
  protected void setFromString(String s)
  {
    try
    {
      int val = Integer.parseInt(s);
      if (val < minVal__.getValue() || val > maxVal__.getValue())
        AssignerVisitor.rangeError (AbstractAny.StringName,
                                    s,
                                    AbstractAny.IntegerName);
      setToValue (val);
    }
    catch(NumberFormatException e)
    {
      setToNull();
    }
  }
  
  protected void incrementValue()
  {
    value_++;
  }
  
  protected void decrementValue()
  {
    value_--;
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

  protected class CopyFrom extends AssignerVisitor
  {
    protected void copy (Any from)
    {
      from.accept (this);
    }

    public void visitAnyInt (IntI i)
    {
      if (i.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue (i.getValue());
    }

    public void visitAnyShort (ShortI s)
    {
      if (s.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue (s.getValue());
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        float val = f.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.FloatName, f,
                      AbstractAny.IntegerName);
        }
        ConstInt.this.setToValue ((int)val);
      }
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        long val = l.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.LongName, l,
                      AbstractAny.IntegerName);
        }
        ConstInt.this.setToValue ((int)val);
      }
    }

    public void visitAnyDate (DateI d)
    {
      if (d.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        long val = d.getTime();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.LongName, d,
                      AbstractAny.IntegerName);
        }
        ConstInt.this.setToValue ((int)val);
        
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DoubleName, d,
                      AbstractAny.IntegerName);
        }
        ConstInt.this.setToValue ((int)val);
      }
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue().doubleValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          rangeError (AbstractAny.DecimalName, d,
                      AbstractAny.IntegerName);
        }
        ConstInt.this.setToValue ((int)val);
      }
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setFromString(s.getValue());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstInt.this.setToNull();
      else
        unsupportedOperation (o);
    }
    
    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue ((int)b.getValue());
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue ((int)c.getValue());
    }

    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstInt.this.setToValue (1);
      else
        ConstInt.this.setToValue (0);
    }
  }
  
  protected class RoundFrom extends AbstractVisitor
  {
    public RoundFrom() {}
    
    public void round (Any from)
    {
      from.accept (this);
    }

    public void visitAnyInt (IntI i)
    {
      if (i.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue (i.getValue());
    }

    public void visitAnyShort (ShortI s)
    {
      if (s.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue (s.getValue());
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        float val = f.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          AssignerVisitor.rangeError (AbstractAny.FloatName, f,
                      AbstractAny.IntegerName);
        }

        int i = Math.round(val);
        
        ConstInt.this.setToValue (i);
      }
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        long val = l.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          AssignerVisitor.rangeError (AbstractAny.LongName, l,
                                      AbstractAny.IntegerName);
        }
        ConstInt.this.setToValue ((int)val);
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          AssignerVisitor.rangeError (AbstractAny.DoubleName, d,
                                      AbstractAny.IntegerName);
        }

        long l = Math.round(val);

        ConstInt.this.setToValue ((int)l);
      }
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstInt.this.setToNull();
      else
      {
        // Check for data truncation
        double val = d.getValue().doubleValue();
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          AssignerVisitor.rangeError (AbstractAny.DecimalName, d,
                                      AbstractAny.IntegerName);
        }
        long l = Math.round(val);
        ConstInt.this.setToValue ((int)l);
      }
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        ConstInt.this.setToNull();
      else
      {
        double val = Double.parseDouble(s.getValue());
        
        if (val < minVal__.getValue() || val > maxVal__.getValue())
        {
          AssignerVisitor.rangeError (AbstractAny.DoubleName, s,
                                      AbstractAny.IntegerName);
        }

        long l = Math.round(val);

        ConstInt.this.setToValue ((int)l);
      }
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        ConstInt.this.setToNull();
      else
        unsupportedOperation (o);
    }
    
    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue ((int)b.getValue());
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstInt.this.setToNull();
      else
        ConstInt.this.setToValue ((int)c.getValue());
    }

    public void visitAnyBoolean (BooleanI b)
    {
      if (b.getValue())
        ConstInt.this.setToValue (1);
      else
        ConstInt.this.setToValue (0);
    }
  }
}
