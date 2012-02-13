/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ReturnException.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * An exception class whose purpose is to act as a control flow
 * interruption to a function execution.
 * <p>
 * Prematurely terminates a function execution optionally
 * defining the result of the referring <code>call</code> function.
 * @author $Author: sanderst $
 * @see com.inqwell.any.Any
 */ 
public class ReturnException extends FlowControlException
{
	private Any result_ = null;
	
	public ReturnException() {}
	public ReturnException(String s) { super(s); }
	public ReturnException(Any result) { result_ = result; }
	public ReturnException(Any result, String s) { super(s); result_ = result; }
	
	public Any getResult() { return result_; }
	public Object clone() throws CloneNotSupportedException { return super.clone(); }  
}
