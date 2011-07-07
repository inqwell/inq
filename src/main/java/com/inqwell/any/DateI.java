/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DateI.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

/**
 * The date value type interface. Concrete implementations are
 * the mutable and const classes and the const decorator for use
 * when constness is required to be dynamic.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public interface DateI extends Value
{
  public java.util.Date getValue();
  public void setValue(java.util.Date value);
  public long getTime();
  public void setTime(long time);
  public void fromString(String s);
  public int compareTo(DateI other);
}
