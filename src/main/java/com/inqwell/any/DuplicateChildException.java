/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

public class DuplicateChildException extends AnyRuntimeException
{
	public DuplicateChildException () { super(); }
	public DuplicateChildException (String s) { super(s); }
}

