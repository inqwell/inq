/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/channel/IsEmpty.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.channel;

import com.inqwell.any.*;

/**
 * Return <code>true</code> or <code>false</false> as to whether
 * the specified input channel is empty.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class IsEmpty extends    AbstractFunc
                     implements Cloneable
{
	
	private Any ic_;
	
	public IsEmpty(Any ic)
	{
		ic_ = ic;
	}
	
	public Any exec(Any a) throws AnyException
	{
		InputChannel ic = (InputChannel)EvalExpr.evalFunc(getTransaction(),
                                                      a,
                                                      ic_);

		AnyBoolean ret = new AnyBoolean(false);
		ret.setValue(ic.isEmpty());
		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		IsEmpty i = (IsEmpty)super.clone();
		i.ic_ = AbstractAny.cloneOrNull(ic_);
		return i;
  }
	
}
