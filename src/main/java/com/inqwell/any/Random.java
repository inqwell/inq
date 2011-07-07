/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Random.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import javax.jms.JMSException;

/**
 * Random number generation.
 * Generate a random number by returning the next in the sequence
 * yielded by the JVM.
 * <p/>
 * The first argument is a value to which the random number will be
 * written and whose type characterises the use of the {@link java.util.Random}
 * class.
 * <p/>
 * The second argument is only used if the first is an integer and defines
 * the maximum bound according to {@link java.util.Random#nextInt(int)}.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class Random extends    AbstractFunc
										implements Cloneable
{
  static private java.util.Random random__;

  private Any type_;
  private Any range_;
  
  static
  {
    synchronized(Random.class)
    {
      if (random__ == null)
        random__     = new java.util.Random();
    }
  }

  public static Any random()
  {
    return new AnyInt(random__.nextInt());
  }

  public static int positiveRandom()
  {
    return random__.nextInt(Integer.MAX_VALUE);
  }

  public Random(Any type, Any range)
  {
    type_  = type;
    range_ = range;
  }
  
  public Any exec (Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any type = EvalExpr.evalFunc(t,
                                 a,
                                 type_);
    
    if (type == null && type_ != null)
      nullOperand(type_);
    
    IntI range = (IntI)EvalExpr.evalFunc(t, a, range_, IntI.class);

    if (range == null && range_ != null)
      nullOperand(range_);

    Any ret;
    if (type == null)
      ret = random();
    else
    {
      NextAny nextAny = new NextAny(type, range);
      nextAny.nextAny();
      ret = type;
    }
    
    return ret;
  }
  
  public Iter createIterator ()
  {
  	return DegenerateIter.i__;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Random r = (Random)super.clone();
    
    r.range_ = AbstractAny.cloneOrNull(range_);
    r.type_  = AbstractAny.cloneOrNull(type_);

    return r;
  }
  
  private Any nextAny(Any type)
  {
    
    return null;
  }
  
  static private class NextAny extends AbstractVisitor
  {
    private Any  type_;
    private IntI range_;
    
    private NextAny(Any type, IntI range)
    {
      type_  = type;
      range_ = range;
    }
    
    private Any nextAny()
    {
      type_.accept(this);
      return type_;      
    }
    
    public void visitAnyBoolean(BooleanI b)
    {
      b.setValue(random__.nextBoolean());
    }

    public void visitAnyDouble(DoubleI d)
    {
      d.setValue(random__.nextDouble());
    }

    public void visitAnyFloat(FloatI f)
    {
      f.setValue(random__.nextFloat());
    }

    public void visitAnyInt(IntI i)
    {
      if (range_ != null)
        i.setValue(random__.nextInt(range_.getValue()));
      else
        i.setValue(random__.nextInt());
    }

    public void visitAnyLong(LongI l)
    {
      l.setValue(random__.nextLong());
    }

    public void visitDecimal(Decimal d)
    {
      // For decimal, do double and convert to string
      double dbl = random__.nextDouble();
      d.fromString(String.valueOf(dbl));
    }
    
    public void visitArray(Array a)
    {
      if (a instanceof AnyByteArray)
      {
        AnyByteArray ba = (AnyByteArray)a;
        random__.nextBytes(ba.getValue());
      }
      else
        throw new AnyRuntimeException("Not a byte array");
    }
  }
}
