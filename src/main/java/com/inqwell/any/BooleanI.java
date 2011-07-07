/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/BooleanI.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

/**
 * The boolean value type interface. Concrete implementations are
 * the mutable and const classes and the const decorator for use
 * when constness is required to be dynamic.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public interface BooleanI extends Value
{
  public boolean  getValue();
  public void setValue(boolean value);
  public void fromString(String s);
}
