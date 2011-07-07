/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/CharI.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

/**
 * The integer value type interface. Concrete implementations are
 * the mutable and const classes and the const decorator for use
 * when constness is required to be dynamic.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public interface CharI extends Value
{
  public char getValue();
  public void setValue(char value);
  public void fromString(String s);
}
