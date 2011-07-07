/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/UpdateEventProcessor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyAlwaysEquals;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Array;
import com.inqwell.any.Assign;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.EventIdMap;
import com.inqwell.any.EventListener;
import com.inqwell.any.Func;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.tools.GraphicsRemoveEventProcessor;
import com.inqwell.any.tools.GraphicsReplaceEventProcessor;

/**
 * An <code>EventListener</code> whose purpose is to apply
 * a received BOT_UPDATE node event to the local instance hierarchy.
 * See also 
 */
public class UpdateEventProcessor extends    AbstractFunc
                                  implements EventListener
{
	private Array      eventTypes_ = AbstractComposite.array();
	private Any        root_;
	private LocateNode target_     = new LocateNode();
	
	private EvalExpr   assigner_;
	
	public static void setupNodeEventDispatcher(EventDispatcher ed,
                                              Transaction     t,
                                              Any             root)
  {
    //System.out.println ("Setting dispatch to UpdateEventProcessor");
																		 

    UpdateEventProcessor n = new GraphicsUpdateEventProcessor
																				(EventIdMap.makeNodeEventType
																					(EventConstants.BOT_UPDATE,
																					 AnyAlwaysEquals.instance(),
																					 AnyAlwaysEquals.instance(),
																					 AnyAlwaysEquals.instance()));
    n.setRoot(root);
    n.setTransaction(t);
    ed.addEventListener(n);
    
    DeleteEventProcessor de = new GraphicsDeleteEventProcessor
                                    (EventIdMap.makeNodeEventType
                                      (EventConstants.BOT_DELETE,
                                       AnyAlwaysEquals.instance(),
                                       AnyAlwaysEquals.instance()),
                                     root);
    de.setTransaction(t);
    ed.addEventListener(de);
        

    ReplaceEventProcessor re = new GraphicsReplaceEventProcessor
      (EventIdMap.makeNodeEventType(EventConstants.NODE_REPLACED,
                                    AnyAlwaysEquals.instance(),
                                    AnyAlwaysEquals.instance()),
       root);

    re.addEventType(EventIdMap.makeNodeEventType(EventConstants.NODE_ADDED,
                                    AnyAlwaysEquals.instance(),
                                    AnyAlwaysEquals.instance()));
    re.setTransaction(t);
    ed.addEventListener(re);

    RemoveEventProcessor rm = new GraphicsRemoveEventProcessor
      (EventIdMap.makeNodeEventType(EventConstants.NODE_REMOVED,
                                    AnyAlwaysEquals.instance(),
                                    AnyAlwaysEquals.instance()),
       root);

    rm.setTransaction(t);
    ed.addEventListener(rm);
    
    ed.addEventListener(new InvokeEventProcessor());
  }

	public UpdateEventProcessor()
	{
		this(null);
	}
	
	public UpdateEventProcessor(Any eventType)
	{
		if (eventType != null)
			addEventType(eventType);
		
    // There can be problems resolving the target node if the client
    // has discarded it and event(s) are still in the input queue.
    // If we can't resolve the target then just silently ignore.
    // Note - we can always resolve the source - it is the value
    // from the event.
    assigner_ = new EvalExpr(target_,
  	                         null,
  	                         new Assign()
                             {
                               public void visitFunc (Func f)
                               {
                                 try
                                 {
                                   f.setTransaction(getTransaction());
                                   Any a     = f.execFunc(param_);
                                   lastFunc_ = (Locate)f;   // enforced by parser so should never throw
                                   if (a == null)
                                     result_ = AnyNull.instance();
                                   else
                                     a.accept(this);
                                 }
                                 catch (AnyException e)
                                 {
                                   throw new RuntimeContainedException(e);
                                 }
                               }
                             });
	}
	
  public Any exec(Any a) throws AnyException
  {
		//System.out.println ("UpdateEventProcessor.exec " + a);
		Event e = (Event)a;
		
		Map m = (Map)e.getId();
		NodeSpecification ns = (NodeSpecification)m.get(EventConstants.EVENT_PATH);
		target_.setNodePath(ns);
		Set fields = (Set)m.get(EventConstants.EVENT_FIELDS);
		
		Map source = (Map)e.getContext();
		Iter i = fields.createIterator();
    Transaction t = getTransaction();
		while (i.hasNext())
		{
			Any field = i.next();
			
			ns.add(field);
			assigner_.setOp2(source.get(field));
	
			EvalExpr.evalFunc(t,
												root_,
												assigner_);
			ns.removeLast();
		}
    t.commit();
		return null;
  }

  public boolean processEvent(Event e) throws AnyException
  {
//		System.out.println (root_);
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
