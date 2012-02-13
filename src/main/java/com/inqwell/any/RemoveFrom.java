/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/RemoveFrom.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Removes from the node operand 1 the child whose key is operand 2
 * <p>
 * Returns the given root node.
 * @author $Author: sanderst $
 * @version $Revision: 1.7 $
 */
public class RemoveFrom extends    AbstractFunc
									      implements Cloneable,
																	 EventListener
{
  private Any     node_;
  private Any     key_;
  private Any     raiseEvent_;
  private boolean iterMode_;
  
  private Any            parentName_;
  private Composite      parentNode_;
  private EventGenerator parentEg_;
  
	/**
	 * Remove the node with the given key from the target node
	 */
  public RemoveFrom(Any node, Any key, Any raiseEvent)
  {
    node_       = node;
    key_        = key;
    raiseEvent_ = raiseEvent;
  }

	/**
	 *
	 */
  public RemoveFrom(Any node, Any key)
  {
		this (node, key, AnyBoolean.TRUE);
  }
  
  public RemoveFrom(Any node, BooleanI raiseEvent)
  {
		this (node, null, raiseEvent);
  }
  
  public RemoveFrom(Any node)
  {
		this (node, null, AnyBoolean.TRUE);
  }
  
  public RemoveFrom() {}

  public Any exec(Any a) throws AnyException
  {
	  Any node = null;
    Transaction t = getTransaction();
	  if (!iterMode_)
	  {
	    node = EvalExpr.evalFunc(t,
                               a,
                               node_);
	  }
	  else
	  {
	  	// If we are doing a removeiter() then we are
	  	// nested inside a foreach and the node
	  	// being removed is $loop
	  	node = t.getLoop();
	  	
	  	parentNode_ = (Composite)t.getIter().getIterRoot();
	  }

    // If the node did not resolve then return null
    if (node == null)
      return null;
    
    // See if there is an explicit key.  If there is then this
    // means the node argument is the container we will remove
    // the named child from. Node must be a map in this case.
		Any key   = EvalExpr.evalFunc(t, a, key_);
    if (key == null && key_ != null)
      throw new AnyException("Could not resolve explicit node name " + key_);

    // Whether to raise a removal event on the node we are
    // removing the child from (if it is an event generator)
		BooleanI raiseEvent = (BooleanI)EvalExpr.evalFunc(t,
																									a,
																									raiseEvent_,
																									BooleanI.class);
																									
  	// Before actually doing the removal, try to determine the
  	// parent's name for this node, and the parent itself
    // First the parent name:
    // 1) an explicit key is given;
    // 2) See if we used a Locate and if so see if it has the name
    //    or vector number
    // 3) The node itself is to be removed so ask it for
    //    the parent name.
    
    parentName_ = key;
			
    Locate l = null;
    boolean vectorRemove = false;

    if (parentName_ == null)
    {
      // No explicit name given. Note whether vector access / get name.
      if (node_ instanceof Locate)
      {
        l = (Locate)node_;
//        if (l.getMapParent() != t.getCurrentStackFrame())
//        {
          if (!(vectorRemove = l.isVectorElement()))
            parentName_ = l.getPath();
//        }
      }
    }
    
    // If the parent name is still null then we assume that the node
    // argument is actually the node we are removing (rather than the
    // container we are removing from) so ask the node for its name
    // in the parent. Must be a container that supports this operation
    // or we'll croak.
    if (parentName_ == null && !vectorRemove)
    {
      if (!(node instanceof Composite))
        throw new AnyException("Cannot determine node's parent name of " + node.getClass());

      parentName_ = ((Composite)node).getNameInParent();
    }
      
    // Now the parent node, similar to above
    // Explicit key means we are removing from the given node, that is
    // the node is not the removee (with me?)
    if (key != null)
      parentNode_ = (Composite)node;
  
    // Still not found it? Try locate first as getParentAny can croak
    if (parentNode_ == null)
    {
      if (l != null)
      {
        //if (l.getMapParent() != t.getCurrentStackFrame())
          parentNode_ = l.getMapParent();
        
        // It might be an array parent
        if (parentNode_ == null)
          parentNode_ = l.getArrayParent();
      }
    }

    // Reckon this must work by now
    if (parentNode_ == null)
      parentNode_ = ((Map)node).getParentAny();

    // close brace
  	
  	Any ret = null;
  	
    // Now do the removal and see if there's an index for
    // the node also.

    int indx = -1;
		if (parentNode_ != null &&
		    parentName_ != null &&
        parentNode_.getNodeSet() != null &&
		    (parentNode_ instanceof Vectored))
		{
      // Determine the vector number for the event
			Vectored v = (Vectored)parentNode_;
		  indx = v.indexOf(parentName_);
		}
		
    // NB we know node is not null by now - TBD
  	if (node != null)
  	{
      // Explicit key ?
			if (key != null)
			{
        // node is where we're removing from
        Map m = (Map)node;
        if (!m.contains(key))
          return null;
          
        // Check remove PRIVILEGE on node for key
        t.checkPrivilege(AbstractMap.P_REMOVE, m, key);
				ret = m.remove(key);
			}
			else
			{
        // Node is the thing we'll remove
				ret = node;
				if (!iterMode_)
				{
          if (parentName_ != null && parentNode_ != null)
          {
            // Check remove PRIVILEGE on parentNode_ for parentName_
            if (parentNode_ instanceof Map)
            {
              Map m = (Map)parentNode_;
              t.checkPrivilege(AbstractMap.P_REMOVE, m, parentName_);
            }
            parentNode_.remove(parentName_);
          }
          else if (!vectorRemove)
          {
            // Check remove PRIVILEGE on node.getParentAny() for
            // key node.getNameInParent()
            Composite c1 = (Composite)node;
            Composite c = c1.getParentAny();
            if (c instanceof Map)
            {
              Map m = (Map)c;
              t.checkPrivilege(AbstractMap.P_REMOVE, m, c1.getNameInParent());
            }
            c1.removeInParent();
          }
          else
          {
            // vector remove - Locate l above is not null
            // We also know that the parentNode_ is set and
            // because of LocateNode processing it must be
            // a Vectored.
            // PRIVILEGE ?
            int child = l.getVectorNumber();
            if (child == -1)
              child = parentNode_.entries() - 1;
            ((Vectored)parentNode_).removeByVector(child);
          }
				}
				else
				{
          // Nothing we can do to enforce privilege
				  t.getIter().remove();
				}
			}
		}

    // Temporary - debug recursion problems
    if (ret == parentNode_)
      throw new IllegalArgumentException("Oops recursion " + ret);

///*
    if ((raiseEvent.getValue()) &&
				(ret instanceof EventGenerator) &&
				(parentNode_ instanceof EventGenerator))
    {
    	int serialNumber = Random.positiveRandom();
			EventGenerator nodeEg = (EventGenerator)ret;
			parentEg_ = (EventGenerator)parentNode_;
			
			// The node has just been removed so if it was an InstanceHierarchy
			// map then it will have no parental listener.  Stick ourselves on
			// to conveniently process events emanating from the sub-structure
			
			nodeEg.addEventListener(this);
      try
      {
        Event e = nodeEg.makeEvent(EventConstants.NODE_REMOVED);
        
		    boolean descend = nodeEg.raiseAgainstChildren(e);
				
				if (e != null)
				{
					//System.out.println ("RemoveFrom.exec() firing " + e);
		      if (indx >= 0)
		      {
		        Map eventType = (Map)e.getId();
		        eventType.replaceItem(EventConstants.EVENT_VECTOR,
		                              new ConstInt(indx));
		      }
		      // For clients it makes sese to include the node as the
		      // event context.  For servers (who will propagate the
		      // event) it doesn't.
          // NOTE: now handled in serialized form of AbstractEvent
		      //if (Globals.process__ != null)
          e.setContext(nodeEg);
				  e.setSerialNumber(serialNumber);
          t.addEvent(e);
					nodeEg.fireEvent(e);
				}
				else
				{
					descend = false;
					e = parentEg_.makeEvent(EventConstants.NODE_REMOVED);
		      //if (Globals.process__ != null)  // NOTE: see above
          e.setContext(nodeEg);
				  e.setSerialNumber(serialNumber);
		      if (indx >= 0)
		      {
		        Map eventType = (Map)e.getId();
		        eventType.replaceItem(EventConstants.EVENT_VECTOR,
		                              new ConstInt(indx));
		      }
          t.addEvent(e);
					processEvent(e);
				}

				// Also we traverse the children and raise sub-events
				// on any Maps that are event generators, excluding
				// the children of those which are node sets.  This
				// ensures we get the correct dispatching behaviour
				// at GUI nodes and other listeners
				if (descend && (((Composite)node).getNodeSet() == null))
				{
				  ShouldDescend s;
					BreadthFirstIter i = new BreadthFirstIter(node, s = new ShouldDescend());
					while (i.hasNext())
					{
						Any c = i.next();
	
						if (c instanceof EventGenerator)
						{
							EventGenerator eg = (EventGenerator)c;
							e = s.getEvent();
					    //System.out.println ("RemoveFrom.exec() firing child " + e);
							if (e != null)
							{
                if (Globals.process__ != null)
                  e.setContext(eg);
				        e.setSerialNumber(serialNumber);
								eg.fireEvent(e);
							}
							else
							{
                // if the EventGenerator does not support the event type
                // then don't descend into it.
                i.skipCurrent(c);
							}
						}
					}
				}
		  }
		  finally
	    {
			  nodeEg.removeEventListener(this);
      }	
		}

//*/

		//System.out.println ("RemoveFrom Returning " + ret);

		if ((ret != null) && (ret.isTransactional()))
			return a;
		else
			return ret;
  }
  
  public Array getDesiredEventTypes()
  {
		return EventConstants.ALL_TYPES;
	}

  public boolean processEvent(Event e) throws AnyException
  {
	  //System.out.println("REMOVER FIRING " + parentName_);
		if (parentEg_ != null)
		{
			if (parentName_ != null)
				e.setParameter(parentName_);
			
			parentEg_.fireEvent (e);
		}
    return true;
  }

  public void setKey(Any key)
  {
		key_ = key;
	}
  
  public void setRaiseEvent(Any raiseEvent)
  {
    raiseEvent_ = raiseEvent;
  }

  public void setRaiseEvent(boolean raiseEvent)
  {
    raiseEvent_ = new ConstBoolean(raiseEvent);
  }

  public void setIterMode(boolean iterMode)
  {
    iterMode_ = iterMode;
  }

  public void setPath(Any path)
  {
		if (path instanceof NodeSpecification)
			node_ = new LocateNode((NodeSpecification)path);
		else
			node_ = new LocateNode((StringI)path);
  }
  
  public void reset()
  {
    parentName_ = null;
    parentNode_ = null;
    parentEg_   = null;
    if (node_ != null && node_ instanceof LocateNode)
    {
      LocateNode l = (LocateNode)node_;
      l.reset();
    }
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(node_);
  	
  	if (key_ != null)
			a.add(key_);

  	if (raiseEvent_ != null)
  	  a.add(raiseEvent_);

  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    RemoveFrom r = (RemoveFrom)super.clone();
    
    r.node_   = AbstractAny.cloneOrNull(node_); // can be null if iter
    r.key_    = AbstractAny.cloneOrNull(key_);
    r.raiseEvent_ = AbstractAny.cloneOrNull(raiseEvent_);
    
    return r;
  }

  protected void beforeExecute(Any a)
  {
    reset();
    super.beforeExecute(a);
  }
  
  protected Any afterExecute(Any ret, Any a)
  {
    reset();
    return super.afterExecute(ret, a);
  }
  
  // Callback-style function for the iterator to be told
  // whether it should descend into a child node. Returns
  // non-null if should descend, null if not
  private class ShouldDescend extends AbstractFunc
  {
    private Event e_;
    
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
        e_ = eg.makeEvent(EventConstants.NODE_REMOVED_CHILD);
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
