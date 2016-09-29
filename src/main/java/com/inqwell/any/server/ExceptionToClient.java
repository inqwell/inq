/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/ExceptionToClient.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.inqwell.any.*;
import com.inqwell.any.channel.*;

/**
 * Forward uncaught exceptions to the connected client.
 * 
 * @author tom
 *
 */
public class ExceptionToClient extends    AbstractExceptionHandler
															 implements ExceptionHandler
{
	private OutputChannel oc_;

	private AnyFuncHolder.FuncHolder f_;
	
  private static     LogManager lm = LogManager.getLogManager();
  private static     Logger l = lm.getLogger("inq");

  public ExceptionToClient(OutputChannel oc)
	{
		oc_ = oc;
	}

  public Any setDefaultFunc(AnyFuncHolder.FuncHolder f)
  {
    Any cur = f_;
    f_ = f;
    return cur;
  }
  
  public void setHandlerProcess(Any id)
  {
    // We hijack this method to harmonise the use of @exception
    // when invoking services. When used in the context of a client's
    // UserProcess the argument is expected to be a function.
    if (id == null)
      setDefaultFunc(null);
    else
    {
      if (!(id instanceof AnyFuncHolder.FuncHolder))
        throw new AnyRuntimeException("Not a function");
      
      setDefaultFunc((AnyFuncHolder.FuncHolder)id);
    }
  }
  
	protected void handle(AnyException e, Transaction t)
	{
		transmitException(e);
	}
	
	protected void handle(AnyRuntimeException e, Transaction t)
	{
		transmitException(e);
	}

	private void transmitException (AnyException e)
	{
	  if (f_ != null)
	    e.setHandler(f_);
	  
		try
		{
			l.severe(e.getMessage());
			l.severe(getStackTrace(e));
			oc_.write (e);
			oc_.flushOutput();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			// Oh dear, we caught an exception but we couldn't send it to our
			// output channel!  Alternatives are (a) ignore; (b) thread dies!
			// Choose ignore for now
		}
	}

	private void transmitException (AnyRuntimeException e)
	{
    if (f_ != null)
      e.setHandler(f_);
    
		try
		{
			l.severe(e.getMessage());
			l.severe(getStackTrace(e));
			oc_.write (e);
			oc_.flushOutput();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			// See above!
		}
	}

	private String getStackTrace(ExceptionI e)
	{
    CharArrayWriter cw;
    PrintWriter pw = new PrintWriter(cw = new CharArrayWriter());
    e.printStackTrace(pw);
    return cw.toString();
	}
}
