/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ExtensionOf.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Evaluate the extension component of the operand, which must be
 * an AnyFile.  Returns the extension as an AnyFile
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class ExtensionOf extends    AbstractFunc
												 implements Cloneable
{
	private Any file_;
	
	public ExtensionOf(Any file)
	{
		file_   = file;
	}
	
	public Any exec(Any a) throws AnyException
	{
		AnyFile file    = (AnyFile)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 file_,
																					 AnyFile.class);

		return file.getExtension();
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    ExtensionOf e = (ExtensionOf)super.clone();
    
    e.file_       = file_.cloneAny();
    
    return e;
  }
}

