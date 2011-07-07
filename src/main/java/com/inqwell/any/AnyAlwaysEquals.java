/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyAlwaysEquals.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.io.ObjectStreamException;

/**
 * An <code>Any</code> which always compares <code>true</code> for
 * <code>equals()</code>.
 * <p>
 * Developer's note: you need to insert a call
 * to <code>AnyAlwaysEquals.isAlwaysEquals(Object)</code> in
 * any <code>equals()</code> methods that you want to
 * behave in this way to ensure that <code>equals()</code>
 * is commutative.
 */
public class AnyAlwaysEquals extends    AbstractAny
                             implements Cloneable
{
  public static Any equals__ = AbstractValue.flyweightString("equals");
  
	static AnyAlwaysEquals instance__;
	
	static
	{
		// Make the only instance ever allowed in this JVM
		instance__ = new AnyAlwaysEquals();
	}

	/**
	 * In order to make <code>equals()</code> commutative
	 * <code>Any</code> implementations of <code>equals()</code>
	 * must call this method to check if they are being
	 * compared with <code>AnyAlwaysEquals</code>
	 * <p>
	 * <b>Note:</b>Composites containing <code>AnyAlwaysEquals</code>
	 * cannot be used as Map keys, as they can violate the contract
	 * between <code>hashCode()</code> and <code>equals()</code>.
	 */
	public static boolean isAlwaysEquals(Object o)
	{
		return(o == instance__ || o instanceof AnyAlwaysEquals);
	}
	
	public static Any instance()
	{
		return instance__;
	}
	
	/**
	 * These objects cannot be constructed by any class other than
	 * ourselves.  See the comments on <code>clone()</code>
	 */
	private AnyAlwaysEquals()
	{
	}
	
	/**
	 * Returns a fixed value
	 */
	public int hashCode()
	{
		return 1;
	}
	
  public String toString()
  {
    return "#";
  }
  
	/**
	 * Always return true
	 */
	public boolean equals(Object o)
	{
		return true;
	}

	/**
	 * Always return true
	 */
	public boolean equals(Any a)
	{
		return true;
	}

  /**
   * Support cloning as for many <code>Any</code>s but
   * just return <code>this</code>.  We don't want unregulated
   * creation of these objects since you can't compare them
   * using normal equals() since that is always <code>true</code>!
   * For those occasions when testing whether an object <i>is</i>
   * <code>AnyAlwaysEquals</code> you have to compare by object
   * identity.
   */
  public Object clone() throws CloneNotSupportedException
  {
  	return this;
  }

  /**
   * You can't even get a separate instance of this object by
   * serializing it in from elsewhere!
   */
	protected Object readResolve() throws ObjectStreamException
	{
		return AnyAlwaysEquals.instance();
	}
}
