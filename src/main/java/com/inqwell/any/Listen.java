/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Listen.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.beans.Facade;

/**
 * Place a listener on a given node to dispatch a given event type.
 * Return value is the listener (an instance of EventDispatcher) that
 * can be used as a token so things can be explicitly removed
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @deprecated (pending fixing of Xylinq server scripts that use service listeners) 
 */
public class Listen extends    AbstractFunc
										implements Cloneable
{
	private Any listenTo_;
	private Any dispatchTo_;
  private Any createData_;
	
	public Listen(Any listenTo,
								Any dispatchTo)
	{
		listenTo_   = listenTo;
		dispatchTo_ = dispatchTo;
	}
	
	public Any exec(Any a) throws AnyException
	{
		EventGenerator listenTo    = (EventGenerator)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 listenTo_,
																					 EventGenerator.class);

		// Note that the EventListener we are looking for is a
		// SendRequest.  In the server environment the only
		// thing we can safely do while processing events from
		// (possibly) other threads is to generate a request
		// to the listener's event queue using an eventReady
		// SendRequest
		EventListener dispatchTo   = (EventListener)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 dispatchTo_,
																					 SendRequest.class,
																					 Locate.class);

		SendRequest sr = (SendRequest)dispatchTo;
		//sr.setTransaction(getTransaction());
		sr.eventReady(getTransaction().getProcess());
		
		Any createData    = EvalExpr.evalFunc(getTransaction(),
                                          a,
                                          createData_);
    if (createData != null)
    {
      // Slight nastiness for the fact that we are changing the
      // event types array inside the SendRequest and we don't
      // clone these there as they are read only unless there's
      // createData
      sr.cloneEventTypes();
      addCreateData(createData, dispatchTo.getDesiredEventTypes());
    }
                                           
		// If we are listening to a node that supplies the
		// context then use that, otherwise use prevailing.
		if (listenTo instanceof Facade)
		{
		  // TODO: This is old code, so interface is speculative in this respect.
			Facade f = (Facade)listenTo;
      if (f.getContext() != null)
      {
				sr.setContext(f.getContext());
      }
      else
      {
				sr.setContext(getTransaction().getContextPath());
      }
		}
		else
		{
			sr.setContext(getTransaction().getContextPath());
		}
		
		//System.out.println ("Listen.exec() listening to " + listenTo + " with " + dispatchTo);

		EventDispatcher ed = new EventDispatcher();
		ed.addEventListener(dispatchTo);
		listenTo.addEventListener(ed);
		
		return ed;
	}
	
  public void setCreateData(Any createData)
  {
    createData_ = createData;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Listen l = (Listen)super.clone();
    
    l.listenTo_   = listenTo_.cloneAny();
    l.dispatchTo_ = dispatchTo_.cloneAny();
    l.createData_ = AbstractAny.cloneOrNull(createData_);
    
    return l;
  }
  
  private void addCreateData(Any createData, Array eventTypes)
  {
    // Generally there will only be one event type that we are
    // listening for but hunt down any creation types and add
    // in the given data
    for (int i = 0; i < eventTypes.entries(); i++)
    {
      Any et = eventTypes.get(i);
      if (et instanceof Map)
      {
        Any basicType = AbstractEvent.getBasicType(et);
        if (basicType.equals(EventConstants.BOT_CREATE))
        {
          Map m = (Map)et;
          m.replaceItem(EventConstants.EVENT_CREATE, createData);
        }
      }
    }
  }
}

