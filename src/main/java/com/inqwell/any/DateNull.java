/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DateNull.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-18 21:44:25 $
 */
package com.inqwell.any;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class DateNull extends    java.util.Date
                      implements Serializable
{

	static DateNull instance__;
	
	static
	{
		// Make the only instance ever allowed in this JVM
		instance__ = new DateNull(2);
	}

  private DateNull(long time)
  {
    super(time);
  }
  
	public static DateNull instance()
	{
		return instance__;
	}
	
  public Object clone()
  {
  	return this;
  }

  protected Object readResolve() throws ObjectStreamException
  {
    return DateNull.instance();
  }
}
  
