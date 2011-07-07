/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/SpawnProcess.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractInputFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyObject;
import com.inqwell.any.BasicProcess;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.DispatchListener;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.ExceptionHandler;
import com.inqwell.any.ExceptionToFunc;
import com.inqwell.any.Func;
import com.inqwell.any.InvokeService;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.ServerConstants;
import com.inqwell.any.Transaction;
import com.inqwell.any.UserProcess;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.ChannelConstants;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;

/**
 * Spawn a new process.  This function can appear in an expression
 * to start a new process in the local
 * <code>inq</code><sup><font size=-2>TM</font></sup>
 * environment.  The new process must have an input channel.
 * This can be passed explicitly as an argument to
 * <code>SpawnProcess</code> or, if omitted, a new channel will
 * be created. An output channel is optional. Typically, it does
 * not make sense for a new process to share the input channel
 * of its creator if the creator is a client peer, since
 * it would be indeterminate which process would receive
 * service requests from the client. However, if a detached
 * process is started with its own input channel, this
 * process may spawn off others with the same input channel
 * forming a group of "worker" processes to which events at
 * their input channel will be dispatched round-robin.
 * <p>
 * Processes can either be <i>child</i> or <i>detached</i>.
 * A child process is related to its parent in that it
 * will be terminated if the parent terminates. Detached
 * processes continue to run until after their creator
 * has terminated.
 * <p>
 * All processes have their own node space in which to maintain
 * any desired state.
 * <p>
 * SpawnProcess takes the following operands:
 * <bl>
 * <li>
 * <i>name</i> The name of the new process
 * </li>
 * <li>
 * <i>type</i> The type of the new process, either child or
 * detached. Child processes will terminate when their parent
 * terminates. Detached processes remain running until they
 * voluntarily exit or are explicitly killed by another process.
 * </li>
 * <li>
 * <i>inputChannel</i> If non-null then the input channel
 * that the new process will wait on for events. If null then a
 * new channel will be created.
 * </li>
 * <li>
 * <i>outputChannel</i> If non-null then the output channel to
 * which the process will send service requests etc.
 * </li>
 * <li>
 * <i>callOnStart</i> If non-null then a <code>Call</code>
 * function that will be executed by the new process prior to
 * it waiting for input events. Any arguments to the call are
 * resolved in the context of the process executing the spawn
 * operation.
 * </li>
 * <li>
 * <i>callOnEnd</i> If non-null then a <code>Call</code>
 * function that will be executed just before the new process
 * terminates.  Any arguments to the call are resolved in the
 * context of the new process.
 * </li>
 * </bl>
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class SpawnProcess extends    AbstractFunc
                          implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any  processName_;
	private Any  processType_;
	private Any  inputChannel_;
	private Any  outputChannel_;
	private Call callOnStart_;
	private Call callOnEnd_;
	private Any  syncExternal_;
	
	public SpawnProcess(Any  processName,
                      Any  processType,
                      Any  inputChannel,
                      Any  outputChannel,
                      Call callOnStart,
                      Call callOnEnd,
                      Any  syncExternal)
	{
		processName_    = processName;
		processType_    = processType;
		inputChannel_   = inputChannel;
		outputChannel_  = outputChannel;
		callOnStart_    = callOnStart;
		callOnEnd_      = callOnEnd;
		syncExternal_   = syncExternal;
	}

	public Any exec(Any a) throws AnyException
	{
		Any    processName   = EvalExpr.evalFunc(getTransaction(),
																						 a,
																						 processName_);

		Any    processType   = EvalExpr.evalFunc(getTransaction(),
																						 a,
																						 processType_);

		InputChannel  inputChannel  = (InputChannel)EvalExpr.evalFunc
                                            (getTransaction(),
																						 a,
																						 inputChannel_,
																						 InputChannel.class);

		OutputChannel outputChannel = (OutputChannel)EvalExpr.evalFunc
                                            (getTransaction(),
																						 a,
																						 outputChannel_,
																						 OutputChannel.class);

		BooleanI syncExternal   = (BooleanI)EvalExpr.evalFunc
		                                        (getTransaction(),
																						 a,
																						 syncExternal_,
																						 BooleanI.class);

    // callOnStart/End are already Call objects, not references
    // so we don't need to resolve them.
    
    if (processName == null)
      nullOperand(processName_);

    if (processType == null)
      nullOperand(processType_);

    Process parent = null;
    
    if (processType.equals(Process.CHILD))
      parent = getTransaction().getProcess();
    else if (!processType.equals(Process.DETACHED))
      throw new AnyException("Illegal process type");
      
    if (inputChannel == null && inputChannel_ != null)
      nullOperand(inputChannel_);
      
    if (syncExternal == null && syncExternal_ != null)
      nullOperand(syncExternal_);
      
    if (outputChannel == null && outputChannel_ != null)
      nullOperand(outputChannel_);
      
    // If no input channel is specified, create a new one.
    if (inputChannel == null)
      inputChannel = new AnyChannel(new FIFO(0,
																						 ChannelConstants.REFERENCE));
    else
      inputChannel = inputChannel.getUnderlyingChannel();
		
		// Create the new process's node space
    BasicProcess.RootMap root = new BasicProcess.RootMap();
    
    Transaction t = new PrimaryTransaction();
    
		EventDispatcher ed   = new EventDispatcher();
		ed.addEventListener(InvokeService.makeInvokeService
                           (EventConstants.INVOKE_SVC,
                            t,
                            root));

    ed.addEventListener(new DispatchListener(root, t));

    //ExceptionHandler eh = new ExceptionToStream(System.out);
    ExceptionHandler eh = new ExceptionToFunc();
    
    UserProcess p = new UserProcess(processName,
                                    inputChannel,
                                    outputChannel,
                                    eh,
                                    t,
                                    root,
                                    ed,
                                    parent,
                                    callOnEnd_);
    root.setProcess(p);
    
    // If the process will be 'shared' by other threads (such as
    // JMS asynchronous listeners or any threading model behind
    // client code using AbstractPlugin) then this operand must
    // be set to true.
    // The object these threads synchronize on must, of course,
    // be unique to the new process, so make something new.
    if (syncExternal != null && syncExternal.getValue())
      p.setSync(new AnyObject());
                      
    // If there's a start call then run it now in the
    // spawning process's thread but in the spawned
    // process's node space
    if (callOnStart_ != null)
    {
      Call callOnStart = (Call)callOnStart_.cloneAny();
      callStartExpr(callOnStart, inputChannel, p, root, a);
    }
    
    // Pass on caller's privilege levels
    Process currProc = getTransaction().getProcess();
    
    // Just for server startup main() currProc can be null
    if (currProc != null)
    {
      p.setRealPrivilegeLevel(currProc.getRealPrivilegeLevel());
      p.setEffectivePrivilegeLevel(currProc.getEffectivePrivilegeLevel());
      
      // Start the new process's thread. If there is no calling
      // process then we assume this class is being run from
      // code, not script, so in case that code has any concurrency
      // issues we expect it to call startThread().
      p.startThread();
    }

		return p;
	}
	
	public void setProcessName(Any processName)
	{
		processName_ = processName;
	}
	
	public void setProcessType(Any processType)
	{
		processType_ = processType;
	}
	
	public void setInputChannel(Any inputChannel)
	{
		inputChannel_ = inputChannel;
	}
	
	public void setOutputChannel(Any outputChannel)
	{
		outputChannel_ = outputChannel;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    SpawnProcess s = (SpawnProcess)super.clone();
    
    s.processName_    = AbstractAny.cloneOrNull(processName_);
    s.processType_    = AbstractAny.cloneOrNull(processType_);
    s.inputChannel_   = AbstractAny.cloneOrNull(inputChannel_);
    s.outputChannel_  = AbstractAny.cloneOrNull(outputChannel_);
    
    // callOnStart_ is cloned when called
    //s.callOnStart_    = (Call)AbstractAny.cloneOrNull(callOnStart_);
    
    // callOnEnd_ will be cloned in the spawned process if it
    // is executed.
    
    return s;
  }
  
  private void callStartExpr(Call         call,
                             InputChannel ichannel,
                             Process      p,
                             Any          root,
                             Any          context) throws AnyException
  {
    // The call isn't handled in the conventional sense. This is because
    // we want to *resolve* the arguments w.r.t the current context
    // and *call* the function with the context of the root of the
    // new process's node space,
    // First, try to resolve the call target. We need to do this
    // so we can get it's parameter definitions.
    AbstractInputFunc calledFunc = call.resolveFunc(context, getTransaction());
    if (calledFunc == null)
      throw new AnyException("Could not resolve function " + call);

    // Note that this is the original fn from the catalog. We want
    // to set the flag in here....
    calledFunc.startUse();

    try
    {
      // Put the transaction in
      call.setTransaction(getTransaction());
      
      // Resolve the arguments against the current context
      Map startArgs = call.resolveArgs(context, calledFunc, getTransaction());
  
      // Put the given input channel and new process on the stack
      startArgs.replaceItem(ServerConstants.ICHANNEL, ichannel);
      startArgs.replaceItem(ServerConstants.PROCESS, p);
      
      
      Call.call((Func)calledFunc.cloneAny(),
                startArgs,
                root,
                getTransaction(),
                calledFunc);
    }
    finally
    {
      calledFunc.endUse();
    }
  }
}
