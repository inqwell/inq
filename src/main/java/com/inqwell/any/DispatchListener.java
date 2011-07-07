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

package com.inqwell.any;

/**
 * An <code>EventListener</code> whose purpose is to process
 * a received DISPATCH event by executing the contained function
 * passing any arguments
 */
public class DispatchListener extends    AbstractAny
                              implements EventListener
{
  private static final long serialVersionUID = 1L;

  static private Array eventTypes__ = AbstractComposite.array();
  
  // The root node and transaction of the process that
  // this DispatchListener is associated with.
  private Any          root_;
  private Transaction  t_;
  
  static
  {
    eventTypes__.add(EventConstants.DISPATCHED);
  }
  
  public DispatchListener(Any root, Transaction t)
  {
    root_ = root;
    t_    = t;
  }
  
  public boolean processEvent(Event e) throws AnyException
  {
    AnyFuncHolder.FuncHolder fh = (AnyFuncHolder.FuncHolder)e.getContext();
    
    DispatchedEvent de = (DispatchedEvent)e;
    Map args = de.getArgs();
    Event originating = de.getOriginating();
    if (args == null && originating != null)
      args = AbstractComposite.simpleMap();
    
    if (originating != null)
    {
      args.add (EventConstants.EVENT_ID, originating.getId());
      args.add (EventConstants.EVENT, originating);
      
      Any ec = originating.getContext();
      if (ec != null)
        args.add(EventConstants.EVENT_CONTEXT, ec);
      
      Any baseType = AbstractEvent.getBasicType(originating.getId());
      if (baseType.equals(EventConstants.EXEC_COMPLETE))
      {
        // For EXEC_COMPLETE events the context data is an event bundle.
        // To make them accessible to scripts we mapify them.
        // This leads to viable paths in event handler functions
        // like @eventData[x].@eventData and @eventData[x].@eventId
        Array eventBundle = (Array)ec;
        if (eventBundle.entries() != 0)
        {
          for (int i = 0; i < eventBundle.entries(); i++)
          {
            AbstractEvent ae = (AbstractEvent)eventBundle.get(i);
            ae.mapify();
          }
        }
      }
      else if (baseType.equals(EventConstants.NODE_REMOVED))
      {
        Map m = (Map)originating.getId();
        
        // If there is a vector number then put a parent path into
        // the event type. Because it has already been dispatched
        // we don't upset the filtering.
        // Hmmm, the event id is already on the stack
        Any vec = m.get(EventConstants.EVENT_VECTOR);
        if (vec != AnyAlwaysEquals.instance())
        {
          NodeSpecification n = (NodeSpecification)m.get(EventConstants.EVENT_PATH);
          n = (NodeSpecification)n.cloneAny();
          n.removeLast();
          n.removeLast();
          m.add(EventConstants.EVENT_PARENT, n);
        }
      }
    }
    
    try
    {
      fh.doFunc(t_, args, root_);
    }
    finally
    {
      if (de.getArgs() == args)
      {
        // Clean up the map we were given in case it gets reused
        if (originating != null)
        {
          args.remove(EventConstants.EVENT_ID);
          args.remove(EventConstants.EVENT);
          if (originating.getContext() != null)
            args.remove(EventConstants.EVENT_CONTEXT);
        }
      }
    }
    return true;
  }

  public Array getDesiredEventTypes()
  {
    return eventTypes__;
  }
}
