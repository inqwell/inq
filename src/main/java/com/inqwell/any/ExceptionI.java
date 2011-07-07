/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ExceptionI.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 19:59:22 $
 */

package com.inqwell.any;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * This interface harmonises the two exception classes within
 * Inq, <code>AnyException</code> and <code>AnyRuntimeException</code>,
 * so they can be treated as one wherever this is required.
 * <p>
 * This interface is only required as long as there are these
 * two classes, given that only the latter is really required. In
 * future, <code>AnyException</code> will be removed, which will
 * also simplify Inq code exception interfaces (by removing them
 * altogether) with corresponding code reduction. TODO.
 * <p>
 * @author Tom
 * @version $Revision: 1.4 $
 *
 */
public interface ExceptionI extends Any
{
  // Standard exception methods we want to access via the interface
  public String getMessage();
  public void printStackTrace(PrintWriter s);
  public void printStackTrace(PrintStream s);
  public void printStackTrace();
  
  // Inq additional methods
  public Any getExceptionMessage();
  public Any getExceptionName();
  public Any getUserInfo();
  public DateI getTime();
  public AnyFuncHolder.FuncHolder getHandler();
  public void setHandler(AnyFuncHolder.FuncHolder handler);
  public boolean isChecked();
  public boolean isCommit();
  public boolean isUser();
  public void setCommit(boolean commit);
  public Any fillInCallStack(Transaction t);
  public Any topOfStack(Transaction t);
  public Any getCallStack();
  public void setLineNumber(int line);
  public int getLineNumber();
}
