/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import com.inqwell.any.server.Server;

/**
 * An ExceptionHandler implementation that invokes any handler function
 * carried in the exception. If this function returns <code>false</code>
 * or there is no handler then the function contained in <code>this</code>
 * is invoked with the exception payload.
 * @author Tom
 *
 */
public class ExceptionToFunc extends    AbstractExceptionHandler
                             implements ExceptionHandler
{
  private static final long serialVersionUID = 1L;
  
  private AnyFuncHolder.FuncHolder f_;

  private BooleanI      isUser_  = new AnyBoolean();
  private BooleanI      handled_ = new AnyBoolean();
  
  private Any           handlerProcess_;
  
  public ExceptionToFunc()
  {
    // Setup a default function in case the process never sets one
    Any f = Server.instance().getDefaultExceptionHandler();
    if (f != null)
      f_ = (AnyFuncHolder.FuncHolder)f.cloneAny();
  }

  protected void handle(AnyException e, Transaction t)
  {
    if (handlerProcess_ == null)
      doException(e, t);
    else
    {
      boolean handled = false;
      try
      {
        handled = sendToHandlerProcess(e, t);
      }
      catch(Exception ee)
      {
        doException(e, t);
        handled = true;
      }
      
      if (!handled)
        doException(e, t);
    }
  }

  protected void handle(AnyRuntimeException e, Transaction t)
  {
    if (handlerProcess_ == null)
      doException(e, t);
    else
    {
      try
      {
        sendToHandlerProcess(e, t);
      }
      catch(Exception ee)
      {
        doException(e, t);
      }
    }
  }
  
  private void doException(ExceptionI e, Transaction t)
  {
    handled_.setValue(false);
    isUser_.setValue(e.isUser());
    Any userInfo = e.getUserInfo();
    AnyFuncHolder.FuncHolder f = e.getHandler();

    try
    {
      // The handlers execute at $root (unless the func defines
      // a context)
      getProcess().setContextPath(ServerConstants.NSROOT);
      if (f != null)
      {
        Any ret = doFunc(e, f, userInfo, e.getTime(), t);
        
        handled_.copyFrom(ret);
      }
      
      // If there was no handler in the exception (or there was and it
      // reported that it didn't handle the exception) then call
      // any default handler that we may have.
      if (!handled_.getValue())
      {
        if  (f_ != null && !f_.isNull())
          doFunc(e, f_, userInfo, e.getTime(), t);
        else
        {
          // No handler carried in the exception and no default
          // handler specified here. Probably in a primordial
          // phase of server startup. Just print to stderr.
          e.printStackTrace();
        }
      }
      
      //t.commit();
    }
    catch(Exception ex)
    {
      // Damn, the hander threw. Give up
      e.printStackTrace();
      ex.printStackTrace();
      try
      {
        t.abort();
      }
      catch(Exception eex)
      {
        eex.printStackTrace();
      }
    }
    finally
    {
      getProcess().setContextPath(null);
      getProcess().setContext(null);
    }
  }

  private boolean sendToHandlerProcess(ExceptionI e, Transaction t) throws AnyException
  {
    Process proc = GetProcess.getProcess(handlerProcess_);
    
    if (proc != null)
    {
      proc.send(e);
      return true;
    }
    else
      return false;
  }
  
  public Any setDefaultFunc(AnyFuncHolder.FuncHolder f)
  {
    Any cur = f_;
    f_ = f;
    return cur;
  }
  
  public void setHandlerProcess(Any id)
  {
    handlerProcess_ = id;
  }
  
  private Any doFunc(ExceptionI               e,
                     AnyFuncHolder.FuncHolder handler,
                     Any                      userInfo,
                     Any                      exTime,
                     Transaction              t) throws AnyException
  {
    Any ret = AnyBoolean.FALSE;
      
    CharArrayWriter cw;
    PrintWriter pw = new PrintWriter(cw = new CharArrayWriter());
    e.printStackTrace(pw);
    //e.printStackTrace();

    Map args = AbstractComposite.simpleMap();

    args.add(msgArg__, new AnyString(e.getMessage()));
    args.add(stackTraceArg__, new AnyString(cw.toString()));
    args.add(isUserArg__, isUser_);
    args.add(exInfoArg__, userInfo);
    args.add(exTimeArg__, exTime);
    args.add(isCommitArg__, new ConstBoolean(e.isCommit()));
    
    // If its a call statement, make sure the arguments
    // get through an xfunc.
    Func f = handler.getFunc();
    if (f instanceof Call)
    {
      // We have to clone the call statement now because we are going to
      // manipulate its call arguments to add the exception stuff.
      // It gets cloned in doFunc (which is now unnecessary) when the
      // call is actually made but meh. The func holder itself is OK
      // because it will already have been cloned by the throw() statement.
      // Note that only exceptions thrown with throw() can have a
      // handler function anyway.
      Call c = (Call)f.cloneAny();
      Map cArgs = c.getArgs();
      cArgs.replaceItem(ExceptionHandler.msgArg__,        new LocateNode("$stack.msg"));
      cArgs.replaceItem(ExceptionHandler.stackTraceArg__, new LocateNode("$stack.stackTrace"));
      cArgs.replaceItem(ExceptionHandler.isUserArg__,     new LocateNode("$stack.isUser"));
      cArgs.replaceItem(ExceptionHandler.exTimeArg__,     new LocateNode("$stack.exTime"));
      cArgs.replaceItem(ExceptionHandler.exInfoArg__,     new LocateNode("$stack.exInfo"));
      cArgs.replaceItem(ExceptionHandler.isCommitArg__,   new LocateNode("$stack.isCommit"));
      
      // Put the clone of the call back into the func holder. All a bit
      // messy but this is the only case
      handler.setValue(c);
    }
    // Unlike its ExceptionToService counterpart, this is not required.
    // We are invoking the handler here, not sending it to a service
    // request to be invoked.
    //args.add(handlerArg__, handler);


    ret = handler.doFunc(t, args, getProcess().getRoot());

    return ret;
  }
}
