/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/BreakException.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * An exception class whose purpose is to act as a control flow
 * interruption to a loop operation.
 * <p/>
 * Prematurely terminates a <code>foreach</code> and other loop types,
 * optionally defining the result of the loop function.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class BreakException extends FlowControlException
{
	private Any result_ = null;
	
	public BreakException() {}
	public BreakException(String s) { super(s); }
	public BreakException(Any result) { result_ = result; }
	public BreakException(Any result, String s) { super(s); result_ = result; }
	
	public Any getResult() { return result_; }
	public Object clone() throws CloneNotSupportedException { return super.clone(); }  
}
