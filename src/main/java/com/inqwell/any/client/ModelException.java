/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/client/ModelException.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.AnyException;

/**
 * Indicates something went wrong when processing a
 * component's data model
 */
public class ModelException extends AnyException
{
  public ModelException () { super(); }
  public ModelException (String s) { super(s); }
}
