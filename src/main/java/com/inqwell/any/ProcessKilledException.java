/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

import com.inqwell.any.AnyFuncHolder.FuncHolder;

/**
 * An exception that is thrown when a process is killed. It allows
 * for execution to abort up the stack while executing any finally
 * blocks there may be on the way.
 * 
 * @author tom
 */
public class ProcessKilledException extends RuntimeContainedException
{

  public ProcessKilledException(Throwable t)
  {
    super(t);
  }

  public ProcessKilledException(Throwable t, String message)
  {
    super(t, message);
  }

  public ProcessKilledException(Throwable t, String message, Any userInfo)
  {
    super(t, message, userInfo);
  }

  public ProcessKilledException(Throwable t, String message, Any userInfo,
      FuncHolder handler)
  {
    super(t, message, userInfo, handler);
  }

  public ProcessKilledException(Throwable t, String message, Any userInfo,
      FuncHolder handler, boolean isUser)
  {
    super(t, message, userInfo, handler, isUser);
  }
}
