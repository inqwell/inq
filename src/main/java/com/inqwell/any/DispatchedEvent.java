/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $ 
 * $Author: sanderst $
 * @version $Revision: 1.2 $
 */
package com.inqwell.any;

/**
 * An event that carries a function call and associated arguments
 * through a process channel for execution in that process's thread.
 * <p>
 * Used by timers and listeners of node events to dispatch their action
 * to the process that started the timeout or listened for the node
 * event.
 * <p>
 * @author Tom
 *
 */
public class DispatchedEvent extends SimpleEvent
{
  private static final long serialVersionUID = 1L;

  private Map args_;
  
  // Note - the func is held as the event's "context".
  
  // The event that gave rise to this event
  private Event underlying_;

  public DispatchedEvent(Any                      eventType,
                         AnyFuncHolder.FuncHolder func,
                         Map                      args)
  {
    this(eventType, func, args, null);
  }


  public DispatchedEvent(Any                      eventType,
                         AnyFuncHolder.FuncHolder func,
                         Map                      args,
                         Event                    underlying)
  {
    super(eventType, func);
    args_ = args;
    underlying_ = underlying;
  }
  
  public Map getArgs()
  {
    return args_;
  }
  
  public Event getOriginating()
  {
    return underlying_;
  }
}
