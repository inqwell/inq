/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Empty.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Empty the specified node of all its child nodes.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Empty extends    AbstractFunc
									 implements Cloneable
{
	
	private Any any_;
	
	public Empty(Any any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Composite c = (Composite)EvalExpr.evalFunc(getTransaction(),
																							 a,
																							 any_);
    if (c != null)
    {
      c.empty();
      
      // If the composite is an event generator then raise NODE_REPLACED
      // Not really replaced but will cause GUIs to update or server
      // propagation to client, which is OK
      if (c instanceof EventGenerator)
      {
        EventGenerator eg = (EventGenerator)c;
        Event e = eg.makeEvent(EventConstants.NODE_REPLACED);
        if (e != null)
        {
          e.setContext(eg);
          eg.fireEvent(e);
        }
      }
    }
//    else
//      nullOperand(any_);

		return c;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		Empty e = (Empty)super.clone();
		e.any_ = AbstractAny.cloneOrNull(any_);
		return e;
  }
}
