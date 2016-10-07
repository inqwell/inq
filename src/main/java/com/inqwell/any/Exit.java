/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Exit.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;

/**
 * The exit function.  Behaviour differs on client and server:
 * <ul><li>On a client, closes our output channel and exits the VM.
 * The server receives the close notification and terminates the
 * associated user process. If there is an exit status the value is
 * passed to <code>System.exit()</code>, otherwise exits with a status
 * of zero.</li>
 * <li>On a server, places zero or supplied exit status
 * at <code>$process.status</code> and terminates the process by
 * closing its input channel. If the process was started
 * with <code>spawn</code> and there is an <code>end</code> function
 * then this will be called prior to termination</li></ul>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Exit extends    AbstractFunc
									implements Cloneable
{
	private static final long serialVersionUID = 1L;
	
	private Any exitStatus_;
	private BooleanI vmExit_;

	public Exit(Any exitStatus, BooleanI vmExit)
	{
    exitStatus_ = exitStatus;
    vmExit_     = vmExit;
	}

	public Any exec(Any a) throws AnyException
	{
    Any exitStatus  = EvalExpr.evalFunc(getTransaction(),
																        a,
																        exitStatus_);
    
    if (exitStatus == null && exitStatus_ != null)
    	nullOperand(exitStatus_);

		IntI i = (exitStatus != null) ? new ConstInt(exitStatus)
		                                  : UserProcess.OK;

		if (Globals.isServer())
		{
		  terminateProcess(a, i);
		}
		else
		{
			// Client or Interactive.
		  if (Globals.process__ == Globals.getProcessForCurrentThread())
		  {
		    // Either "main" when interactive or the awt thread or the Inq
		    // client process.

		    // Exit the VM, passing out any status and closing the o/p
		    // channel gracefully.
		    OutputChannel outputChannel = (OutputChannel)EvalExpr.evalFunc
		    (getTransaction(),
		        a,
		        new LocateNode(ServerConstants.ROCHANNEL),
		        OutputChannel.class);
		    
		    if (outputChannel != null)
		      outputChannel.close();
		    
		    System.exit(i.getValue());
		  }
		  else
		  {
	      terminateProcess(a, i);
		  }
		}
		
		return null;
	}

  public Object clone () throws CloneNotSupportedException
  {
		Exit e = (Exit)super.clone();

    e.exitStatus_ = AbstractAny.cloneOrNull(exitStatus_);

    return e;
  }
  
  private void terminateProcess(Any a, IntI status) throws AnyException
  {
  	// If vmExit_ is TRUE then just exit anyway
  	if (vmExit_ != null && vmExit_.getValue())
	    System.exit(status.getValue());

    // Server. Terminate the calling process gracefully by closing its
    // input channel, setting any status at $process.status.
  	
    InputChannel inputChannel = (InputChannel)EvalExpr.evalFunc
                                    (getTransaction(),
                                     a,
                                     new LocateNode(ServerConstants.RICHANNEL),
                                     InputChannel.class);
    
    Process p = getTransaction().getProcess();
    p.replaceItem(Process.STATUS, status);
    
    inputChannel.close();
  }
}
