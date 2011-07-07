/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyTimer.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.Timer;

public class AnyTimer extends    AnyObject
											implements Cloneable
{
	public static AnyTimer null__ = new AnyTimer((Timer)null);

	/**
	 * Construct to wrap a pre-loaded Timer
	 */
	public AnyTimer(Timer t)
	{
		super(t);
	}
	
	public Timer getTimer()
	{
		return (Timer)getValue();
	}
	
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (!(a instanceof AnyTimer))
				throw new IllegalArgumentException("AnyTimer.copyFrom()");
			
			AnyTimer t = (AnyTimer)a;
			this.setValue(t.getValue());
		}
    return this;
  }
  
  public void cancel()
  {
    getTimer().cancel();
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
