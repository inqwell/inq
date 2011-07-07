/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

public interface ObjectI extends Any
{
  public Object getValue();
  public void setValue(Object value);
}
