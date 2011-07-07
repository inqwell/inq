/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ContinueException.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * An exception class whose purpose is to act as a control flow
 * interruption to a loop operation.
 * <p>
 * Prematurely terminates the <i>current iteration</i> of
 * a <code>foreach</code> and other loop types.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class ContinueException extends FlowControlException
{
	public ContinueException() {}
	public ContinueException(String s) { super(s); }
	
	public Object clone() throws CloneNotSupportedException { return super.clone(); }  
}
