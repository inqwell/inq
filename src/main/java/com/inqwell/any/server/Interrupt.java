/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Interrupt.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * Attempt to interrupt the specified process.  If the
 * specified process is currently executing a transaction
 * then it will be aborted at some point in the not too
 * distant future.
 * <p>
 * Returns the process.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Interrupt extends    AbstractFunc
                        implements Cloneable
{
	private Any process_;
	
	public Interrupt(Any process)
	{
		process_ = process;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Process process = (Process)EvalExpr.evalFunc(getTransaction(),
                                                 a,
                                                 process_,
                                                 Process.class);

		process.getTransaction().interrupt();

		return process;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Interrupt i = (Interrupt)super.clone();
		i.process_ = AbstractAny.cloneOrNull(process_);
		return i;
  }
	
}
