/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ExceptionHandler.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;

/**
 * Allow for replaceable exception handling behaviour where exceptions are
 * actually handled.
 */
public interface ExceptionHandler extends Any
{
  public static Any msgArg__         = new ConstString("msg");
  public static Any stackTraceArg__  = new ConstString("stackTrace");
  public static Any isUserArg__      = new ConstString("isUser");
  public static Any handlerArg__     = new ConstString("handler");
  public static Any exTimeArg__      = new ConstString("exTime");
  public static Any exInfoArg__      = new ConstString("exInfo");
  public static Any isCommitArg__    = new ConstString("isCommit");

  /**
	 * Do something to handle a checked exception
	 */
	public void handleException (AnyException e, Transaction t);
	
	/**
	 * Do something to handle an unchecked exception
	 */
	public void handleException (AnyRuntimeException e, Transaction t);
	
	/**
	 * Gets the last exception handled by this handler.
	 * @return the exception or null if no exception is available.
	 */
  public Throwable getLastException();
  
	/**
   * Optional operation to tell this exception handler
   * the process's input channel
   */
	public void setInputChannel(InputChannel ic);
	
	public void setServerConnected(boolean isConnected);
  
  public Any setDefaultFunc(AnyFuncHolder.FuncHolder f);
  
  public void setHandlerProcess(Any id);
  
  public void setProcess(Process p);
}
