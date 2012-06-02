/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;


/**
 * WaitProc waits for the specified process to terminate with
 * an optional timeout in ms. Returns the process's exit status
 * if the process terminated [before timing out], or 127 if
 * the process did not terminate after timing out.
 *
 * @author $Author: sanderst $
 */
public class WaitProc extends    AbstractFunc
                      implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any proc_;
  private Any timeout_;
  
  public WaitProc(Any any, Any timeout)
  {
    proc_    = any;
    timeout_ = timeout;
  }

  public Any exec(Any a) throws AnyException
  {
    Process proc     = (Process)EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    proc_,
                                    Process.class);

    if (proc == null)
      nullOperand(proc_);
    
    Any timeout = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    timeout_);
    
    if (timeout == null && timeout_ != null)
      nullOperand(timeout_);
    
    LongI l = new AnyLong(0);
    if (timeout != null)
      l.copyFrom(timeout);

    long lt = l.getValue();
    
    proc.join(lt);
    
    Any exitStatus;
    if (proc.isAlive())
    {
    	// Process still alive (after a timeout)
    	exitStatus = UserProcess.ALIVE;
    }
    else
    {
    	// Is there an exit status?
    	exitStatus = proc.getIfContains(Process.STATUS);
    	if (exitStatus == null)
    		exitStatus = UserProcess.OK;
    }
    
    return exitStatus;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    WaitProc w = (WaitProc)super.clone();
    w.proc_    = proc_.cloneAny();
    w.timeout_ = AbstractAny.cloneOrNull(timeout_);
    return w;
  }
}
