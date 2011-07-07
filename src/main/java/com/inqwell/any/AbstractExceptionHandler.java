/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/AbstractExceptionHandler.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;

public abstract class AbstractExceptionHandler extends    AbstractAny
                                               implements ExceptionHandler
{
  // The Process this exception handler is for
  private Process process_;
  
  // The last exception that was handled
  private Throwable lastException_;
  
  public void handleException (AnyException e, Transaction t)
  {
    lastException_ = e;
    handle(e, t);
  }
  
  /**
   * Do something to handle an unchecked exception
   */
  public void handleException (AnyRuntimeException e, Transaction t)
  {
    lastException_ = e;
    handle(e, t);
  }

  /**
   * No-op
   */
	public void setInputChannel(InputChannel ic) {}
	
	/**
   * No-op
   */
	public void setServerConnected(boolean isConnected) {}
  
  public Any setDefaultFunc(AnyFuncHolder.FuncHolder f)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setHandlerProcess(Any id)
  {
    if (id != null)
      throw new UnsupportedOperationException();
  }

  public void setProcess(Process process)
  {
    process_ = process;
  }
  
  public Throwable getLastException()
  {
    return lastException_;
  }
  
  protected abstract void handle(AnyException e, Transaction t);
  
  protected abstract void handle(AnyRuntimeException e, Transaction t);
  
  protected Process getProcess()
  {
    return process_;
  }
}
