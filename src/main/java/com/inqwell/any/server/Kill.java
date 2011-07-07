/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Kill.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * Attempt to kill the specified process.  Returns <code>null</code>
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Kill extends    AbstractFunc
                  implements Cloneable
{
	
	private Any process_;
	
	public Kill(Any process)
	{
		process_ = process;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Process process = (Process)EvalExpr.evalFunc(getTransaction(),
                                                 a,
                                                 process_,
                                                 Process.class);

		process.kill(getTransaction().getProcess());

		return null;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Kill k = (Kill)super.clone();
		k.process_ = AbstractAny.cloneOrNull(process_);
		return k;
  }
	
}
