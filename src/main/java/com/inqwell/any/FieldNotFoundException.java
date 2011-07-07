/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

public class FieldNotFoundException extends AnyRuntimeException
{
	public FieldNotFoundException () { super(); }
	public FieldNotFoundException (String s) { super(s); }
}

