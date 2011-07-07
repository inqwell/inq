/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractEvent.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Provides support for the 'fixed' element of the client data - the
 * context.
 * <p>
 * Hash and equality semantics are defined by our id so that the
 * specific instances define the meaning of the event.  This
 * should not be overriden by specific implementations but specific
 * event classes are free to do what they want with the event
 * parameter and context method implementation
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Event
 */ 
public abstract class AbstractEvent extends    SimpleMap
                                    implements Event,
																							 Cloneable
{
	private Any eventId_;
  private Any context_;
  private boolean isConsumed_ = false;
  private int serialNumber_   = -1;
  transient private int sequence_;
  
  private static int sequence__ = 1;
  
  public static Any getBasicType(Any eventId)
  {
    if (eventId instanceof Map)
    {
  		Map m = (Map)eventId;
  		return m.get(EventConstants.EVENT_TYPE);
    }
    return eventId;
  }
  
  public static boolean isBotEvent(Any basicType)
  {
  	if (basicType.equals(EventConstants.BOT_UPDATE) ||
  	    basicType.equals(EventConstants.BOT_CREATE) ||
  	    basicType.equals(EventConstants.BOT_DELETE))
  	  return true;
  	else
  	  return false;
  }
  
  public static boolean isDispatchingBaseType(EventListener l, Any eventType)
  {
    boolean ret = false;
    
    eventType = getBasicType(eventType);
    
    Array a = l.getDesiredEventTypes();
    for (int i = 0; i < a.entries(); i++)
    {
      Any et = a.get(i);
      Any baseEt = getBasicType(et);
      if (eventType.equals(baseEt))
      {
        ret = true;
        break;
      }
    }
    
    return ret;
  }
  
  public AbstractEvent(Any eventId)
  {
		eventId_ = eventId;
    setSequence();
	}
		
  public Event cloneEvent()
  {
    return (Event)cloneAny();
  }

  public void setContext(Any a)
  {
  	context_ = a;
  }
  
  public int getSequence()
  {
    return sequence_;
  }
  
  public Any getContext()
  {
  	return context_;
  }

  public Any getId()
  {
		return eventId_;
	}

  public void reset (Any a)
  {
		setContext(a);
	}

  public void setSerialNumber(int serialNumber)
  {
  	serialNumber_ = serialNumber;
  }
  
  public int  getSerialNumber()
  {
  	return serialNumber_;
  }

  public boolean isConsumed()
  {
		return isConsumed_;
  }
  
  public void consume()
  {
		isConsumed_ = true;
  }

  public java.util.EventObject getUnderlyingEvent()
  {
    return null;
  }
  
  public int hashCode()
  {    
    return eventId_.hashCode();
  }
  
  public void mapify()
  {
    // Place all event data carried as members into the map as children
    replaceItem(EventConstants.EVENT_ID, getId());
    
    Any ec = getContext();
    if (ec != null)
    {
      replaceItem(EventConstants.EVENT_CONTEXT, ec);
    }
  }
  
  public String toString()
  {
  	String ret = "" + eventId_;
    if (context_ != null)
      ret = ret + " context " + context_;
    ret = ret + " " + super.toString();
    
    return ret;
  }
  
  public boolean equals(Object o)
  {
		if (!(o instanceof Event))
			return false;
		
		Event e = (Event)o;
		
    return eventId_.equals(e.getId());
  }
  
  public Object clone() throws CloneNotSupportedException
  {
		AbstractEvent e = (AbstractEvent)super.clone();
		e.isConsumed_ = false;
		e.eventId_    = eventId_.cloneAny();
		//e.context_    = AbstractAny.cloneOrNull(context_);
    // Clones have their own global sequence.
    e.setSequence();
		return e;
  }
  
  private void setSequence()
  {
    synchronized(Event.class)
    {
      sequence_ = sequence__;
      sequence__++;
    }
  }

  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    // Serialized form. For NODE_REMOVED events don't serialize the
    // context, since it is the node we are removing. In another
    // environment the node at the path carried in the event is
    // presumably still there.
    // Using writeObject for this purpose has the effect of applying
    // this functionality to all stream destinations, not just a
    // peer Inq environment. Fix this if it is ever a problem.
    Any baseType = AbstractEvent.getBasicType(eventId_);
    if (baseType.equals(EventConstants.NODE_REMOVED))
    {
      Any context = context_;
      context_    = null;
      stream.defaultWriteObject();
      context_ = context;
    }
    else
    {
      stream.defaultWriteObject();
    }
  }
  
  // Ensure events that have been serialized in have a local
  // sequence number.
  private void readObject(ObjectInputStream instr)
																							throws IOException,
																										 ClassNotFoundException
	{
		instr.defaultReadObject();
    setSequence();
	}

}
