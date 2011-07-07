/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NodeEvent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * A class for events which propagate through a composite structure
 * to indicate things like deletion, modification and so forth.  The event
 * parameter is a node specification which, at any point, will locate the
 * originating node.
 * <p>
 * <code>NodeEvents</code> also carry the node to which the event applies.
 * This supports the serialization of the event to another JVM: with the
 * node specification being equally valid a client process can, for example,
 * locate the node in its copy of the structure and assign a new value
 * from the node.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public  class NodeEvent extends AbstractEvent
                                implements Cloneable
{
  
  private NodeSpecification nodeSpec_;

  public NodeEvent(Any eventId)
  {
		super(eventId);
    nodeSpec_ = (NodeSpecification)NodeSpecification.NULLNS.cloneAny();
    Map m = (Map)eventId;
    m.add(EventConstants.EVENT_PATH, nodeSpec_);
    Any eventType = m.get(EventConstants.EVENT_TYPE);
    if (eventType.equals(EventConstants.NODE_REMOVED) ||
        eventType.equals(EventConstants.NODE_ADDED) ||
        eventType.equals(EventConstants.NODE_REPLACED))
			m.add(EventConstants.EVENT_VECTOR, AnyAlwaysEquals.instance());

  }
  
  public void setParameter (Any a)
  {
		//System.out.println ("NodeEvent.setParameter() " + a);
    // Extend the NodeSpecification with the supplied Any
    nodeSpec_.add(0, a);
    nodeSpec_.add(0, NodeSpecification.strict__);
  }
 
  public Any getParameter ()
  {
    return nodeSpec_;
  }

  public NodeSpecification getNodeSpec ()
  {
    return nodeSpec_;
  }

  public Any getOriginatorName()
  {
	  return nodeSpec_.getLast();
  }

  public Any getSourceName()
  {
	  return nodeSpec_.get(1);
  }

  public Object clone() throws CloneNotSupportedException
  {
    NodeEvent n = (NodeEvent)super.clone();
    
    // The event id is a map which contains the node spec.
    // Since we clone the id in our superclass we've already got
    // a new member in that map.  Fetch it out so that
    // setParameter works
    
    Map id = (Map)n.getId();
    n.nodeSpec_ = (NodeSpecification)id.get(EventConstants.EVENT_PATH);
    

    // There's no need to clone the node - all events can share
    // the same reference
    
    return n;
  }
}
