/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/IsInstance.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Check if an object is (or could be) an instance of the given
 * Descriptor.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class IsInstance extends    AbstractFunc
												 implements Cloneable
{
	private Any descriptor_;
	private Any instance_;
	
	public IsInstance(Any descriptor, Any instance)
	{
		descriptor_ = descriptor;
		instance_   = instance;
	}
	
	public Any exec(Any a) throws AnyException
	{
    AnyBoolean ret = new AnyBoolean();
    
		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 descriptor_,
																					 Descriptor.class);

		Map        instance   = (Map)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 instance_,
																					 Map.class);

		if (instance != null)
    {
      Descriptor id = instance.getDescriptor();
			ret.setValue(descriptor.equals(id));
    }

		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    IsInstance i = (IsInstance)super.clone();
    
    i.descriptor_   = descriptor_.cloneAny();
    i.instance_     = instance_.cloneAny();
    
    return i;
  }
	
}
