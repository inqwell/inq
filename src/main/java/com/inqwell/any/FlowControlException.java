/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

/**
 * Base class for exceptions that are implemented to manage flow
 * control discontinuities in loops
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public class FlowControlException extends AnyException
{
  public FlowControlException() {}
  public FlowControlException(String s) { super(s); }
}
