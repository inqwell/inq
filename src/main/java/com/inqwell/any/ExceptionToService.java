/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ExceptionToService.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.io.PrintWriter;
import java.io.CharArrayWriter;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;

public class ExceptionToService extends    AbstractExceptionHandler
                                implements ExceptionHandler
{

	private OutputChannel ic_; // the process's input channel (to which we write)
  private boolean       isConnected_;

  private Any           service_;
  private Any           context_;
  private BooleanI      isUser_ = new AnyBoolean();

	public ExceptionToService(Any service, Any context)
	{
    service_ = service;
    context_ = context;
	}

	protected void handle(AnyException e, Transaction t)
	{
    // Check if there is a handler, a context and whether user/system
    isUser_.setValue(e.isUser());
    Any userInfo = e.getUserInfo();
    AnyFuncHolder.FuncHolder f = e.getHandler();
    Any context = null;
    if (f != null)
      context = f.getContextPath();

    svcRequest(e, f, context, userInfo, e.getTime());
	}

	protected void handle(AnyRuntimeException e, Transaction t)
	{
    // Check if there is a handler, a context and whether user/system
    isUser_.setValue(e.isUser());
    Any userInfo = e.getUserInfo();
    AnyFuncHolder.FuncHolder f = e.getHandler();
    Any context = null;
    if (f != null)
      context = f.getContextPath();

    svcRequest(e, f, context, userInfo, e.getTime());
	}

	public void setInputChannel(InputChannel ic)
	{
    ic_ = (OutputChannel)ic;
  }

	public void setServerConnected(boolean isConnected)
	{
    isConnected_ = isConnected;
	}

	private void svcRequest(ExceptionI               e,
                          AnyFuncHolder.FuncHolder handler,
                          Any                      context,
                          Any                      userInfo,
                          Any                      exTime)
	{
    try
    {
      CharArrayWriter cw;
      PrintWriter pw = new PrintWriter(cw = new CharArrayWriter());
      e.printStackTrace(pw);
e.printStackTrace();

      Map args = AbstractComposite.simpleMap();

      //System.out.println("********* 1 " + e.getMessage());
      args.add(msgArg__, new AnyString(e.getMessage()));

      //System.out.println("********* 2 " + cw.toString());
      args.add(stackTraceArg__, new AnyString(cw.toString()));

      if (handler != null)
      {
        // If its a call statement, make sure the arguments
        // get through an xfunc.  No need to clone the arguments
        // as long as this ExceptionHandler is always used on
        // the incoming side of serialization!!
        Func f = handler.getFunc();
        if (f instanceof Call)
        {
          Call c = (Call)f;
          Map cArgs = c.getArgs();
          cArgs.replaceItem(ExceptionHandler.msgArg__,        new LocateNode("$stack.msg"));
          cArgs.replaceItem(ExceptionHandler.stackTraceArg__, new LocateNode("$stack.stackTrace"));
          cArgs.replaceItem(ExceptionHandler.isUserArg__,     new LocateNode("$stack.isUser"));
          cArgs.replaceItem(ExceptionHandler.exTimeArg__,     new LocateNode("$stack.exTime"));
          cArgs.replaceItem(ExceptionHandler.exInfoArg__,     new LocateNode("$stack.exInfo"));
          cArgs.replaceItem(ExceptionHandler.isCommitArg__,   new LocateNode("$stack.isCommit"));
        }
        args.add(handlerArg__, handler);
      }

      args.add(isUserArg__, isUser_);
      args.add(exInfoArg__, userInfo);
      args.add(exTimeArg__, exTime);

      // If there was a context supplied in the exception
      // then use it, otherwise use the default for this
      // ExceptionHandler
      if (context == null)
        context = context_;

      ic_.write(SendRequest.makeRequestEvent(isConnected_
                                               ? EventConstants.INVOKE_SVC
                                               : EventConstants.INVOKE_LOGINSVC,
                                             service_,
                                             context,
                                             null,
                                             args,
                                             null));
    }
    catch (AnyException ex)
    {
      ex.printStackTrace();
    }
	}
}
