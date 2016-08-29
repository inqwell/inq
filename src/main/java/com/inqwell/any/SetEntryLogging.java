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
 * 
 * 
 * <p>
 * @author $Author: sanderst $
 */
public class SetEntryLogging extends    AbstractFunc
									           implements Cloneable
{
	private Any package_;
	private Any entity_;
	
	private boolean add_;
	
	public SetEntryLogging(Any pkg, Any entity, boolean add)
	{
		package_ = pkg;
		entity_  = entity;

		add_     = add;
	}
	
	public Any exec(Any a) throws AnyException
	{
		if (add_)
			Call.addLoggedEntity(package_, entity_);
		else
			Call.removeLoggedEntity(package_, entity_);
		
		return AnyNull.instance();
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    SetEntryLogging s = (SetEntryLogging)super.clone();
        
    return s;
  }
}
