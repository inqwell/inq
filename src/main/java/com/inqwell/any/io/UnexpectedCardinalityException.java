/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any.io;

import com.inqwell.any.AnyRuntimeException;

public class UnexpectedCardinalityException extends AnyRuntimeException
{
  public UnexpectedCardinalityException () { super(); }
  public UnexpectedCardinalityException (String s) { super(s); }
}
