/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AddTo.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Adds the node operand 1 to the path operand 2
 * building any intermediate maps required as the same class as
 * operand 3
 * <p>
 * Returns the given root node.
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 */
public class AddTo extends    AbstractFunc
									 implements Cloneable
{
  private Any node_;
  private Any path_;
  private Any mapProto_;
  private Any raiseEvent_;

  private BuildNodeMap bn_ = new BuildNodeMap();

	/**
	 * Operand 1 is resolved to a non-<code>Func</code>;
	 * operand 2 must resolve to
	 * an <code>StringI</code>; operand 3 must resolve to
	 * a <code>Map</code>.
	 * If <code>raiseEvent</code> is <code>true</code> and the node
	 * added is an <code>EventGenerator</code>then
	 * a <code>NODE_REPLACED</code> event is raised on the node
	 * added.
	 */
  public AddTo(Any node, Any path, Any raiseEvent, Any mapProto)
  {
    node_       = node;
    path_       = path;
    raiseEvent_ = raiseEvent;
    mapProto_   = mapProto;
  }

	/**
	 * Place <code>node</code> at <code>path</code>
	 * using <code>mapProto</code>.
	 * If <code>node</code> is an event generator then
	 * raise a <code>NODE_REPLACED</code> event on it.
	 */
  public AddTo(Any node, Any path, Any raiseEvent)
  {
		this (node, path, raiseEvent, null);
  }

	/**
	 * Place <code>node</code> at <code>path</code>.
	 * If <code>node</code> is an
	 * implementation of the <code>Map</code> interface
	 * then its class is used to build intervening nodes.
	 * Otherwise <code>AnyPMap</code> is used.
	 * If <code>node</code> is an event generator then
	 * raise a <code>NODE_REPLACED</code> event on it.
	 */
  public AddTo(Any node, Any path)
  {
		this (node, path, AnyBoolean.TRUE, null);
  }

  public AddTo() {}

  public Any exec(Any a) throws AnyException
  {
		Any node         = EvalExpr.evalFunc(getTransaction(), a, node_);

		if (node == null)
			nullOperand(node_);
		
		Any path         = EvalExpr.evalFunc(getTransaction(),
																				 a,
																				 path_);

		if (path == null)
			nullOperand(path_);
		
		NodeSpecification ns;
		if (path instanceof NodeSpecification)
    {
			ns = (NodeSpecification)path;
      if (ns.mustShallowCopy())
        ns = (NodeSpecification)ns.shallowCopy();
    }
		else
			ns = new NodeSpecification((StringI)path);

		Map proto        = (Map)EvalExpr.evalFunc(getTransaction(),
																							a,
																							mapProto_,
																							Map.class);

		if (proto == null && mapProto_ != null)
			nullOperand(mapProto_);
		
		BooleanI raiseEvent = (BooleanI)EvalExpr.evalFunc(getTransaction(),
																									a,
																									raiseEvent_,
																									BooleanI.class);


		if (raiseEvent == null && raiseEvent_ != null)
			nullOperand(raiseEvent_);
		
		bn_.setTransaction(getTransaction());

		bn_.setMapProto(proto);
//		System.out.println ("AddTo.exec targetNode is " + targetNode);
//		System.out.println ("AddTo.exec targetNode of class " + targetNode.getClass().getName());
//		System.out.println ("AddTo.exec adding  " + node + " as " + path);
    bn_.build(ns, node, a);

    Any eventId      = bn_.getRaisedEventId();
    Any childEventId = bn_.getChildRaisedEventId();
    int insertPos    = bn_.getInsertionPosition();

    if ((raiseEvent.getValue()) &&
				(node instanceof EventGenerator))
    {
    	int serialNumber = Random.positiveRandom();
    	
			boolean descend = true;

			EventGenerator eg = (EventGenerator)node;
			Event e = eg.makeEvent(eventId);
			if (e != null)
			{
        Map parent = bn_.getMapParent();
        Any nodeSet = parent.getNodeSet();
        // Are we adding to a node-set? If so we must be a new row so
        // provide vector
				e.setContext(eg);
				e.setSerialNumber(serialNumber);
        if (insertPos >= 0 && nodeSet != null)
        {
          // There is a vector. The build node process generates either
          // an add or a replace, both of which are composite event id types
          // containing the vector, when available
          IntI vector = new ConstInt(insertPos);
          Map m = (Map)e.getId();
          m.replaceItem(EventConstants.EVENT_VECTOR, vector);
        }
	//			System.out.println ("AddTo.exec() firing " + e + " *** on *** " + node.getClass());
        getTransaction().addEvent(e);
				eg.fireEvent(e);
			}
			else
			{
				descend = false;
				// The node in question doesn't support this event type.
				// This is the case with server-side typedef instances so
				// raise the event on their parent.  No point on descending the
				// structure in this case.  Server-side typedef instances propagate
				// events to all their observers so it doesn't make sense
				// for them to raise such an event when they are being
				// inserted into a per-process structure.  QV removal
        // NOTE: deliberately leaving the vector out here - the node
        // is not (erm, likely) to be a node-set child anyway....
				Map parent = bn_.getMapParent();
				if (parent instanceof EventGenerator)
				{
					EventGenerator parentEg = (EventGenerator)parent;
					e = parentEg.makeEvent(eventId);
					e.setContext(eg);
					e.setParameter(bn_.getPath());
				  e.setSerialNumber(serialNumber);
          Any nodeSet = parent.getNodeSet();
          if (insertPos >= 0 && nodeSet != null)
          {
            IntI vector = new ConstInt(insertPos);
            Map m = (Map)e.getId();
            m.replaceItem(EventConstants.EVENT_VECTOR, vector);
          }
          getTransaction().addEvent(e);
					parentEg.fireEvent(e);
				}
			}

			// Also we traverse the children and raise sub-events
			// on any Maps that are event generators, excluding
			// the children of those which are node sets.  This
			// ensures we get the correct dispatching behaviour
			// at GUI nodes.
			if (descend && (node instanceof Map) && !(((Map)node).getNodeSet() != null))
			{
			  ShouldDescend s;
				BreadthFirstIter i = new BreadthFirstIter(node, s = new ShouldDescend(childEventId));
				while (i.hasNext())
				{
					Any c = i.next();

					if (c instanceof EventGenerator)
					{
						eg = (EventGenerator)c;
						e = s.getEvent();
						if (e != null)
						{
							e.setContext(eg);
				      e.setSerialNumber(serialNumber);
							eg.fireEvent(e);
						}
					}
				}
			}
		}

	  return node;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(node_);
  	a.add(path_);
  	return a.createIterator();
  }

  public void setNode(Any node)
  {
    node_       = node;
  }

  public void setPath(Any path)
  {
    path_       = path;
  }

  public void setMapProto(Any mapProto)
  {
    mapProto_   = mapProto;
  }

  public void setRaiseEvent(Any raiseEvent)
  {
    raiseEvent_ = raiseEvent;
  }

  public void setRaiseEvent(boolean raiseEvent)
  {
    raiseEvent_ = new AnyBoolean(raiseEvent);
  }

  public Object clone () throws CloneNotSupportedException
  {
    AddTo a = (AddTo)super.clone();

    a.node_   = node_.cloneAny();
    a.path_   = path_.cloneAny();

    a.mapProto_   = AbstractAny.cloneOrNull(mapProto_);
    a.raiseEvent_ = AbstractAny.cloneOrNull(raiseEvent_);

    a.bn_     = (BuildNodeMap)bn_.cloneAny();
    return a;
  }
  
  // Callback-style function for the iterator to be told
  // whether it should descend into a child node. Returns
  // non-null if should descend, null if not
  private class ShouldDescend extends AbstractFunc
  {
    private Any   eventId_;
    private Event e_;

    private ShouldDescend(Any eventId)
    {
      eventId_ = eventId;
    }
    
    public Any exec(Any a) throws AnyException
    {
      if (a instanceof Map)
      {
        Map m = (Map)a;

        if (m.getNodeSet() != null)
          return null;
      }
      
      if (a instanceof EventGenerator)
      {
        EventGenerator eg = (EventGenerator)a;
        e_ = eg.makeEvent(eventId_);
        if (e_ == null || !eg.raiseAgainstChildren(e_))
          return null;
      }
      return AnyBoolean.TRUE;
    }
    
    private Event getEvent()
    {
      return e_;
    }
  }
}
