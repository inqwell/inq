/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SystemCmd.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.io.InputStream;
import java.io.OutputStream;

import com.inqwell.any.io.AbstractStream;
import com.inqwell.any.io.NullOutputStream;

/**
 * Run a system command. Runs the specified command and returns
 * its exit status
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class SystemCmd extends    AbstractFunc
									     implements Cloneable
{
	
	private Any command_;
	
	private Any cmdInput_;
	private Any cmdOutput_;
	private Any cmdError_;
	
	private Any wait_;

	public SystemCmd(Any command,
                   Any cmdOutput,
                   Any cmdError,
                   Any cmdInput,
                   Any wait)
	{
		command_   = command;
    cmdOutput_ = cmdOutput;
    cmdError_  = cmdError;
    cmdInput_  = cmdInput;
    wait_      = wait;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any command = EvalExpr.evalFunc(getTransaction(),
																	  a,
																	  command_);
		if (command == null)
		  nullOperand(command_);

		AbstractStream cmdInput = (AbstractStream)EvalExpr.evalFunc(getTransaction(),
																											  a,
																											  cmdInput_,
                                                        AbstractStream.class);
		
		if (cmdInput == null && cmdInput_ != null)
		  nullOperand(cmdInput_);

    AbstractStream cmdOutput = (AbstractStream)EvalExpr.evalFunc(getTransaction(),
																											   a,
																											   cmdOutput_,
                                                         AbstractStream.class);

		if (cmdOutput == null && cmdOutput_ != null)
		  nullOperand(cmdOutput_);

    AbstractStream cmdError  = (AbstractStream)EvalExpr.evalFunc(getTransaction(),
																											   a,
																											   cmdError_,
                                                         AbstractStream.class);

		if (cmdError == null && cmdError_ != null)
		  nullOperand(cmdError_);

    Any wait = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 wait_);

    if (wait == null && wait_ != null)
      nullOperand(wait_);
    
    // It only makes sense to wait if we at least are reading
    // the stdout. Otherwise if the command generates any and
    // we just call Process.waitFor the calling thread blocks
    // indefinitely. There could still be a problem if anything
    // is generated on stderr and we don't pick that up.
    // Furthermore, if the calling thread is either reading stdout/err
    // or writing stdin it doesn't make sense to wait either, but we
    // can't really know that for ourselves.
    // Assume it's OK to wait and leave all decisions to script.
    boolean bWait = true;
    if (wait != null)
      bWait = new ConstBoolean(wait).getValue();

    String[] cmdarray = null;
    String   cmd      = null;
    
		if (command instanceof Vectored)
		{
			Vectored v = (Vectored)command;
			if (v.entries() == 0)
			  throw new IllegalArgumentException("Vector is empty");
			  
			cmdarray = new String[v.entries()];
			for (int i = 0; i < v.entries(); i++)
			{
				Any arg = evaluateCommandToken(a, v.getByVector(i));
				  
			  cmdarray[i] = arg.toString();
			}
		}
		else
		{
			cmd = command.toString();
		}
		
    java.lang.Process p = null;
    java.lang.Runtime r = java.lang.Runtime.getRuntime();
    
		IntI ret = null;
		
    try
    {
	    if (cmdarray != null)
	      p = r.exec(cmdarray);
	    else
	      p = r.exec(cmd);
	
	    CommandReader stdout = null;
	    CommandReader stderr = null;
      CommandWriter stdin  = null;
	    
			if (cmdOutput != null)
			{
        stdout = new CommandReader(Thread.currentThread().getName() + "_syscmd_stdout", p.getInputStream(), cmdOutput);
      }
      else
      {
        // Always provide a thread to read all the command o/p, sinking
        // it to a NullOutputStream
        stdout = new CommandReader(Thread.currentThread().getName() + "_syscmd_stdout",
                                   p.getInputStream(),
                                   new NullOutputStream());
      }
      
      if (cmdError != null)
			{
        // If err is the same as out then we already have a thread
        // writing to it. That thread will close the stream so this
        // one doesn't need to.
        stderr = new CommandReader(Thread.currentThread().getName() + "_syscmd_stderr",
                                   p.getErrorStream(),
                                   cmdError,
                                   cmdError != cmdOutput);
      }
      else
      {
        // Always provide thread to read all the command o/p, sinking
        // it to a NullOutputStream
        stderr = new CommandReader(Thread.currentThread().getName() + "_syscmd_stderr",
                                   p.getErrorStream(),
                                   new NullOutputStream());
      }

      stderr.start();
      // If out and err are the same then join with the err thread so
      // we don't close the stream prematurely and lose anything
      stdout.start(cmdError == cmdOutput ? stderr : null);
      
      if (cmdInput != null)
      {
        stdin = new CommandWriter(Thread.currentThread().getName() + "_syscmd_stdin",
                                  p.getOutputStream(),
                                  cmdInput);
        stdin.start();
      }
      else
      {
        // If no input was specified then close the Process stream, so
        // it sees eof
        p.getOutputStream().close();
      }
      
      if (bWait)
      {
        // Wait for any i/o threads to die. Note, again, that this is not a good
        // option to employ when any of the streams are being handled by the
        // calling thread.
        if (stdout != null)
          stdout.join();
        
        if (stderr != null)
          stderr.join();
        
        if (stdin != null)
          stdin.join();
        
        // TODO: Should we look for exceptions in the thread instances?
        
        // Now pick up the exit status
        int exitValue = p.waitFor();
        ret = new AnyInt(exitValue);
      }
    }
    catch(InterruptedException ie)
    {
      // If we are interrupted then check if we have
      // also been killed. If so attempt to destroy the
      // underlying Process
      if (getTransaction().getProcess().killed())
      {
        p.destroy();
        throw new ProcessKilledException(ie);
      }
      else
        throw new ContainedException(ie);
    }
    catch(Exception e)
    {
    	throw new ContainedException(e);
    }

		return ret;
	}
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(command_);
  	if (cmdOutput_ != null)
	  	a.add(cmdOutput_);
  	if (cmdInput_ != null)
	  	a.add(cmdInput_);
    if (cmdError_ != null)
      a.add(cmdError_);
    if (wait_ != null)
      a.add(wait_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		SystemCmd s  = (SystemCmd)super.clone();
		s.command_   = command_.cloneAny();
		s.cmdInput_  = AbstractAny.cloneOrNull(cmdInput_);
		s.cmdOutput_ = AbstractAny.cloneOrNull(cmdOutput_);
		s.cmdError_  = AbstractAny.cloneOrNull(cmdError_);
		return s;
  }
	
	private Any evaluateCommandToken(Any a, Any token) throws AnyException
	{
		Any arg = EvalExpr.evalFunc(getTransaction(),
																a,
																token);

				
		if (arg == null)
		  nullOperand(token);
		  
		return arg;
	}
	
	// Collect the output from the specified Process stream and write it
	// to the given Inq iostream
	private static class CommandReader extends Thread
	{
		private InputStream    is_;
		private OutputStream   cmdOutput_;
    private AbstractStream inqOutput_;
    private boolean        close_;
    private CommandReader  joinWith_;
    private Throwable      t_;

    private CommandReader(String name, InputStream is, AbstractStream cmdOutput)
    {
      this(name, is, cmdOutput, true);
    }
    
    private CommandReader(String         name,
                          InputStream    is,
                          AbstractStream cmdOutput,
                          boolean        close)
    {
      setDaemon(true);
      setName(name);
      is_ = is;
      inqOutput_ = cmdOutput;
      cmdOutput_ = cmdOutput.getUnderlyingOutputStream();
      close_     = close;
    }
    
    // For when no explicit stream is provided to collect command o/p. Then
    // we provide a NullOutputStream as a sink.
    private CommandReader(String name, InputStream is, OutputStream os)
    {
      setDaemon(true);
      setName(name);
      is_ = is;
      inqOutput_ = null;
      cmdOutput_ = os;
    }
    
    private void start(CommandReader joinWith)
    {
      joinWith_  = joinWith;
      start();
    }
    
		public void run()
		{
      try
      {
        int i;
        while ((i = is_.read()) >= 0)
          cmdOutput_.write(i);
      }
      catch(Exception e)
      {
        t_ = e;
      }
      finally
      {
        try
        {
        	// Close the Inq stream once the command has closed
        	// its output stream.
          cmdOutput_.flush();
          
          if (joinWith_ != null)
            joinWith_.join();
          
          if (close_)
          {
            if (inqOutput_ != null)
              inqOutput_.close();
            else
              cmdOutput_.close();
          }
        }
        catch(Exception e)
        {
          t_ = e;
        }
      }
		}
    
    private Throwable getThrowable()
    {
      return t_;
    }
	}
  
  // Collect the input from the specified Inq iostream and write it
  // to the given Process stream
  private static class CommandWriter extends Thread
  {
    private OutputStream  os_;
    private InputStream   cmdInput_;
    private Throwable     t_;
    
    private CommandWriter(String name, OutputStream os, AbstractStream cmdInput)
    {
      setDaemon(true);
      setName(name);
      os_ = os;
      cmdInput_ = cmdInput.getUnderlyingInputStream();
    }
    
    public void run()
    {
      try
      {
        int i;
        while ((i = cmdInput_.read()) >= 0)
          os_.write(i);
      }
      catch(Exception e)
      {
        t_ = e;
      }
      finally
      {
        // When we've seen the end of stdin close the receiving
        // process's stream.
        try
        {
          os_.close();
          
          // and close the input in case its a pipe
          cmdInput_.close();
        }
        catch(Exception e)
        {
          t_ = e;
        }
      }
    }
    
    private Throwable getThrowable()
    {
      return t_;
    }
  }
}
