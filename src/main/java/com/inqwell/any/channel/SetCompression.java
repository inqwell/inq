/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/channel/SetCompression.java $
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
public class SetCompression extends    AbstractFunc
                            implements Cloneable
{
	
	private Any oc_;
	private Any mode_;
	
	public SetCompression(Any oc, Any mode)
	{
		oc_   = oc;
    mode_ = mode;
	}
	
	public Any exec(Any a) throws AnyException
	{
		AnyChannel oc = (AnyChannel)EvalExpr.evalFunc(getTransaction(),
                                                  a,
                                                  oc_,
                                                  AnyChannel.class);

		Any mode = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 oc_);

		BooleanI compressed = new ConstBoolean(mode);
		oc.setCompressed(compressed.getValue());
		return oc;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		SetCompression s = (SetCompression)super.clone();
		s.oc_   = oc_.cloneAny();
		s.mode_ = mode_.cloneAny();
    
		return s;
  }
	
}
