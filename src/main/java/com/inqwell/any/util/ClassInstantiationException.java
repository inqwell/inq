/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any.util;
import com.inqwell.any.ContainedException;

/**
 * Wraps up the exceptions that can be thrown by Class.forName and
 * Class.newInstance() that is ClassNotFoundException, IllegalAccessException
 * and InstantiationException.
 */
public class ClassInstantiationException extends ContainedException
{
  public ClassInstantiationException (Exception e) { super(e); }
}
