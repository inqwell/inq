/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/ExceptionToStream.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.io.PrintStream;
import java.io.OutputStream;

public class ExceptionToStream extends    AbstractExceptionHandler
															 implements ExceptionHandler
{
	private PrintStream ps_;
	
	public ExceptionToStream(OutputStream os)
	{
		ps_ = new PrintStream(os);
	}

	public ExceptionToStream(PrintStream ps)
	{
		ps_ = ps;
	}

	protected void handle(AnyException e, Transaction t)
	{
		System.out.println(e.getMessage());
		e.printStackTrace(ps_);
	}
	
	protected void handle(AnyRuntimeException e, Transaction t)
	{
		System.out.println(e.getMessage());
		e.printStackTrace(ps_);
	}
}
