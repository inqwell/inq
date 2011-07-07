/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/ServiceInvocationEvent.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.client.swing;

import java.awt.event.InvocationEvent;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.ExceptionHandler;
import com.inqwell.any.Map;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.AnyView;

/**
 * Dispatch the execution of a Runnable and perform transaction cleanup
 * afterwards.  This event is placed on the awt event queue when an
 * inq service is declared or requested to run on the swing thread.
 */
public class ServiceInvocationEvent extends InvocationEvent
{
  private Transaction t_;
  
	public ServiceInvocationEvent(Transaction t,
                                Object source,
                                Runnable runnable)
	{
    super(source, runnable);
    t_ = t;
	}
  
  public void dispatch()
  {
    ExceptionHandler eh  = t_.getProcess().getExceptionHandler();
    Map context     = t_.getContext();
    Any contextPath = t_.getContextPath();
    
    try
    {
      AnyView.syncGuiStart();
      super.dispatch();
      // Should we commit if there was a context?
      t_.commit();
    }
    catch (AnyRuntimeException ex)
    {
      // Runtime exceptions from the Any framework.  We can't get
      // AnyExceptions because this violates the dispatch method's
      // signature.
      ex.fillInCallStack(t_);
      //ex.printStackTrace();
      eh.handleException(ex, t_);
      t_.getCallStack().empty();
    }
    
    // Handle uncaught JDK exceptions
    catch (Exception ex)
    {
      AnyException ce = new ContainedException(ex);
      ce.fillInCallStack(t_);
      //ce.printStackTrace();
      eh.handleException(ce, t_);
      t_.getCallStack().empty();
    }
    
    catch (StackOverflowError ex)
    {
      AnyException ce = new ContainedException(ex);
      ce.topOfStack(t_);
      //ce.printStackTrace();
      eh.handleException(ce, t_);
      t_.getCallStack().empty();
    }
    finally
    {
      AnyView.syncGuiEnd(-1);
      t_.getProcess().setContext(context);
      t_.getProcess().setContextPath(contextPath);
    }
  }
}
