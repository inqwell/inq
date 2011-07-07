/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any.jms;

import javax.jms.Message;
import javax.jms.MessageListener;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Call;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;

/**
 * A JMS MessageListener that calls an Inq function in
 * the {@link #onMessage(Message)} method.
 * <p/>
 * Instances of class are associated with the {@link com.inqwell.any.Process}
 * that created it and run using the resources of that process.
 * <p/>
 * The JMS provider most likely creates its own thread in which
 * the {@link #onMessage(Message)} method is called. This class
 * uses the process's <code>sync</code> object to ensure that
 * its Inq thread and JMS thread do not run together.
 * <p/>
 * A Process still retains its input channel and may, using its
 * Inq thread, process service requests and other Inq events in
 * the normal way.
 * 
 * @author tom
 *
 */
public class AnyMessageListener extends    AbstractAny
                                implements MessageListener
{
  private Call    call_;
  private Process process_;
  private Map     context_;
  private Any     contextPath_;
  private Map     args_ = AbstractMap.simpleMap();
  
  static private Any MESSAGE = AbstractValue.flyweightString("message");
    
  public AnyMessageListener(Call call, Process process)
  {
    process_     = process;
    call_        = call;
    context_     = process.getRoot();
    contextPath_ = process.getContextPath();
    args_.add(MESSAGE, AnyNull.instance());
  }
  
  @Override
  public void onMessage(Message msg)
  {
    // Wrap as appropriate
    AnyMessage m = AnyMessage.makeMessage(msg);
    Any sync = process_.getIfContains(Process.sync__);
    if (sync != null)
    {
      synchronized(sync)
      {
        execFunc(call_, m, sync);
      }
    }
    else
      execFunc(call_, m, sync);
  }

  private void execFunc(Call c, Any message, Any sync)
  {
    boolean abort = false;
    boolean haveProcessForThread = Globals.haveProcessForThread();
    
    if (!haveProcessForThread)
      Globals.setProcessForThread(Thread.currentThread(), process_);
    
    // Call the function
    
    // Fetch its arguments. Leave anything that might be there
    // already replacing only the "item" argument. If there are
    // no arguments defined then force the "item" argument
    args_.replaceItem(MESSAGE, message);
    
    Transaction t = process_.getTransaction();

    t.mqDirty(true);

    // We  only want to throw runtime exceptions however regretably
    // at the moment Inq defines a checked one. Insulate the caller
    // from this
    try
    {
      // Establish the process environment and pass the call
      // statement our transaction

      process_.setContext((com.inqwell.any.Map)context_);
      process_.setContextPath(contextPath_);
      c.setTransaction(t);
      c.setArgs(args_);

      // Execute the call statement
      c.execFunc(context_);
    }
    catch(AnyException e)
    {
      // Ordinary exceptions from the Any framework
      // (includes ContainedException)
      // (wrapped in a ContainedException)
      e.fillInCallStack(t);
      t.getCallStack().empty();
      abort = true;
      throw new RuntimeContainedException(e);
    }
    catch (AnyRuntimeException e)
    {
      // Runtime exceptions from the Any framework
      //e.printStackTrace();
      e.fillInCallStack(t);
      t.getCallStack().empty();
      abort = true;
      throw e;
    }
    catch (Exception e)
    {
      // Handle uncaught JDK exceptions
      AnyRuntimeException ce = new RuntimeContainedException(e);
      ce.fillInCallStack(t);
      t.getCallStack().empty();
      abort = true;
      throw ce;
    }
    catch (StackOverflowError e)
    {
      AnyRuntimeException ce = new RuntimeContainedException(e);
      ce.topOfStack(t);
      t.getCallStack().empty();
      abort = true;
      throw ce;
    }
    catch (Error e)
    {
      // Serious errors, will cause thread to terminate
      AnyRuntimeException ce = new RuntimeContainedException(e);
      abort = true;
      throw ce;
    }
    finally
    {
      // Tidy the Call statement's arguments
      args_.replaceItem(MESSAGE, AnyNull.instance());
      // Tidy the Call statement's transaction
      c.setTransaction(Transaction.NULL_TRANSACTION);
      c.setArgs(null);
      
      try
      {
        closeTxn(t, abort, sync);
      }
      finally
      {
        // Tidy the process
        process_.setContext(null);
        process_.setContextPath(null);
  
        if (!haveProcessForThread)
          Globals.removeProcessForThread(Thread.currentThread(), process_);
      }      
    }
  }

  private void closeTxn(Transaction t, boolean abort, Any sync)
  {
    try
    {
      t.getCallStack().empty();
      if (abort)
      {
        // Unwind any transaction that may be in progress
        t.abort();
        process_.setContext(null);
        process_.setContextPath(null);
      }
      else
      {
        // All OK - commit transaction
        t.commit();
        process_.setContext(null);
        process_.setContextPath(null);
      }
    }
    catch(Exception e)
    {
      RuntimeContainedException ce = new RuntimeContainedException(e);
      ce.fillInCallStack(t);
      try { t.abort(); } catch(Exception ee) {ee.printStackTrace();}
      process_.setContext(null);
      process_.setContextPath(null);
      throw(ce);
    }
    finally
    {
      if (sync != null)
      {
        synchronized(sync)
        {
          sync.notifyAll();
        }
      }
    }
  }

}
