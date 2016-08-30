/**
 * Copyright (C) 2016 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

/**
 * Enable or disable entry/exit logging on a specified package/function.
 * 
 * <p>
 * @author $Author: sanderst $
 */
public class ClearLineLogging extends    AbstractFunc
									          implements Cloneable
{
	public ClearLineLogging()
	{
	}
	
	public Any exec(Any a) throws AnyException
	{
    EvalExpr.clearLineLogging();
    
		return AnyNull.instance();
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    ClearLineLogging s = (ClearLineLogging)super.clone();
        
    return s;
  }
}
