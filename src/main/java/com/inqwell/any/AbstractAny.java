/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/AbstractAny.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.io.PrintStream;

/**
 * Provides default implementations of all methods in the Any interface
 * allowing any class to be part of the Any hierarchy
 */
public abstract class AbstractAny implements Any
{
  static protected String reason__ = "Attempt to mutate a value that has become const";

  public static Any cloneOrNull(Any a)
	{
		if (a != null)
		{
			a = a.cloneAny();
		}
		return a;
	}
	
	public static void stackTrace()
	{
		stackTrace(null, System.out);
	}
	
	public static void stackTrace(PrintStream s)
	{
		stackTrace(null, s);
	}
	
	public static void stackTrace(String message)
	{
		stackTrace(message, System.out);
	}
	
	public static void stackTrace(String message, PrintStream s)
	{
		Throwable t = new Throwable(message);
		t.fillInStackTrace();
		t.printStackTrace(s);
	}
	
  public static Any ripSafe(Any a, Transaction t)
  {
    // Check if there is information in the transaction that says
    // we would be aliasing a private map instance. We don't want
    // this to happen
    if (t.getResolving() == Transaction.R_MAP)
    {
      a = t.getLastTMap();
    }
    else
    // And check if we are field ripping. We definitely don't want to do
    // this either, even if the container is already joined into the
    // transaction (we may not yet have modified the field meaning it
    // would not become part of the eventual event). Make it const.
    if (t.getResolving() == Transaction.R_FIELD)
    {
      a = t.getLastTField();
      if (a != null)
        a = a.bestowConstness();
    }
    
    return a;
  }
  
  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitUnknown(this);
  }

  public Any copyFrom (Any a)
  {
    throw new IllegalArgumentException ("copyFrom() not supported to " + getClass() + " from " + a.getClass());
  }
  
  public Any buildNew (Any a)
  {
    try
    {
      Any ret = (Any)getClass().newInstance();
      if (a != null)
        ret.copyFrom(a);
      
      return ret;
    }
    catch (InstantiationException e)
    {
      throw new RuntimeContainedException(e);
    }
    catch (IllegalAccessException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public boolean equals(Object o)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(o))
			return true;

    if (o instanceof Any)
      return equals ((Any)o);
    return false;
  }

  public boolean equals(Any a)
  {
		return this == a;
  }
  
  public boolean isTransactional()
  {
		return false;
  }

  public boolean isConst()
  {
    return false;
  }

  public Any bestowConstness()
  {
    return this;
  }
  
  public final Any cloneAny ()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw (new RuntimeContainedException(e));
    }
    return a;
  }
  
  protected final void constViolation()
  {
    throw new AnyRuntimeException("Attempt to mutate const value");
  }

  protected final void constViolation(String reason)
  {
    throw new AnyRuntimeException(reason);
  }

  /**
   * Static Section
   */
  
  // Some string constanst for the fundamental type names
  public static final Any BooleanName = new ConstString ("boolean");
  public static final Any CharName    = new ConstString ("char");
  public static final Any ByteName    = new ConstString ("byte");
  public static final Any ShortName   = new ConstString ("short");
  public static final Any IntegerName = new ConstString ("integer");
  public static final Any LongName    = new ConstString ("long");
  public static final Any FloatName   = new ConstString ("float");
  public static final Any DoubleName  = new ConstString ("double");
  public static final Any DecimalName = new ConstString ("decimal");
  public static final Any StringName  = new ConstString ("string");

  /**
   * Utility function which any class may call if it wants to signal
   * that cloning is invalid.  Although this is the default case for
   * Java classes, and Any classes via cloneAny(), if cloning has been
   * switched on within a given class hierarchy and you want to switch
   * it off again then the clone() method can use this function.
   */
  static public void cloneNotSupported(Object o) throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException (o.getClass().toString() +
                                          " does not support clone()");
  }
}
