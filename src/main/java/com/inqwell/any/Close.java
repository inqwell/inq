/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Close.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.io.PhysicalIO;

/**
 * Close an I/O stream.
 * 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Close extends    AbstractFunc
									 implements Cloneable
{
	private Any toClose_;
	
	public Close(Any toClose)
	{
		toClose_ = toClose;
	}
	
	public Any exec(Any a) throws AnyException
	{
		PhysicalIO toClose    = (PhysicalIO)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 toClose_,
																					 PhysicalIO.class);

		//System.out.println ("Close.exec() toClose_ " + toClose_);
		//System.out.println ("Close.exec() deleting " + toClose);
		toClose.close();

		return toClose;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    Close c = (Close)super.clone();
    
    c.toClose_   = toClose_.cloneAny();
    
    return c;
  }
	
}
