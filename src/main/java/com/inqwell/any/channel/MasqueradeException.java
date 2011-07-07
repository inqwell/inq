/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/channel/MasqueradeException.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.SetUniqueKey;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.CharArrayWriter;
import java.io.IOException;

/**
 * An exception class instances of which are constructed from other exception
 * class instances, appearing to take on their current stack trace and message
 * content at the time the instance is created.
 * <p>
 * This class supports the transport of exception information from instances
 * of exceptions raised in one JVM to another where the original exception class
 * is not available.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class MasqueradeException extends AnyException
{
	private String stackTrace_;
	
  public MasqueradeException(Exception                e,
                             Any                      userInfo,
                             AnyFuncHolder.FuncHolder handler,
                             boolean                  isUser)
  {
    super(e.getMessage(), userInfo, handler, isUser);
    stackTrace_ = MasqueradeException.spoofStackTrace(e);
  }
  
//	public MasqueradeException(Exception e)
//	{
//		super(e.getMessage());
//		stackTrace_ = MasqueradeException.spoofStackTrace(e);
//	}
	
	public void printStackTrace() { System.err.println (stackTrace_); }
	public void printStackTrace(PrintStream s) { s.println(stackTrace_); }
	public void printStackTrace(PrintWriter s) { s.println(stackTrace_); }
	public Object clone() throws CloneNotSupportedException { return super.clone(); }

  static String spoofStackTrace(Exception e)
  {
		CharArrayWriter cw;
		PrintWriter pw = new PrintWriter(cw = new CharArrayWriter());
		e.printStackTrace(pw);
		return cw.toString();
  }
  
}
