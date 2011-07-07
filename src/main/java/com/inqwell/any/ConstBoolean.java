/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ConstBoolean.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

/**
 * An immutable boolean
 */
public class ConstBoolean extends    AbstractValue
                          implements BooleanI,
                                     Cloneable
{
  private boolean value_;

  public ConstBoolean() { value_ = false; }
  public ConstBoolean(boolean b) { value_ = b; }
  public ConstBoolean(String s) { setFromString(s); }
  public ConstBoolean(int i) { value_ = (i != 0) ? true : false; }
  public ConstBoolean(Any a) { initialiseFrom(a); }

  public String toString()
  {
    if (value_)
      return (java.lang.Boolean.TRUE.toString());
    else
      return (java.lang.Boolean.FALSE.toString());
  }

	public void fromString(String s)
	{
    constViolation();
	}
	
  public int hashCode()
  {
    if (value_)
      return (java.lang.Boolean.TRUE.hashCode());
    else
      return (java.lang.Boolean.FALSE.hashCode());
  }

  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    return (a instanceof BooleanI) &&
           (((BooleanI)a).getValue() == getValue());
  }

  public Any bestowConstness()
  {
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    if (this.getClass() == ConstBoolean.class) 
      return this;

    return super.clone();
  }

  public boolean isConst()
  {
    return true;
  }

  public boolean isNull() { return false; }
  
  public void setNull()
  {
    constViolation();
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitAnyBoolean(this);
  }

  public boolean  getValue() { return value_; }
  
  public void setValue(boolean value)
  {
    constViolation();
  }

  public Any copyFrom (Any a)
  {
    constViolation();
    return this;
  }

  protected void initialiseFrom(Any a)
  {
    if (a == null)
      value_ = false;
    else
    {
      AssignerVisitor v = makeCopier();
      v.copy(a);
    }
  }
  
  protected void setToValue(boolean value)
  {
    value_ = value;
  }
  
  protected void setFromString(String s)
  {
    setToValue(AnyBoolean.booleanValue(s));
  }

  protected AssignerVisitor makeCopier()
  {
		return new CopyFrom();
  }

  protected class CopyFrom extends AssignerVisitor
  {
    protected void copy (Any from)
    {
      from.accept (this);
    }

    public void visitAnyInt (IntI i)
    {
      if (i.isNull())
        ConstBoolean.this.setToValue (false);
      else if (i.getValue() != 0)
        ConstBoolean.this.setToValue (true);
      else
        ConstBoolean.this.setToValue (false);
    }

    public void visitAnyString (StringI s)
    {
			if (s.isNull())
        ConstBoolean.this.setToValue (false);
      else
        setFromString(s.getValue());
		}
		
    public void visitAnyBoolean (BooleanI b)
    {
      ConstBoolean.this.setToValue (b.getValue());
    }

    public void visitAnyDate (DateI d)
    {
      if (d.isNull())
        ConstBoolean.this.setToValue (false);
      else
        ConstBoolean.this.setToValue (true);
    }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        ConstBoolean.this.setToValue (false);
      else if (b.getValue() != 0)
        ConstBoolean.this.setToValue (true);
      else
        ConstBoolean.this.setToValue (false);
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        ConstBoolean.this.setToValue (false);
      else if (c.getValue() != 0)
        ConstBoolean.this.setToValue (true);
      else
        ConstBoolean.this.setToValue (false);
    }

    public void visitAnyShort (ShortI s)
    {
      if (s.isNull())
        ConstBoolean.this.setToValue (false);
      else if (s.getValue() != 0)
        ConstBoolean.this.setToValue (true);
      else
        ConstBoolean.this.setToValue (false);
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        ConstBoolean.this.setToValue (false);
      else if (f.getValue() != 0)
        ConstBoolean.this.setToValue (true);
      else
        ConstBoolean.this.setToValue (false);
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        ConstBoolean.this.setToValue (false);
      else if (d.getValue() != 0)
        ConstBoolean.this.setToValue (true);
      else
        ConstBoolean.this.setToValue (false);
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        ConstBoolean.this.setToValue (false);
      else if (l.getValue() != 0)
        ConstBoolean.this.setToValue (true);
      else
        ConstBoolean.this.setToValue (false);
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        ConstBoolean.this.setToValue (false);
      else if (d.signum() == 0)
        ConstBoolean.this.setToValue (false);
      else
        ConstBoolean.this.setToValue (true);
    }

		public void visitAnyObject (ObjectI o)
		{
			if (o.getValue() == null)
				ConstBoolean.this.setToValue(false);
			else
      {
        if (AnyNull.isNull(o.getValue()))
          ConstBoolean.this.setToValue(false);
        else
          ConstBoolean.this.setToValue(true);
      }
		}

		public void visitUnknown(Any o)
		{
			ConstBoolean.this.setToValue (true);
		}

		public void visitMap (Map m)
		{
			ConstBoolean.this.setToValue (true);
		}

		public void visitSet (Set s)
		{
      ConstBoolean.this.setToValue (true);
		}

		public void visitArray (Array a)
		{
      ConstBoolean.this.setToValue (true);
		}
  }
}

