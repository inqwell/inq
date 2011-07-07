/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyNull.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-21 22:18:47 $
 */
package com.inqwell.any;

import java.io.ObjectStreamException;

/**
 * An <code>Any</code> which represents <code>null</code>.
 */
public class AnyNull extends    AbstractAny
                     implements Cloneable
{
	static AnyNull instance__;
	
	static
	{
		// Make a reuasble instance
		instance__ = new AnyNull();
	}

	public static boolean isNull(Object o)
	{
		return(o == instance__ || o == null || ((o instanceof Value) && ((Value)o).isNull()) || o instanceof AnyNull);
	}
	
	public static Any instance()
	{
		return instance__;
	}
  
  public static boolean isNullInstance(Any a)
  {
    return a == instance__;
  }
	
	public AnyNull()
	{
	}
	
	public boolean equals(Object o)
	{
    if (o == null)
      return true;    // == Java null
    return (o == this);
	}

  public Any copyFrom (Any a)
  {
    // Copying to AnyNull is a silent no-operation
    return this;
  }
  
  /**
   * Support cloning as for many <code>Any</code>s but
   * just return <code>this</code>.  We don't want unregulated
   * creation of these objects since you can't compare them
   * using normal equals() since that is always <code>true</code>!
   * For those occasions when testing whether an object <i>is</i>
   * <code>AnyNull</code> you have to compare by object
   * identity.
   */
  public Object clone() throws CloneNotSupportedException
  {
  	return this;
  }

  public String toString()
  {
    return AnyString.EMPTY.toString();
  }

  /**
   * You can't even get a separate instance of this object by
   * serializing it in from elsewhere!
   */
	protected Object readResolve() throws ObjectStreamException
	{
		return AnyNull.instance();
	}
}
