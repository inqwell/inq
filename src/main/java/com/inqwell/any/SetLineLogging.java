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
public class SetLineLogging extends    AbstractFunc
									          implements Cloneable
{
	private Any execFQName_;
	private int start_;
	private int end_;
		
	public SetLineLogging(Any execFQName, int start, int end)
	{
		start_ = start;
		end_   = end;

		execFQName_ = execFQName;
	}
	
	public Any exec(Any a) throws AnyException
	{
    EvalExpr.setLineLogging(execFQName_, start_, end_);
    
		return AnyNull.instance();
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    SetLineLogging s = (SetLineLogging)super.clone();
        
    return s;
  }
}
