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
	  
		System.err.println ("transmitException(AnyException): " + e);
		e.printStackTrace();
		try
		{
			oc_.write (e);
			oc_.flushOutput();
		}
		catch (Exception ex)
		{
			System.err.println (ex.getMessage());
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
    
		System.err.println ("transmitException(AnyRuntimeException): " + e);
		e.printStackTrace();
		try
		{
			oc_.write (e);
			oc_.flushOutput();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			// See above!
		}
	}

}
