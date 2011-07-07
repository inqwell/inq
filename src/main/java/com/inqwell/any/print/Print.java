/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/print/Print.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */
package com.inqwell.any.print;

import com.inqwell.any.Any;
import com.inqwell.any.Map;
import com.inqwell.any.AnyException;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.AnyPrintable;

/**
 * Print an AnyPrintable.
 * 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Print extends    AbstractFunc
									 implements Cloneable
{
	private Any toPrint_;
	private Any jobAttrs_;
	
	public Print(Any toPrint, Any jobAttrs)
	{
    toPrint_  = toPrint;
		jobAttrs_ = jobAttrs;
	}
	
	public Any exec(Any a) throws AnyException
	{
		AnyPrintable toPrint    = (AnyPrintable)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 toPrint_,
																					 AnyPrintable.class);

		Map jobAttrs    = (Map)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 jobAttrs_,
																					 Map.class);

    if (toPrint != null)
      PrintUtil.print(toPrint, jobAttrs);

		return toPrint;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    Print p = (Print)super.clone();
    
    p.toPrint_   = toPrint_.cloneAny();
    p.jobAttrs_  = AbstractAny.cloneOrNull(jobAttrs_);
    
    return p;
  }
}
