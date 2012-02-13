/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/UnListen.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Remove a listener previously placed on a node by Listen.  Returns
 * the node that the listener was removed from.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class UnListen extends    AbstractFunc
                      implements Cloneable
{
	private Any listeningTo_;
	private Any dispatchingTo_;
	
	public UnListen(Any listeningTo,
                  Any dispatchingTo)
	{
		listeningTo_   = listeningTo;
		dispatchingTo_ = dispatchingTo;
	}
	
	public Any exec(Any a) throws AnyException
	{
		EventGenerator listeningTo  = (EventGenerator)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 listeningTo_,
																					 EventGenerator.class);
    
    // listeningTo cannot be null
    if (listeningTo == null)
      nullOperand(listeningTo_);
    
    // Check if the single argument version is being used
    if (dispatchingTo_ == null)
    {
      ListenTo.EventDispatcherListeningTo d = (ListenTo.EventDispatcherListeningTo)listeningTo;
      listeningTo = d.unlisten();
    }
    else
    {
      // dispatchingTo can still resolve to null just so we can write
      // script like
      //    listen(unlisten(listeningTo, dispatchingTo), ...)
      // when initially there is no dispatcher
  		EventListener dispatchingTo = (EventListener)EvalExpr.evalFunc
  																					(getTransaction(),
  																					 a,
  																					 dispatchingTo_,
  																					 EventListener.class);
  
  		if (dispatchingTo != null && listeningTo != null)
        listeningTo.removeEventListener(dispatchingTo);
    }
    
		return listeningTo;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    UnListen u = (UnListen)super.clone();
    
    u.listeningTo_   = listeningTo_.cloneAny();
    u.dispatchingTo_ = AbstractAny.cloneOrNull(dispatchingTo_);
    
    return u;
  }
}

