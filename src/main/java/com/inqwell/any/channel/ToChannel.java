/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/ToChannel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;

/**
 * A function object with which to parameterise an com.inqwell.any.rmi.AnyByRMII object
 * so that the reveived Any is passed to a channel.
 */
public class ToChannel extends AbstractFunc
{
	private OutputChannel oc_;

	public ToChannel (OutputChannel oc)
	{
		oc_ = oc;
	}

	public Any exec (Any a) throws AnyException 
	{
		oc_.write (a);
		return null;
	}
}
