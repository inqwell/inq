/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.util.HashMap;



/**
 * Provides default implementations for the methods defined
 * in interface Value.
 */
public abstract class AbstractValue implements Value
{

  static protected String reason__ = "Attempt to mutate a value that has become const";

  static private Map flyweights__;
  static private HashMap<String, Any> flyweightStrings__;

	/**
	 * Returns the flyweighted const object or the argument, if not already seen.
	 * If the object is not const and moreover not permanently const then
	 * an IllegalArgumentException is thrown.
	 */
	public static Any flyweightConst(Any a)
	{
	  if (!a.isConst())
	    throw new IllegalArgumentException("Not a const value");

	  if (a != a.bestowConstness())
	    throw new IllegalArgumentException("Not a permanent const value");

	  synchronized(AbstractValue.class)
	  {
	    if (flyweights__ == null)
	      flyweights__ = AbstractComposite.simpleMap();
	    
  	  if (flyweights__.contains(a))
  	    a = flyweights__.get(a);
  	  else
  	    flyweights__.add(a, a);
  	}

  	return a;
  }

  public static Any flyweightString(String s)
  {
    synchronized(AbstractValue.class)
    {
      if (flyweightStrings__ == null)
        flyweightStrings__ = new HashMap<String, Any>();
      
      if (flyweightStrings__.containsKey(s))
        return flyweightStrings__.get(s);
      
      Any a = new ConstString(s);
      flyweightStrings__.put(s, a);
      
      return flyweightConst(a);
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

  public boolean equals(Any a) { return false; }

  public final Any cloneAny ()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw (new RuntimeContainedException (e));
    }
    return a;
  }

  public Any buildNew (Any a)
  {
    try
    {
      AbstractValue ret = (AbstractValue)getClass().newInstance();
      if (a != null)
        ret.initialiseFrom(a);

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

  public boolean isTransactional()
  {
		return false;
  }

  public boolean isConst()
  {
    return false;
  }

  public boolean like (Any a)
  {
    if (getClass() == a.getClass())
      return true;
    else
      return false;
  }

  public Any bestowConstness()
  {
    throw new AnyRuntimeException("No const implementation available");
  }

  protected abstract void initialiseFrom(Any a);

  /**
   * Checks if a is of the same class as this.  Return a if so, otherwise
   * make a new instance of class this and assign value from a using
   * copyFrom()
   */
  protected final Any likeMe(Any a)
  {
    if (getClass().isInstance(a))
      return a;
    else
      return buildNew(a);
  }

  protected final void constViolation()
  {
    throw new AnyRuntimeException("Attempt to mutate const value");
  }

  protected final void constViolation(String reason)
  {
    throw new AnyRuntimeException(reason);
  }
}
