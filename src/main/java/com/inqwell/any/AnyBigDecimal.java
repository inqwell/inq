/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyBigDecimal.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.math.BigDecimal;

/**
 * Concrete class AnyBigDecimal.  BigDecimal data type of Any.
 * Neither this class or any operator classes support assignment
 * from the non-integral fundamental types, represented by
 * <code>DoubleI</code> or <code>FloatI</code>, so as to
 * prevent the loss of accuracy associated with these types
 * to creep in to calculations without realising.
 */
public class AnyBigDecimal extends    ConstBigDecimal
                           implements Numeric,
                                      Decimal,
                                      Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyBigDecimal.class);
  
  private transient AssignerVisitor copier_;
  
  public AnyBigDecimal()
  {
  	super();
  }

  public AnyBigDecimal(BigDecimal d)
  {
  	super(d);
  }

  public AnyBigDecimal(String s)
  {
  	super(s);
  }

  public AnyBigDecimal(String s, int scale)
  {
    super(s, scale);
  }
  
	public void fromString(String s)
	{
    setFromString(s);
	}

  public Any bestowConstness()
  {
    return new ConstBigDecimalDecor(this);
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyBigDecimal a = (AnyBigDecimal)super.clone();

    a.copier_ = null;

    return a;
  }

  public Any buildNew(Any a)
  {
    AnyBigDecimal ret = new AnyBigDecimal();
    ret.setScale(this.scale());
    if (a != null)
      ret.copyFrom(a);
    return ret;
  }
  
  public Any copyFrom (Any a)
  {
    if (a != null)
    {
      if (a != this)
      {
        if (copier_ == null)
          copier_ = makeCopier();
        copier_.copy (a);
      }
    }
    else
      setNull();

    return this;
  }
  
  public void setValue(BigDecimal value)
  {
    setToValue(value);
  }

  public void setNull()
  {
    setToNull();
  }

  public void setScale(int scale)
  {
    setToScale(scale);
  }

  public void fromInt(int i)
  {
    setFromInt (i);
  }

  public boolean isConst()
  {
    return false;
  }
}

