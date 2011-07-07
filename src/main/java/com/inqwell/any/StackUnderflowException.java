/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/StackUnderflowException.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

public class StackUnderflowException extends AnyRuntimeException
{
  public StackUnderflowException () { super(); }
  public StackUnderflowException (String s) { super(s); }
}
