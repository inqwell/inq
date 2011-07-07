/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Indicates that an unknown or unsupported data type has been specified in
 * a Business Object Type definition
 */
public class UnknownTypeException extends AnyException
{  
  public UnknownTypeException () { super(); }
  public UnknownTypeException (String s) { super(s); }
}
