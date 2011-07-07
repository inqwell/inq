/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/ReplacingStream.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.io;

import com.inqwell.any.Any;
import com.inqwell.any.Func;
import com.inqwell.any.AnyException;

/**
 * Supports a common interface for ReplacingInputStream and
 * ReplacingOutputStream.
 */
public interface ReplacingStream
{
  public void setReplacementInfo(Any a, Any b);
  public Any  getReplacementInfo(Any a);
}
