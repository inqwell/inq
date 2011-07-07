/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/DeleteEventProcessor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * An <code>EventListener</code> whose purpose is to apply
 * a received BOT_DELETE node event to the local instance hierarchy
 */
public class DeleteEventProcessor extends    AbstractFunc
                                  implements EventListener
{
  private Array      eventTypes_ = AbstractComposite.array();
  private Any        root_;

  private LocateNode locater_      = new LocateNode();

  public DeleteEventProcessor()
  {
    this(EventIdMap.makeNodeEventType(EventConstants.BOT_DELETE,
                                      AnyAlwaysEquals.instance(),
                                      AnyAlwaysEquals.instance()));
  }
  
  public DeleteEventProcessor(Any eventType)
  {
    this(eventType, null);
  }
  
  public DeleteEventProcessor(Any eventType, Any root)
  {
    if (eventType != null)
      addEventType(eventType);
    
    root_ = root;
  }
  
  public Any exec(Any a) throws AnyException
  {
    //System.out.println ("DeleteEventProcessor.exec " + a);
    Event e = (Event)a;
    
    Map m = (Map)e.getId();
    NodeSpecification ns = (NodeSpecification)m.get(EventConstants.EVENT_PATH);
    
    locater_.setNodePath(ns);
    
//    System.out.println ("DeleteEventProcessor " + ns + " " + replaceWith);
    EventGenerator node = (EventGenerator)EvalExpr.evalFunc(getTransaction(),
                                                        root_,
                                                        locater_);

    if (node != null)
    {
      Event ev = node.makeEvent(EventConstants.BOT_DELETE);
      node.fireEvent(ev);
    }
    
    return null;
  }

  public boolean processEvent(Event e) throws AnyException
  {
    exec(e);
    return true;
  }

  public Array getDesiredEventTypes()
  {
    return eventTypes_;
  }

  public void setRoot(Any root)
  {
    root_ = root;
  }

  public void addEventType(Any eventType)
  {
    eventTypes_.add(eventType);
  }
}
